package com.winexp.maidtavern.network.clientbound;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundUnorderedPayload(Order order, WaiterOrderManager.UnorderReason reason) implements CustomPacketPayload {
    public static final Type<ClientboundUnorderedPayload> TYPE = new Type<>(MaidTavern.asResource("clientbound/unordered"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUnorderedPayload> STREAM_CODEC = StreamCodec.composite(
            Order.STREAM_CODEC,
            ClientboundUnorderedPayload::order,
            WaiterOrderManager.UnorderReason.STREAM_CODEC,
            ClientboundUnorderedPayload::reason,
            ClientboundUnorderedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Component title = reason == WaiterOrderManager.UnorderReason.DONE ? Component.literal("订单已完成") : Component.literal("订单已取消");
        MutableComponent message = Component.literal(order.uuid().toString());
        if (reason != WaiterOrderManager.UnorderReason.DONE) {
            message.append("\n");
            message.append("原因：" + reason.toString());
        }
        Minecraft.getInstance().getToasts().addToast(SystemToast.multiline(Minecraft.getInstance(), new SystemToast.SystemToastId(1000), title, message));
    }
}
