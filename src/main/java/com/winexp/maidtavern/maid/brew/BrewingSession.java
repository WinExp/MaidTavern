package com.winexp.maidtavern.maid.brew;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public record BrewingSession(ResourceLocation recipeId, BlockPos barrelPos, MutableBoolean fluidPlaced, MutableBoolean ingredientsPlaced) {
    public static final Codec<BrewingSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(BrewingSession::recipeId),
            BlockPos.CODEC.fieldOf("barrel_pos").forGetter(BrewingSession::barrelPos),
            Codec.BOOL.fieldOf("fluid_placed").forGetter(session -> session.fluidPlaced.booleanValue()),
            Codec.BOOL.fieldOf("ingredients_placed").forGetter(session -> session.ingredientsPlaced.booleanValue())
    ).apply(instance, BrewingSession::create));

    public @Nullable BarrelRecipe getRecipe(RecipeManager manager) {
        return (BarrelRecipe) manager.byKey(recipeId).orElse(null);
    }

    public static BrewingSession create(ResourceLocation recipeId, BlockPos barrelPos) {
        return BrewingSession.create(recipeId, barrelPos, false, false);
    }

    public static BrewingSession create(ResourceLocation recipeId, BlockPos barrelPos, boolean fluidPlaced, boolean ingredientsPlaced) {
        return new BrewingSession(recipeId, barrelPos, new MutableBoolean(fluidPlaced), new MutableBoolean(ingredientsPlaced));
    }
}
