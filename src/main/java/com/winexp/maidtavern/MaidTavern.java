package com.winexp.maidtavern;

import com.mojang.logging.LogUtils;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.menu.MaidTavernMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MaidTavern.MOD_ID)
public class MaidTavern {
    public static final String MOD_ID = "maidtavern";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidTavern() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MaidTavernItems.register(modEventBus);
        MaidTavernEntities.register(modEventBus);
        MaidTavernMenuTypes.register(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
