package com.winexp.maidtavern.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GhostSlot extends Slot {
    private final List<SlotClickedListener> listeners = new ArrayList<>();
    public HighlightPredicate highlightPredicate;
    public boolean highlightable = true;
    public boolean renderTooltip = true;
    private boolean active = true;

    public GhostSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addListener(SlotClickedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SlotClickedListener listener) {
        listeners.remove(listener);
    }

    public void onClicked(Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess) {
        for (SlotClickedListener listener : listeners) {
            listener.onSlotClicked(this, player, carriedStack, slotStack, action, carriedSlotAccess);
        }
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    @Override
    public boolean isHighlightable() {
        return highlightable;
    }

    public interface SlotClickedListener {
        void onSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess);
    }

    public interface HighlightPredicate {
        boolean shouldRenderHighlight(GhostSlot slot, int mouseX, int mouseY);
    }
}
