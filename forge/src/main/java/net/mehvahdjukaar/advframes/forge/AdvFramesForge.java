package net.mehvahdjukaar.advframes.forge;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MehVahdJukaar
 */
@Mod(AdvFrames.MOD_ID)
public class AdvFramesForge {


    public AdvFramesForge() {
        AdvFrames.commonInit();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(AdvFramesForge::serverStarting);
    }

    public static void serverStarting(ServerAboutToStartEvent event) {
        AdvFrames.onServerStarting(event.getServer());
    }

}

