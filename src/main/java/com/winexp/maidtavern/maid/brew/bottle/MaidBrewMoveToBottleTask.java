package com.winexp.maidtavern.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;

public class MaidBrewMoveToBottleTask extends MaidSurroundingMoveTask {
    private final IBrewTask task;

    public MaidBrewMoveToBottleTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        setMaxCheckRate(20);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        return super.checkExtraStartConditions(level, maid)
                && !brain.hasMemoryValue(InitEntities.TARGET_POS.get())
                && !brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                && brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        searchForDestination(level, maid);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!task.isBottleValid(maid, pos) && !task.shouldPlaceBottle(maid, pos)) return false;
        return !MaidUtils.isTargetOccupied(maid, pos);
    }
}
