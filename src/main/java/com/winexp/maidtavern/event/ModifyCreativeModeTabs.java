package com.winexp.maidtavern.event;

import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber
public class ModifyCreativeModeTabs {
    @SubscribeEvent
    public static void modifyCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitCreativeTabs.MAIN_TAB.get()) {
            event.accept(MaidTavernItems.BREWING_LIST);
            event.accept(MaidTavernItems.STORAGE_BINDING_TOOL);
        }
    }
}
