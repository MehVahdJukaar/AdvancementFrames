package net.mehvahdjukaar.advframes;

import com.mojang.blaze3d.vertex.BufferUploader;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.client.*;
import net.mehvahdjukaar.advframes.integration.CreateCompat;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;

public class AdvFramesClient {
    public static final ResourceLocation TASK_MODEL = AdvFrames.res("item/task");
    public static final ResourceLocation GOAL_MODEL = AdvFrames.res("item/goal");
    public static final ResourceLocation CHALLENGE_MODEL = AdvFrames.res("item/challenge");
    protected static long gameTime;

    public static void init() {
        ClientConfigs.init();
        ClientHelper.addSpecialModelRegistration(AdvFramesClient::registerSpecialModels);
        ClientHelper.addBlockEntityRenderersRegistration(AdvFramesClient::registerBlockEntityRenderers);
        ClientHelper.addModelLoaderRegistration(AdvFramesClient::registerModelLoaders);

        ClientHelper.addClientSetup(AdvFramesClient::clientSetup);
    }

    public static void clientSetup() {
        if (PlatHelper.isModLoaded("create")) CreateCompat.setupClient();
        ClientHelper.registerRenderType(AdvFrames.ADVANCEMENT_FRAME.get(), RenderType.cutout());
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(AdvFrames.res("advancement_frame"), new NestedModelLoader("frame", AdvancementFrameModel::new));
    }

    private static void registerBlockEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
        event.register(AdvFrames.STAT_FRAME_TILE.get(), StatFrameBlockTileRenderer::new);
    }


    private static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        event.register(TASK_MODEL);
        event.register(GOAL_MODEL);
        event.register(CHALLENGE_MODEL);
    }

    public static void setStatScreen(StatFrameBlockTile tile, Player player) {
        if (player instanceof LocalPlayer lp) {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = new StatSelectScreen(tile, lp.getStats());
            minecraft.setScreen(screen);
        }
    }

    //not using set screen to avoid firing forge event since SOME mods like to override ANY screen that extends advancement screen (looking at you better advancements XD)
    public static void setAdvancementScreen(AdvancementFrameBlockTile tile, Player player) {
        if (player instanceof LocalPlayer lp) {
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


    public static void updatePlayerStats(Object2IntMap<Stat<?>> stats) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (var entry : stats.object2IntEntrySet()) {
                Stat<?> stat = entry.getKey();
                int i = entry.getIntValue();
                player.getStats().setValue(Minecraft.getInstance().player, stat, i);
            }
        }
    }
}
