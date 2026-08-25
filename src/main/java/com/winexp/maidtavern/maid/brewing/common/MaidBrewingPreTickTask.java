package com.winexp.maidtavern.maid.brewing.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.config.MaidTavernConfig;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MaidBrewingPreTickTask extends Behavior<EntityMaid> {
    private static final int DEFAULT_PATH_FINDING_COOLDOWN = 20;

    private @Nullable MaidPathFindingBFS pathFinding;
    private int pathFindingCooldown = DEFAULT_PATH_FINDING_COOLDOWN;

    public MaidBrewingPreTickTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        brain.eraseMemory(InitEntities.TARGET_POS.get());
        validateMemories(maid);
        checkWorkExpiration(maid);
        if (--pathFindingCooldown <= 0) {
            checkWorkPathFinding(maid);
            pathFindingCooldown = DEFAULT_PATH_FINDING_COOLDOWN;
        }
    }

    private void validateMemories(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        ServerLevel level = (ServerLevel) maid.level();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null) {
            if (level.getRecipeManager().byKey(session.entry().recipeId()).isEmpty()) {
                brain.eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
            }
        }

        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList != null) {
            BrewingList.Builder builder = new BrewingList.Builder(brewingList);
            for (BrewingList.Entry entry : brewingList.getEntries()) {
                ResourceLocation recipeId = entry.recipeId();
                if (level.getRecipeManager().byKey(recipeId).isEmpty()) {
                    builder.remove(recipeId);
                }
            }
            brewingList = builder.build();
            if (brewingList.isEmpty()) {
                brain.eraseMemory(MaidTavernEntities.BREWING_LIST.get());
            } else {
                brain.setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);
            }
        }

        StorageBinding binding = brain.getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(null);
        if (binding != null) {
            if (binding.isAllEmpty()) {
                brain.eraseMemory(MaidTavernEntities.STORAGE_BINDING.get());
            }
        }

        if (MaidBrewingStateManager.isWorking(maid)) {
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
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
        if (work == null) return;
        if (!work.isCloseEnough(maid)) {
            brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), 0);
            return;
        }
        int time = brain.getMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get()).get() + 1;
        if (time <= MaidTavernConfig.CONFIG.workExpirationTime.getAsInt()) {
            brain.setMemory(MaidTavernEntities.WORK_EXPIRATION_TIME.get(), time);
        } else {
            MaidBrewingStateManager.stopWork(maid);
            MaidTavern.LOGGER.warn("Work {} is stopped accidentally because of expiration", work.type());
        }
    }

    private void checkWorkPathFinding(EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingWork work = MaidBrewingStateManager.getWork(maid);
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
                    MaidBrewingStateManager.stopWork(maid);
                    MaidTavern.LOGGER.warn("Work {} is stopped accidentally because of path finding check", work.type());
                }
            }
        }
    }
}
