package com.winexp.maidtavern.client.event;

import com.winexp.maidtavern.item.MouseScrollingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class OnMouseScroll {
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.isSpectator()) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof MouseScrollingItem mouseScrollingItem) {
            if (mouseScrollingItem.onMouseScroll(player, stack, event.getScrollDelta())) {
                event.setCanceled(true);
            }
        }
    }
}
