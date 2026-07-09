package com.winexp.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MaidUtils {
    public static boolean isTargetOccupied(EntityMaid maid, BlockPos pos) {
        var nearestEntities = maid.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (nearestEntities.isPresent()) {
            for (LivingEntity entity : nearestEntities.get()) {
                if (entity instanceof EntityMaid maid1) {
                    var maid1Target = maid1.getBrain().getMemory(InitEntities.TARGET_POS.get()).get();
                    if (maid1Target.currentBlockPosition().equals(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
