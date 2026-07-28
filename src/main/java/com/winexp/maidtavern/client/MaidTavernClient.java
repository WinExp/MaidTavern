package com.winexp.maidtavern.client;

import com.winexp.maidtavern.menu.BrewingListMenu;
import com.winexp.maidtavern.menu.MaidTavernMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class MaidTavernClient {
    @SubscribeEvent
    public static void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(MaidTavernMenuTypes.BREWING_LIST.get(), BrewingListMenu::create));
    }
}
