package com.winexp.maidtavern.compat.maid;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.behavior.core.MaidDrinkingTask;
import com.winexp.maidtavern.maid.behavior.core.MaidWorkPreCheckTask;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class TavernExtraMaidBrain implements IExtraMaidBrain {
    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return Lists.newArrayList(
                MaidTavernEntities.MOLOTOV_DRUNK.get(),

                MaidTavernEntities.BREWING_LIST.get(),
                MaidTavernEntities.BREWING_SESSION.get(),
                MaidTavernEntities.BREWING_LIST_ROTATION_COUNTER.get(),

                MaidTavernEntities.STORAGE_BINDING.get(),

                MaidTavernEntities.CURRENT_WORK.get(),
                MaidTavernEntities.PATH_FINDING_ATTEMPT.get(),
                MaidTavernEntities.WORK_EXPIRATION_TIME.get(),

                MaidTavernEntities.WAITER_ORDERS.get(),
                MaidTavernEntities.WAITER_STORAGE_BINDING.get()
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getCoreBehaviors() {
        return Lists.newArrayList(
                Pair.of(5, new MaidDrinkingTask())
        );
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> getWorkBehaviors() {
        return Lists.newArrayList(
            Pair.of(Integer.MIN_VALUE, new MaidWorkPreCheckTask())
        );
    }
}
