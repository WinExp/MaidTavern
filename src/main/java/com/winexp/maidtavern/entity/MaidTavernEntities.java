package com.winexp.maidtavern.entity;

import com.mojang.serialization.Codec;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class MaidTavernEntities {
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, MaidTavern.MOD_ID);

    public static final RegistryObject<MemoryModuleType<Integer>> MOLOTOV_DRUNK = MEMORY_MODULE_TYPES
            .register("molotov_drunk", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final RegistryObject<MemoryModuleType<BrewingList>> BREWING_LIST = MEMORY_MODULE_TYPES
            .register("brewing_list", () -> new MemoryModuleType<>(Optional.of(BrewingList.CODEC)));
    public static final RegistryObject<MemoryModuleType<BrewingSession>> BREWING_SESSION = MEMORY_MODULE_TYPES
            .register("brewing_session", () -> new MemoryModuleType<>(Optional.of(BrewingSession.CODEC)));

    public static final RegistryObject<MemoryModuleType<StorageBinding>> STORAGE_BINDING = MEMORY_MODULE_TYPES
            .register("storage_binding", () -> new MemoryModuleType<>(Optional.of(StorageBinding.CODEC)));

    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
