package com.winexp.maidtavern.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BarrelBlock;
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
