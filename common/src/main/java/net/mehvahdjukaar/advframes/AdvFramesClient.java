package net.mehvahdjukaar.advframes;

import com.mojang.blaze3d.vertex.BufferUploader;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.client.AdvancementFrameBlockTileRenderer;
import net.mehvahdjukaar.advframes.client.AdvancementFrameModel;
import net.mehvahdjukaar.advframes.client.AdvancementSelectScreen;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AdvFramesClient {
    public static final ResourceLocation TASK_MODEL = AdvFrames.res("item/task");
    public static final ResourceLocation GOAL_MODEL = AdvFrames.res("item/goal");
    public static final ResourceLocation CHALLENGE_MODEL = AdvFrames.res("item/challenge");

    public static void init(){
        ClientHelper.addSpecialModelRegistration(AdvFramesClient::registerSpecialModels);
        ClientHelper.addBlockEntityRenderersRegistration(AdvFramesClient::registerBlockEntityRenderers);
        ClientHelper.addModelLoaderRegistration(AdvFramesClient::registerModelLoaders);

        ClientHelper.addClientSetup(()->ClientHelper.registerRenderType(AdvFrames.ADVANCEMENT_FRAME.get(), RenderType.cutout()));
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(AdvFrames.res("advancement_frame"), new NestedModelLoader("frame", AdvancementFrameModel::new));
    }

    private static void registerBlockEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
    }


    private static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        event.register(TASK_MODEL);
        event.register(GOAL_MODEL);
        event.register(CHALLENGE_MODEL);
    }


    //not using set screen to avoid firing forge event since SOME mods like to override ANY screen that extends advancement screen (looking at you better advancements XD)
    public static void setAdvancementScreen(AdvancementFrameBlockTile tile, Player player) {
        if(player instanceof LocalPlayer lp) {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = new AdvancementSelectScreen(tile, lp.connection.getAdvancements());

            clearForgeGuiLayers(minecraft);
            Screen old = minecraft.screen;

            if (old != null) {
                old.removed();
            }

            minecraft.screen = screen;
            BufferUploader.reset();
            minecraft.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            screen.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
            minecraft.noRender = false;

            minecraft.updateTitle();
        }
    }

    @ExpectPlatform
    private static void clearForgeGuiLayers(Minecraft minecraft) {
        throw new AssertionError();
    }


}
