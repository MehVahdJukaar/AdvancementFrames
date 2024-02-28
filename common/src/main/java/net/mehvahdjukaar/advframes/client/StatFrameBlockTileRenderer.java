package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.stats.Stat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BooleanSupplier;

public class StatFrameBlockTileRenderer extends BaseFrameTileRenderer<StatFrameBlockTile> {

    private static final float PAPER_Y_MARGIN = 6.5f / 16f;
    private static final float PAPER_X_MARGIN = 0.125f;

    public StatFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StatFrameBlockTile tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int packedOverlay) {

        Stat<?> stat = tile.getStat();
        if (stat != null) {

            Direction dir = tile.getBlockState().getValue(StatFrameBlock.FACING);
            float yaw = -dir.toYRot();
            Vec3 cameraPos = camera.getPosition();
            BlockPos pos = tile.getBlockPos();
            //TODO: Fix
            // if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, 15 / 16f)) return;
            LOD lod = new LOD(cameraPos, pos);

            poseStack.pushPose();

            poseStack.translate(0.5, 0.5, 0.5);

            poseStack.mulPose(RotHlpr.rot(tile.getBlockState().getValue(AdvancementFrameBlock.FACING).getOpposite()));
            double z = -7 / 16f + 0.01;
            poseStack.translate(0, 0, z);

            poseStack.pushPose();
            poseStack.translate(0, 11 / 16f, -1 / 32f + 0.001);
            //maybe use texture renderer for this so we can use shading (not just block shade)
            var textProperties = computeRenderProperties(light, dir.step(), lod::isVeryNear);

            if (tile.needsVisualUpdate()) {
                updateAndCacheLines(tile, stat, textProperties);
            }

            List<FormattedCharSequence> rendererLines = tile.getCachedLines();

            float scale = tile.getFontScale();
            poseStack.scale(scale, -scale, scale);
            int numberOfLines = rendererLines.size();
            boolean centered = ClientConfigs.CENTERED_TEXT.get();


            for (int lin = 0; lin < numberOfLines; ++lin) {
                FormattedCharSequence str = rendererLines.get(lin);
                //border offsets. always add 0.5 to center properly
                float dx = centered ? (-font.width(str) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;
                float dy = (((1f / scale) - (8 * numberOfLines)) / 2f) + 0.5f;
                Matrix4f pose = poseStack.last().pose();
                font.drawInBatch(str, dx, dy + 8 * lin, textProperties.darkenedColor(), false,
                        pose, buffer, Font.DisplayMode.NORMAL, 0, textProperties.light());
            }


            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, 9 / 16f, -1 / 32f + 0.001);
            float valueScale = 1f / 64;
            poseStack.scale(valueScale, -valueScale, valueScale);

            String number = stat.format(tile.getValue());
            float dx = centered ? (-font.width(number) / 2f) + 0.5f : -(0.5f - PAPER_X_MARGIN) / scale;

            Component c = Component.literal(number).withStyle(ChatFormatting.DARK_RED);
            font.drawInBatch(c, dx, 40, textProperties.darkenedColor(), true,
                    poseStack.last().pose(), buffer,
                    Font.DisplayMode.NORMAL, 0, textProperties.light());

            poseStack.popPose();


            renderTopTextBottomText(lod, tile, poseStack, buffer, light, 0.3125f);

            poseStack.popPose();
        }
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