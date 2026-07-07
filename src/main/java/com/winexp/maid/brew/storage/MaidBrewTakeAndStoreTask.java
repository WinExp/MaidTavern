package com.winexp.maid.brew.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.ImmutableMap;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.IBrewTask;
import com.winexp.maid.brew.BrewingList;
import com.winexp.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MaidBrewTakeAndStoreTask extends Behavior<EntityMaid> {
    private final IBrewTask task;
    private @Nullable ItemStack toStoreStackCached;

    public MaidBrewTakeAndStoreTask(IBrewTask task) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
    }

    private @Nullable ItemStack getToStoreStack(EntityMaid maid) {
        if (toStoreStackCached == null) {
            toStoreStackCached = task.getToStoreStack(maid);
        }
        return toStoreStackCached;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        toStoreStackCached = null;
        Brain<EntityMaid> brain = maid.getBrain();
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return false;
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        BlockPos pos = targetPos.currentBlockPosition();
        if (!task.isStorageValid(maid, pos)) return false;

        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(task.getCloseEnoughDist(), 2)) {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                brain.eraseMemory(InitEntities.TARGET_POS.get());
            }
            return false;
        }
        return true;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        BlockPos pos = brain.getMemory(InitEntities.TARGET_POS.get()).get().currentBlockPosition();
        BaseContainerBlockEntity container = (BaseContainerBlockEntity) level.getBlockEntity(pos);
        IItemHandlerModifiable storage = new InvWrapper(container);
        IItemHandlerModifiable maidInv = maid.getAvailableInv(true);
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        for (ResourceLocation recipeId : brewingList.getRecipes()) {
            BarrelRecipe recipe = (BarrelRecipe) level.getRecipeManager().byKey(recipeId).map(RecipeHolder::value).orElse(null);
            if (recipe != null && task.hasRequiredMaterialsInStorage(maid, recipeId, storage)) {
                Predicate<ItemStack> fluidPredicate = stack -> recipe.fluid().getBucket() == stack.getItem();
                int fluidRequired = 4 - ItemHandlerUtil.countItems(maidInv, fluidPredicate);
                if (fluidRequired > 0) {
                    List<ItemStack> stacks = ItemHandlerUtil.findStacks(storage, fluidPredicate, fluidRequired);
                    for (ItemStack stack : stacks) {
                        ItemHandlerUtil.replaceStack(storage, stack, ItemHandlerHelper.insertItemStacked(maidInv, stack, false));
                    }
                }
                for (Ingredient ingredient : recipe.getIngredients()) {
                    if (ingredient.isEmpty()) continue;
                    int ingredientRequired = 16 - ItemHandlerUtil.countItems(maidInv, ingredient);
                    if (ingredientRequired > 0) {
                        List<ItemStack> stacks = ItemHandlerUtil.findStacks(storage, ingredient, ingredientRequired);
                        for (ItemStack stack : stacks) {
                            ItemHandlerUtil.replaceStack(storage, stack, ItemHandlerHelper.insertItemStacked(maidInv, stack, false));
                        }
                    }
                }
            }
        }
        while (true) {
            ItemStack toStoreStack = getToStoreStack(maid);
            if (toStoreStack == null) break;
            ItemHandlerUtil.replaceStack(maidInv, toStoreStack,
                    ItemHandlerHelper.insertItemStacked(storage, toStoreStack, false));
            toStoreStackCached = null;
        }
        brain.eraseMemory(InitEntities.TARGET_POS.get());
        maid.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
    }
}
