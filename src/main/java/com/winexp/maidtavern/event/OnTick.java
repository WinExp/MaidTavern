package com.winexp.maidtavern.event;

import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class OnTick {
    @SubscribeEvent
    public static void onLevelPreTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        WaiterOrderManager.get(level).tick();
    }
}
