package com.winexp.maid.grape;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFarmMoveTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MaidGrapeMoveTask extends MaidFarmMoveTask {
    public MaidGrapeMoveTask(IFarmTask task, float movementSpeed) {
        super(task, movementSpeed);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        super.start(level, maid, gameTimeIn);
        if (maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            BlockPos blockPos = maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).get().currentBlockPosition();
            maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos.above(TaskGrape.GRAPE_HEIGHT)));
        }
    }
}
