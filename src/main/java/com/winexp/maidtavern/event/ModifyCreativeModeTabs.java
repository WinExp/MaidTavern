package com.winexp.maidtavern.event;

import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class ModifyCreativeModeTabs {
    @SubscribeEvent
    public static void modifyCreativeModeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == InitCreativeTabs.MAIN_TAB.get()) {
            event.accept(MaidTavernItems.BREWING_LIST);
        }
    }
}
