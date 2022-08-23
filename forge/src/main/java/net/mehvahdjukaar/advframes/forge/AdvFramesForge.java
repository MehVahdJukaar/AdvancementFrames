package net.mehvahdjukaar.advframes.forge;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(AdvFrames.MOD_ID)
public class AdvFramesForge {

    public AdvFramesForge() {
        AdvFrames.commonInit();

        MinecraftForge.EVENT_BUS.addListener(AdvFramesForge::serverStarting);

        if (PlatformHelper.getEnv().isClient()) {
            AdvFramesClient.init();
        }
    }

    public static void serverStarting(ServerAboutToStartEvent event) {
        AdvFrames.onServerStarting(event.getServer());
    }

}

