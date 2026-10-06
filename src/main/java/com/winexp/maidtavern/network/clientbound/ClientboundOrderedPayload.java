package com.winexp.maidtavern.network.clientbound;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.client.gui.order_menu.OrderMenuScreen;
import com.winexp.maidtavern.logistics.waiter.Order;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOrderedPayload(Order order) implements CustomPacketPayload {
    public static final Type<ClientboundOrderedPayload> TYPE = new Type<>(MaidTavern.asResource("clientbound/ordered"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOrderedPayload> STREAM_CODEC = StreamCodec.composite(
            Order.STREAM_CODEC,
            ClientboundOrderedPayload::order,
            ClientboundOrderedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof OrderMenuScreen orderMenu) {
            orderMenu.setUuid(order.uuid());
        }
    }
}
