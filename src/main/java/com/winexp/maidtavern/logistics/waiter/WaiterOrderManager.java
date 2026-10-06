package com.winexp.maidtavern.logistics.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.winexp.maidtavern.config.MaidTavernConfig;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.behavior.waiter.OrderState;
import com.winexp.maidtavern.maid.behavior.waiter.TaskWaiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaiterOrderManager extends SavedData {
    public static final String NAME = "waiter_order_manager";

    private final ServerLevel level;
    private final Map<UUID, Order> orders = new HashMap<>();
    private final Map<UUID, Integer> aliveTimeMap = new HashMap<>();
    private final Map<UUID, EntityMaid> claimerMap = new HashMap<>();
    private final Multimap<EntityMaid, UUID> claimedMap = LinkedHashMultimap.create();

    public static Factory<WaiterOrderManager> factory(ServerLevel level) {
        return new Factory<>(() -> new WaiterOrderManager(level), (tag, provider) -> load(level, tag));
    }

    public static WaiterOrderManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(level), NAME);
    }

    private WaiterOrderManager(ServerLevel level) {
        this.level = level;
    }

    public void tick() {
        for (Order order : List.copyOf(orders.values())) {
            if (order.items().isEmpty()) {
                unorder(order.uuid());
                continue;
            } else if (order.targetPos().isEmpty()) {
                unorder(order.uuid());
                continue;
            }
            if (!isClaimed(order.uuid())) {
                int aliveTime = aliveTimeMap.put(order.uuid(), aliveTimeMap.get(order.uuid()) - 1);
                if (aliveTime <= 0) {
                    unorder(order.uuid());
                }
            }
        }
        for (EntityMaid claimer : List.copyOf(claimedMap.keySet())) {
            if (!(claimer.getTask() instanceof TaskWaiter)) {
                for (UUID order : List.copyOf(claimedMap.get(claimer))) {
                    unclaim(order);
                }
                continue;
            }
            orders:
            for (UUID uuid : List.copyOf(claimedMap.get(claimer))) {
                Order order = getOrder(uuid);
                for (BlockPos pos : order.targetPos()) {
                    if (!claimer.isWithinRestriction(pos)) {
                        unclaim(uuid);
                        continue orders;
                    }
                }
            }
        }
    }

    public ServerLevel getLevel() {
        return level;
    }

    public boolean isOrdered(UUID uuid) {
        return orders.containsKey(uuid);
    }

    public @Nullable Order getOrder(UUID uuid) {
        return orders.get(uuid);
    }

    public List<Order> getOrders() {
        return List.copyOf(orders.values());
    }

    public void order(Order order) {
        if (isOrdered(order.uuid())) return;
        orders.put(order.uuid(), order);
        aliveTimeMap.put(order.uuid(), MaidTavernConfig.CONFIG.orderAliveTime.getAsInt());
        setDirty();
    }

    public void unorder(UUID order) {
        if (!isOrdered(order)) return;
        unclaim(order);
        orders.remove(order);
        aliveTimeMap.remove(order);
        setDirty();
    }

    public boolean isClaimed(UUID order) {
        if (!isOrdered(order)) throw new IllegalStateException();
        return claimerMap.containsKey(order);
    }

    public boolean hasClaims(EntityMaid claimer) {
        return claimedMap.containsKey(claimer);
    }

    public List<Order> getClaimedOrders(EntityMaid claimer) {
        return claimedMap.get(claimer).stream().map(orders::get).toList();
    }

    public @Nullable EntityMaid getClaimer(UUID order) {
        if (!isOrdered(order)) throw new IllegalStateException();
        return claimerMap.get(order);
    }

    public @Nullable Order tryClaim(EntityMaid maid) {
        if (!(maid.getTask() instanceof TaskWaiter)) throw new IllegalStateException();
        else if (orders.isEmpty()) return null;
        orders:
        for (Order order : orders.values()) {
            if (isClaimed(order.uuid())) continue;
            else if (!(maid.getTask() instanceof TaskWaiter)) continue;
            for (BlockPos pos : order.targetPos()) {
                if (!maid.isWithinRestriction(pos)) continue orders;
            }
            claim(maid, order.uuid());
            return order;
        }
        return null;
    }

    public void claim(EntityMaid claimer, UUID order) {
        if (!isOrdered(order)) throw new IllegalStateException();
        else if (isClaimed(order)) return;
        claimedMap.put(claimer, order);
        claimerMap.put(order, claimer);
        HashMap<UUID, OrderState> ordersMap = claimer.getBrain().getMemory(MaidTavernEntities.WAITER_ORDERS.get()).orElse(null);
        if (ordersMap == null) {
            ordersMap = new HashMap<>();
            claimer.getBrain().setMemory(MaidTavernEntities.WAITER_ORDERS.get(), ordersMap);
        }
        ordersMap.put(order, OrderState.RETRIEVING);
        setDirty();
    }

    public void unclaim(UUID order) {
        if (!isOrdered(order)) throw new IllegalStateException();
        else if (!isClaimed(order)) return;
        EntityMaid claimer = getClaimer(order);
        claimedMap.remove(claimer, order);
        claimerMap.remove(order);
        claimer.getBrain().getMemory(MaidTavernEntities.WAITER_ORDERS.get()).ifPresent(ordersMap -> ordersMap.remove(order));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag ordersTag = new ListTag();
        for (Order order : orders.values()) {
            Tag orderTag = Order.CODEC.encodeStart(NbtOps.INSTANCE, order).getOrThrow();
            ordersTag.add(orderTag);
        }
        tag.put("orders", ordersTag);
        return tag;
    }

    private static WaiterOrderManager load(ServerLevel level, CompoundTag tag) {
        WaiterOrderManager manager = new WaiterOrderManager(level);
        ListTag ordersTag = tag.getList("orders", Tag.TAG_COMPOUND);
        for (Tag orderTag : ordersTag) {
            Order.CODEC.parse(NbtOps.INSTANCE, orderTag).result().ifPresent(order -> {
                if (manager.isOrdered(order.uuid())) return;
                manager.order(order);
            });
        }
        return manager;
    }
}
