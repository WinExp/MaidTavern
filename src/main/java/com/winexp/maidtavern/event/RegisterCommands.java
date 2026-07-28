package com.winexp.maidtavern.event;

import com.winexp.maidtavern.command.MaidTavernCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RegisterCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        MaidTavernCommand.register(event.getDispatcher());
    }
}
