package com.winexp.maidtavern.maid.brew.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaidBrewAddIngredientTask extends Behavior<EntityMaid> {
    private final IBrewTask task;
    private final double closeEnoughDist;
    private final int stepCooldown;
    private int cooldown;

    public MaidBrewAddIngredientTask(IBrewTask task, double closeEnoughDist, int stepCooldown) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_SESSION.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
        this.closeEnoughDist = closeEnoughDist;
        this.stepCooldown = stepCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingSession session = getSession(maid);
        if (!session.stage().isBrewing()) {
            brain.eraseMemory(InitEntities.TARGET_POS.get());
            clearSession(maid);
            return false;
        }
        BlockPos pos = session.barrelPos().orElse(null);
        if (pos == null) {
            brain.eraseMemory(InitEntities.TARGET_POS.get());
            clearSession(maid);
            return false;
        }
        BlockState state = level.getBlockState(pos);
        IBarrel barrel = BarrelBlock.getBarrelEntity(level, pos, state);
        if (!task.isBarrelValid(maid, barrel) || !task.hasIngredients(maid, session.entry().recipeId())) {
            brain.eraseMemory(InitEntities.TARGET_POS.get());
            clearSession(maid);
            return false;
        }

        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(closeEnoughDist, 2)) {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(targetV3d)) {
                brain.eraseMemory(InitEntities.TARGET_POS.get());
                clearSession(maid);
            }
            return false;
        }
        return true;
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
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        BlockPos pos = targetPos.currentBlockPosition();
        BlockState state = level.getBlockState(pos);
        IBarrel barrel = BarrelBlock.getBarrelEntity(level, pos, state);
        BrewingSession session = getSession(maid);

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
        if (cooldown > 0) maid.swing(InteractionHand.MAIN_HAND);
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
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(InitEntities.TARGET_POS.get());
        clearSession(maid);
    }
}
