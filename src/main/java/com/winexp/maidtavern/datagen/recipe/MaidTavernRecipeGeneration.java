package com.winexp.maidtavern.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MaidTavernRecipeGeneration extends RecipeProvider {
    private final List<MaidTavernRecipeProvider> providers = new ArrayList<>();

    public MaidTavernRecipeGeneration(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);

        providers.add(new ShapedRecipeGeneration(output, registries));
        providers.add(new ShapelessRecipeGeneration(output, registries));
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (MaidTavernRecipeProvider provider : providers) {
            provider.buildRecipes(output);
        }
    }
}
