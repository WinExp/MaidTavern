package com.winexp.maidtavern.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.BarrelBlockEntity;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BarrelBlockEntity.class, remap = false)
public interface BarrelBlockEntityAccessor {
    @Accessor("ingredient")
    ItemStackHandler getIngredients();

    @Accessor("fluid")
    FluidTank getFluidTank();
}
