package com.winexp.maidtavern.network.serverbound;

import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brewing.StorageBinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetStorageBindingTypeC2SMessage(StorageBinding.Type bindingType) {
    public static void encode(SetStorageBindingTypeC2SMessage msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.bindingType);
    }

    public static SetStorageBindingTypeC2SMessage decode(FriendlyByteBuf buf) {
        return new SetStorageBindingTypeC2SMessage(buf.readEnum(StorageBinding.Type.class));
    }

    public static void handle(SetStorageBindingTypeC2SMessage msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> onHandle(msg, context));
        }
        context.setPacketHandled(true);
    }

    private static void onHandle(SetStorageBindingTypeC2SMessage msg, NetworkEvent.Context context) {
        Player player = context.getSender();
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(MaidTavernItems.STORAGE_BINDING_TOOL.get())) return;
        MaidTavernItems.STORAGE_BINDING_TYPE_DATA.set(stack, msg.bindingType);
    }
}
