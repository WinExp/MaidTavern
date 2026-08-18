package com.winexp.maidtavern.maid.brewing.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.BrewingWork;
import com.winexp.maidtavern.maid.brewing.BrewingWorkTypes;
import com.winexp.maidtavern.maid.brewing.IBrewingTask;
import com.winexp.maidtavern.maid.brewing.MaidBrewingStateManager;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.MaidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.PositionTracker;

public class MaidBrewingMoveToBottleTask extends MaidSurroundingMoveTask {
    private final IBrewingTask task;
    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidBrewingMoveToBottleTask(IBrewingTask task, float movementSpeed, int verticalSearchRange, double closeEnoughDist, int minCheckTime) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
        setMaxCheckRate(minCheckTime);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        return super.checkExtraStartConditions(level, maid)
                && !MaidBrewingStateManager.isWorking(maid)
                && !brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                && brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        searchForDestination(level, maid);
        Brain<EntityMaid> brain = maid.getBrain();
        brain.getMemory(InitEntities.TARGET_POS.get()).map(PositionTracker::currentBlockPosition).ifPresent(targetPos ->
                MaidBrewingStateManager.startWork(maid, new BrewingWork(BrewingWorkTypes.BOTTLE, targetPos, movementSpeed, closeEnoughDist)));
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!task.isBottleValid(maid, pos) && !task.shouldPlaceBottle(maid, pos)) return false;
        return !MaidUtil.isTargetOccupied(maid, pos);
    }
}
