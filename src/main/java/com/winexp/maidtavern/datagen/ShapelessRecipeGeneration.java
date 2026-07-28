package com.winexp.maidtavern.datagen;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class ShapelessRecipeGeneration extends RecipeProvider {
    public ShapelessRecipeGeneration(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> output) {
        ItemLike[] GRAPES = new ItemLike[] {
                ModItems.GRAPE.get(),
                ModItems.ICE_GRAPE.get(),
                ModItems.GOLD_GRAPE.get(),
                ModItems.GREEN_GRAPE.get()
        };

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MaidTavernItems.BREWING_LIST.get())
                .requires(Items.PAPER)
                .requires(Items.FEATHER)
                .requires(Ingredient.of(GRAPES))
                .unlockedBy("has_grape", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(GRAPES).build()))
                .save(output);
    }
}
