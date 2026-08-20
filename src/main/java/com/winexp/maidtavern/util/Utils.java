package com.winexp.maidtavern.util;

import com.winexp.maidtavern.mixin.RecipeManagerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Utils {
    public static @Nullable Container getContainer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Container container)) return null;
        else if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, level, pos, false);
        }
        else return container;
    }

    public static Optional<RecipeHolder<? extends Recipe<?>>> byKey(RecipeManager manager, ResourceLocation id) {
        return manager.byKey(id).map(recipe -> new RecipeHolder<>(id, recipe));
    }

    public static <T extends Recipe<C>, C extends Container> List<RecipeHolder<T>> getAllRecipesFor(RecipeManager manager, RecipeType<T> recipeType) {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ((RecipeManagerAccessor) manager).getRecipes();
        List<RecipeHolder<T>> results = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Recipe<?>> entry : recipes.get(recipeType).entrySet()) {
            results.add(new RecipeHolder<>(entry.getKey(), (T) entry.getValue()));
        }
        return results;
    }
}
