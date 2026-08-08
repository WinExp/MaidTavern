package com.winexp.maidtavern.maid.brew;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.IBarrel;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record BrewingList(ImmutableMap<ResourceLocation, Config> entries) {
    public static final Codec<BrewingList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("entries").forGetter(BrewingList::getEntries)
    ).apply(instance, BrewingList::new));
    public static final StreamCodec<FriendlyByteBuf, BrewingList> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BrewingList::getEntries,
            BrewingList::new
    );
    public static final BrewingList DEFAULT = new BrewingList(ImmutableMap.of());

    public BrewingList(Collection<Entry> entries) {
        this(entries.stream().collect(Collectors.toMap(Entry::recipeId, Entry::config)));
    }

    public BrewingList(Map<ResourceLocation, Config> entries) {
        this(ImmutableMap.copyOf(entries));
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public boolean contains(ResourceLocation recipeId) {
        return entries.containsKey(recipeId);
    }

    public @Nullable Entry get(ResourceLocation recipeId) {
        if (isEmpty()) return null;
        return new Entry(recipeId, entries.get(recipeId));
    }

    public List<Entry> getEntries() {
        return entries.entrySet().stream().map(entry -> new Entry(entry.getKey(), entry.getValue())).toList();
    }

    public record Entry(ResourceLocation recipeId, Config config) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(Entry::recipeId),
                Config.CODEC.fieldOf("config").forGetter(Entry::config)
        ).apply(instance, Entry::new));
        public static final StreamCodec<FriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                Entry::recipeId,
                Config.STREAM_CODEC,
                Entry::config,
                Entry::new
        );

        public @Nullable BarrelRecipe getRecipe(RecipeManager recipeManager) {
            return (BarrelRecipe) recipeManager.byKey(recipeId).map(RecipeHolder::value).orElse(null);
        }
    }

    public record Config(int brewLevel, ImmutableSet<BlockPos> barrelPos) {
        public static final IntegerRange BREW_LEVEL_RANGE = IntegerRange.of(IBarrel.BREWING_STARTED, IBarrel.BREWING_FINISHED);
        private static final StreamCodec<ByteBuf, Integer> RANGED_VAR_INT = new StreamCodec<>() {
            @Override
            public Integer decode(ByteBuf buf) {
                return BREW_LEVEL_RANGE.fit(VarInt.read(buf));
            }

            @Override
            public void encode(ByteBuf buf, Integer integer) {
                VarInt.write(buf, BREW_LEVEL_RANGE.fit(integer));
            }
        };
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(BREW_LEVEL_RANGE.getMinimum(), BREW_LEVEL_RANGE.getMaximum()).fieldOf("brew_level").forGetter(Config::brewLevel),
                BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, List::copyOf).fieldOf("barrel_pos").forGetter(Config::barrelPos)
        ).apply(instance, Config::new));
        public static final StreamCodec<FriendlyByteBuf, Config> STREAM_CODEC = StreamCodec.composite(
                RANGED_VAR_INT,
                Config::brewLevel,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ImmutableSet::copyOf, List::copyOf),
                Config::barrelPos,
                Config::new
        );
        public static final Config DEFAULT = new Config(IBarrel.BREWING_FINISHED, ImmutableSet.of());

        public Config(int brewLevel, Collection<BlockPos> barrelPos) {
            this(brewLevel, ImmutableSet.copyOf(barrelPos));
        }

        public static class Builder {
            private int brewLevel;
            private final Set<BlockPos> barrelPos = new HashSet<>();

            public Builder() {
                this(Config.DEFAULT);
            }

            public Builder(Config config) {
                brewLevel = config.brewLevel;
                barrelPos.addAll(config.barrelPos);
            }

            public Builder brewLevel(int brewLevel) {
                this.brewLevel = brewLevel;
                return this;
            }

            public Builder addBarrelPos(BlockPos pos) {
                barrelPos.add(pos);
                return this;
            }

            public Builder removeBarrelPos(BlockPos pos) {
                barrelPos.remove(pos);
                return this;
            }

            public Config build() {
                return new Config(brewLevel, barrelPos);
            }
        }
    }

    public static class Builder {
        private final Map<ResourceLocation, Config> entries = new HashMap<>();

        public Builder() {
            this(BrewingList.DEFAULT);
        }

        public Builder(BrewingList brewingList) {
            entries.putAll(brewingList.entries);
        }

        public Builder put(ResourceLocation recipeId, Config config) {
            entries.put(recipeId, config);
            return this;
        }

        public Builder remove(ResourceLocation recipeId) {
            entries.remove(recipeId);
            return this;
        }

        public BrewingList build() {
            return new BrewingList(entries);
        }
    }
}
