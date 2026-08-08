package com.winexp.maidtavern.maid.brew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BrewingSession(BrewingList.Entry entry, Optional<BlockPos> barrelPos, Stage stage) {
    public static final Codec<BrewingSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BrewingList.Entry.CODEC.fieldOf("entry").forGetter(BrewingSession::entry),
            BlockPos.CODEC.optionalFieldOf("barrel_pos").forGetter(BrewingSession::barrelPos),
            Stage.CODEC.fieldOf("stage").forGetter(BrewingSession::stage)
    ).apply(instance, BrewingSession::new));

    public BrewingSession(BrewingList.Entry entry, @Nullable BlockPos barrelPos, Stage stage) {
        this(entry, Optional.ofNullable(barrelPos), stage);
    }

    public static BrewingSession create(BrewingList.Entry entry, @Nullable BlockPos barrelPos) {
        return new BrewingSession(entry, Optional.ofNullable(barrelPos), Stage.TAKE_INGREDIENTS);
    }

    public BrewingSession withBarrelPos(@Nullable BlockPos barrelPos) {
        return new BrewingSession(entry, Optional.ofNullable(barrelPos), stage);
    }

    public BrewingSession withStage(Stage stage) {
        return new BrewingSession(entry, barrelPos, stage);
    }

    public enum Stage implements StringRepresentable {
        TAKE_INGREDIENTS(false, false, false),
        BREWING(true, false, false),
        FLUIDS_PLACED(true, true, false),
        INGREDIENTS_PLACED(true, true, true);

        public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);

        private final boolean brewing;
        private final boolean fluidsPlaced;
        private final boolean ingredientsPlaced;

        Stage(boolean brewing, boolean fluidPlaced, boolean ingredientsPlaced) {
            this.brewing = brewing;
            this.fluidsPlaced = fluidPlaced;
            this.ingredientsPlaced = ingredientsPlaced;
        }

        public boolean isBrewing() {
            return brewing;
        }

        public boolean isFluidsPlaced() {
            return fluidsPlaced;
        }

        public boolean isIngredientsPlaced() {
            return ingredientsPlaced;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }
}
