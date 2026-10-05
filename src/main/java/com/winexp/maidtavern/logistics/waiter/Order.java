package com.winexp.maidtavern.logistics.waiter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public record Order(UUID uuid, HashSet<ItemStack> items, HashSet<BlockPos> targetPos) {
    public static final Codec<Order> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(Order::uuid),
            ItemStack.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("items").forGetter(Order::items),
            BlockPos.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("target_pos").forGetter(Order::targetPos)
    ).apply(instance, Order::new));
    public static final Codec<Order> CODEC_WITHOUT_UUID = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("items").forGetter(Order::items),
            BlockPos.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("target_pos").forGetter(Order::targetPos)
    ).apply(instance, Order::new));

    public Order(HashSet<ItemStack> items, HashSet<BlockPos> targetPos) {
        this(Mth.createInsecureUUID(), items, targetPos);
    }
}
