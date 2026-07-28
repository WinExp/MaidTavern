package com.winexp.maidtavern.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.winexp.maidtavern.item.MaidInteractionItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class OnInteractMaid {
    @SubscribeEvent
    public static void onInteractMaid(InteractMaidEvent event) {
        if (event.getStack().getItem() instanceof MaidInteractionItem item) {
            if (item.useOnMaid(event.getWorld(), event.getPlayer(), event.getMaid(), event.getStack())) {
                event.setCanceled(true);
            }
        }
    }
}
