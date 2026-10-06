package com.winexp.maidtavern.logistics.waiter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
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
    public static final StreamCodec<RegistryFriendlyByteBuf, Order> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            Order::uuid,
            ItemStack.LIST_STREAM_CODEC.map(HashSet::new, List::copyOf),
            Order::items,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(HashSet::new, List::copyOf),
            Order::targetPos,
            Order::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Order> STREAM_CODEC_WITHOUT_UUID = StreamCodec.composite(
            ItemStack.LIST_STREAM_CODEC.map(HashSet::new, List::copyOf),
            Order::items,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(HashSet::new, List::copyOf),
            Order::targetPos,
            Order::new
    );

    public Order(UUID uuid, Collection<ItemStack> items, Collection<BlockPos> targetPos) {
        this(uuid, new HashSet<>(items), new HashSet<>(targetPos));
    }

    public Order(Collection<ItemStack> items, Collection<BlockPos> targetPos) {
        this(Mth.createInsecureUUID(), items, targetPos);
    }
}
