package com.winexp.maidtavern.datagen;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelGeneration extends ItemModelProvider {
    public ItemModelGeneration(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MaidTavern.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MaidTavernItems.BREWING_LIST.get());
    }
}
