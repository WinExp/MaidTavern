package com.winexp.maidtavern.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidStealEdibleUseTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.maid.task.IMaidTaskExt;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MaidStealEdibleUseTask.class, remap = false)
public class MaidStealEdibleUseTaskMixin {
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;)Z", at = @At("HEAD"), cancellable = true)
    private void checkConditions(ServerLevel level, EntityMaid maid, CallbackInfoReturnable<Boolean> cir) {
        if (maid.getTask() instanceof IMaidTaskExt ext) {
            if (!ext.enableStealEdible(maid)) {
                maid.getBrain().eraseMemory(InitEntities.MAID_EDIBLE_BLOCK_ACTION.get());
                cir.setReturnValue(false);
            }
        }
    }
}
