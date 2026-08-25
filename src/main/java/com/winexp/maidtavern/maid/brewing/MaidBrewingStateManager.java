package com.winexp.maidtavern.maid.brewing;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.Brain;
import org.jetbrains.annotations.Nullable;

public class MaidBrewingStateManager {
    private static void ensureIsBrewTask(EntityMaid maid) {
        if (!(maid.getTask() instanceof IBrewingTask)) {
            throw new IllegalStateException();
        }
    }

    public static boolean isWorking(EntityMaid maid) {
        ensureIsBrewTask(maid);
        return maid.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_WORK.get());
    }

    public static @Nullable BrewingWork getWork(EntityMaid maid) {
        ensureIsBrewTask(maid);
        return maid.getBrain().getMemory(MaidTavernEntities.BREWING_WORK.get()).orElse(null);
    }

    public static boolean isSameWorkType(EntityMaid maid, ResourceLocation type) {
        ensureIsBrewTask(maid);
        if (!isWorking(maid)) {
            return false;
        }
        return getWork(maid).type().equals(type);
    }

    public static BlockPos getWorkPos(EntityMaid maid) {
        ensureIsBrewTask(maid);
        if (!isWorking(maid)) {
            throw new IllegalStateException();
        }
        return getWork(maid).pos();
    }

    public static void startWork(EntityMaid maid, BrewingWork work) {
        ensureIsBrewTask(maid);
        if (isWorking(maid)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.setMemory(MaidTavernEntities.BREWING_WORK.get(), work);
        brain.setMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get(), 0);
        brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
        brain.eraseMemory(InitEntities.TARGET_POS.get());
    }

    public static void resetWorkExpiration(EntityMaid maid) {
        ensureIsBrewTask(maid);
        if (!isWorking(maid)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
    }

    public static void stopWork(EntityMaid maid) {
        ensureIsBrewTask(maid);
        if (!isWorking(maid)) {
            throw new IllegalStateException();
        }
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(MaidTavernEntities.BREWING_WORK.get());
        brain.eraseMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get());
        brain.eraseMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get());
    }

    public static int getRotationCounter(EntityMaid maid) {
        return maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST_ROTATION_COUNTER.get()).orElse(0);
    }

    public static void addRotationCounter(EntityMaid maid) {
        int count = getRotationCounter(maid) + 1;
        maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST_ROTATION_COUNTER.get(), count % 16384);
    }
}
