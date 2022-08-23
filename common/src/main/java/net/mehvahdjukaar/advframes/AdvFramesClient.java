package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.advframes.client.AdvancementFrameBlockTileRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.resources.ResourceLocation;

public class AdvFramesClient {
    public static final ResourceLocation TASK_MODEL = AdvFrames.res("item/task");
    public static final ResourceLocation GOAL_MODEL = AdvFrames.res("item/goal");
    public static final ResourceLocation CHALLENGE_MODEL = AdvFrames.res("item/challenge");

    public static void init(){
        ClientPlatformHelper.addSpecialModelRegistration(AdvFramesClient::registerSpecialModels);
        ClientPlatformHelper.addBlockEntityRenderersRegistration(AdvFramesClient::registerBlockEntityRenderers);
    }

    private static void registerBlockEntityRenderers(ClientPlatformHelper.BlockEntityRendererEvent event) {
        event.register(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
    }


    private static void registerSpecialModels(ClientPlatformHelper.SpecialModelEvent event) {
        event.register(TASK_MODEL);
        event.register(GOAL_MODEL);
        event.register(CHALLENGE_MODEL);
    }
}
