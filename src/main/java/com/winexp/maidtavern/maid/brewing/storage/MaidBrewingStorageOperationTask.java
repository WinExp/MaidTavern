package com.winexp.maidtavern.maid.brewing.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.*;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import com.winexp.maidtavern.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.List;

public class MaidBrewingStorageOperationTask extends Behavior<EntityMaid> {
    private final IBrewingTask task;

    public MaidBrewingStorageOperationTask(IBrewingTask task) {
        super(ImmutableMap.of(
                MaidTavernEntities.BREWING_WORK.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!MaidBrewingStateManager.isSameWorkType(maid, BrewingWorkTypes.STORAGE)) return false;
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BlockPos pos = work.pos();
        if (!MaidUtil.isStorageValid(level, pos)) return false;

        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null && session.stage() != BrewingSession.Stage.TAKE_INGREDIENTS) return false;

        return work.isCloseEnough(maid);
    }

    private void extractStacks(EntityMaid maid, IItemHandlerModifiable storage, IItemHandlerModifiable inventory) {
        Brain<EntityMaid> brain = maid.getBrain();
        List<Pair<ItemStack, Integer>> bottles = task.getBottlesToExtract(inventory, storage);
        if (ItemHandlerUtil.canInsertAny(inventory, bottles.stream().map(Pair::getFirst).toList())) {
            for (Pair<ItemStack, Integer> pair : bottles) {
                ItemStack stack = pair.getFirst();
                int count = pair.getSecond();
                if (!ItemHandlerUtil.canInsert(inventory, stack.copyWithCount(count))) continue;
                ItemHandlerHelper.insertItemStacked(inventory, stack.copyWithCount(count), false);
                stack.shrink(count);
            }
        }

        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session == null) return;
        List<Pair<ItemStack, Integer>> ingredients = task.getIngredientsToExtract(inventory, storage, maid.level().getRecipeManager(), session.entry());
        if (ItemHandlerUtil.canInsertAll(inventory, ingredients.stream().map(Pair::getFirst).toList())) {
            for (Pair<ItemStack, Integer> pair : ingredients) {
                ItemStack stack = pair.getFirst();
                int count = pair.getSecond();
                ItemHandlerHelper.insertItemStacked(inventory, stack.copyWithCount(count), false);
                stack.shrink(count);
            }
        }
        brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), session.withStage(BrewingSession.Stage.START_BREWING));
    }

    private void insertResults(EntityMaid maid, IItemHandlerModifiable storage, IItemHandlerModifiable inventory) {
        for (ItemStack stack : task.getResultsToInsert(maid)) {
            if (!ItemHandlerUtil.canInsert(storage, stack)) continue;
            ItemHandlerUtil.replaceStack(inventory, stack,
                    ItemHandlerHelper.insertItemStacked(storage, stack, false));
        }
    }

    private void insertByproducts(EntityMaid maid, IItemHandlerModifiable storage, IItemHandlerModifiable inventory) {
        for (ItemStack stack : task.getByproductsToInsert(maid)) {
            if (!ItemHandlerUtil.canInsert(storage, stack)) continue;
            ItemHandlerUtil.replaceStack(inventory, stack,
                    ItemHandlerHelper.insertItemStacked(storage, stack, false));
        }
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BlockPos pos = work.pos();
        Container container = Utils.getContainer(level, pos);
        IItemHandlerModifiable storage = new InvWrapper(container);
        IItemHandlerModifiable inventory = maid.getAvailableInv(true);
        StorageBinding binding = brain.getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(null);
        if (binding == null || binding.ingredients().contains(pos)) {
            extractStacks(maid, storage, inventory);
        }
        if (binding == null || binding.results().contains(pos)) {
            insertResults(maid, storage, inventory);
        }
        if (binding == null || binding.byproducts().contains(pos)) {
            insertByproducts(maid, storage, inventory);
        }
        MaidBrewingStateManager.stopWork(maid);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, maid.getSoundSource(), 1, 1);
    }
}
