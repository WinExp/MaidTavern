package com.winexp.maidtavern.event;

import com.winexp.maidtavern.menu.GhostSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

@EventBusSubscriber
public class OnGhostSlotClicked {
    @SubscribeEvent
    public static void onGhostSlotClicked(ItemStackedOnOtherEvent event) {
        if (event.getSlot() instanceof GhostSlot slot) {
            slot.onClicked(event.getPlayer(), event.getCarriedItem(), event.getStackedOnItem(), event.getClickAction(), event.getCarriedSlotAccess());
            event.setCanceled(true);
        }
    }
}
