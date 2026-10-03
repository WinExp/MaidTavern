package com.winexp.maidtavern.maid.behavior.core;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.config.MaidTavernConfig;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.work.MaidWorkManager;
import com.winexp.maidtavern.maid.work.Work;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Optional;

public class MaidWorkPreCheckTask extends Behavior<EntityMaid> {
    private static final int DEFAULT_PATH_FINDING_COOLDOWN = 20;

    private int pathFindingCooldown = DEFAULT_PATH_FINDING_COOLDOWN;

    public MaidWorkPreCheckTask() {
        super(ImmutableMap.of());
    }

    @Override
    public void start(ServerLevel level, EntityMaid maid, long gameTime) {
        validateMemories(maid);
        checkWorkExpiration(maid);
        if (--pathFindingCooldown <= 0) {
            checkWorkPathFinding(maid);
            pathFindingCooldown = DEFAULT_PATH_FINDING_COOLDOWN;
        }
    }

    private void validateMemories(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        Work work = MaidWorkManager.getWork(maid);
        if (work != null) {
            if (!MaidWorkManager.isSameWorkTask(maid, work)) {
                MaidWorkManager.stopWork(maid);
                return;
            }
            brain.eraseMemory(InitEntities.TARGET_POS.get());

            Integer pathfindingAttempt = brain.getMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get()).orElse(null);
            if (pathfindingAttempt == null) {
                brain.setMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get(), 0);
            }
            Integer time = brain.getMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get()).orElse(null);
            if (time == null) {
                brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
            }
        }
    }

    private void checkWorkExpiration(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        Work work = MaidWorkManager.getWork(maid);
        if (work == null) return;
        int time = brain.getMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get()).get() + 1;
        if (time <= MaidTavernConfig.CONFIG.workExpirationTime.getAsInt()) {
            brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), time);
        } else {
            MaidWorkManager.stopWork(maid);
            MaidTavern.LOGGER.warn("Work {}/{} is stopped accidentally because of expiration", work.task(), work.type());
        }
    }

    private void checkWorkPathFinding(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        Work work = MaidWorkManager.getWork(maid);
        if (work == null) return;
        BlockPos pos = work.pos();
        if (!work.isCloseEnough(maid)) {
            Optional<WalkTarget> walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET);
            if (walkTarget.isEmpty() || !walkTarget.get().getTarget().currentPosition().equals(pos.getCenter())) {
                int attempt = brain.getMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get()).get() + 1;
                if (attempt <= MaidTavernConfig.CONFIG.pathFindingAttempt.getAsInt()) {
                    BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, work.movementSpeed(), 0);
                    brain.setMemory(MaidTavernEntities.PATH_FINDING_ATTEMPT.get(), attempt);
                } else {
                    MaidWorkManager.stopWork(maid);
                    MaidTavern.LOGGER.warn("Work {}/{} is stopped accidentally because of path finding check", work.task(), work.type());
                }
            }
        }
    }
}
