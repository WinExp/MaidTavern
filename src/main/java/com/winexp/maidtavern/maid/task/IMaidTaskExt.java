package com.winexp.maidtavern.maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.item.ItemEntity;

public interface IMaidTaskExt {
    default boolean enableStealEdible(EntityMaid maid) {
        return true;
    }

    default boolean shouldPickupGaveItem(EntityMaid maid, ItemEntity itemEntity) {
        return false;
    }
}
