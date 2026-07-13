package com.winexp.maidtavern.maid.brew.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;

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
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();

        BlockPos pos = targetPos.currentBlockPosition();
        IBarrel barrel = task.getBarrel(level, pos);
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).get();
        if (!task.isBarrelValid(maid, barrel) || !task.hasIngredients(maid, session.recipeId())) {
            brain.eraseMemory(InitEntities.TARGET_POS.get());
            clearSession(maid);
            return false;
        }

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
        if (!maid.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return false;
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).orElse(null);
        if (targetPos == null) return false;
        BlockPos pos = targetPos.currentBlockPosition();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).get();
        IBarrel barrel = task.getBarrel(level, pos);
        if (!task.isBarrelValid(maid, barrel) || !task.hasIngredients(maid, session.recipeId())) {
            clearSession(maid);
            return false;
        }
        return true;
    }

    @Override
    protected void tick(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).get();
        BlockPos pos = targetPos.currentBlockPosition();
        IBarrel barrel = task.getBarrel(level, pos);
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).get();

        if (--cooldown > 0) return;
        BarrelRecipe recipe = session.getRecipe(maid.level().getRecipeManager());
        if (recipe == null) {
            clearSession(maid);
            return;
        }
        if (!barrel.isOpen()) {
            barrel.openLid(maid);
            cooldown = stepCooldown;
        } else if (!session.fluidPlaced().booleanValue()) {
            for (int i = 0; i < 4; i++) {
                barrel.addFluid(maid, ItemHandlerUtil.findStack(maid.getAvailableInv(true), stack ->
                        stack.is(recipe.fluid().getBucket())));
            }
            session.fluidPlaced().setTrue();
            cooldown = stepCooldown;
        } else if (!session.ingredientsPlaced().booleanValue()) {
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
            session.ingredientsPlaced().setTrue();
            if (isPlaced) cooldown = stepCooldown;
        } else {
            barrel.closeLid(maid);
            clearSession(maid);
        }
    }

    private void clearSession(EntityMaid maid) {
        maid.getBrain().eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
    }

    @Override
    protected void stop(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(InitEntities.TARGET_POS.get());
    }
}
