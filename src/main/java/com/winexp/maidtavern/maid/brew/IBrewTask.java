package com.winexp.maidtavern.maid.brew;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBrewTask extends IMaidTask {
    @Nullable IBarrel getBarrel(Level level, BlockPos pos);

    @Contract("_, null -> false")
    boolean isBarrelValid(EntityMaid maid, @Nullable IBarrel barrel);

    boolean hasIngredients(EntityMaid maid, ResourceLocation recipeId);

    boolean shouldExtract(EntityMaid maid);

    List<Pair<ItemStack, Integer>> getStacksToExtract(EntityMaid maid, IItemHandler storage);

    List<ItemStack> getResultsToInsert(EntityMaid maid);

    List<ItemStack> getByproductsToInsert(EntityMaid maid);

    boolean isStorageValid(Level level, BlockPos pos);

    boolean isBottleValid(EntityMaid maid, BlockPos pos);

    boolean shouldPlaceBottle(EntityMaid maid, BlockPos pos);
}
