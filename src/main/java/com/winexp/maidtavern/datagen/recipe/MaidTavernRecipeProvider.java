package com.winexp.maidtavern.datagen.recipe;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public abstract class MaidTavernRecipeProvider extends RecipeProvider {
    public MaidTavernRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public abstract void buildRecipes(Consumer<FinishedRecipe> writer);
}
