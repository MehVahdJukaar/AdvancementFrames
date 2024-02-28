package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.moonlight.api.ModSharedVariables;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class ClientConfigs {

    public static final Supplier<Boolean> ANIMATED_ICONS;
    public static final Supplier<Boolean> CENTERED_TEXT;
    public static final Supplier<Integer> STAT_UPDATE_INTERVAL;

    static {
        ConfigBuilder builder = ConfigBuilder.create(AdvFrames.MOD_ID, ConfigType.CLIENT);

        builder.push("visuals");
        ANIMATED_ICONS = builder.comment("Makes advancement icons animated")
                .define("animated_icons", false);
        CENTERED_TEXT = builder.comment("Makes stat frame text centered")
                .define("centered_text", true);
        STAT_UPDATE_INTERVAL = builder.comment("How ofter the client will request stats update from server in seconds")
                        .define("stat_update_interval", 60, 1, 1000);
        builder.pop();
        builder.buildAndRegister();

    }

    private static float signColorMult = 1;
    private static void refresh() {
        Double b = ModSharedVariables.getDouble("color_multiplier");
        signColorMult = (float) (b == null ? 1 : b);
    }

    public static float getSignColorMult() {
        return signColorMult;
    }

    public static void init() {
        ClientHelper.addClientSetup(ClientConfigs::refresh);
    }
}
