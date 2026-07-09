package com.winexp.maid.grape;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFarmMoveTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MaidGrapeMoveTask extends MaidFarmMoveTask {
    private final IGrapeTask task;

    public MaidGrapeMoveTask(IGrapeTask task, float movementSpeed) {
        super(task, movementSpeed);
        this.task = task;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        super.start(level, maid, gameTimeIn);
        if (maid.getBrain().hasMemoryValue(InitEntities.TARGET_POS.get())) {
            BlockPos pos = maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).get().currentBlockPosition().above();
            maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,
                    new BlockPosTracker(task.getGrapePos(level, pos)));
        }
    }
}
