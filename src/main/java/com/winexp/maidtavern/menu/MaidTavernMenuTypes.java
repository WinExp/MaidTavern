package com.winexp.maidtavern.menu;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MaidTavernMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MaidTavern.MOD_ID);

    public static final RegistryObject<MenuType<BrewingListMenu>> BREWING_LIST = MENU_TYPES.register("brewing_list", () ->
            IForgeMenuType.create(BrewingListMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
