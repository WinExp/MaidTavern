package com.winexp.maid.brew.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.IBrewTask;
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
import net.minecraft.world.level.block.state.BlockState;

public class MaidBrewMoveToBarrelTask extends MaidMoveToBlockTask {
    private final IBrewTask task;
    private final float movementSpeed;

    public MaidBrewMoveToBarrelTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        this.movementSpeed = movementSpeed;
        setMaxCheckRate(60);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid) || brain.hasMemoryValue(InitEntities.TARGET_POS.get())) return false;
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return true;
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList != null) {
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                if (task.hasRequiredMaterials(maid, recipeId, null)) {
                    brewingList.select(recipeId);
                    return true;
                }
            }
        }
        return false;
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
                ResourceLocation recipeId = brewingList.get();
                brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), BrewingSession.create(recipeId, targetPos.get().currentBlockPosition()));
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = (BlockPos.MutableBlockPos) pos;
        BlockState state = level.getBlockState(mutablePos);
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
        if (task.isBarrelAvailable(maid, barrel)) {
            mutablePos.set(BarrelBlock.getOriginPos(pos, state).above(2));
            return true;
        }
        return false;
    }
}
