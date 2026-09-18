package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.client.*;
import net.mehvahdjukaar.advframes.integration.CreateCompat;
import net.mehvahdjukaar.moonlight.api.client.model.NestedUnbakedModel;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;

public class AdvFramesClient {
    public static final Identifier TASK_MODEL = AdvFrames.res("item/task");
    public static final Identifier GOAL_MODEL = AdvFrames.res("item/goal");
    public static final Identifier CHALLENGE_MODEL = AdvFrames.res("item/challenge");
    protected static long gameTime;

    public static void init() {
        ClientConfigs.init();
        ClientHelper.addStandaloneModelRegistration(AdvFramesClient::registerStandaloneModels);
        ClientHelper.addBlockEntityRenderersRegistration(AdvFramesClient::registerBlockEntityRenderers);
        ClientHelper.addBlockModelRegistration(AdvFramesClient::registerBlockModels);

        ClientHelper.addClientSetup(AdvFramesClient::clientSetup);
    }

    public static void clientSetup() {
        if (PlatHelper.isModLoaded("create")) CreateCompat.setupClient();
    }

    private static void registerBlockModels(ClientHelper.BlockModelEvent event) {
        event.register(AdvFrames.res("advancement_frame"), NestedUnbakedModel.codec("frame", AdvancementFrameModel::new));
    }

    private static void registerBlockEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), AdvancementFrameBlockTileRenderer::new);
        event.register(AdvFrames.STAT_FRAME_TILE.get(), StatFrameBlockTileRenderer::new);
    }


    private static void registerStandaloneModels(ClientHelper.StandaloneModelEvent event) {
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
            screen.added();
            minecraft.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            screen.init(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

            minecraft.updateTitle();
        }
    }

    @PlatformImpl
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
