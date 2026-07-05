package com.winexp.util;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemHandlerUtil {
    public static boolean isEmpty(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public static boolean canInsert(IItemHandler itemHandler, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(itemHandler, stack.copyWithCount(1), true).isEmpty();
    }

    public static boolean hasItem(IItemHandler itemHandler, Predicate<ItemStack> predicate) {
        return matchesCount(itemHandler, predicate, MinMaxBounds.Ints.atLeast(1));
    }

    public static boolean matchesCount(IItemHandler itemHandler, Predicate<ItemStack> predicate, MinMaxBounds.Ints countRange) {
        if (countRange.matches(0)) return true;
        return countRange.matches(countItems(itemHandler, predicate));
    }

    public static int countItems(IItemHandler itemHandler, Predicate<ItemStack> predicate) {
        int count = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (predicate.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static @Nullable ItemStack findStack(IItemHandler itemHandler, Predicate<ItemStack> predicate) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (predicate.test(stack)) return stack;
        }
        return null;
    }

    public static List<ItemStack> findStacks(IItemHandler itemHandler, Predicate<ItemStack> predicate, int count) {
        if (count <= 0) return List.of();
        List<ItemStack> stacks = new ArrayList<>();
        int count1 = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (predicate.test(stack)) {
                count1 += stack.getCount();
                stacks.add(stack);
            }
            if (count1 >= count) return stacks;
        }
        return stacks;
    }

    public static boolean replaceStack(IItemHandlerModifiable itemHandler, ItemStack from, ItemStack to) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack == from) {
                itemHandler.setStackInSlot(i, to);
                return true;
            }
        }
        return false;
    }
}
