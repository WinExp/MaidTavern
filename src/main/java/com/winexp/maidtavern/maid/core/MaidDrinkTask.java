package com.winexp.maidtavern.maid.core;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.MolotovBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.util.ItemUtils;
import com.google.common.collect.ImmutableMap;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.util.ItemHandlerUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class MaidDrinkTask extends MaidCheckRateTask {
    private static final Predicate<ItemStack> DRINK_PREDICATE = stack -> {
        Item item = stack.getItem();
        return item instanceof DrinkBlockItem || item instanceof MolotovBlockItem;
    };

    public MaidDrinkTask() {
        super(ImmutableMap.of());
        setMaxCheckRate(600);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, EntityMaid maid) {
        if (!super.checkExtraStartConditions(level, maid)) return false;
        return ItemHandlerUtil.contains(maid.getAvailableInv(true), DRINK_PREDICATE);
    }

    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTime) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = maid.getItemInHand(hand);
            if (tryDrink(maid, stack, hand)) return;
        }
        for (ItemStack stack : ItemHandlerUtil.toStacks(maid.getAvailableInv(false))) {
            if (tryDrink(maid, stack, null)) return;
        }
    }

    private boolean tryDrink(EntityMaid maid, ItemStack stack, @Nullable InteractionHand hand) {
        int molotovDrunk = maid.getBrain().getMemory(MaidTavernEntities.MOLOTOV_DRUNK.get()).orElse(0);
        Item item = stack.getItem();
        boolean success = false;
        if (item instanceof DrinkBlockItem drink) {
            drink.finishUsingItem(stack, maid.level(), maid);
            maid.playSound(stack.getDrinkingSound(), 1.0f, 1.0f);
            maid.gameEvent(GameEvent.DRINK);
            success = true;
        } else if (item instanceof MolotovBlockItem && molotovDrunk < 3) {
            maid.setRemainingFireTicks(80);
            ItemUtils.getItemToLivingEntity(maid, ModItems.EMPTY_BOTTLE.get().getDefaultInstance());
            maid.getBrain().setMemory(MaidTavernEntities.MOLOTOV_DRUNK.get(), molotovDrunk + maid.getRandom().nextInt(2) + 1);
            maid.playSound(SoundEvents.FIRECHARGE_USE, 1.0f, 1.0f);
            success = true;
        }
        if (success) {
            if (hand != null) {
                maid.swing(hand);
            }
            stack.shrink(1);
        }
        return success;
    }
}
