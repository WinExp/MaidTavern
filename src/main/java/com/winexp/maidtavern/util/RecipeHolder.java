package com.winexp.maidtavern.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
}
