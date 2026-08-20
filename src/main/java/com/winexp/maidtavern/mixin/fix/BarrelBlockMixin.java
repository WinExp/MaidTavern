package com.winexp.maidtavern.mixin.fix;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BarrelBlock.class)
public abstract class BarrelBlockMixin extends Block {
    private BarrelBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pathComputationType) {
        return false;
    }
}
