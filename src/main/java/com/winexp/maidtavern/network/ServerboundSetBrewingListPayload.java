package com.winexp.maidtavern.network;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetBrewingListPayload(InteractionHand hand, BrewingList brewingList) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundSetBrewingListPayload> TYPE = new Type<>(MaidTavern.asResource("serverbound/set_brewing_list"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetBrewingListPayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class),
            ServerboundSetBrewingListPayload::hand,
            BrewingList.STREAM_CODEC,
            ServerboundSetBrewingListPayload::brewingList,
            ServerboundSetBrewingListPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(MaidTavernItems.BREWING_LIST)) return;
        stack.set(MaidTavernItems.BREWING_LIST_DATA, brewingList);
    }
}
