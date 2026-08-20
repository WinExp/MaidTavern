package com.winexp.maidtavern.event;

import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModifyCreativeModeTabs {
    @SubscribeEvent
    public static void modifyCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitCreativeTabs.MAIN_TAB.get()) {
            event.accept(MaidTavernItems.BREWING_LIST);
            event.accept(MaidTavernItems.STORAGE_BINDING_TOOL);
            event.accept(MaidTavernItems.BARREL_SELECTION_TOOL);
        }
    }
}
