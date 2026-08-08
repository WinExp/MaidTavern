package com.winexp.maidtavern.maid.brew.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaidBrewMoveToStorageTask extends MaidSurroundingMoveTask {
    private final IBrewTask task;
    private @Nullable MaidPathFindingBFS pathFinding;
    private @Nullable BlockPos selectedBarrelPos;
    private @Nullable BrewingList.Entry selectedEntry;

    public MaidBrewMoveToStorageTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
        setMaxCheckRate(20);
        moveRange = new BoundingBox(-1, -2, -1, 1, 1, 1);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid)
                || brain.hasMemoryValue(InitEntities.TARGET_POS.get())
                || brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                || !brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get())) return false;
        return task.shouldExtract(maid) || !task.getResultsToInsert(maid).isEmpty() || !task.getByproductsToInsert(maid).isEmpty();
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        selectedBarrelPos = null;
        selectedEntry = null;
        Brain<EntityMaid> brain = maid.getBrain();
        searchForDestination(level, maid);
        if (selectedEntry != null) {
            brain.setMemory(MaidTavernEntities.BREWING_SESSION.get(), BrewingSession.create(selectedEntry, selectedBarrelPos));
        }
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof Container container)) return false;
        if (!task.isStorageValid(level, pos)) return false;
        MaidPathFindingBFS pathFinding = getOrCreateArrivalMap(level, maid);
        Brain<EntityMaid> brain = maid.getBrain();
        IItemHandler containerInv = new InvWrapper(container);
        IItemHandler maidInv = maid.getAvailableInv(true);
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).get();
        StorageBinding binding = brain.getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(null);

        if (ItemHandlerUtil.canInsertAny(maidInv, task.getBottlesToExtract(maidInv, containerInv).stream().map(Pair::getFirst).toList())) {
            if (binding == null || binding.ingredients().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, task.getResultsToInsert(maid))) {
            if (binding == null || binding.results().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, task.getByproductsToInsert(maid))) {
            if (binding == null || binding.byproducts().contains(pos)) return true;
        }

        for (BrewingList.Entry entry : brewingList.getEntries()) {
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
                if (!task.isBarrelValid(maid, BarrelBlock.getBarrelEntity(level, barrelPos, barrelState)) || !checkPathReach(maid, pathFinding, barrelPos.above(2))) continue;
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
