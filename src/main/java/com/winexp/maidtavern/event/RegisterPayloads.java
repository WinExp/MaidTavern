package com.winexp.maidtavern.event;

import com.winexp.maidtavern.network.ServerboundSetBrewingListPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class RegisterPayloads {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ServerboundSetBrewingListPayload.TYPE, ServerboundSetBrewingListPayload.STREAM_CODEC, ServerboundSetBrewingListPayload::handle);
    }
}
