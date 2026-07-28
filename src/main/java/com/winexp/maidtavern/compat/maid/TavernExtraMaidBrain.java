package com.winexp.maidtavern.compat.maid;

import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.google.common.collect.Lists;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class TavernExtraMaidBrain implements IExtraMaidBrain {
    @Override
    public List<MemoryModuleType<?>> getExtraMemoryTypes() {
        return Lists.newArrayList(
                MaidTavernEntities.BREWING_LIST.get(),
                MaidTavernEntities.BREWING_SESSION.get(),

                MaidTavernEntities.STORAGE_BINDING.get()
        );
    }
}
