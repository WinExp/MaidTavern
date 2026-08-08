package com.winexp.maidtavern.network.serverbound;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetStorageBindingTypePayload(StorageBinding.Type bindingType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundSetStorageBindingTypePayload> TYPE = new CustomPacketPayload.Type<>(MaidTavern.asResource("serverbound/set_storage_binding_type"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetStorageBindingTypePayload> STREAM_CODEC = StreamCodec.composite(
            StorageBinding.Type.STREAM_CODEC,
            ServerboundSetStorageBindingTypePayload::bindingType,
            ServerboundSetStorageBindingTypePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(MaidTavernItems.STORAGE_BINDING_TOOL)) return;
        stack.set(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, bindingType);
    }
}
