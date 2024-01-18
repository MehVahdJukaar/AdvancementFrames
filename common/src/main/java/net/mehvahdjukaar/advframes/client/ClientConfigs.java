package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class ClientConfigs {

    public static final Supplier<Boolean> ANIMATED_ICONS;

    static {
        ConfigBuilder builder = ConfigBuilder.create(AdvFrames.MOD_ID, ConfigType.CLIENT);

        builder.push("visuals");
        ANIMATED_ICONS = builder.comment("Makes advancement icons animated")
                .define("animated_icons", false);
        builder.pop();

        builder.buildAndRegister();

    }

    public static void init() {
    }
}
