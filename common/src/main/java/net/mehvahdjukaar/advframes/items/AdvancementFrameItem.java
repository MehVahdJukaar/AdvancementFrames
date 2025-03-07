package net.mehvahdjukaar.advframes.items;

import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancementFrameItem extends BlockItem {

    public AdvancementFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean flag = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (level.isClientSide && !flag && player != null) {
            if (level.getBlockEntity(pos) instanceof AdvancementFrameBlockTile tile) {
                AdvFramesClient.setAdvancementScreen(tile, player);
            }
        }
        return flag;
    }
}
