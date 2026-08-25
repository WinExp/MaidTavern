package com.winexp.maidtavern;

import com.mojang.logging.LogUtils;
import com.winexp.maidtavern.config.MaidTavernConfig;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.network.serverbound.MaidTavernNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MaidTavern.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MaidTavern {
    public static final String MOD_ID = "maidtavern";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidTavern() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MaidTavernConfig.CONFIG_SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MaidTavernItems.register(modEventBus);
        MaidTavernEntities.register(modEventBus);
    }

    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MaidTavernNetworking::init);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
