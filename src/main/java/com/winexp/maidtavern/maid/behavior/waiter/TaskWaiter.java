package com.winexp.maidtavern.maid.behavior.waiter;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.maid.behavior.core.IMaidTaskExt;
import com.winexp.maidtavern.maid.behavior.waiter.common.MaidWaiterPreTickTask;
import com.winexp.maidtavern.maid.behavior.waiter.delivering.MaidWaiterDeliveringTask;
import com.winexp.maidtavern.maid.behavior.waiter.delivering.MaidWaiterMoveToTargetTask;
import com.winexp.maidtavern.maid.behavior.waiter.storage.MaidWaiterMoveToStorageTask;
import com.winexp.maidtavern.maid.behavior.waiter.storage.MaidWaiterRetrievingTask;
import com.winexp.maidtavern.maid.work.MaidWorkManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskWaiter implements IMaidTaskExt {
    public static final ResourceLocation UID = MaidTavern.asResource("waiter");
    public static final ItemStack ICON = ModItems.WINE.toStack();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ICON;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid maid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of(Integer.MIN_VALUE, new MaidWaiterPreTickTask()),
                Pair.of(5, new MaidWaiterMoveToStorageTask(0.45f, 4, 3, 20)),
                Pair.of(5, new MaidWaiterRetrievingTask()),
                Pair.of(5, new MaidWaiterMoveToTargetTask(0.45f, 3, 20)),
                Pair.of(5, new MaidWaiterDeliveringTask())
        );
    }

    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return !MaidWorkManager.isWorking(maid);
    }

    @Override
    public boolean enableEating(EntityMaid maid) {
        return !MaidWorkManager.isWorking(maid);
    }

    @Override
    public boolean enableDrinking(EntityMaid maid) {
        return false;
    }
}
