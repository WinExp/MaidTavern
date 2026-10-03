package com.winexp.maidtavern.mixin.fix;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BottleBlock.class)
public abstract class BottleBlockMixin extends Block {
    public BottleBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
