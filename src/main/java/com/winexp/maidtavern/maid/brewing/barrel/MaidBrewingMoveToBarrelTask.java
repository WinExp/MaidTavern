package com.winexp.maidtavern.maid.brewing.barrel;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.*;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.MaidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public class MaidBrewingMoveToBarrelTask extends MaidSurroundingMoveTask {
    public static final BoundingBox MOVE_RANGE = new BoundingBox(-2, -1, -2, 2, 1, 2);

    private final IBrewingTask task;
    private final float movementSpeed;
    private final double closeEnoughDist;
    private BrewingList.Entry selectedEntry;

    public MaidBrewingMoveToBarrelTask(IBrewingTask task, float movementSpeed, int verticalSearchRange, double closeEnoughDist, int minCheckTime) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
        setMaxCheckRate(minCheckTime);
        moveRange = MOVE_RANGE;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        selectedEntry = null;
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid)
                || MaidBrewingStateManager.isWorking(maid)) return false;
        if (brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())) return true;
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) return false;
        List<BrewingList.Entry> entries = brewingList.orderPolicy().apply(brewingList.getEntries(), MaidBrewingStateManager.getRotationCounter(maid));
        for (BrewingList.Entry entry : entries) {
            ResourceLocation recipeId = entry.recipeId();
            if (task.hasIngredients(maid, recipeId)) {
                selectedEntry = entry;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingSession session = brain.getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
        if (session != null) {
            selectedEntry = session.entry();
            BlockPos barrelPos = session.barrelPos().orElse(null);
            if (barrelPos == null) {
                searchForDestination(level, maid);
                barrelPos = brain.getMemory(InitEntities.TARGET_POS.get()).map(PositionTracker::currentBlockPosition).orElse(null);
                if (barrelPos == null) {
                    brain.eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
                    return;
                }
                brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), session.withBarrelPos(barrelPos.below(2)));
            } else barrelPos = barrelPos.above(2);
            BlockState barrelState = level.getBlockState(barrelPos);
            IBarrel barrel = BarrelBlock.getBarrelEntity(level, barrelPos, barrelState);
            if (!task.isBarrelValid(maid, barrel)) {
                brain.eraseMemory(MaidTavernEntities.BREWING_SESSION.get());
                return;
            }
            BehaviorUtils.setWalkAndLookTargetMemories(maid, barrelPos, movementSpeed, 0);
            MaidBrewingStateManager.startWork(maid, new BrewingWork(BrewingWorkTypes.ADD_INGREDIENTS, barrelPos, movementSpeed, closeEnoughDist));
        } else {
            searchForDestination(level, maid);
            var targetPos = brain.getMemory(InitEntities.TARGET_POS.get());
            targetPos.map(PositionTracker::currentBlockPosition).ifPresent(pos -> {
                brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), new BrewingSession(selectedEntry, pos.below(2), BrewingSession.Stage.START_BREWING));
                MaidBrewingStateManager.startWork(maid, new BrewingWork(BrewingWorkTypes.ADD_INGREDIENTS, pos, movementSpeed, closeEnoughDist));
                MaidBrewingStateManager.addRotationCounter(maid);
            });
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = (BlockPos.MutableBlockPos) pos;
        BlockState state = level.getBlockState(pos);
        BarrelBlockEntity barrel = BarrelBlock.getBarrelEntity(level, pos, state);
        if (!task.isBarrelValid(maid, barrel)) return false;
        BlockPos barrelOriginPos = barrel.getBlockPos();
        BrewingList.Config config = selectedEntry.config();
        if (!config.barrelPos().isEmpty() && !config.barrelPos().contains(barrelOriginPos)) return false;
        mutablePos.set(barrelOriginPos.above(2));
        return !MaidUtil.isPosOccupied(maid, barrelOriginPos, maid1 -> {
            BrewingSession session = maid1.getBrain().getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
            return session == null ? null : session.barrelPos().orElse(null);
        });
    }
}
