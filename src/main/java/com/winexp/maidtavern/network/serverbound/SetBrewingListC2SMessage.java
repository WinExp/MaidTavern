package com.winexp.maidtavern.network.serverbound;

import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetBrewingListC2SMessage(InteractionHand hand, BrewingList brewingList) {
    public static void encode(SetBrewingListC2SMessage msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        BrewingList.encode(msg.brewingList, buf);
    }

    public static SetBrewingListC2SMessage decode(FriendlyByteBuf buf) {
        return new SetBrewingListC2SMessage(buf.readEnum(InteractionHand.class), BrewingList.decode(buf));
    }

    public static void handle(SetBrewingListC2SMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> onHandle(msg, context));
        }
        context.setPacketHandled(true);
    }

    private static void onHandle(SetBrewingListC2SMessage msg, NetworkEvent.Context context) {
        Player player = context.getSender();
        ItemStack stack = player.getItemInHand(msg.hand);
        if (!stack.is(MaidTavernItems.BREWING_LIST.get())) return;
        MaidTavernItems.BREWING_LIST_DATA.set(stack, msg.brewingList);
    }
}
