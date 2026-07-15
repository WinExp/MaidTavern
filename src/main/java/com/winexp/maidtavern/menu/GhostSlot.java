package com.winexp.maidtavern.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GhostSlot extends Slot {
    private final List<SlotClickedListener> listeners = new ArrayList<>();

    public GhostSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
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
    public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        super.tryRemove(count, decrement, player);
        return Optional.empty();
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int increment) {
        super.safeInsert(stack.copy(), increment);
        return stack;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    public interface SlotClickedListener {
        void onSlotClicked(GhostSlot slot, Player player, ItemStack carriedStack, ItemStack slotStack, ClickAction action, SlotAccess carriedSlotAccess);
    }
}
