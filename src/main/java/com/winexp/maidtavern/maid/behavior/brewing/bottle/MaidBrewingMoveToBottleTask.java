package com.winexp.maidtavern.maid.behavior.brewing.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.work.Work;
import com.winexp.maidtavern.maid.behavior.brewing.BrewingWorkTypes;
import com.winexp.maidtavern.maid.behavior.brewing.IBrewingTask;
import com.winexp.maidtavern.maid.work.MaidWorkManager;
import com.winexp.maidtavern.maid.behavior.core.MaidSurroundingMoveTask;
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
                && !MaidWorkManager.isWorking(maid)
                && !brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                && brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        searchForDestination(level, maid);
        Brain<EntityMaid> brain = maid.getBrain();
        brain.getMemory(InitEntities.TARGET_POS.get()).map(PositionTracker::currentBlockPosition).ifPresent(targetPos ->
                MaidWorkManager.startWork(maid, new Work(BrewingWorkTypes.TASK, BrewingWorkTypes.BOTTLE, targetPos, movementSpeed, closeEnoughDist)));
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!task.isBottleValid(maid, pos) && !task.shouldPlaceBottle(maid, pos)) return false;
        return !MaidUtil.isWorkPosOccupied(maid, pos);
    }
}
