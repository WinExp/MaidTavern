package com.winexp.maidtavern.item;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public interface MouseScrollingItem {
    boolean onMouseScroll(LocalPlayer player, ItemStack stack, double scrollX, double scrollY);
}
