package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final int INFINITE_STAT_RANGE = 32;

    public static final Supplier<Integer> STAT_UPDATE_RANGE;

    static {
        ConfigBuilder builder = ConfigBuilder.create(AdvFrames.MOD_ID, ConfigType.COMMON);

        builder.push("stat_frame");
        STAT_UPDATE_RANGE = builder.comment("Stat frames only update when their owner is within this many chunks. 0 never updates, " + INFINITE_STAT_RANGE + " is infinite")
                .define("update_range", 4, 0, INFINITE_STAT_RANGE);
        builder.pop();
        builder.build();
    }

    public static void init() {
    }
}
