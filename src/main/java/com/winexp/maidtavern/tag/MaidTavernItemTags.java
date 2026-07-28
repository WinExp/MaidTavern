package com.winexp.maidtavern.tag;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MaidTavernItemTags {
    public static final TagKey<Item> BREWING_BYPRODUCTS = item("brewing_byproducts");

    public static TagKey<Item> item(String id) {
        return TagKey.create(Registries.ITEM, MaidTavern.asResource(id));
    }
}
