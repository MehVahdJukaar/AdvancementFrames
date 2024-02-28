package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.advframes.blocks.*;
import net.mehvahdjukaar.advframes.integration.CreateCompat;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
                    BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)
                            .mapColor(MapColor.NONE)
                            .sound(SoundType.WOOD)
                            .strength(0.25f, 0.25f)
                            .noCollission()));

    public static final Supplier<Item> ADVANCEMENT_FRAME_ITEM = RegHelper.registerItem(ADVANCEMENT_FRAME_NAME,
            () -> new BlockItem(ADVANCEMENT_FRAME.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<AdvancementFrameBlockTile>> ADVANCEMENT_FRAME_TILE = RegHelper.registerBlockEntityType(
            ADVANCEMENT_FRAME_NAME, () -> PlatHelper.newBlockEntityType(
                    AdvancementFrameBlockTile::new, ADVANCEMENT_FRAME.get()));

    public static final ResourceLocation STAT_FRAME_NAME = AdvFrames.res("stat_frame");
    public static final Supplier<Block> STAT_FRAME = RegHelper.registerBlock(STAT_FRAME_NAME,
            () -> new StatFrameBlock(BlockBehaviour.Properties.copy(ADVANCEMENT_FRAME.get())));

    public static final Supplier<Item> ASTAT_FRAME_ITEM = RegHelper.registerItem(STAT_FRAME_NAME,
            () -> new BlockItem(STAT_FRAME.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<StatFrameBlockTile>> STAT_FRAME_TILE = RegHelper.registerBlockEntityType(
            STAT_FRAME_NAME, () -> PlatHelper.newBlockEntityType(
                    StatFrameBlockTile::new, STAT_FRAME.get()));


    //called on mod creation
    public static void commonInit() {
        NetworkHandler.init();
        RegHelper.addItemsToTabsRegistration(AdvFrames::addCreativeTabItems);
        PlatHelper.addCommonSetup(AdvFrames::commonSetup);
    }

    public static void commonSetup() {
        if (PlatHelper.isModLoaded("create")) CreateCompat.setup();

    }

    private static void addCreativeTabItems(RegHelper.ItemToTabEvent event) {
        event.addBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(Items.ITEM_FRAME),
                ADVANCEMENT_FRAME_ITEM.get(), ASTAT_FRAME_ITEM.get());
    }


    public static void onServerStarting(MinecraftServer server) {
        BaseFrameBlockTile.setup(server.getProfileCache(), server.getSessionService(), server);
    }
}
