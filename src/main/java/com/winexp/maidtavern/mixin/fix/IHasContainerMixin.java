package com.winexp.maidtavern.mixin.fix;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.item.IHasContainer;
import com.llamalad7.mixinextras.sugar.Local;
import com.winexp.maidtavern.maid.task.IMaidTaskExt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IHasContainer.class)
public interface IHasContainerMixin {
    @Inject(method = "returnContainerToEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER))
    private static void pickupItem(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir, @Local ItemEntity itemEntity) {
        if (entity instanceof EntityMaid maid && maid.getTask() instanceof IMaidTaskExt ext) {
            if (ext.shouldPickupGaveItem(maid, itemEntity)) {
                maid.pickupItem(itemEntity, false);
            }
        }
    }
}
