package com.winexp.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.TapBlock;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.IBrewTask;
import com.winexp.util.MaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.state.BlockState;

public class MaidBrewMoveToBottleTask extends MaidMoveToBlockTask {
    private final IBrewTask task;
    private Direction tapFacing;

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
        Brain<EntityMaid> brain = maid.getBrain();
        brain.getMemory(InitEntities.TARGET_POS.get()).ifPresent(targetPos -> {
            PositionTracker positionTracker = new BlockPosTracker(targetPos.currentBlockPosition().relative(tapFacing.getOpposite()));
            brain.setMemory(MemoryModuleType.LOOK_TARGET, positionTracker);
            brain.setMemory(InitEntities.TARGET_POS.get(), positionTracker);
        });
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!task.isBottleValid(maid, pos) && !task.shouldPlaceBottle(maid, pos)) return false;
        BlockState tapState = level.getBlockState(pos.above());
        BlockPos.MutableBlockPos mutablePos = (BlockPos.MutableBlockPos) pos;
        tapFacing = tapState.getValue(TapBlock.FACING);
        mutablePos.move(tapFacing);
        return !MaidUtils.isTargetOccupied(maid, pos);
    }
}
