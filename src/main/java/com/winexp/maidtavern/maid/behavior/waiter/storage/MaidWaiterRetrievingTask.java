package com.winexp.maidtavern.maid.behavior.waiter.storage;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.behavior.waiter.Order;
import com.winexp.maidtavern.maid.behavior.waiter.WaiterWorkTypes;
import com.winexp.maidtavern.maid.work.MaidWorkManager;
import com.winexp.maidtavern.maid.work.Work;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import com.winexp.maidtavern.util.Utils;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.List;
import java.util.ListIterator;

public class MaidWaiterRetrievingTask extends Behavior<EntityMaid> {
    public MaidWaiterRetrievingTask() {
        super(ImmutableMap.of(
                MaidTavernEntities.CURRENT_WORK.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.WAITER_ORDERS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MaidWorkManager.isSameWorkType(maid, WaiterWorkTypes.RETRIEVING)) return false;
        Work work = MaidWorkManager.getWork(maid);
        BlockPos pos = work.pos();
        if (!MaidUtil.isStorageValid(level, pos)) return false;

        return work.isCloseEnough(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        Work work = MaidWorkManager.getWork(maid);
        BlockPos pos = work.pos();
        List<Order> orders = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).get();
        List<BlockPos> storageBinding = brain.getMemory(MaidTavernEntities.WAITER_STORAGE_BINDING.get()).orElse(null);
        if (storageBinding != null && !storageBinding.contains(pos)) return;
        Container container = Utils.getContainer(level, pos);
        if (container == null) return;
        IItemHandler containerInv = new InvWrapper(container);
        IItemHandler maidInv = maid.getAvailableInv(true);
        boolean success = false;
        ListIterator<Order> it = orders.listIterator();
        order:
        while (it.hasNext()) {
            Order order = it.next();
            if (order.stage() != Order.Stage.RETRIEVING) continue;
            for (ItemStack targetStack : order.items()) {
                if (!ItemHandlerUtil.matchesCount(containerInv, stack ->
                        ItemStack.isSameItemSameComponents(stack, targetStack), MinMaxBounds.Ints.atLeast(targetStack.getCount()))) {
                    continue order;
                }
            }
            if (!ItemHandlerUtil.canInsertAll(maidInv, order.items())) {
                continue;
            }
            for (ItemStack targetStack : order.items()) {
                ItemHandlerHelper.insertItemStacked(maidInv, targetStack.copy(), false);
                List<ItemStack> invStacks = ItemHandlerUtil.findStacks(containerInv, stack1 ->
                        ItemStack.isSameItemSameComponents(stack1, targetStack));
                int count = targetStack.getCount();
                for (ItemStack invStack : invStacks) {
                    int shrinkCount = Math.min(count, invStack.getCount());
                    invStack.shrink(shrinkCount);
                    count -= shrinkCount;
                    if (count <= 0) break;
                }
            }
            it.set(order.withStage(Order.Stage.DELIVERING));
            success = true;
        }
        if (success) {
            MaidWorkManager.stopWork(maid);
            maid.swing(InteractionHand.MAIN_HAND);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, maid.getSoundSource(), 1, 1);
        }
    }
}
