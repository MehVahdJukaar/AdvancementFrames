package net.mehvahdjukaar.advframes.blocks;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatFrameBlock extends BaseFrameBlock {
    protected static final VoxelShape SHAPE_DOWN = Block.box(0, 15, 2, 16, 16, 14);
    protected static final VoxelShape SHAPE_UP = Block.box(0, 0, 2, 16, 1, 14);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0, 2, 15, 16, 14, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(0, 2, 0, 16, 14, 1);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 2, 0, 1, 14, 16);
    protected static final VoxelShape SHAPE_WEST = Block.box(15, 2, 0, 16, 14, 16);

    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public StatFrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(TRIGGERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRIGGERED);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TRIGGERED)) {
            level.setBlock(pos, state.cycle(TRIGGERED), 2);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return Utils.getTicker(blockEntityType, AdvFrames.STAT_FRAME_TILE.get(), StatFrameBlockTile::tick);
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof StatFrameBlockTile tile) {
                if (tile.getStat() == null) {
                    AdvFramesClient.setStatScreen(tile, player);
                } else {
                    Component ownerName = tile.getOwnerName();
                    if (ownerName != null) {

                        Stat stat = tile.getStat();
                        Component name = ownerName.copy().withStyle(ChatFormatting.GOLD);
                        Component title = getStatComponent(stat);
                        Component number = Component.literal(stat.format(tile.getValue())).withStyle(ChatFormatting.DARK_RED);
                        player.sendOverlayMessage(Component.translatable("advancementframes.message.stat",
                                title, name, number));
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StatFrameBlockTile(pos, state);
    }


    public static MutableComponent getStatComponent(Stat<?> stat) {
        StatType<?> type = stat.getType();
        Object value = stat.getValue();
        MutableComponent text;
        Identifier statId = BuiltInRegistries.STAT_TYPE.getKey(type);
        switch (value) {
            case ItemLike i -> text = Component.translatable(
                    "stat.advancementframes." + statId.getPath(), Component.translatable(i.asItem().getDescriptionId()).getString());
            case EntityType<?> e -> text = Component.translatable(
                    "stat.advancementframes." + statId.getPath(), e.getDescription().getString());
            case Identifier identifier -> {
                String string = stat.getValue().toString();
                text = Component.translatable("stat." + string.replace(':', '.'));
            }
            default -> text = Component.literal("Unsupported Stat");
        }
        return text;
    }

}
