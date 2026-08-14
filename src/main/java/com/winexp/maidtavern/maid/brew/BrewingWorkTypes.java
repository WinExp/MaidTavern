package com.winexp.maidtavern.maid.brew;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.resources.ResourceLocation;

public class BrewingWorkTypes {
    public static final ResourceLocation
            ADD_INGREDIENTS = of("add_ingredients"),
            BOTTLE = of("bottle"),
            STORAGE = of("storage");

    public static ResourceLocation of(String id) {
        return MaidTavern.asResource(id);
    }
}
