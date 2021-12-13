package net.mehvahdjukaar.advframes.init;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.client.AdvancementFrameBlockTileRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdvFrames.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
    }

    @OnlyIn(Dist.CLIENT)
    public static void init(final FMLClientSetupEvent event) {

    }


}
