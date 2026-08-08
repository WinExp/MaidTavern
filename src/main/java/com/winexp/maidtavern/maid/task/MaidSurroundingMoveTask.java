package com.winexp.maidtavern.maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.winexp.maidtavern.util.MaidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class MaidSurroundingMoveTask extends MaidMoveToBlockTask {
    public static final BoundingBox DEFAULT_MOVE_RANGE = new BoundingBox(-1, -1, -1, 1, 1, 1);

    protected BoundingBox moveRange = DEFAULT_MOVE_RANGE;

    public MaidSurroundingMoveTask(float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
    }

    @Override
    protected boolean checkPathReach(EntityMaid maid, MaidPathFindingBFS pathFinding, BlockPos pos) {
        return MaidUtil.checkSurroundingPathReach(pathFinding, pos, moveRange);
    }
}
