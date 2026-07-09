package com.winexp.maid.grape;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IGrapeTask extends IFarmTask {
    @Nullable BlockPos getGrapePos(Level level, BlockPos pos);

    @Override
    default boolean isSeed(ItemStack stack) {
        return false;
    }

    @Override
    default boolean canPlant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        return false;
    }

    @Override
    default ItemStack plant(EntityMaid maid, BlockPos basePos, BlockState baseState, ItemStack seed) {
        return seed;
    }

    @Override
    default boolean checkCropPosAbove() {
        return false;
    }
}
