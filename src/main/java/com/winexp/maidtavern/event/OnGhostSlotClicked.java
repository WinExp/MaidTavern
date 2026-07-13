package com.winexp.maidtavern.event;

import com.winexp.maidtavern.menu.GhostSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

@EventBusSubscriber
public class OnGhostSlotClicked {
    @SubscribeEvent
    public static void onGhostSlotClicked(ItemStackedOnOtherEvent event) {
        if (event.getSlot() instanceof GhostSlot slot) {
            Player player = event.getPlayer();
            ItemStack carriedItem = event.getCarriedItem();
            ItemStack slotItem = event.getStackedOnItem();
            if (carriedItem.isEmpty()) slot.tryRemove(slotItem.getCount(), Integer.MAX_VALUE, player);
            else slot.set(carriedItem.copy());
            event.setCanceled(true);
        }
    }
}
