package com.winexp.maidtavern.event;

import com.winexp.maidtavern.menu.GhostSlot;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class OnGhostSlotClicked {
    @SubscribeEvent
    public static void onGhostSlotClicked(ItemStackedOnOtherEvent event) {
        if (event.getSlot() instanceof GhostSlot slot) {
            slot.onClicked(event.getPlayer(), event.getCarriedItem(), event.getStackedOnItem(), event.getClickAction(), event.getCarriedSlotAccess());
            event.setCanceled(true);
        }
    }
}
