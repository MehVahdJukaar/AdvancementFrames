package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.DynamicTextureRenderer;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderableDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AdvancementFrameBlockTileRenderer extends BaseFrameTileRenderer<AdvancementFrameBlockTile,
        AdvancementFrameBlockTileRenderer.AdvancementFrameRenderState> {

    public AdvancementFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static class AdvancementFrameRenderState extends FrameRenderState {
        @Nullable
        public Identifier iconTexture;
    }

    @Override
    public AdvancementFrameRenderState createRenderState() {
        return new AdvancementFrameRenderState();
    }

    @Override
    public void extractRenderState(AdvancementFrameBlockTile tile, AdvancementFrameRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.iconTexture = null;
        DisplayInfo advancement = tile.getAdvancement();
        if (advancement == null) return;

        ItemStackTemplate icon = advancement.getIcon();
        RenderableDynamicTexture tex;
        //if it doesnt have fancy nbt we can use default optimized item renderer. useful since it can
        if (!ClientConfigs.ANIMATED_ICONS.get() && icon.components().isEmpty()) {
            tex = DynamicTextureRenderer.requestFlatItemTexture(icon.item().value(), 64);
        } else {
            //always renders animated cause its cooler
            int i = Objects.hash(icon.components(), icon.item());
            tex = DynamicTextureRenderer.requestFlatItemStackTexture(AdvFrames.res("" + i), icon.create(), 64);
        }
        if (tex != null) state.iconTexture = tex.getTextureLocation();
    }

    @Override
    public void submit(AdvancementFrameRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        Identifier tex = state.iconTexture;
        if (tex == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(RotHlpr.rot(state.facing.getOpposite()));
        double z = -7 / 16f + 0.01;
        poseStack.translate(0, 0, z);

        poseStack.pushPose();

        float s = 0.25f;
        int light = state.lightCoords;
        poseStack.scale(1, -1, -1);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(tex), (pose, buffer) -> {
            buffer.addVertex(pose, -s, s, 0).setColor(-1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            buffer.addVertex(pose, s, s, 0).setColor(-1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            buffer.addVertex(pose, s, -s, 0).setColor(-1).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
            buffer.addVertex(pose, -s, -s, 0).setColor(-1).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 0, -1);
        });

        poseStack.popPose();

        submitTopTextBottomText(state, poseStack, collector, 0.375f);

        poseStack.popPose();
    }

}
