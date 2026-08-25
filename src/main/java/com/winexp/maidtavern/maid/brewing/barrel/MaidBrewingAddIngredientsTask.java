package com.winexp.maidtavern.maid.brewing.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.*;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MaidBrewingAddIngredientsTask extends Behavior<EntityMaid> {
    private final IBrewingTask task;
    private final int stepCooldown;
    private int cooldown;

    public MaidBrewingAddIngredientsTask(IBrewingTask task, int stepCooldown) {
        super(ImmutableMap.of(
                MaidTavernEntities.BREWING_WORK.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_SESSION.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
        this.stepCooldown = stepCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MaidBrewingStateManager.isSameWorkType(maid, BrewingWorkTypes.ADD_INGREDIENTS)) return false;
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        BrewingSession session = getSession(maid);
        if (!session.stage().isBrewing()) {
            stop(maid);
            return false;
        }
        BlockPos pos = session.barrelPos().orElse(null);
        if (pos == null) {
            stop(maid);
            return false;
        }
        BlockState state = level.getBlockState(pos);
        IBarrel barrel = BarrelBlock.getBarrelEntity(level, pos, state);
        if (!task.isBarrelValid(maid, barrel) || !task.hasIngredients(maid, session.entry().recipeId())) {
            stop(maid);
            return false;
        }

        return work.isCloseEnough(maid);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, EntityMaid maid, long gameTime) {
        BrewingSession session = getSession(maid);
        if (session == null) return false;
        BlockPos pos = session.barrelPos().orElse(null);
        if (pos == null) return false;
        BlockState state = level.getBlockState(pos);
        IBarrel barrel = BarrelBlock.getBarrelEntity(level, pos, state);
        return task.isBarrelValid(maid, barrel) && task.hasIngredients(maid, session.entry().recipeId());
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        BrewingSession session = getSession(maid);
        BlockPos pos = session.barrelPos().get();
        BlockState state = level.getBlockState(pos);
        IBarrel barrel = BarrelBlock.getBarrelEntity(level, pos, state);

        if (--cooldown > 0) return;
        BarrelRecipe recipe = session.entry().getRecipe(maid.level().getRecipeManager());
        if (!barrel.isOpen()) {
            barrel.openLid(maid);
            cooldown = stepCooldown;
        } else if (!session.stage().isFluidsPlaced()) {
            for (int i = 0; i < 4; i++) {
                barrel.addFluid(maid, ItemHandlerUtil.findStack(maid.getAvailableInv(true), stack ->
                        stack.is(recipe.fluid().getBucket())));
            }
            setSession(maid, session.withStage(BrewingSession.Stage.FLUIDS_PLACED));
            cooldown = stepCooldown;
        } else if (!session.stage().isIngredientsPlaced()) {
            boolean isPlaced = false;
            ingredient:
            for (Ingredient ingredient : recipe.ingredients()) {
                if (ingredient.isEmpty()) continue;
                for (ItemStack ingredientStack : ingredient.getItems()) {
                    if (ingredientStack.isEmpty()) continue;
                    List<ItemStack> stacks = ItemHandlerUtil.findStacks(maid.getAvailableInv(true), stack ->
                            ItemStack.isSameItemSameComponents(stack, ingredientStack));
                    int count = 0;
                    List<ItemStack> addStacks = new ArrayList<>();
                    for (ItemStack stack : stacks) {
                        addStacks.add(stack);
                        count += stack.getCount();
                        if (count >= 16) {
                            for (ItemStack addStack : addStacks) {
                                barrel.addIngredient(maid, addStack);
                                isPlaced = true;
                            }
                            continue ingredient;
                        }
                    }
                }
            }
            setSession(maid, session.withStage(BrewingSession.Stage.INGREDIENTS_PLACED));
            if (isPlaced) cooldown = stepCooldown;
        } else {
            barrel.closeLid(maid);
            clearSession(maid);
            cooldown = stepCooldown;
        }
        if (cooldown > 0) {
            maid.swing(InteractionHand.MAIN_HAND);
        }
        MaidBrewingStateManager.resetWorkExpiration(maid);
    }

    private @Nullable BrewingSession getSession(EntityMaid maid) {
        return maid.getBrain().getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
    }

    private void setSession(EntityMaid maid, BrewingSession session) {
        maid.getBrain().setMemory(MaidTavernEntities.BREWING_SESSION.get(), session);
    }

    private void clearSession(EntityMaid maid) {
        maid.getBrain().eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        stop(maid);
    }

    private void stop(EntityMaid maid) {
        MaidBrewingStateManager.stopWork(maid);
        clearSession(maid);
    }
}
