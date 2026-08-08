package com.winexp.maidtavern.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.function.Function;

public class MaidUtil {
    public static boolean isTargetOccupied(EntityMaid maid, BlockPos pos) {
        return isPosOccupied(maid, pos, maid1 -> maid1.getBrain().getMemory(InitEntities.TARGET_POS.get())
                .map(PositionTracker::currentBlockPosition).orElse(null));
    }

    public static boolean isPosOccupied(EntityMaid maid, BlockPos pos, Function<EntityMaid, BlockPos> posGetter) {
        var nearestEntities = maid.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (nearestEntities.isPresent()) {
            for (LivingEntity entity : nearestEntities.get()) {
                if (entity instanceof EntityMaid maid1) {
                    BlockPos maid1Pos = posGetter.apply(maid1);
                    if (pos.equals(maid1Pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkSurroundingPathReach(MaidPathFindingBFS pathFinding, BlockPos pos, BoundingBox moveRange) {
        for (int x = moveRange.minX(); x <= moveRange.maxX(); x++) {
            for (int y = moveRange.minY(); y <= moveRange.maxY(); y++) {
                for (int z = moveRange.minZ(); z <= moveRange.maxZ(); z++) {
                    if (pathFinding.canPathReach(pos.offset(x, y, z))) return true;
                }
            }
        }
        return false;
    }

    public static boolean isStorageValid(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof Container) {
            if (!state.is(Blocks.BARREL)
                    && !state.is(Blocks.CHEST)) return false;
            return !state.is(Blocks.CHEST) || ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, pos, false) != null;
        }
        return false;
    }
}
