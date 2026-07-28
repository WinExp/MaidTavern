package com.winexp.maidtavern.maid.brew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record StorageBinding(List<BlockPos> ingredients, List<BlockPos> results, List<BlockPos> byproducts) {
    public static final StorageBinding EMPTY = new StorageBinding(List.of(), List.of(), List.of());

    public static final Codec<StorageBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("ingredients").forGetter(StorageBinding::ingredients),
            BlockPos.CODEC.listOf().fieldOf("results").forGetter(StorageBinding::results),
            BlockPos.CODEC.listOf().fieldOf("byproducts").forGetter(StorageBinding::byproducts)
    ).apply(instance, StorageBinding::new));

    public boolean isAllEmpty() {
        return ingredients.isEmpty() && results.isEmpty() && byproducts.isEmpty();
    }

    public List<BlockPos> get(Type type) {
        return switch (type) {
            case INGREDIENTS -> ingredients;
            case RESULTS -> results;
            case BYPRODUCTS -> byproducts;
        };
    }

    public StorageBinding with(Type type, List<BlockPos> list) {
        return switch (type) {
            case INGREDIENTS -> new StorageBinding(list, results, byproducts);
            case RESULTS -> new StorageBinding(ingredients, list, byproducts);
            case BYPRODUCTS -> new StorageBinding(ingredients, results, list);
        };
    }

    public StorageBinding add(Type type, BlockPos pos) {
        return add(type, List.of(pos));
    }

    public StorageBinding add(Type type, List<BlockPos> list) {
        List<BlockPos> newList = new ArrayList<>(get(type));
        newList.addAll(list);
        return with(type, newList);
    }

    public StorageBinding remove(Type type, BlockPos pos) {
        List<BlockPos> newList = new LinkedList<>(get(type));
        newList.remove(pos);
        return with(type, newList);
    }

    public enum Type {
        INGREDIENTS,
        RESULTS,
        BYPRODUCTS
    }
}
