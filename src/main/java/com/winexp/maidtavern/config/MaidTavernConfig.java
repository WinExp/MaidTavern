package com.winexp.maidtavern.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MaidTavernConfig {
    public static final MaidTavernConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    public final ForgeConfigSpec.BooleanValue enableDrinking;
    public final ForgeConfigSpec.IntValue pathFindingAttempt;
    public final ForgeConfigSpec.IntValue workExpirationTime;

    static {
        Pair<MaidTavernConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(MaidTavernConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public MaidTavernConfig(ForgeConfigSpec.Builder builder) {
        enableDrinking = builder.define("enable_drinking", true);
        pathFindingAttempt = builder.defineInRange("path_finding_attempt", 4, 1, 10);
        workExpirationTime = builder.defineInRange("work_expiration_time", 300, 200, Integer.MAX_VALUE);
    }
}
