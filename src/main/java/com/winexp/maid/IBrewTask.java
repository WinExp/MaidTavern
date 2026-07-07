package com.winexp.maid;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.winexp.maid.brew.BrewingSession;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface IBrewTask extends IMaidTask {
    double getCloseEnoughDist();

    @Nullable IBarrel getBarrel(ServerLevel level, BlockPos pos);

    @Contract("_, null -> false")
    boolean isBarrelAvailable(EntityMaid maid, @Nullable IBarrel barrel);

    boolean hasRequiredMaterials(EntityMaid maid, ResourceLocation recipeId, @Nullable BrewingSession session);

    boolean hasRequiredMaterialsInStorage(EntityMaid maid, ResourceLocation recipeId, IItemHandler itemHandler);

    @Nullable ItemStack getToStoreStack(EntityMaid maid);

    boolean isStorageValid(EntityMaid maid, BlockPos pos);

    boolean isBottleValid(EntityMaid maid, BlockPos pos);
}
