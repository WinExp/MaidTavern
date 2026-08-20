package com.winexp.maidtavern.maid.brewing.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.*;
import com.winexp.maidtavern.maid.brewing.barrel.MaidBrewingMoveToBarrelTask;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import com.winexp.maidtavern.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaidBrewingMoveToStorageTask extends MaidSurroundingMoveTask {
    private final IBrewingTask task;
    private final float movementSpeed;
    private final double closeEnoughDist;
    private @Nullable MaidPathFindingBFS pathFinding;
    private @Nullable BlockPos selectedBarrelPos;
    private @Nullable BrewingList.Entry selectedEntry;

    private List<ItemStack> resultsToInsert;
    private List<ItemStack> byproductsToInsert;
    private List<BrewingList.Entry> entries;

    public MaidBrewingMoveToStorageTask(IBrewingTask task, float movementSpeed, int verticalSearchRange, double closeEnoughDist, int minCheckTime) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
        setMaxCheckRate(minCheckTime);
        moveRange = new BoundingBox(-1, -2, -1, 1, 1, 1);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid)
                || MaidBrewingStateManager.isWorking(maid)
                || brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                || !brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get())) return false;
        return task.shouldExtract(maid) || !task.getResultsToInsert(maid).isEmpty() || !task.getByproductsToInsert(maid).isEmpty();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        selectedBarrelPos = null;
        selectedEntry = null;
        resultsToInsert = task.getResultsToInsert(maid);
        byproductsToInsert = task.getByproductsToInsert(maid);
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        entries = brewingList.orderPolicy().apply(brewingList.getEntries(), MaidBrewingStateManager.getRotationCounter(maid));

        searchForDestination(level, maid);
        var targetPos = brain.getMemory(InitEntities.TARGET_POS.get()).orElse(null);
        if (targetPos == null) return;
        MaidBrewingStateManager.startWork(maid, new BrewingWork(BrewingWorkTypes.STORAGE, targetPos.currentBlockPosition(), movementSpeed, closeEnoughDist));
        if (selectedEntry != null) {
            brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), BrewingSession.create(selectedEntry, selectedBarrelPos));
            MaidBrewingStateManager.addRotationCounter(maid);
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        Container container = Utils.getContainer(level, pos);
        if (container == null) return false;
        if (!MaidUtil.isStorageValid(level, pos)) return false;
        MaidPathFindingBFS pathFinding = getOrCreateArrivalMap(level, maid);
        Brain<EntityMaid> brain = maid.getBrain();
        IItemHandler containerInv = new InvWrapper(container);
        IItemHandler maidInv = maid.getAvailableInv(true);
        StorageBinding binding = brain.getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(null);

        if (ItemHandlerUtil.canInsertAny(maidInv, task.getBottlesToExtract(maidInv, containerInv).stream().map(Pair::getFirst).toList())) {
            if (binding == null || binding.ingredients().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, resultsToInsert)) {
            if (binding == null || binding.results().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, byproductsToInsert)) {
            if (binding == null || binding.byproducts().contains(pos)) return true;
        }

        for (BrewingList.Entry entry : entries) {
            BrewingList.Config config = entry.config();
            List<BlockPos> barrelPosList = new ArrayList<>(config.barrelPos());
            Collections.shuffle(barrelPosList);
            boolean valid = barrelPosList.isEmpty();
            for (BlockPos barrelPos : barrelPosList) {
                BlockState barrelState = level.getBlockState(barrelPos);
                if (MaidUtil.isPosOccupied(maid, barrelPos, maid1 -> {
                    BrewingSession session = maid1.getBrain().getMemory(MaidTavernEntities.BREWING_SESSION.get()).orElse(null);
                    return session == null ? null : session.barrelPos().orElse(null);
                })) continue;
                if (!task.isBarrelValid(maid, BarrelBlock.getBarrelEntity(level, barrelPos, barrelState))
                        || !MaidUtil.checkSurroundingPathReach(pathFinding, barrelPos.above(2), MaidBrewingMoveToBarrelTask.MOVE_RANGE)) continue;
                selectedBarrelPos = barrelPos;
                valid = true;
                break;
            }
            if (!valid) continue;

            if (ItemHandlerUtil.canInsertAny(maidInv, task.getIngredientsToExtract(maidInv, containerInv, level.getRecipeManager(), entry).stream().map(Pair::getFirst).toList())) {
                if (binding == null || binding.ingredients().contains(pos)) {
                    selectedEntry = entry;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected MaidPathFindingBFS getOrCreateArrivalMap(ServerLevel worldIn, EntityMaid maid) {
        if (pathFinding == null) pathFinding = super.getOrCreateArrivalMap(worldIn, maid);
        return pathFinding;
    }

    @Override
    protected void clearCurrentArrivalMap(MaidPathFindingBFS pathFinding) {
        super.clearCurrentArrivalMap(pathFinding);
        this.pathFinding = null;
    }
}
