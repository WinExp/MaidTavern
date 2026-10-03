package com.winexp.maidtavern.maid.behavior.waiter;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.resources.ResourceLocation;

public class WaiterWorkTypes {
    public static final ResourceLocation TASK = TaskWaiter.UID;

    public static final ResourceLocation
            RETRIEVING = of("retrieving"),
            DELIVERING = of("delivering");

    public static ResourceLocation of(String id) {
        return MaidTavern.asResource(id);
    }
}
