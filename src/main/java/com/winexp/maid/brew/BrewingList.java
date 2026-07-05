package com.winexp.maid.brew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BrewingList {
    public static final Codec<BrewingList> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("recipes").validate(list -> {
                if (!list.isEmpty()) return DataResult.success(list);
                else return DataResult.error(() -> "recipes cannot be empty");
            }).forGetter(BrewingList::getRecipes)
    ).apply(instance, BrewingList::new));
    private final List<ResourceLocation> recipeIds;

    public BrewingList(List<ResourceLocation> recipes) {
        if (recipes.isEmpty()) throw new IllegalArgumentException("recipes cannot be empty");
        recipeIds = new ArrayList<>(recipes);
    }

    public boolean isEmpty() {
        return recipeIds.isEmpty();
    }

    public @Nullable ResourceLocation pop() {
        if (isEmpty()) return null;
        ResourceLocation recipeId = recipeIds.removeFirst();
        recipeIds.addLast(recipeId);
        return recipeId;
    }

    public @Nullable ResourceLocation get() {
        if (isEmpty()) return null;
        return recipeIds.getFirst();
    }

    public void remove(ResourceLocation recipeId) {
        recipeIds.remove(recipeId);
    }

    public List<ResourceLocation> getRecipes() {
        return new ArrayList<>(recipeIds);
    }
}
