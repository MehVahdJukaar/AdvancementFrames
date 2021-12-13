package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.init.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: MehVahdJukaar
 */
@Mod(AdvFrames.MOD_ID)
public class AdvFrames {
    public static final String MOD_ID = "advancementframes";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public AdvFrames(){

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.init(bus);


        MinecraftForge.EVENT_BUS.addListener(AdvFrames::serverAboutToStart);
    }


    public static void serverAboutToStart(final ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        AdvancementFrameBlockTile.setup(server.getProfileCache(),server.getSessionService(), server);
    }
}
