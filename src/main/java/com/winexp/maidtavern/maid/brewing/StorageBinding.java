package com.winexp.maidtavern.maid.brewing;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public record StorageBinding(ImmutableSet<BlockPos> ingredients, ImmutableSet<BlockPos> results, ImmutableSet<BlockPos> byproducts) {
    public static final StorageBinding EMPTY = new StorageBinding(ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());

    public static final Codec<StorageBinding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, List::copyOf).fieldOf("ingredients").forGetter(StorageBinding::ingredients),
            BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, List::copyOf).fieldOf("results").forGetter(StorageBinding::results),
            BlockPos.CODEC.listOf().xmap(ImmutableSet::copyOf, List::copyOf).fieldOf("byproducts").forGetter(StorageBinding::byproducts)
    ).apply(instance, StorageBinding::new));

    public StorageBinding(Collection<BlockPos> ingredients, Collection<BlockPos> results, Collection<BlockPos> byproducts) {
        this(ImmutableSet.copyOf(ingredients), ImmutableSet.copyOf(results), ImmutableSet.copyOf(byproducts));
    }

    public static void encode(StorageBinding binding, FriendlyByteBuf buf) {
        buf.writeCollection(binding.ingredients, FriendlyByteBuf::writeBlockPos);
        buf.writeCollection(binding.results, FriendlyByteBuf::writeBlockPos);
        buf.writeCollection(binding.byproducts, FriendlyByteBuf::writeBlockPos);
    }

    public static StorageBinding decode(FriendlyByteBuf buf) {
        return new StorageBinding(
                (Collection<BlockPos>) buf.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos),
                buf.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos),
                buf.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos)
        );
    }

    public boolean isAllEmpty() {
        return ingredients.isEmpty() && results.isEmpty() && byproducts.isEmpty();
    }

    public List<BlockPos> get(Type type) {
        return switch (type) {
            case INGREDIENTS -> List.copyOf(ingredients);
            case RESULTS -> List.copyOf(results);
            case BYPRODUCTS -> List.copyOf(byproducts);
        };
    }

    public StorageBinding with(Type type, Collection<BlockPos> list) {
        return switch (type) {
            case INGREDIENTS -> new StorageBinding(list, results, byproducts);
            case RESULTS -> new StorageBinding(ingredients, list, byproducts);
            case BYPRODUCTS -> new StorageBinding(ingredients, results, list);
        };
    }

    public StorageBinding add(Type type, BlockPos pos) {
        return add(type, List.of(pos));
    }

    public StorageBinding add(Type type, Collection<BlockPos> list) {
        List<BlockPos> newList = new ArrayList<>(get(type));
        newList.addAll(list);
        return with(type, newList);
    }

    public StorageBinding remove(Type type, BlockPos pos) {
        List<BlockPos> newList = new LinkedList<>(get(type));
        newList.remove(pos);
        return with(type, newList);
    }

    public enum Type implements StringRepresentable {
        INGREDIENTS,
        RESULTS,
        BYPRODUCTS;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
