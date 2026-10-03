package com.winexp.maidtavern.maid.work;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record Work(ResourceLocation task, ResourceLocation type, BlockPos pos, float movementSpeed, double closeEnoughDist) {
    public static final Codec<Work> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("task").forGetter(Work::task),
            ResourceLocation.CODEC.fieldOf("type").forGetter(Work::type),
            BlockPos.CODEC.fieldOf("pos").forGetter(Work::pos),
            Codec.FLOAT.fieldOf("movement_speed").forGetter(Work::movementSpeed),
            Codec.DOUBLE.fieldOf("close_enough_dist").forGetter(Work::closeEnoughDist)
    ).apply(instance, Work::new));

    public boolean isCloseEnough(EntityMaid maid) {
        return maid.distanceToSqr(pos.getCenter()) <= Math.pow(closeEnoughDist, 2);
    }
}
