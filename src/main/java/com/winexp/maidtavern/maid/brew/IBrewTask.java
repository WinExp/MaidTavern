package com.winexp.maidtavern.maid.brew;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBrewTask extends IMaidTask {
    @Contract("_, null -> false")
    boolean isBarrelValid(EntityMaid maid, @Nullable IBarrel barrel);

    boolean hasIngredients(EntityMaid maid, ResourceLocation recipeId);

    boolean shouldExtract(EntityMaid maid);

    List<Pair<ItemStack, Integer>> getBottlesToExtract(IItemHandler inventory, IItemHandler storage);

    List<Pair<ItemStack, Integer>> getIngredientsToExtract(IItemHandler inventory, IItemHandler storage, RecipeManager manager, BrewingList.Entry entry);

    List<ItemStack> getResultsToInsert(EntityMaid maid);

    List<ItemStack> getByproductsToInsert(EntityMaid maid);

    boolean isBottleValid(EntityMaid maid, BlockPos pos);

    boolean shouldPlaceBottle(EntityMaid maid, BlockPos pos);
}
