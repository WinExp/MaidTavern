package com.winexp.maidtavern.maid.behavior.waiter.delivering;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import com.winexp.maidtavern.maid.behavior.waiter.OrderState;
import com.winexp.maidtavern.maid.behavior.waiter.WaiterWorkTypes;
import com.winexp.maidtavern.logistics.work.MaidWorkHelper;
import com.winexp.maidtavern.logistics.work.Work;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

public class MaidWaiterMoveToTargetTask extends MaidCheckRateTask {
    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidWaiterMoveToTargetTask(float movementSpeed, double closeEnoughDist, int minCheckTime) {
        super(ImmutableMap.of());
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
        setMaxCheckRate(minCheckTime);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid)) return false;
        return !MaidWorkHelper.isWorking(maid) && brain.hasMemoryValue(MaidTavernEntities.WAITER_ORDERS.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        WaiterOrderManager manager = WaiterOrderManager.get(level);
        Map<UUID, OrderState> ordersMap = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).get();
        IItemHandler maidInv = maid.getAvailableInv(true);
        order:
        for (Order order : manager.getClaimedOrders(maid)) {
            if (ordersMap.get(order.uuid()) != OrderState.DELIVERING) continue;
            for (ItemStack targetStack : order.items()) {
                if (!ItemHandlerUtil.matchesCount(maidInv, stack ->
                        ItemStack.isSameItemSameComponents(stack, targetStack), MinMaxBounds.Ints.atLeast(targetStack.getCount()))) {
                    manager.unclaim(order.uuid());
                    continue order;
                }
            }
            BlockPos nearestPos = order.targetPos().stream().min(Comparator.comparingDouble(blockPos ->
                    maid.distanceToSqr(blockPos.getCenter()))).get();
            BehaviorUtils.setWalkAndLookTargetMemories(maid, nearestPos, movementSpeed, 0);
            MaidWorkHelper.startWork(maid, new Work(WaiterWorkTypes.TASK, WaiterWorkTypes.DELIVERING, nearestPos, movementSpeed, closeEnoughDist));
            break;
        }
    }
}
