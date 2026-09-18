package net.mehvahdjukaar.advframes.platform;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(AdvFrames.MOD_ID)
public class AdvFramesForge {

    public AdvFramesForge(IEventBus bus) {
        AdvFrames.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            AdvFramesClient.init();
        }
    }

}

