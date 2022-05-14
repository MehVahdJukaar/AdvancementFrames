package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.init.ClientSetup;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

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
    public boolean shouldRender(T p_173568_, Vec3 p_173569_) {
        return BlockEntityRenderer.super.shouldRender(p_173568_, p_173569_);
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {

        DisplayInfo advancement = tile.getAdvancement();
        if (advancement != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            poseStack.mulPose(tile.getBlockState().getValue(AdvancementFrameBlock.FACING).getRotation());
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));
            poseStack.translate(0, 0, -7 / 16f + 0.01);

            poseStack.pushPose();

            ResourceLocation r = switch (advancement.getFrame()) {
                case GOAL -> ClientSetup.GOAL_MODEL;
                case TASK -> ClientSetup.TASK_MODEL;
                case CHALLENGE -> ClientSetup.CHALLENGE_MODEL;
            };

            poseStack.pushPose();
            poseStack.translate(0, 0, -0.041f);
            BakedModel frame = itemRenderer.getItemModelShaper().getModelManager().getModel(r);
            itemRenderer.render(Items.DIAMOND.getDefaultInstance(),
                    ItemTransforms.TransformType.GUI, false, poseStack, buffer, light, overlay, frame);

            poseStack.popPose();
            ItemStack stack = advancement.getIcon();


            //itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GUI, light, overlay, poseStack, buffer, 0);
            BakedModel itemModel = itemRenderer.getModel(stack, null, null, 0);
            poseStack.scale(0.5f, 0.5f, 0.5f);

            if (!itemModel.isGui3d()) {

                poseStack.translate(0, 0, -0.049f);

                itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, light, overlay, itemModel);
            } else {
                poseStack.scale(1F, 1F, 0.0001f);
                Direction.Axis axis = tile.getBlockState().getValue(AdvancementFrameBlock.FACING).getAxis();

                if (axis == Direction.Axis.Y) {
                    itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, light, overlay, itemModel);
                } else {

                    //--------------

                    GraphicsStatus cache = minecraft.options.graphicsMode;
                    minecraft.options.graphicsMode = GraphicsStatus.FANCY;


                    poseStack.pushPose();


                    Consumer<MultiBufferSource> finish = (MultiBufferSource buf) -> {
                        if (buf instanceof MultiBufferSource.BufferSource)
                            ((MultiBufferSource.BufferSource) buf).endBatch();
                    };

                    try {

                        finish.accept(buffer);

                        float b = axis == Direction.Axis.X ? 0.7f : 0.8f;
                        b *= 1.1f;
                        poseStack.last().normal().set(1, -1, 1);
                        int sky = (int) (b * LightTexture.sky(light));
                        int block = LightTexture.block(light);
                        int l = LightTexture.pack(block, sky);
                        itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, l, overlay, itemModel);

                        finish.accept(buffer);
                    } catch (Exception ignored) {
                    }

                    poseStack.popPose();


                    minecraft.options.graphicsMode = cache;

                    //poseStack.popPose();
                    //Lighting.setupLevel(poseStack.last().pose());
                    //poseStack.pushPose();

                    //--------------
                }
            }


            poseStack.popPose();

            if (Minecraft.renderNames()) {
                HitResult hit = minecraft.hitResult;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = tile.getBlockPos();
                    BlockPos hitPos = new BlockPos(hit.getLocation());
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
                            poseStack.scale(scale, -scale, -scale);
                            Matrix4f matrix4f = poseStack.last().pose();

                            float dx = -width / 2f;


                            font.drawInBatch(component, dx, 0, tile.getColor().getColor(), true, matrix4f, buffer, false, opacity, light);
                            poseStack.popPose();

                            String name = tile.getOwner().getName();

                            if (name != null && !name.isEmpty()) {
                                component = new TextComponent(name);
                                poseStack.pushPose();

                                width = font.width(component);
                                scale = 0.025f;
                                if (width > 48) {
                                    scale /= width / 48;
                                }

                                poseStack.translate(0, -0.375 + 4 * scale, 0.0125);
                                poseStack.scale(scale, -scale, -scale);
                                matrix4f = poseStack.last().pose();

                                dx = -width / 2;

                                font.drawInBatch(component, dx, 0, -1, true, matrix4f, buffer, false, opacity, light);
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
