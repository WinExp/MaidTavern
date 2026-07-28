package com.winexp.maidtavern.item;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MaidTavernItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MaidTavern.MOD_ID);

    public static final RegistryObject<BrewingListItem> BREWING_LIST = ITEMS
            .register("brewing_list", () ->
                    new BrewingListItem(new Item.Properties()
                            .stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
