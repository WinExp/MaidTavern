package com.winexp.maidtavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ShapelessRecipeGeneration extends MaidTavernRecipeProvider {
    public ShapelessRecipeGeneration(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MaidTavernItems.BREWING_LIST.get())
                .requires(Items.PAPER)
                .requires(Items.FEATHER)
                .requires(ModItems.GRAPE.get())
                .unlockedBy("has_grape", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.GRAPE.get()).build()))
                .save(writer);

        resetData(RecipeCategory.MISC, MaidTavernItems.BREWING_LIST.get(), writer);
        resetData(RecipeCategory.MISC, MaidTavernItems.STORAGE_BINDING_TOOL.get(), writer);
        resetData(RecipeCategory.MISC, MaidTavernItems.BARREL_SELECTION_TOOL.get(), writer);
    }

    public void resetData(RecipeCategory category, ItemLike itemLike, Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(category, itemLike)
                .requires(itemLike)
                .unlockedBy("has_" + ForgeRegistries.ITEMS.getKey(itemLike.asItem()).getPath(), inventoryTrigger(ItemPredicate.Builder.item()
                        .of(itemLike).build()))
                .save(writer, RecipeBuilder.getDefaultRecipeId(itemLike).withSuffix("/reset_data"));
    }
}
