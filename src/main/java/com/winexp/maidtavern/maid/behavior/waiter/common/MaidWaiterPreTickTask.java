package com.winexp.maidtavern.maid.behavior.waiter.common;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.behavior.waiter.Order;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.Iterator;
import java.util.List;

public class MaidWaiterPreTickTask extends Behavior<EntityMaid> {
    public MaidWaiterPreTickTask() {
        super(ImmutableMap.of());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        List<Order> orders = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).orElse(null);
        if (orders != null) {
            Iterator<Order> it = orders.iterator();
            while (it.hasNext()) {
                Order order = it.next();
                if (order.items().isEmpty()) {
                    it.remove();
                    continue;
                }
                if (order.targetPos().isEmpty()) {
                    it.remove();
                    continue;
                }
                for (BlockPos pos : order.targetPos()) {
                    if (!maid.isWithinRestriction(pos)) {
                        it.remove();
                        break;
                    }
                }
            }
            if (orders.isEmpty()) {
                brain.eraseMemory(MaidTavernEntities.WAITER_ORDERS.get());
            }
        }

        List<BlockPos> storageBinding = brain.getMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get()).orElse(null);
        if (storageBinding != null && storageBinding.isEmpty()) {
            brain.eraseMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get());
        }
    }
}
