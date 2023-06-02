package net.mehvahdjukaar.advframes.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;

public class AdvFramesFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        AdvFrames.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            ClientHelper.addClientSetup(AdvFramesClient::init);
        }

        ServerLifecycleEvents.SERVER_STARTING.register(AdvFrames::onServerStarting);
    }
}
