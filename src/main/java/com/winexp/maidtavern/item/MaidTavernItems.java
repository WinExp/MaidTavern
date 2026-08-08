package com.winexp.maidtavern.item;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MaidTavernItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaidTavern.MOD_ID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MaidTavern.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BrewingList>> BREWING_LIST_DATA = DATA_COMPONENTS
            .registerComponentType("brewing_list", builder -> builder
                    .persistent(BrewingList.CODEC)
                    .networkSynchronized(BrewingList.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageBinding>> STORAGE_BINDING_DATA = DATA_COMPONENTS
            .registerComponentType("storage_binding", builder -> builder
                    .persistent(StorageBinding.CODEC)
                    .networkSynchronized(StorageBinding.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageBinding.Type>> STORAGE_BINDING_TYPE_DATA = DATA_COMPONENTS
            .registerComponentType("storage_binding_type", builder -> builder
                    .persistent(StorageBinding.Type.CODEC)
                    .networkSynchronized(StorageBinding.Type.STREAM_CODEC));

    public static final DeferredItem<BrewingListItem> BREWING_LIST = ITEMS
            .register("brewing_list", () ->
                    new BrewingListItem(new Item.Properties()
                            .component(BREWING_LIST_DATA, BrewingList.DEFAULT)
                            .stacksTo(1)));

    public static final DeferredItem<StorageBindingToolItem> STORAGE_BINDING_TOOL = ITEMS
            .register("storage_binding_tool", () ->
                    new StorageBindingToolItem(new Item.Properties()
                            .component(STORAGE_BINDING_DATA, StorageBinding.EMPTY)
                            .component(STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS)
                            .stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
    }
}
