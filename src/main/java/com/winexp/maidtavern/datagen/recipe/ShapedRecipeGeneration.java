package com.winexp.maidtavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ShapedRecipeGeneration extends MaidTavernRecipeProvider {
    public ShapedRecipeGeneration(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MaidTavernItems.BARREL_SELECTION_TOOL)
                .pattern("#X#")
                .pattern("X1X")
                .pattern("#X#")
                .define('#', Items.GOLD_INGOT)
                .define('X', Items.IRON_INGOT)
                .define('1', ModItems.BARREL)
                .unlockedBy("has_barrel", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.BARREL)))
                .save(output);
    }
}
