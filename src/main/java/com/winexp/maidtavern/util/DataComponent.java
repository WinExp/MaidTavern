package com.winexp.maidtavern.util;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public record DataComponent<T>(String id, T defaultValue, Codec<T> codec) {
    public T get(ItemStack stack) {
        Tag tag = stack.getOrCreateTag().get(id);
        if (tag == null) return defaultValue;
        return codec.parse(NbtOps.INSTANCE, tag).result().orElse(defaultValue);
    }

    public void set(ItemStack stack, T value) {
        Tag tag = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, message -> {});
        stack.addTagElement(id, tag);
    }
}
