package com.winexp.maidtavern.maid.behavior.waiter;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum OrderState implements StringRepresentable {
    RETRIEVING,
    DELIVERING;

    public static final Codec<OrderState> CODEC = StringRepresentable.fromEnum(OrderState::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
