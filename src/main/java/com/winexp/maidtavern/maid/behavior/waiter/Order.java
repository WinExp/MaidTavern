package com.winexp.maidtavern.maid.behavior.waiter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;

public record Order(HashSet<ItemStack> items, HashSet<BlockPos> targetPos, Stage stage) {
    public static final Codec<Order> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("items").forGetter(Order::items),
            BlockPos.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("target_pos").forGetter(Order::targetPos),
            Stage.CODEC.fieldOf("stage").forGetter(Order::stage)
    ).apply(instance, Order::new));

    public Order withStage(Stage stage) {
        return new Order(items, targetPos, stage);
    }

    public enum Stage implements StringRepresentable {
        RETRIEVING,
        DELIVERING;

        public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
