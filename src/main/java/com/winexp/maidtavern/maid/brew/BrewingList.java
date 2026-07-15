package com.winexp.maidtavern.maid.brew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BrewingList {
    public static final Codec<BrewingList> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("recipes").forGetter(BrewingList::getRecipes)
    ).apply(instance, BrewingList::new));
    public static final StreamCodec<ByteBuf, BrewingList> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BrewingList::getRecipes,
            BrewingList::new
    );

    private final List<ResourceLocation> recipeIds = new LinkedList<>();

    public BrewingList() {
        this(List.of());
    }

    public BrewingList(BrewingList brewingList) {
        this(brewingList.recipeIds);
    }

    public BrewingList(List<ResourceLocation> recipes) {
        for (ResourceLocation recipeId : recipes) {
            if (!recipeIds.contains(recipeId)) {
                recipeIds.add(recipeId);
            }
        }
    }

    public int size() {
        return recipeIds.size();
    }

    public boolean isEmpty() {
        return recipeIds.isEmpty();
    }

    public boolean contains(ResourceLocation recipeId) {
        return recipeIds.contains(recipeId);
    }

    public void shuffle() {
        if (isEmpty()) return;
        Collections.shuffle(recipeIds);
    }

    public @Nullable ResourceLocation pop() {
        if (isEmpty()) return null;
        ResourceLocation recipeId = recipeIds.getFirst();
        select(recipeId);
        return recipeId;
    }

    public @Nullable ResourceLocation get() {
        if (isEmpty()) return null;
        return recipeIds.getFirst();
    }

    public @Nullable ResourceLocation get(int idx) {
        if (isEmpty()) return null;
        if (idx >= recipeIds.size()) return null;
        return recipeIds.get(idx);
    }

    public boolean select(ResourceLocation recipeId) {
        if (isEmpty()) return false;
        if (!recipeIds.remove(recipeId)) return false;
        recipeIds.addFirst(recipeId);
        return true;
    }

    public boolean add(ResourceLocation recipeId) {
        if (recipeIds.contains(recipeId)) return false;
        recipeIds.add(recipeId);
        return true;
    }

    public boolean remove(ResourceLocation recipeId) {
        return recipeIds.remove(recipeId);
    }

    public @Nullable ResourceLocation remove(int idx) {
        if (isEmpty()) return null;
        if (idx >= recipeIds.size()) return null;
        return recipeIds.remove(idx);
    }

    public List<ResourceLocation> getRecipes() {
        return new ArrayList<>(recipeIds);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BrewingList list) {
            return recipeIds.equals(list.recipeIds);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return recipeIds.hashCode();
    }
}
