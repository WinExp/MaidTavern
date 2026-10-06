package com.winexp.maidtavern.util;

import com.winexp.maidtavern.client.gui.brewing_list.BrewingListScreen;
import com.winexp.maidtavern.client.gui.order_menu.OrderMenuScreen;
import com.winexp.maidtavern.maid.behavior.brewing.BrewingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ScreenUtil {
    private static void setScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openBrewingListScreen(Player player, InteractionHand hand, BrewingList brewingList) {
        setScreen(new BrewingListScreen((LocalPlayer) player, hand, brewingList));
    }

    public static void openOrderMenuScreen() {
        setScreen(new OrderMenuScreen());
    }
}
