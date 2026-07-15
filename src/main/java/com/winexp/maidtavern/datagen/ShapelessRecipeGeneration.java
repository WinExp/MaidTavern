package com.winexp.maidtavern.datagen;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ShapelessRecipeGeneration extends RecipeProvider {
    public ShapelessRecipeGeneration(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ItemLike[] GRAPES = new ItemLike[] {
                ModItems.GRAPE,
                ModItems.ICE_GRAPE,
                ModItems.GOLD_GRAPE,
                ModItems.GREEN_GRAPE
        };

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MaidTavernItems.BREWING_LIST)
                .requires(Items.PAPER)
                .requires(Items.FEATHER)
                .requires(Ingredient.of(GRAPES))
                .unlockedBy("has_grape", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(GRAPES)))
                .save(output);
    }
}
