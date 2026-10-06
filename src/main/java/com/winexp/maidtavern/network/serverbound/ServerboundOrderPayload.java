package com.winexp.maidtavern.network.serverbound;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOrderPayload(Order order) implements CustomPacketPayload {
    public static final Type<ServerboundOrderPayload> TYPE = new Type<>(MaidTavern.asResource("serverbound/order"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOrderPayload> STREAM_CODEC = StreamCodec.composite(
            Order.STREAM_CODEC_WITHOUT_UUID,
            ServerboundOrderPayload::order,
            ServerboundOrderPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ServerLevel level = player.serverLevel();
        WaiterOrderManager.get(level).order(order, player.getUUID());
    }
}
