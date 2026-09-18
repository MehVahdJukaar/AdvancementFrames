package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.BaseFrameBlock;
import net.mehvahdjukaar.advframes.blocks.BaseFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFrameTileRenderer<T extends BaseFrameBlockTile, S extends BaseFrameTileRenderer.FrameRenderState>
        implements BlockEntityRenderer<T, S> {

    protected final Font font;
    protected final Minecraft minecraft;

    protected BaseFrameTileRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.font = context.font();
    }

    public static class FrameRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean veryNear;
        @Nullable
        public FormattedCharSequence title;
        public int titleColor;
        @Nullable
        public FormattedCharSequence ownerName;
    }

    @Override
    public void extractRenderState(T tile, S state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = tile.getBlockState().getValue(BaseFrameBlock.FACING);
        state.veryNear = LOD.at(tile).isVeryNear();
        state.title = null;
        state.ownerName = null;

        if (Minecraft.renderNames() && state.veryNear && isLookingAt(tile.getBlockPos())) {
            Component title = tile.getTitle();
            if (title != null) {
                state.title = title.getVisualOrderText();
                state.titleColor = ARGB.opaque(tile.getTitleColor().getColor());
            }
            Component name = tile.getOwnerName();
            if (name != null) state.ownerName = name.getVisualOrderText();
        }
    }

    private boolean isLookingAt(BlockPos pos) {
        HitResult hit = minecraft.hitResult;
        return hit != null && hit.getType() == HitResult.Type.BLOCK && pos.equals(BlockPos.containing(hit.getLocation()));
    }

    public void submitTopTextBottomText(S state, PoseStack poseStack, SubmitNodeCollector collector, float offset) {
        if (state.title != null) {
            submitCenteredLine(state.title, state.titleColor, state, poseStack, collector, offset);
        }
        if (state.ownerName != null) {
            submitCenteredLine(state.ownerName, -1, state, poseStack, collector, -offset);
        }
    }

    private void submitCenteredLine(FormattedCharSequence line, int color, S state, PoseStack poseStack,
                                    SubmitNodeCollector collector, float y) {
        poseStack.pushPose();

        float width = font.width(line);
        float scale = 0.025f;
        if (width > 48) {
            scale /= width / 48;
        }

        poseStack.translate(0, y + 4 * scale, 0.0125);
        poseStack.scale(scale, -scale, scale);

        collector.submitText(poseStack, -width / 2f, 0, line, true, Font.DisplayMode.POLYGON_OFFSET,
                state.lightCoords, color, 0, 0);
        poseStack.popPose();
    }

}
