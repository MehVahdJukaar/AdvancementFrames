package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.mehvahdjukaar.advframes.client.AdvancementSelectScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;

public class AdvancementFrameBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE_DOWN = Block.box(1, 15, 1, 15, 16, 15);
    protected static final VoxelShape SHAPE_UP = Block.box(1, 0, 1, 15, 1, 15);
    protected static final VoxelShape SHAPE_NORTH = Block.box(1, 1, 15, 15, 15, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(1, 1, 0, 15, 15, 1);
    protected static final VoxelShape SHAPE_EAST = Block.box(0, 1, 1, 1, 15, 15);
    protected static final VoxelShape SHAPE_WEST = Block.box(15, 1, 1, 16, 15, 15);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public AdvancementFrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getMaterial().isSolid();
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext p_60558_) {
        return switch (state.getValue(FACING)){
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
        };
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == stateIn.getValue(FACING).getOpposite() && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (player instanceof LocalPlayer p) {
            if (level.getBlockEntity(pos) instanceof AdvancementFrameBlockTile tile) {
                if(tile.getAdvancement() == null){
                    setScreen(tile, p);
                }else {
                    GameProfile owner = tile.getOwner();
                    if(owner != null && owner.getName() != null){
                        DisplayInfo advancement = tile.getAdvancement();
                        if(player.isSecondaryUseActive()){
                            player.displayClientMessage(advancement.getDescription(),true);
                        }else{
                            Component name = new TextComponent(owner.getName()).withStyle(ChatFormatting.GOLD);
                            Component title = new TextComponent(advancement.getTitle().getString())
                                    .withStyle(tile.getColor());
                            player.displayClientMessage(new TranslatableComponent("advancementframes.message",name, title),true);
                        }
                    }
                }
            };
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState p_49849_, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity instanceof LocalPlayer player) {
            if (level.getBlockEntity(pos) instanceof AdvancementFrameBlockTile tile) {
                setScreen(tile, player);
            }
        }
        super.setPlacedBy(level, pos, p_49849_, entity, stack);
    }

    //not using set screen to avoid firing forge event since SOME mods like to override ANY screen that extends advancement screen (looking at you better advancements)
    public void setScreen(AdvancementFrameBlockTile tile, LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = new AdvancementSelectScreen(tile, player.connection.getAdvancements());

        ForgeHooksClient.clearGuiLayers(minecraft);
        Screen old = minecraft.screen;

        if (old != null){
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancementFrameBlockTile(pos, state);
    }

}
