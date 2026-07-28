package com.winexp.maidtavern.maid.brew.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import com.winexp.maidtavern.maid.task.MaidSurroundingMoveTask;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class MaidBrewMoveToStorageTask extends MaidSurroundingMoveTask {
    private final IBrewTask task;

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
        searchForDestination(level, maid);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof Container container)) return false;
        if (!task.isStorageValid(level, pos)) return false;
        Brain<EntityMaid> brain = maid.getBrain();
        IItemHandler containerInv = new InvWrapper(container);
        StorageBinding binding = brain.getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(null);

        if (!task.getStacksToExtract(maid, containerInv).isEmpty()) {
            if (binding == null || binding.ingredients().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, task.getResultsToInsert(maid))) {
            if (binding == null || binding.results().contains(pos)) return true;
        }

        if (ItemHandlerUtil.canInsertAny(containerInv, task.getByproductsToInsert(maid))) {
            if (binding == null || binding.byproducts().contains(pos)) return true;
        }
        return false;
    }
}
