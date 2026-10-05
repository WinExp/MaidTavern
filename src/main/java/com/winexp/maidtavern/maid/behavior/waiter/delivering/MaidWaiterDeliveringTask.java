package com.winexp.maidtavern.maid.behavior.waiter.delivering;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import com.winexp.maidtavern.maid.behavior.waiter.OrderState;
import com.winexp.maidtavern.maid.behavior.waiter.WaiterWorkTypes;
import com.winexp.maidtavern.logistics.work.MaidWorkHelper;
import com.winexp.maidtavern.logistics.work.Work;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import com.winexp.maidtavern.util.MaidUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.*;

public class MaidWaiterDeliveringTask extends Behavior<EntityMaid> {
    private static final UUID FAKE_PLAYER_UUID = Mth.createInsecureUUID();

    public MaidWaiterDeliveringTask() {
        super(ImmutableMap.of(
                MaidTavernEntities.CURRENT_WORK.get(), MemoryStatus.VALUE_PRESENT,
                MaidTavernEntities.WAITER_ORDERS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!MaidWorkHelper.isSameWorkType(maid, WaiterWorkTypes.DELIVERING)) return false;
        Work work = MaidWorkHelper.getWork(maid);

        return work.isCloseEnough(maid);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        Brain<EntityMaid> brain = maid.getBrain();
        Work work = MaidWorkHelper.getWork(maid);
        WaiterOrderManager manager = WaiterOrderManager.get(level);
        Map<UUID, OrderState> ordersMap = brain.getMemory(MaidTavernEntities.WAITER_ORDERS.get()).get();
        IItemHandler maidInv = maid.getAvailableInv(true);
        boolean extracted = false;
        boolean placed = false;
        order:
        for (Order order : manager.getClaimedOrders(maid)) {
            if (ordersMap.get(order.uuid()) != OrderState.DELIVERING) continue;
            if (!order.targetPos().contains(work.pos())) {
                continue;
            }
            order.items().removeIf(ItemStack::isEmpty);
            for (ItemStack targetStack : order.items()) {
                if (!ItemHandlerUtil.matchesCount(maidInv, stack ->
                        ItemStack.isSameItemSameComponents(stack, targetStack), MinMaxBounds.Ints.atLeast(targetStack.getCount()))) {
                    continue order;
                }
            }
            List<BlockPos> sortedPos = order.targetPos().stream().sorted(Comparator.comparingDouble(blockPos ->
                    maid.distanceToSqr(blockPos.getCenter()))).toList();
            for (BlockPos pos : sortedPos) {
                if (maid.distanceToSqr(pos.getCenter()) > Math.pow(work.closeEnoughDist(), 2)) {
                    MaidWorkHelper.stopWork(maid);
                    if (!order.items().isEmpty()) {
                        BehaviorUtils.setWalkAndLookTargetMemories(maid, pos, work.movementSpeed(), 0);
                        MaidWorkHelper.startWork(maid, new Work(WaiterWorkTypes.TASK, WaiterWorkTypes.DELIVERING, pos, work.movementSpeed(), work.closeEnoughDist()));
                    }
                    return;
                }
                if (MaidUtil.isStorageValid(level, pos)) {
                    Container container = (Container) level.getBlockEntity(pos);
                    IItemHandler containerInv = new InvWrapper(container);
                    if (ItemHandlerUtil.canInsertAny(containerInv, order.items())) {
                        for (ItemStack targetStack : List.copyOf(order.items())) {
                            ItemStack stackCopy = targetStack.copy();
                            order.items().remove(targetStack);
                            targetStack = ItemHandlerHelper.insertItemStacked(containerInv, targetStack, false);
                            shrinkItems(maidInv, stackCopy, stackCopy.getCount() - targetStack.getCount());
                            if (!targetStack.isEmpty()) {
                                order.items().add(targetStack);
                            }
                        }
                        extracted = true;
                    }
                }
                if (!extracted) {
                    placed = tryPlaceBlock(order, level, pos, maidInv);
                }
                order.targetPos().remove(pos);
                boolean isAllEmpty = true;
                for (ItemStack targetStack : order.items()) {
                    if (!targetStack.isEmpty()) {
                        isAllEmpty = false;
                        break;
                    }
                }
                if (isAllEmpty) {
                    break;
                }
            }
            for (ItemStack targetStack : order.items()) {
                if (!targetStack.isEmpty()) {
                    shrinkItems(maidInv, targetStack, targetStack.getCount());
                    ItemEntity itemEntity = new ItemEntity(level, maid.getX(), maid.getY(), maid.getZ(), targetStack);
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
            manager.unorder(order.uuid());
            MaidWorkHelper.stopWork(maid);
            if (extracted) {
                maid.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1, 1);
            }
            if (extracted || placed) {
                maid.swing(InteractionHand.MAIN_HAND);
            }
            break;
        }
    }

    private boolean tryPlaceBlock(Order order, ServerLevel level, BlockPos pos, IItemHandler maidInv) {
        if (level.getBlockState(pos.below()).canBeReplaced()) return false;
        boolean success = false;
        BlockState state = level.getBlockState(pos);
        Iterator<ItemStack> it = order.items().iterator();
        while (it.hasNext()) {
            ItemStack targetStack = it.next();
            ItemStack stackCopy = targetStack.copy();
            int consumes = 0;
            if (targetStack.getItem() instanceof BottleBlockItem bottleItem) {
                BlockHitResult hitResult;
                boolean isDrink = state.getBlock() instanceof DrinkBlock && targetStack.getItem() instanceof DrinkBlockItem;
                if (isDrink) {
                    hitResult = BlockHitResult.miss(pos.getCenter(), Direction.UP, pos);
                } else {
                    hitResult = BlockHitResult.miss(pos.below().getCenter(), Direction.UP, pos.below());
                }
                FakePlayer player = new FakePlayer(level, new GameProfile(FAKE_PLAYER_UUID, "Arm"));
                player.setItemInHand(InteractionHand.MAIN_HAND, targetStack);
                int count = targetStack.getCount();
                for (int i = 0; i < count; i++) {
                    InteractionResult result;
                    if (isDrink)  {
                        DrinkBlockItem drink = (DrinkBlockItem) targetStack.getItem();
                        result = drink.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult)) == InteractionResult.SUCCESS
                                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                    } else {
                        result = bottleItem.place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, targetStack, hitResult));
                    }
                    if (result.consumesAction()) {
                        state = level.getBlockState(pos);
                        if (!isDrink && state.getBlock() instanceof DrinkBlock) {
                            hitResult = BlockHitResult.miss(pos.getCenter(), Direction.UP, pos);
                            isDrink = true;
                        }
                        consumes++;
                        success = true;
                    } else {
                        break;
                    }
                }
            }
            shrinkItems(maidInv, stackCopy, consumes);
            if (targetStack.isEmpty()) {
                it.remove();
            }
        }
        return success;
    }

    private void shrinkItems(IItemHandler inv, ItemStack stack, int count) {
        List<ItemStack> invStacks = ItemHandlerUtil.findStacks(inv, stack1 ->
                ItemStack.isSameItemSameComponents(stack, stack1));
        for (ItemStack invStack : invStacks) {
            int shrinkCount = Math.min(count, invStack.getCount());
            invStack.shrink(shrinkCount);
            count -= shrinkCount;
            if (count <= 0) break;
        }
    }
}
