package com.winexp.maidtavern.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MaidTavernDataGeneration {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeClient(),
                new ItemModelGeneration(output, existingFileHelper)
        );
        var blocks = generator.addProvider(
                event.includeServer(),
                new BlockTagsGeneration(output, registries, existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new ItemTagsGeneration(output, registries, blocks.contentsGetter(), existingFileHelper)
        );
        generator.addProvider(
                event.includeServer(),
                new ShapelessRecipeGeneration(output, registries)
        );
    }
}
