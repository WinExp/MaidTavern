package com.winexp.maidtavern.maid.brew.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.IBrewTask;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class MaidBrewPreCheckTask extends Behavior<EntityMaid> {
    private final IBrewTask task;

    public MaidBrewPreCheckTask(IBrewTask task) {
        super(ImmutableMap.of());
        this.task = task;
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
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
    }
}
