package com.winexp.maidtavern.entity;

import com.mojang.serialization.Codec;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brew.BrewingList;
import com.winexp.maidtavern.maid.brew.BrewingSession;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class MaidTavernEntities {
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, MaidTavern.MOD_ID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> MOLOTOV_DRUNK = MEMORY_MODULE_TYPES
            .register("molotov_drunk", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BrewingList>> BREWING_LIST = MEMORY_MODULE_TYPES
            .register("brewing_list", () -> new MemoryModuleType<>(Optional.of(BrewingList.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BrewingSession>> BREWING_SESSION = MEMORY_MODULE_TYPES
            .register("brewing_session", () -> new MemoryModuleType<>(Optional.of(BrewingSession.CODEC)));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<StorageBinding>> STORAGE_BINDING = MEMORY_MODULE_TYPES
            .register("storage_binding", () -> new MemoryModuleType<>(Optional.of(StorageBinding.CODEC)));

    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
