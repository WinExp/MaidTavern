package com.winexp.maidtavern.maid.behavior.waiter.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.behavior.core.MaidSurroundingMoveTask;
import com.winexp.maidtavern.maid.behavior.waiter.Order;
import com.winexp.maidtavern.maid.behavior.waiter.WaiterWorkTypes;
import com.winexp.maidtavern.maid.work.MaidWorkManager;
import com.winexp.maidtavern.maid.work.Work;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.Utils;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.List;

public class MaidWaiterMoveToStorageTask extends MaidSurroundingMoveTask {
    private final float movementSpeed;
    private final double closeEnoughDist;

    public MaidWaiterMoveToStorageTask(float movementSpeed, int verticalSearchRange, double closeEnoughDist, int minCheckTime) {
        super(movementSpeed, verticalSearchRange);
        this.movementSpeed = movementSpeed;
        this.closeEnoughDist = closeEnoughDist;
        setMaxCheckRate(minCheckTime);
        moveRange = new BoundingBox(-1, -2, -1, 1, 1, 1);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        if (!super.checkExtraStartConditions(level, maid)) return false;
        return !MaidWorkManager.isWorking(maid) && brain.hasMemoryValue(MaidTavernEntities.WAITER_ORDERS.get());
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTimeIn) {
        searchForDestination(level, maid);
        maid.getBrain().getMemory(InitEntities.TARGET_POS.get()).map(PositionTracker::currentBlockPosition).ifPresent(pos ->
                MaidWorkManager.startWork(maid, new Work(WaiterWorkTypes.TASK, WaiterWorkTypes.RETRIEVING, pos, movementSpeed, closeEnoughDist)));
    }

    @Override
    protected boolean shouldMoveTo(ServerLevel level, EntityMaid maid, BlockPos pos) {
        Brain<EntityMaid> brain = maid.getBrain();
        List<Order> orders = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).get();
        List<BlockPos> storageBinding = brain.getMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get()).orElse(null);
        if (storageBinding != null && !storageBinding.contains(pos)) return false;
        Container container = Utils.getContainer(level, pos);
        if (container == null) return false;
        IItemHandler containerInv = new InvWrapper(container);
        IItemHandler maidInv = maid.getAvailableInv(true);
        order:
        for (Order order : orders) {
            if (order.stage() != Order.Stage.RETRIEVING) continue;
            for (ItemStack targetStack : order.items()) {
                if (!ItemHandlerUtil.matchesCount(containerInv, stack ->
                        ItemStack.isSameItemSameComponents(stack, targetStack), MinMaxBounds.Ints.atLeast(targetStack.getCount()))) {
                    continue order;
                }
            }
            if (!ItemHandlerUtil.canInsertAll(maidInv, List.copyOf(order.items()))) {
                continue;
            }
            return true;
        }
        return false;
    }
}
