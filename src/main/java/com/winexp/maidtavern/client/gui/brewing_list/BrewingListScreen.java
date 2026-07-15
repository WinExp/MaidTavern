package com.winexp.maidtavern.client.gui.brewing_list;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.menu.BrewingListMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BrewingListScreen extends AbstractContainerScreen<BrewingListMenu> {
    public static final ResourceLocation LIST_LOCATION = MaidTavern.asResource("textures/gui/brewing_list/list.png");

    public BrewingListScreen(BrewingListMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(LIST_LOCATION, (width - 384) / 2, (height - 384) / 2, 0, 0, 384, 384, 512, 512);
    }
}
