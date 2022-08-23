package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class AdvFrames {
    public static final String MOD_ID = "advancementframes";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public static final ResourceLocation ADVANCEMENT_FRAME_NAME = AdvFrames.res("advancement_frame");
    public static final Supplier<Block> ADVANCEMENT_FRAME = RegHelper.registerBlock(ADVANCEMENT_FRAME_NAME,
            () -> new AdvancementFrameBlock(
                    BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                            .sound(SoundType.WOOD)
                            .strength(0.25f, 0.25f)
                            .noCollission()));

    public static final Supplier<Item> ADVANCEMENT_FRAME_ITEM = RegHelper.registerItem(ADVANCEMENT_FRAME_NAME,
            () -> new BlockItem(ADVANCEMENT_FRAME.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final Supplier<BlockEntityType<AdvancementFrameBlockTile>> ADVANCEMENT_FRAME_TILE = RegHelper.registerBlockEntityType(
            ADVANCEMENT_FRAME_NAME, () -> PlatformHelper.newBlockEntityType(
                    AdvancementFrameBlockTile::new, ADVANCEMENT_FRAME.get()));


    //called on mod creation
    public static void commonInit() {
        NetworkHandler.registerMessages();
    }


    public static void onServerStarting(MinecraftServer server) {
        AdvancementFrameBlockTile.setup(server.getProfileCache(), server.getSessionService(), server);
    }
}
