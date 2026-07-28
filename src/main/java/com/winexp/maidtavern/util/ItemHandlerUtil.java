package com.winexp.maidtavern.util;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemHandlerUtil {
    public static List<ItemStack> toStacks(IItemHandler itemHandler) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static boolean isEmpty(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public static boolean canInsert(IItemHandler itemHandler, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(itemHandler, stack.copy(), true).isEmpty();
    }

    public static boolean canInsertAny(IItemHandler itemHandler, List<ItemStack> stacks) {
        if (stacks.isEmpty()) return false;
        for (ItemStack stack : stacks) {
            if (canInsert(itemHandler, stack)) return true;
        }
        return false;
    }

    public static boolean contains(IItemHandler itemHandler, Predicate<ItemStack> predicate) {
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

    public static List<ItemStack> findStacks(IItemHandler itemHandler, Predicate<ItemStack> predicate) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (predicate.test(stack)) {
                stacks.add(stack);
            }
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
