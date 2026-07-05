package com.winexp.maid.brew.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.IBrewTask;
import com.winexp.maid.brew.BrewingList;
import com.winexp.maid.brew.BrewingSession;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MaidBrewMoveToBarrelTask extends MaidMoveToBlockTask {
    private final IBrewTask task;
    private final float movementSpeed;

    public MaidBrewMoveToBarrelTask(IBrewTask task, float movementSpeed) {
        super(movementSpeed, 3);
        this.task = task;
        this.movementSpeed = movementSpeed;
        setMaxCheckRate(40);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid) || brain.hasMemoryValue(InitEntities.TARGET_POS.get())) return false;
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return true;
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList != null) {
            return task.hasRequiredMaterials(maid, brewingList.get());
        } else return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null) {
            if (maid.level().getRecipeManager().byKey(session.recipeId()).isEmpty()) return;
            BlockPos barrelPos = session.barrelPos();
            IBarrel barrel = task.getBarrel(level, barrelPos);
            if (!task.isBarrelAvailable(maid, barrel)) {
                brain.eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
                return;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(maid, barrelPos, movementSpeed, 0);
            brain.setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(barrelPos));
        } else {
            searchForDestination(level, maid);
            var targetPos = brain.getMemory(InitEntities.TARGET_POS.get());
            if (targetPos.isPresent()) {
                ResourceLocation recipeId = brewingList.pop();
                brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), BrewingSession.create(recipeId, targetPos.get().currentBlockPosition()));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        IBarrel barrel = task.getBarrel(level, pos);
        Brain<EntityMaid> brain = maid.getBrain();
        var nearestEntities = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
        // 酒桶占用检查，防止多个女仆使用同一酒桶
        if (nearestEntities.isPresent()) {
            for (LivingEntity entity : nearestEntities.get()) {
                if (entity instanceof EntityMaid maid1
                        && maid1.getBrain().hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) {
                    var maid1Target = maid1.getBrain().getMemory(InitEntities.TARGET_POS.get());
                    if (maid1Target.isPresent() && maid1Target.get().currentBlockPosition().equals(pos)) {
                        return false;
                    }
                }
            }
        }
        return task.isBarrelAvailable(maid, barrel);
    }
}
