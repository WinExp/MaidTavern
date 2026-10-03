package com.winexp.maidtavern.maid.behavior.brewing;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.winexp.maidtavern.entity.MaidTavernEntities;

public class BrewingUtils {
    public static int getRotationCounter(EntityMaid maid) {
        return maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST_ROTATION_COUNTER.get()).orElse(0);
    }

    public static void addRotationCounter(EntityMaid maid) {
        int count = getRotationCounter(maid) + 1;
        maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST_ROTATION_COUNTER.get(), count % 16384);
    }
}
