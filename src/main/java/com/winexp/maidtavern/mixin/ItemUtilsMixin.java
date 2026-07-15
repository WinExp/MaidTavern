package com.winexp.maidtavern.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ItemUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.winexp.maidtavern.maid.task.IMaidTaskExt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemUtils.class)
public class ItemUtilsMixin {
    @Inject(method = "getItemToLivingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V", shift = At.Shift.AFTER))
    private static void pickupItem(LivingEntity entity, ItemStack stack, int preferredSlot, CallbackInfo ci, @Local ItemEntity dropItem) {
        if (entity instanceof EntityMaid maid && maid.getTask() instanceof IMaidTaskExt ext) {
            if (ext.shouldPickupGaveItem(maid, dropItem)) {
                maid.pickupItem(dropItem, false);
            }
        }
    }
}
