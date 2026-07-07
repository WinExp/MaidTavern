package com.winexp.maid.brew.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.entity.MaidTavernEntities;
import com.winexp.maid.brew.IBrewTask;
import com.winexp.maid.brew.BrewingList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MaidBrewMoveToStorageTask extends MaidMoveToBlockTask {
    private final IBrewTask task;
    private @Nullable ItemStack toStoreStackCached;

    public MaidBrewMoveToStorageTask(IBrewTask task, float movementSpeed, int verticalSearchRange) {
        super(movementSpeed, verticalSearchRange);
        this.task = task;
    }

    private @Nullable ItemStack getToStoreStack(EntityMaid maid) {
        if (toStoreStackCached == null) {
            toStoreStackCached = task.getToStoreStack(maid);
        }
        return toStoreStackCached;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        toStoreStackCached = null;
        Brain<EntityMaid> brain = maid.getBrain();
        BrewingList brewingList = brain.getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        boolean takeFlag = false;
        if (brewingList != null) {
            for (ResourceLocation recipeId : brewingList.getRecipes()) {
                if (!takeFlag && !task.hasRequiredMaterials(maid, recipeId, null)) {
                    takeFlag = true;
                }
            }
        } else return false;

        if (!super.checkExtraStartConditions(level, maid)
                || brain.hasMemoryValue(InitEntities.TARGET_POS.get())
                || brain.hasMemoryValue(MaidTavernEntities.BREWING_SESSION.get())
                || !brain.hasMemoryValue(MaidTavernEntities.BREWING_LIST.get())) return false;
        return takeFlag || getToStoreStack(maid) != null;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        searchForDestination(level, maid);
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        return task.isStorageValid(maid, pos);
    }
}
