package net.mehvahdjukaar.advframes.init;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AdvFrames.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AdvFrames.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, AdvFrames.MOD_ID);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        NetworkHandler.registerMessages();
    }

    private static RegistryObject<Item> regItem(String name, Supplier<? extends Item> sup) {
        return ITEMS.register(name, sup);
    }

    protected static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group) {
        return regItem(blockSup.getId().getPath(), () -> new BlockItem(blockSup.get(), (new Item.Properties()).tab(group)));
    }


    public static final String ADVANCEMENT_FRAME_NAME = "advancement_frame";
    public static final RegistryObject<Block> ADVANCEMENT_FRAME = BLOCKS.register(ADVANCEMENT_FRAME_NAME, () -> new AdvancementFrameBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(0.25f, 0.25f)
                    .noCollission()));

    public static final RegistryObject<Item> ADVANCEMENT_FRAME_ITEM = regBlockItem(ADVANCEMENT_FRAME, CreativeModeTab.TAB_DECORATIONS);

    public static final RegistryObject<BlockEntityType<AdvancementFrameBlockTile>> ADVANCEMENT_FRAME_TILE = TILES.register(ADVANCEMENT_FRAME_NAME, () -> BlockEntityType.Builder.of(
            AdvancementFrameBlockTile::new, ADVANCEMENT_FRAME.get()).build(null));
}
