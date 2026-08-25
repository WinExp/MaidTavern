package com.winexp.maidtavern.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MaidTavernConfig {
    public static final MaidTavernConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.BooleanValue enableDrinking;
    public final ModConfigSpec.IntValue pathFindingAttempt;
    public final ModConfigSpec.IntValue workExpirationTime;

    static {
        Pair<MaidTavernConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(MaidTavernConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public MaidTavernConfig(ModConfigSpec.Builder builder) {
        enableDrinking = builder.define("enable_drinking", true);
        pathFindingAttempt = builder.defineInRange("path_finding_attempt", 4, 1, 10);
        workExpirationTime = builder.defineInRange("work_expiration_time", 300, 200, Integer.MAX_VALUE);
    }
}
