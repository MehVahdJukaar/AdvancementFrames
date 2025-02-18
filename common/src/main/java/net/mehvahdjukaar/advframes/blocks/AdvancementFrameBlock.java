package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class AdvancementFrameBlock extends BaseFrameBlock {
    protected static final VoxelShape SHAPE_DOWN = Block.box(1, 15, 1, 15, 16, 15);
    protected static final VoxelShape SHAPE_UP = Block.box(1, 0, 1, 15, 1, 15);
    protected static final VoxelShape SHAPE_NORTH = Block.box(1, 1, 15, 15, 15, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(1, 1, 0, 15, 15, 1);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 1, 1, 1, 15, 15);
    protected static final VoxelShape SHAPE_WEST = Block.box(15, 1, 1, 16, 15, 15);

    public static final EnumProperty<Type> TYPE = EnumProperty.create("frame_type", Type.class);

    public AdvancementFrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(TYPE, Type.NONE));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof AdvancementFrameBlockTile tile) {
                if (tile.getAdvancement() == null) {
                    AdvFramesClient.setAdvancementScreen(tile, player);
                } else {
                    Component ownerName = tile.getOwnerName();

                    if (ownerName != null) {
                        DisplayInfo advancement = tile.getAdvancement();
                        if (player.isSecondaryUseActive()) {
                            player.displayClientMessage(advancement.getDescription(), true);
                        } else {
                            Component name = ownerName.copy().withStyle(ChatFormatting.GOLD);
                            Component title = Component.literal(advancement.getTitle().getString())
                                    .withStyle(tile.getTitleColor());

                            player.displayClientMessage(Component.translatable(
                                    "advancementframes.message.advancement", name, title), true);
                        }
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancementFrameBlockTile(pos, state);
    }

    public enum Type implements StringRepresentable {
        TASK,
        CHALLENGE,
        GOAL,
        NONE;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static Type get(@Nullable DisplayInfo type) {
            if (type == null) return NONE;
            return values()[type.getType().ordinal()];
        }

        @Environment(EnvType.CLIENT)
        @Nullable
        public ModelResourceLocation getModel() {
            return switch (this) {
                case GOAL -> AdvFramesClient.GOAL_MODEL;
                case TASK -> AdvFramesClient.TASK_MODEL;
                case CHALLENGE -> AdvFramesClient.CHALLENGE_MODEL;
                case NONE -> null;
            };
        }
    }

}
