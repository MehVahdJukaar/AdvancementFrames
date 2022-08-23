package net.mehvahdjukaar.advframes.forge;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdvFrames.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdvFramesForgeClient {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(AdvFramesClient::init);
    }

}
