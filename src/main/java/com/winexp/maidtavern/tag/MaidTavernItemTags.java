package com.winexp.maidtavern.tag;

import com.winexp.maidtavern.MaidTavern;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MaidTavernItemTags {
    public static final TagKey<Item> MAID_STORE_WHEN_BREWING = item("maid_store_when_brewing");

    public static TagKey<Item> item(String id) {
        return TagKey.create(Registries.ITEM, MaidTavern.asResource(id));
    }
}
