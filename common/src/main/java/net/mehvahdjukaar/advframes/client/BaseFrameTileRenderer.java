package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.blocks.BaseFrameBlockTile;
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


    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;
    private final Font font;
    private final Minecraft minecraft;

    public BaseFrameTileRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.entityRenderer = minecraft.getEntityRenderDispatcher();
        this.font = minecraft.font;
    }

    public void renderTopTextBottomText(T tile, PoseStack poseStack, MultiBufferSource buffer,
                                         int light) {
        if (Minecraft.renderNames()) {
            HitResult hit = minecraft.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = tile.getBlockPos();
                BlockPos hitPos = BlockPos.containing(hit.getLocation());
                if (pos.equals(hitPos)) {
                    double d0 = entityRenderer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (d0 < 16 * 16) {

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

                            poseStack.translate(0, 0.375 + 4 * scale, 0.0125);
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

                            poseStack.translate(0, -0.375 + 4 * scale, 0.0125);
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

}
