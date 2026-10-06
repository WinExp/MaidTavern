package com.winexp.maidtavern.network.clientbound;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.client.gui.order_menu.OrderMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ClientboundOrderedPayload(UUID uuid) implements CustomPacketPayload {
    public static final Type<ClientboundOrderedPayload> TYPE = new Type<>(MaidTavern.asResource("clientbound/ordered"));
    public static final StreamCodec<FriendlyByteBuf, ClientboundOrderedPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            ClientboundOrderedPayload::uuid,
            ClientboundOrderedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof OrderMenuScreen orderMenu) {
            orderMenu.setUuid(uuid);
        }
    }
}
