package com.winexp.maidtavern.maid.brew;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record BrewingWork(ResourceLocation type, BlockPos pos, float movementSpeed, double closeEnoughDist) {
    public static final Codec<BrewingWork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(BrewingWork::type),
            BlockPos.CODEC.fieldOf("pos").forGetter(BrewingWork::pos),
            Codec.FLOAT.fieldOf("movement_speed").forGetter(BrewingWork::movementSpeed),
            Codec.DOUBLE.fieldOf("close_enough_dist").forGetter(BrewingWork::closeEnoughDist)
    ).apply(instance, BrewingWork::new));

    public boolean isCloseEnough(EntityMaid maid) {
        return maid.distanceToSqr(pos.getCenter()) <= Math.pow(closeEnoughDist, 2);
    }
}
