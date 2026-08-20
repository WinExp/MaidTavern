package com.winexp.maidtavern.network.serverbound;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class MaidTavernNetworking {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MaidTavern.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        CHANNEL.registerMessage(0, SetBrewingListC2SMessage.class,
                SetBrewingListC2SMessage::encode, SetBrewingListC2SMessage::decode,
                SetBrewingListC2SMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, SetStorageBindingTypeC2SMessage.class,
                SetStorageBindingTypeC2SMessage::encode, SetStorageBindingTypeC2SMessage::decode,
                SetStorageBindingTypeC2SMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToClient(Player player, Object packet) {
        if (player instanceof ServerPlayer serverPlayer) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
