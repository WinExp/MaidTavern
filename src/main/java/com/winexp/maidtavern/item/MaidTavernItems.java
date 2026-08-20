package com.winexp.maidtavern.item;

import com.google.common.collect.ImmutableSet;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import com.winexp.maidtavern.maid.brewing.StorageBinding;
import com.winexp.maidtavern.util.DataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class MaidTavernItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MaidTavern.MOD_ID);

    public static final DataComponent<BrewingList> BREWING_LIST_DATA = new DataComponent<>("brewing_list", BrewingList.DEFAULT, BrewingList.CODEC);

    public static final DataComponent<StorageBinding> STORAGE_BINDING_DATA = new DataComponent<>("storage_binding", StorageBinding.EMPTY, StorageBinding.CODEC);

    public static final DataComponent<StorageBinding.Type> STORAGE_BINDING_TYPE_DATA = new DataComponent<>("storage_binding_type", StorageBinding.Type.INGREDIENTS, StorageBinding.Type.CODEC);

    public static final DataComponent<ImmutableSet<BlockPos>> BARREL_POSITIONS_DATA = new DataComponent<>("barrel_positions", ImmutableSet.of(), BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, List::copyOf));

    public static final RegistryObject<BrewingListItem> BREWING_LIST = ITEMS
            .register("brewing_list", () ->
                    new BrewingListItem(new Item.Properties()
                            .stacksTo(1)));

    public static final RegistryObject<StorageBindingToolItem> STORAGE_BINDING_TOOL = ITEMS
            .register("storage_binding_tool", () ->
                    new StorageBindingToolItem(new Item.Properties()
                            .stacksTo(1)));

    public static final RegistryObject<BarrelSelectionToolItem> BARREL_SELECTION_TOOL = ITEMS
            .register("barrel_selection_tool", () ->
                    new BarrelSelectionToolItem(new Item.Properties()
                            .stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
