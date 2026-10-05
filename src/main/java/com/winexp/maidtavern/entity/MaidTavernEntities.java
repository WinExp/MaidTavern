package com.winexp.maidtavern.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.behavior.brewing.BrewingList;
import com.winexp.maidtavern.maid.behavior.brewing.BrewingSession;
import com.winexp.maidtavern.maid.behavior.brewing.StorageBinding;
import com.winexp.maidtavern.maid.behavior.waiter.OrderState;
import com.winexp.maidtavern.logistics.work.Work;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<HashMap<UUID, OrderState>>> WAITER_ORDERS =
            register("waiter_orders", Codec.unboundedMap(UUIDUtil.STRING_CODEC, OrderState.CODEC).xmap(HashMap::new, Map::copyOf));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<ImmutableList<BlockPos>>> WAITER_STORAGE_BINDING =
            register("waiter_storage_binding", BlockPos.CODEC.listOf().xmap(ImmutableList::copyOf, List::copyOf));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Work>> CURRENT_WORK =
            register("current_work", Work.CODEC);
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
