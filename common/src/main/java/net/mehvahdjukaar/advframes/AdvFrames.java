package net.mehvahdjukaar.advframes;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.integration.CreateCompat;
import net.mehvahdjukaar.advframes.network.ModMessages;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.Identifier;
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

    public static Identifier res(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static final Logger LOGGER = LogManager.getLogger("Advancement Frames");

    public static final Identifier ADVANCEMENT_FRAME_NAME = AdvFrames.res("advancement_frame");
    public static final Supplier<Block> ADVANCEMENT_FRAME = RegHelper.registerBlock(ADVANCEMENT_FRAME_NAME,
            AdvancementFrameBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)
                    .mapColor(MapColor.NONE)
                    .sound(SoundType.WOOD)
                    .strength(0.25f, 0.25f)
                    .noCollision());

    public static final Supplier<BlockItem> ADVANCEMENT_FRAME_ITEM = RegHelper.registerBlockItem(
            ADVANCEMENT_FRAME_NAME, ADVANCEMENT_FRAME, new Item.Properties());

    public static final Supplier<BlockEntityType<AdvancementFrameBlockTile>> ADVANCEMENT_FRAME_TILE = RegHelper.registerBlockEntityType(
            ADVANCEMENT_FRAME_NAME, AdvancementFrameBlockTile::new, ADVANCEMENT_FRAME);

    public static final Identifier STAT_FRAME_NAME = AdvFrames.res("stat_frame");
    public static final Supplier<Block> STAT_FRAME = RegHelper.registerBlock(STAT_FRAME_NAME,
            StatFrameBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(ADVANCEMENT_FRAME.get()));

    public static final Supplier<BlockItem> ASTAT_FRAME_ITEM = RegHelper.registerBlockItem(
            STAT_FRAME_NAME, STAT_FRAME, new Item.Properties());

    public static final Supplier<BlockEntityType<StatFrameBlockTile>> STAT_FRAME_TILE = RegHelper.registerBlockEntityType(
            STAT_FRAME_NAME, StatFrameBlockTile::new, STAT_FRAME);


    //called on mod creation
    public static void commonInit() {
        ModMessages.init();
        RegHelper.addItemsToTabsRegistration(AdvFrames::addCreativeTabItems);
        PlatHelper.addCommonSetup(AdvFrames::commonSetup);
        if (PlatHelper.isModLoaded("create")) CreateCompat.init();
    }

    public static void commonSetup() {
        if (PlatHelper.isModLoaded("create")) CreateCompat.setup();

    }

    private static void addCreativeTabItems(RegHelper.ItemToTabEvent event) {
        event.addBefore(CreativeModeTabs.FUNCTIONAL_BLOCKS, i -> i.is(Items.ITEM_FRAME),
                ADVANCEMENT_FRAME_ITEM.get(), ASTAT_FRAME_ITEM.get());
    }

}
