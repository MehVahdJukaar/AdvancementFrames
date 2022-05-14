package net.mehvahdjukaar.advframes.init;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.client.AdvancementFrameBlockTileRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AdvFrames.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    public static final ResourceLocation TASK_MODEL = AdvFrames.res("item/task");
    public static final ResourceLocation GOAL_MODEL = AdvFrames.res("item/goal");
    public static final ResourceLocation CHALLENGE_MODEL = AdvFrames.res("item/challenge");

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ForgeModelBakery.addSpecialModel(TASK_MODEL);
        ForgeModelBakery.addSpecialModel(GOAL_MODEL);
        ForgeModelBakery.addSpecialModel(CHALLENGE_MODEL);
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
    }


}
