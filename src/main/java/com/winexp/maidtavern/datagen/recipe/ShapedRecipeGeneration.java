package com.winexp.maidtavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ShapedRecipeGeneration extends MaidTavernRecipeProvider {
    public ShapedRecipeGeneration(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MaidTavernItems.STORAGE_BINDING_TOOL.get())
                .pattern("#X#")
                .pattern("X1X")
                .pattern("#X#")
                .define('#', Items.GOLD_INGOT)
                .define('X', Items.IRON_INGOT)
                .define('1', Items.BARREL)
                .unlockedBy("has_barrel", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(Items.BARREL).build()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MaidTavernItems.BARREL_SELECTION_TOOL.get())
                .pattern("#X#")
                .pattern("X1X")
                .pattern("#X#")
                .define('#', Items.GOLD_INGOT)
                .define('X', Items.IRON_INGOT)
                .define('1', ModItems.BARREL.get())
                .unlockedBy("has_barrel", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.BARREL.get()).build()))
                .save(writer);
    }
}
