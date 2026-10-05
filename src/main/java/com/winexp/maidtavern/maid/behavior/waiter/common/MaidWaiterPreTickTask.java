package com.winexp.maidtavern.maid.behavior.waiter.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import com.winexp.maidtavern.maid.behavior.waiter.OrderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MaidWaiterPreTickTask extends Behavior<EntityMaid> {
    public MaidWaiterPreTickTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        WaiterOrderManager manager = WaiterOrderManager.get(level);
        HashMap<UUID, OrderState> ordersMap = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).orElse(null);
        if (ordersMap != null && !ordersMap.isEmpty()) {
            for (UUID uuid : ordersMap.keySet()) {
                if (!manager.isOrdered(uuid)) {
                    ordersMap.remove(uuid);
                    continue;
                }
                if (manager.isClaimed(uuid)) {
                    if (manager.getClaimer(uuid) != maid) {
                        ordersMap.remove(uuid);
                    }
                } else {
                    manager.claim(maid, uuid);
                }
            }
        }
        for (Order order : manager.getClaimedOrders(maid)) {
            if (ordersMap == null || !ordersMap.containsKey(order.uuid())) {
                if (ordersMap == null) {
                    ordersMap = new HashMap<>();
                    brain.setMemory(MaidTavernEntities.WAITER_ORDERS.get(), ordersMap);
                }
                ordersMap.put(order.uuid(), OrderState.RETRIEVING);
            }
        }

        if (ordersMap != null && ordersMap.isEmpty()) {
            brain.eraseMemory(MaidTavernEntities.WAITER_ORDERS.get());
        }

        List<BlockPos> storageBinding = brain.getMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get()).orElse(null);
        if (storageBinding != null && storageBinding.isEmpty()) {
            brain.eraseMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get());
        }
    }
}
