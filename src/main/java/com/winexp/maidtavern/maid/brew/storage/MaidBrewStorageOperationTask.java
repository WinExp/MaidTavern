package com.winexp.maidtavern.maid.brew.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import com.winexp.maidtavern.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.Optional;

public class MaidBrewStorageOperationTask extends Behavior<EntityMaid> {
    private final IBrewTask task;
    private final double closeEnoughDist;

    public MaidBrewStorageOperationTask(IBrewTask task, double closeEnoughDist) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
        this.closeEnoughDist = closeEnoughDist;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        BlockPos pos = targetPos.currentBlockPosition();
        if (!MaidUtil.isStorageValid(level, pos)) return false;

        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null && session.stage() != BrewingSession.Stage.TAKE_INGREDIENTS) return false;

        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(closeEnoughDist, 2)) {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                brain.eraseMemory(InitEntities.TARGET_POS.get());
            }
            return false;
        }
        return true;
    }

    private void extractStacks(EntityMaid maid, IItemHandlerModifiable storage, IItemHandlerModifiable inventory) {
        Brain<EntityMaid> brain = maid.getBrain();
        IItemHandler maidInv = maid.getAvailableInv(true);
        for (Pair<ItemStack, Integer> pair : task.getBottlesToExtract(maidInv, storage)) {
            ItemStack stack = pair.getFirst();
            int count = pair.getSecond();
            if (!ItemHandlerUtil.canInsert(inventory, stack.copyWithCount(count))) continue;
            ItemHandlerHelper.insertItemStacked(inventory, stack.copyWithCount(count), false);
            stack.shrink(count);
        }

        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session == null) return;
        for (Pair<ItemStack, Integer> pair : task.getIngredientsToExtract(maidInv, storage, maid.level().getRecipeManager(), session.entry())) {
            ItemStack stack = pair.getFirst();
            int count = pair.getSecond();
            if (!ItemHandlerUtil.canInsert(inventory, stack.copyWithCount(count))) continue;
            ItemHandlerHelper.insertItemStacked(inventory, stack.copyWithCount(count), false);
            stack.shrink(count);
        }
        brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), session.withStage(BrewingSession.Stage.BREWING));
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
        BlockPos pos = brain.getMemory(InitEntities.TARGET_POS.get()).get().currentBlockPosition();
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
        brain.eraseMemory(InitEntities.TARGET_POS.get());
        maid.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
    }
}
