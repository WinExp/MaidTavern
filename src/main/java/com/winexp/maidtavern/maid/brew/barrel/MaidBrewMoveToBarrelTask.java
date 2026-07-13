package com.winexp.maidtavern.maid.brew.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MaidBrewMoveToBarrelTask extends MaidSurroundingMoveTask {
    private final IBrewTask task;
    private final float movementSpeed;

    public MaidBrewMoveToBarrelTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        this.movementSpeed = movementSpeed;
        setMaxCheckRate(60);
        moveRange = new BoundingBox(-2, -1, -2, 2, 1, 2);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid) || brain.hasMemoryValue(InitEntities.TARGET_POS.get())) return false;
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return true;
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList != null) {
            brewingList.shuffle();
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                if (task.hasIngredients(maid, recipeId)) {
                    brewingList.select(recipeId);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null) {
            if (maid.level().getRecipeManager().byKey(session.recipeId()).isEmpty()) return;
            BlockPos barrelPos = session.barrelPos();
            IBarrel barrel = task.getBarrel(level, barrelPos);
            if (!task.isBarrelValid(maid, barrel)) {
                brain.eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
                return;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(maid, barrelPos, movementSpeed, 0);
            brain.setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(barrelPos));
        } else {
            searchForDestination(level, maid);
            var targetPos = brain.getMemory(InitEntities.TARGET_POS.get());
            if (targetPos.isPresent()) {
                ResourceLocation recipeId = brewingList.get();
                brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), BrewingSession.create(recipeId, targetPos.get().currentBlockPosition()));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = (BlockPos.MutableBlockPos) pos;
        BlockState state = level.getBlockState(mutablePos);
        IBarrel barrel = task.getBarrel(level, pos);
        if (!task.isBarrelValid(maid, barrel)) return false;
        mutablePos.set(BarrelBlock.getOriginPos(pos, state).above(2));
        return !MaidUtils.isTargetOccupied(maid, pos);
    }
}
