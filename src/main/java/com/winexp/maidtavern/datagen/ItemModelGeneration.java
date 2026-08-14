package com.winexp.maidtavern.datagen;

import com.winexp.maidtavern.MaidTavern;
import com.winexp.maidtavern.item.MaidTavernItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModelGeneration extends ItemModelProvider {
    public ItemModelGeneration(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MaidTavern.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(MaidTavernItems.BREWING_LIST.get());
        basicItem(MaidTavernItems.STORAGE_BINDING_TOOL.get());

        ResourceLocation id = MaidTavernItems.BARREL_SELECTION_TOOL.getId();
        this.getBuilder(id.withPrefix("item/").withSuffix("/closed").toString()).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", id.withPrefix("item/").withSuffix("/closed"));
        id = id.withPrefix("item/");
        basicItem(MaidTavernItems.BARREL_SELECTION_TOOL.get(), "/opened").override()
                .model(getExistingFile(id.withSuffix("/closed")))
                .predicate(ResourceLocation.withDefaultNamespace("custom_model_data"), 100);
    }

    public ItemModelBuilder basicItem(Item item, String suffix) {
        return this.basicItem(BuiltInRegistries.ITEM.getKey(item), suffix);
    }

    public ItemModelBuilder basicItem(ResourceLocation item, String suffix) {
        return this.getBuilder(item.toString()).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath()).withSuffix(suffix));
    }
}
