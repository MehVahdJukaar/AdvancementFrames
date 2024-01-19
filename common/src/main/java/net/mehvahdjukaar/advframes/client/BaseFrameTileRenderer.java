package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.BaseFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;

public abstract class BaseFrameTileRenderer<T extends BaseFrameBlockTile> implements BlockEntityRenderer<T> {

    protected final ItemRenderer itemRenderer;
    protected final EntityRenderDispatcher entityRenderer;
    protected final Font font;
    protected final Minecraft minecraft;
    protected final Camera camera;

    protected BaseFrameTileRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.entityRenderer = minecraft.getEntityRenderDispatcher();
        this.font = minecraft.font;
        this.camera = minecraft.gameRenderer.getMainCamera();
    }

    public void renderTopTextBottomText(LOD lod, T tile, PoseStack poseStack,
                                        MultiBufferSource buffer,
                                        int light, float offset) {
        if (Minecraft.renderNames() && lod.isVeryNear()) {
            HitResult hit = minecraft.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = tile.getBlockPos();
                BlockPos hitPos = BlockPos.containing(hit.getLocation());
                if (pos.equals(hitPos)) {

                    Component title = tile.getTitle();
                    //poseStack.mulPose(entityRenderer.cameraOrientation());
                    //float f1 = minecraft.options.getBackgroundOpacity(0.25F);
                    int opacity = 0;// (int) (f1 * 255.0F) << 24;

                    if (title != null) {


                        poseStack.pushPose();

                        float width = font.width(title);
                        float scale = 0.025f;
                        if (width > 48) {
                            scale /= width / 48;
                        }

                        poseStack.translate(0, offset + 4 * scale, 0.0125);
                        poseStack.scale(scale, -scale, scale);
                        Matrix4f matrix4f = poseStack.last().pose();

                        float dx = -width / 2f;


                        font.drawInBatch(title, dx, 0, tile.getTitleColor().getColor(),
                                true, matrix4f, buffer, Font.DisplayMode.POLYGON_OFFSET, opacity, light);
                        poseStack.popPose();

                    }

                    Component name = tile.getOwnerName();
                    if (name != null) {
                        poseStack.pushPose();

                        float width = font.width(name);
                        float scale = 0.025f;
                        if (width > 48) {
                            scale /= width / 48;
                        }

                        poseStack.translate(0, -offset + 4 * scale, 0.0125);
                        poseStack.scale(scale, -scale, scale);
                        var matrix4f = poseStack.last().pose();

                        float dx = -width / 2;

                        font.drawInBatch(name, dx, 0, -1, true, matrix4f, buffer,
                                Font.DisplayMode.POLYGON_OFFSET, opacity, light);
                        poseStack.popPose();
                    }
                }
            }
        }
    }

}
