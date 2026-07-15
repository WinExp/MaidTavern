package com.winexp.maidtavern.maid.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.item.ItemEntity;

public interface IMaidTaskExt {
    boolean enableStealEdible(EntityMaid maid);

    boolean shouldPickupGaveItem(EntityMaid maid, ItemEntity itemEntity);
}
