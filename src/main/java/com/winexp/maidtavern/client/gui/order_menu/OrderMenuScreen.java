package com.winexp.maidtavern.client.gui.order_menu;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModDataComponents;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.network.serverbound.ServerboundOrderPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;
import java.util.UUID;

public class OrderMenuScreen extends Screen {
    private ExtendedButton testButton;
    private EditBox uuidEditBox;

    public OrderMenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        testButton = new ExtendedButton((this.width / 2) - 40, (this.height / 2) - 50, 80, 40, Component.literal("Test"), this::testButtonClicked);
        addRenderableWidget(testButton);
        uuidEditBox = new EditBox(minecraft.font, (this.width / 2) - 40, (this.height / 2) + 10, 80, 20, Component.empty());
        uuidEditBox.setEditable(false);
        addRenderableWidget(uuidEditBox);
    }

    private void testButtonClicked(Button button) {
        ItemStack stack = ModItems.WINE.toStack();
        stack.set(ModDataComponents.BREW_LEVEL, 5);
        Minecraft.getInstance().getConnection().send(new ServerboundOrderPayload(new Order(List.of(stack), List.of(
                new BlockPos(-14, -59, 22),
                new BlockPos(-15, -59, 22),
                new BlockPos(-16, -59, 22)
        ))));
    }

    public void setUuid(UUID uuid) {
        uuidEditBox.setValue(uuid.toString());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
