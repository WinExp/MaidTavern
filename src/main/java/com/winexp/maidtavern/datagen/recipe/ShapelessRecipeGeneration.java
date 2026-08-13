package com.winexp.maidtavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ShapelessRecipeGeneration extends MaidTavernRecipeProvider {
    public ShapelessRecipeGeneration(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MaidTavernItems.BREWING_LIST)
                .requires(Items.PAPER)
                .requires(Items.FEATHER)
                .requires(ModItems.GRAPE)
                .unlockedBy("has_grape", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.GRAPE)))
                .save(output);

        resetData(RecipeCategory.MISC, MaidTavernItems.STORAGE_BINDING_TOOL, output);
        resetData(RecipeCategory.MISC, MaidTavernItems.BARREL_BINDING_TOOL, output);
    }

    public void resetData(RecipeCategory category, ItemLike itemLike, RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(category, itemLike)
                .requires(itemLike)
                .unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath(), inventoryTrigger(ItemPredicate.Builder.item()
                        .of(itemLike)))
                .save(output, RecipeBuilder.getDefaultRecipeId(itemLike).withSuffix("/reset_data"));
    }
}
