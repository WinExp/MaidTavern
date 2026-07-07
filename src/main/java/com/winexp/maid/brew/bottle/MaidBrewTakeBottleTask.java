package com.winexp.maid.brew.bottle;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ItemUtils;
import com.google.common.collect.ImmutableMap;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.IBrewTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MaidBrewTakeBottleTask extends Behavior<EntityMaid> {
    private final IBrewTask task;
    private @Nullable BlockPos bottlePosCached;

    public MaidBrewTakeBottleTask(IBrewTask task) {
        super(ImmutableMap.of(
                InitEntities.TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.BREWING_LIST.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.task = task;
    }

    private @Nullable BlockPos getBottlePos(EntityMaid maid, BlockPos targetPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos testPos = targetPos.relative(direction);
            if (task.isBottleValid(maid, testPos)) return testPos;
        }
        return null;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        bottlePosCached = null;
        Brain<EntityMaid> brain = maid.getBrain();
        PositionTracker targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).get();
        Vec3 targetV3d = targetPos.currentPosition();
        if (maid.distanceToSqr(targetV3d) > Math.pow(task.getCloseEnoughDist(), 2)) {
            return false;
        }
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return false;
        bottlePosCached = getBottlePos(maid, targetPos.currentBlockPosition());
        if (bottlePosCached == null) return false;
        return task.isBottleValid(maid, bottlePosCached);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        BlockState state = level.getBlockState(bottlePosCached);
        Block.getDrops(state, level, bottlePosCached, level.getBlockEntity(bottlePosCached))
                .forEach(stack -> ItemUtils.getItemToLivingEntity(maid, stack));
        level.setBlock(bottlePosCached, Blocks.AIR.defaultBlockState(), Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
        maid.playSound(SoundType.STONE.getPlaceSound(), 1.0f, 1.0f);
        brain.eraseMemory(InitEntities.TARGET_POS.get());
    }
}
