package com.winexp.maidtavern.event;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.BrewingList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class OnInteractMaid {
    @SubscribeEvent
    public static void onInteractMaid(InteractMaidEvent event) {
        ItemStack stack = event.getStack();
        if (stack.has(MaidTavernItems.BREWING_LIST_DATA)) {
            BrewingList brewingList = stack.get(MaidTavernItems.BREWING_LIST_DATA);
            event.getMaid().getBrain().setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);
            event.setCanceled(true);
        }
    }
}
