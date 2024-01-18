package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatFrameBlock extends BaseFrameBlock {
    protected static final VoxelShape SHAPE_DOWN = Block.box(2, 15, 1, 15, 16, 15);
    protected static final VoxelShape SHAPE_UP = Block.box(1, 0, 1, 15, 1, 15);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0, 2, 15, 16, 14, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0, 2, 0, 16, 14, 1);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 2, 0, 1, 14, 16);
    protected static final VoxelShape SHAPE_WEST = Block.box(15, 2, 0, 16, 14, 16);

    public StatFrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext p_60558_) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
        };
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof StatFrameBlockTile tile) {
                if (tile.getStat() == null) {
                    AdvFramesClient.setStatScreen(tile, player);
                } else {
                    GameProfile owner = tile.getOwner();
                    if (owner != null && owner.getName() != null) {
                        /*
                        DisplayInfo advancement = tile.getStat();
                        if (player.isSecondaryUseActive()) {
                            player.displayClientMessage(advancement.getDescription(), true);
                        } else {
                            Component name = Component.literal(owner.getName()).withStyle(ChatFormatting.GOLD);
                            Component title = Component.literal(advancement.getTitle().getString())
                                    .withStyle(tile.getColor());
                            player.displayClientMessage(Component.translatable("advancementframes.message", name, title), true);
                        }*/
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StatFrameBlockTile(pos, state);
    }
}
