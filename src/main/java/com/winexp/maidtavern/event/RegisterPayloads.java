package com.winexp.maidtavern.event;

import com.winexp.maidtavern.network.clientbound.ClientboundOrderedPayload;
import com.winexp.maidtavern.network.serverbound.ServerboundOrderPayload;
import com.winexp.maidtavern.network.serverbound.ServerboundSetBrewingListPayload;
import com.winexp.maidtavern.network.serverbound.ServerboundSetStorageBindingTypePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class RegisterPayloads {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("3");
        registrar.playToServer(ServerboundSetBrewingListPayload.TYPE, ServerboundSetBrewingListPayload.STREAM_CODEC, ServerboundSetBrewingListPayload::handle);
        registrar.playToServer(ServerboundSetStorageBindingTypePayload.TYPE, ServerboundSetStorageBindingTypePayload.STREAM_CODEC, ServerboundSetStorageBindingTypePayload::handle);
        registrar.playToServer(ServerboundOrderPayload.TYPE, ServerboundOrderPayload.STREAM_CODEC, ServerboundOrderPayload::handle);

        registrar.playToClient(ClientboundOrderedPayload.TYPE, ClientboundOrderedPayload.STREAM_CODEC, ClientboundOrderedPayload::handle);
    }
}
