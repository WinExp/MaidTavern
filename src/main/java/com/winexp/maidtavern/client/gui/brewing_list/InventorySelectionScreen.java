package com.winexp.maidtavern.client.gui.brewing_list;

import com.mojang.blaze3d.systems.RenderSystem;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.menu.GhostSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class InventorySelectionScreen extends AbstractContainerScreen<InventorySelectionScreen.InventorySelectionMenu> {
    public static final ResourceLocation BACKGROUND = MaidTavern.asResource("textures/gui/brewing_list/inventory_selection.png");

    public InventorySelectionScreen(LocalPlayer player, Predicate<ItemStack> filter, Consumer<ItemStack> callback) {
        super(new InventorySelectionMenu(player.getInventory(), filter, callback), player.getInventory(), Component.empty());
        menu.closeScreenRunnable = this::onClose;
        imageWidth = 176;
        imageHeight = 90;
    }

    @Override
    public void onClose() {
        minecraft.popGuiLayer();
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (type == ClickType.QUICK_MOVE) type = ClickType.PICKUP;
        menu.clicked(slotId, mouseButton, type, minecraft.player);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 0);
        for (Slot slot : menu.slots) {
            GhostSlot ghostSlot = (GhostSlot) slot;
            int x = slot.x;
            int y = slot.y;
            if (!ghostSlot.highlightable) {
                guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0x804a4a4a, 0x804a4a4a, 0);
            }
        }
        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (hoveredSlot != null && hoveredSlot instanceof GhostSlot slot) {
            if (!slot.renderTooltip) return;
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        renderBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND, (width - 256) / 2, (height - 256) / 2, 0, 0, 256, 256, 256, 256);
    }

    @OnlyIn(Dist.CLIENT)
    public static class InventorySelectionMenu extends AbstractContainerMenu {
        private final Predicate<ItemStack> filter;
        private final Consumer<ItemStack> callback;
        private Runnable closeScreenRunnable;

        private InventorySelectionMenu(Inventory inventory, Predicate<ItemStack> filter, Consumer<ItemStack> callback) {
            super(null, 0);
            this.filter = filter;
            this.callback = callback;
            for (int i = 0; i < 9; i++) {
                GhostSlot slot = new GhostSlot(inventory, i, 8 + i * 18, 66);
                slot.addListener(this::onSlotClicked);
                addSlot(slot);
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    GhostSlot slot = new GhostSlot(inventory, 9 + i * 9 + j, 8 + j * 18, 8 + i * 18);
                    slot.addListener(this::onSlotClicked);
                    addSlot(slot);
                }
            }
            rebuildAvailableMap();
        }

        private void onSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ClickAction action, SlotAccess carriedSlotAccess) {
            ItemStack stack = slot.getItem();
            if (!filter.test(stack)) return;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            callback.accept(stack);
            closeScreenRunnable.run();
        }

        private void rebuildAvailableMap() {
            for (Slot slot : slots) {
                GhostSlot ghostSlot = (GhostSlot) slot;
                ghostSlot.highlightable = false;
                ghostSlot.renderTooltip = false;
                if (!ghostSlot.isActive()) continue;
                if (filter.test(ghostSlot.getItem())) {
                    ghostSlot.highlightable = true;
                    ghostSlot.renderTooltip = true;
                }
            }
        }

        @Override
        public ItemStack quickMoveStack(Player player, int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
