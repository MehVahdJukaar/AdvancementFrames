package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

public class AdvancementFrameBlockTileRenderer<T extends AdvancementFrameBlockTile> implements BlockEntityRenderer<T> {

    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher entityRenderer;
    private final Font font;
    private final Minecraft minecraft;

    public AdvancementFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.itemRenderer = minecraft.getItemRenderer();
        this.entityRenderer = minecraft.getEntityRenderDispatcher();
        this.font = minecraft.font;
    }

    @Override
    public boolean shouldRender(T t, Vec3 vec3) {
        return BlockEntityRenderer.super.shouldRender(t, vec3);
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {

        DisplayInfo advancement = tile.getAdvancement();
        if (advancement != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            poseStack.mulPose(tile.getBlockState().getValue(AdvancementFrameBlock.FACING).getRotation());
            poseStack.mulPose(RotHlpr.XN90);
            poseStack.translate(0, 0, -7 / 16f + 0.01);

            poseStack.pushPose();

            ResourceLocation r = switch (advancement.getFrame()) {
                case GOAL -> AdvFramesClient.GOAL_MODEL;
                case TASK -> AdvFramesClient.TASK_MODEL;
                case CHALLENGE -> AdvFramesClient.CHALLENGE_MODEL;
            };

            poseStack.pushPose();
            poseStack.translate(0, 0, -0.041f);
            BakedModel frame = ClientHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), r);
            itemRenderer.render(Items.DIAMOND.getDefaultInstance(),
                    ItemDisplayContext.GUI, false, poseStack, buffer, light, overlay, frame);

            poseStack.popPose();

            ItemStack stack = advancement.getIcon();


            ResourceLocation tex;
            //if it doesnt have fancy nbt we can use default optimized item renderer. useful since it can
            ItemStack def = stack.getItem().getDefaultInstance();
            if (false && ItemStack.isSameItemSameTags(def, stack)) {
                tex = RenderedTexturesManager.requestFlatItemTexture(stack.getItem(), 64).getTextureLocation();
            } else {
                //always renders animated cause its cooler
                int i = Objects.hash(stack.getTag(), stack.getItem());
                FrameBufferBackedDynamicTexture  tt = RenderedTexturesManager.requestFlatItemStackTexture(AdvFrames.res("" + i), stack, 64);
                tt.download();
                tt.getPixels().setPixelRGBA(2,2, 0xff00ffff);
                tt.upload();
                tex = tt.getTextureLocation();
            }

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(tex));

            Matrix4f tr = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            float z = 0f;
            float s = 0.25f;
            //poseStack.translate(0.5, 0.5, 0);
            vertexConsumer.vertex(tr, -s, s, z).color(1f, 1f, 1f, 1f).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, -s, -s, z).color(1f, 1f, 1f, 1f).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();

            vertexConsumer.vertex(tr, s, -s, z).color(1f, 1f, 1f, 1f).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();
            vertexConsumer.vertex(tr, s, s, z).color(1f, 1f, 1f, 1f).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(normal, 0f, 0f, 1f).endVertex();


            poseStack.popPose();

            if (Minecraft.renderNames()) {
                HitResult hit = minecraft.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = tile.getBlockPos();
                    BlockPos hitPos = BlockPos.containing (hit.getLocation());
                    if (pos.equals(hitPos)) {
                        double d0 = entityRenderer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        if (d0 < 16 * 16) {
                            //poseStack.mulPose(entityRenderer.cameraOrientation());
                            //float f1 = minecraft.options.getBackgroundOpacity(0.25F);
                            int opacity = 0;// (int) (f1 * 255.0F) << 24;

                            poseStack.pushPose();

                            Component component = advancement.getTitle();

                            float width = font.width(component);
                            float scale = 0.025f;
                            if (width > 48) {
                                scale /= width / 48;
                            }


                            poseStack.translate(0, 0.375 + 4 * scale, 0.0125);
                            poseStack.scale(scale, -scale, scale);
                            Matrix4f matrix4f = poseStack.last().pose();

                            float dx = -width / 2f;


                            font.drawInBatch(component, dx, 0, tile.getColor().getColor(),
                                    true, matrix4f, buffer, Font.DisplayMode.POLYGON_OFFSET, opacity, light);
                            poseStack.popPose();

                            String name = tile.getOwner().getName();

                            if (name != null && !name.isEmpty()) {
                                component = Component.literal(name);
                                poseStack.pushPose();

                                width = font.width(component);
                                scale = 0.025f;
                                if (width > 48) {
                                    scale /= width / 48;
                                }

                                poseStack.translate(0, -0.375 + 4 * scale, 0.0125);
                                poseStack.scale(scale, -scale, scale);
                                matrix4f = poseStack.last().pose();

                                dx = -width / 2;

                                font.drawInBatch(component, dx, 0, -1, true, matrix4f, buffer,
                                        Font.DisplayMode.POLYGON_OFFSET, opacity, light);
                                poseStack.popPose();
                            }
                        }
                    }
                }
            }

            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
