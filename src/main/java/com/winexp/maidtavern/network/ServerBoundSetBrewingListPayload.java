package com.winexp.maidtavern.network;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.menu.BrewingListMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundSetBrewingListPayload(int containerId, BrewingList brewingList) implements CustomPacketPayload {
    public static final Type<ServerBoundSetBrewingListPayload> TYPE = new Type<>(MaidTavern.asResource("set_brewing_list"));
    public static final StreamCodec<ByteBuf, ServerBoundSetBrewingListPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ServerBoundSetBrewingListPayload::containerId,
            BrewingList.STREAM_CODEC,
            ServerBoundSetBrewingListPayload::brewingList,
            ServerBoundSetBrewingListPayload::new
    );

    @Override
    public Type<ServerBoundSetBrewingListPayload> type() {
        return TYPE;
    }

    public static void handle(ServerBoundSetBrewingListPayload payload, IPayloadContext context) {
        Player player = context.player();
        int containerId = payload.containerId();
        BrewingList brewingList = payload.brewingList;
        if (!player.hasContainerOpen() || containerId != player.containerMenu.containerId) return;
        if (player.containerMenu instanceof BrewingListMenu menu) {
            menu.brewingList = brewingList;
            menu.updateSlots();
        }
    }
}
