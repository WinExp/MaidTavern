package com.winexp.maidtavern.datagen.recipe;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MaidTavernRecipeGeneration extends MaidTavernRecipeProvider {
    private final List<MaidTavernRecipeProvider> providers = new ArrayList<>();

    public MaidTavernRecipeGeneration(PackOutput output) {
        super(output);

        providers.add(new ShapedRecipeGeneration(output));
        providers.add(new ShapelessRecipeGeneration(output));
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        for (MaidTavernRecipeProvider provider : providers) {
            provider.buildRecipes(writer);
        }
    }
}
