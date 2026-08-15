package com.winexp.maidtavern.util;

import com.winexp.maidtavern.client.gui.brewing_list.BrewingListScreen;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ScreenUtil {
    public static void openBrewingListScreen(Player player, InteractionHand hand, BrewingList brewingList) {
        Minecraft.getInstance().setScreen(new BrewingListScreen((LocalPlayer) player, hand, brewingList));
    }
}
