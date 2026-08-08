package com.winexp.maidtavern.client.event;

import com.winexp.maidtavern.item.MouseScrollingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class OnMouseScroll {
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.isSpectator()) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof MouseScrollingItem mouseScrollingItem) {
            if (mouseScrollingItem.onMouseScroll(player, stack, event.getScrollDeltaX(), event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        }
    }
}
