package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.stats.Stat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BooleanSupplier;

public class StatFrameBlockTileRenderer extends BaseFrameTileRenderer<StatFrameBlockTile,
        StatFrameBlockTileRenderer.StatFrameRenderState> {

    private static final float PAPER_Y_MARGIN = 6.5f / 16f;
    private static final float PAPER_X_MARGIN = 0.125f;

    public StatFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static class StatFrameRenderState extends FrameRenderState {
        public boolean hasStat;
        public List<FormattedCharSequence> lines = List.of();
        public float fontScale;
        public String value = "";
        public TextUtil.RenderProperties textProperties;
    }

    @Override
    public StatFrameRenderState createRenderState() {
        return new StatFrameRenderState();
    }

    @Override
    public void extractRenderState(StatFrameBlockTile tile, StatFrameRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        Stat<?> stat = tile.getStat();
        state.hasStat = stat != null;
        if (stat == null) return;

        //maybe use texture renderer for this so we can use shading (not just block shade)
        boolean veryNear = state.veryNear;
        state.textProperties = computeRenderProperties(state.lightCoords, state.facing.step(), () -> veryNear);
        if (tile.needsVisualUpdate()) {
            updateAndCacheLines(tile, stat, state.textProperties);
        }
        state.lines = tile.getCachedLines();
        state.fontScale = tile.getFontScale();
        state.value = stat.format(tile.getValue());
    }

    @Override
    public void submit(StatFrameRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (!state.hasStat) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        poseStack.mulPose(RotHlpr.rot(state.facing.getOpposite()));
        double z = -7 / 16f + 0.01;
        poseStack.translate(0, 0, z);

        poseStack.pushPose();
        poseStack.translate(0, 11 / 16f, -1 / 32f + 0.001);

        TextUtil.RenderProperties textProperties = state.textProperties;
        List<FormattedCharSequence> rendererLines = state.lines;

        float scale = state.fontScale;
        poseStack.scale(scale, -scale, scale);
        int numberOfLines = rendererLines.size();
        boolean centered = ClientConfigs.CENTERED_TEXT.get();

        for (int lin = 0; lin < numberOfLines; ++lin) {
            FormattedCharSequence str = rendererLines.get(lin);
            //border offsets. always add 0.5 to center properly
            float dx = centered ? (-font.width(str) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;
            float dy = (((1f / scale) - (8 * numberOfLines)) / 2f) + 0.5f;
            collector.submitText(poseStack, dx, dy + 8 * lin, str, false, Font.DisplayMode.NORMAL,
                    textProperties.light(), textProperties.darkenedColor(), 0, 0);
        }

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 9 / 16f, -1 / 32f + 0.001);
        float valueScale = 1f / 64;
        poseStack.scale(valueScale, -valueScale, valueScale);

        String number = state.value;
        float dx = centered ? (-font.width(number) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;

        Component c = Component.literal(number).withStyle(ChatFormatting.DARK_RED);
        collector.submitText(poseStack, dx, 40, c.getVisualOrderText(), true, Font.DisplayMode.NORMAL,
                textProperties.light(), textProperties.darkenedColor(), 0, 0);

        poseStack.popPose();

        submitTopTextBottomText(state, poseStack, collector, 0.3125f);

        poseStack.popPose();
    }

    private void updateAndCacheLines(StatFrameBlockTile tile, Stat<?> stat, TextUtil.RenderProperties textProperties) {
        float paperWidth = 1 - (2 * PAPER_X_MARGIN);
        float paperHeight = 1 - (2 * PAPER_Y_MARGIN);

        MutableComponent text = StatFrameBlock.getStatComponent(stat);

        text = text.setStyle(textProperties.style());

        var p = TextUtil.fitLinesToBox(font, text, paperWidth, paperHeight);
        tile.setFontScale(p.getSecond());
        tile.setCachedPageLines(p.getFirst());
    }

    public TextUtil.RenderProperties computeRenderProperties(int combinedLight, Vector3f normal, BooleanSupplier shouldShowGlow) {
        return TextUtil.renderProperties(DyeColor.BLACK, false,
                ClientConfigs.getSignColorMult(),
                combinedLight,
                Style.EMPTY,
                normal, shouldShowGlow);
    }
}
