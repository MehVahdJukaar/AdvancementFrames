package net.mehvahdjukaar.advframes.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.fabric.FabricSetupCallbacks;

public class AdvFramesFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        AdvFrames.commonInit();

        if (PlatformHelper.getEnv().isClient()) {
            FabricSetupCallbacks.CLIENT_SETUP.add(AdvFramesClient::init);
        }

        ServerLifecycleEvents.SERVER_STARTING.register(AdvFrames::onServerStarting);
    }
}
