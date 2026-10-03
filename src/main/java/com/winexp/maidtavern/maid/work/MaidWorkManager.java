package com.winexp.maidtavern.maid.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.Brain;
import org.jetbrains.annotations.Nullable;

public class MaidWorkManager {
    public static boolean isWorking(EntityMaid maid) {
        return maid.getBrain().hasMemoryValue(MaidTavernEntities.CURRENT_WORK.get());
    }

    public static @Nullable Work getWork(EntityMaid maid) {
        return maid.getBrain().getMemory(MaidTavernEntities.CURRENT_WORK.get()).orElse(null);
    }

    public static boolean isSameWorkTask(EntityMaid maid, Work work) {
        return work.task().equals(maid.getTask().getUid());
    }

    public static boolean isSameWorkType(EntityMaid maid, ResourceLocation type) {
        Work work = getWork(maid);
        if (work == null) {
            return false;
        }
        return work.type().equals(type);
    }

    public static void startWork(EntityMaid maid, Work work) {
        if (isWorking(maid) || !isSameWorkTask(maid, work)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.setMemory(MaidTavernEntities.CURRENT_WORK.get(), work);
        brain.setMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get(), 0);
        brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
        brain.eraseMemory(InitEntities.TARGET_POS.get());
    }

    public static void resetWorkExpiration(EntityMaid maid) {
        if (!isWorking(maid)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
    }

    public static void stopWork(EntityMaid maid) {
        if (!isWorking(maid)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(MaidTavernEntities.CURRENT_WORK.get());
        brain.eraseMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get());
        brain.eraseMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get());
    }
}
