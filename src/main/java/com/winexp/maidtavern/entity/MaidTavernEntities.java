package com.winexp.maidtavern.entity;

import com.mojang.serialization.Codec;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import com.winexp.maidtavern.maid.brewing.BrewingSession;
import com.winexp.maidtavern.maid.brewing.BrewingWork;
import com.winexp.maidtavern.maid.brewing.StorageBinding;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MaidTavernEntities {
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, MaidTavern.MOD_ID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> MOLOTOV_DRUNK =
            register("molotov_drunk", Codec.INT);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BrewingList>> BREWING_LIST =
            register("brewing_list", BrewingList.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BrewingSession>> BREWING_SESSION =
            register("brewing_session", BrewingSession.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> BREWING_LIST_ROTATION_COUNTER =
            register("brewing_list_rotation_counter", Codec.INT);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<StorageBinding>> STORAGE_BINDING =
            register("storage_binding", StorageBinding.CODEC);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BrewingWork>> BREWING_WORK =
            register("brewing_work", BrewingWork.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> PATH_FINDING_ATTEMPT =
            register("path_finding_attempt", Codec.INT);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> WORK_EXPIRATION_TIME =
            register("work_expiration_time", Codec.INT);

    private static <T> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<T>> register(String name, @Nullable Codec<T> codec) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.ofNullable(codec)));
    }

    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
