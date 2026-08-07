package com.winexp.maidtavern;

import com.mojang.logging.LogUtils;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MaidTavern.MOD_ID)
public class MaidTavern {
    public static final String MOD_ID = "maidtavern";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidTavern(IEventBus modEventBus, ModContainer modContainer) {
        MaidTavernItems.register(modEventBus);
        MaidTavernEntities.register(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
