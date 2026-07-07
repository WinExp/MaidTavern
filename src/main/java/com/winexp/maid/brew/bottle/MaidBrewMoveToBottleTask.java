package com.winexp.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.IBrewTask;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.block.state.BlockState;

public class MaidBrewMoveToBottleTask extends MaidMoveToBlockTask {
    private final IBrewTask task;

    public MaidBrewMoveToBottleTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
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
        if (!task.isBottleValid(maid, pos)) return false;
        BlockState tapState = level.getBlockState(pos.above());
        BlockPos.MutableBlockPos mutablePos = (BlockPos.MutableBlockPos) pos;
        mutablePos.move(tapState.getValue(TapBlock.FACING));
        return true;
    }
}
