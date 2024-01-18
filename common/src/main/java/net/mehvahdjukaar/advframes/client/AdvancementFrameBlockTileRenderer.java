package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.FrameBufferBackedDynamicTexture;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;

import java.util.Objects;

public class AdvancementFrameBlockTileRenderer extends BaseFrameTileRenderer<AdvancementFrameBlockTile> {


    public AdvancementFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AdvancementFrameBlockTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        DisplayInfo advancement = tile.getAdvancement();
        if (advancement != null) {

            ItemStack stack = advancement.getIcon();

            ResourceLocation tex;
            //if it doesnt have fancy nbt we can use default optimized item renderer. useful since it can
            if (!ClientConfigs.ANIMATED_ICONS.get() && ItemStack.isSameItemSameTags(stack.getItem().getDefaultInstance(), stack)) {
                FrameBufferBackedDynamicTexture tt = RenderedTexturesManager.requestFlatItemTexture(stack.getItem(), 64);
                tex = tt.getTextureLocation();
                if (!tt.isInitialized()) return;
            } else {
                //always renders animated cause its cooler
                int i = Objects.hash(stack.getTag(), stack.getItem());
                FrameBufferBackedDynamicTexture tt = RenderedTexturesManager.requestFlatItemStackTexture(AdvFrames.res("" + i), stack, 64);
                tex = tt.getTextureLocation();
                if (!tt.isInitialized()) return;
            }

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            poseStack.mulPose(RotHlpr.rot(tile.getBlockState().getValue(AdvancementFrameBlock.FACING).getOpposite()));
            double z = -7 / 16f + 0.01;
            poseStack.translate(0, 0, z);

            poseStack.pushPose();

            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(tex));

            float s = 0.25f;
            int lu = light & '\uffff';
            int lv = light >> 16 & '\uffff';
            poseStack.scale(1, -1, -1);
            VertexUtil.addQuad(vertexConsumer, poseStack, -s, -s, s, s, lu, lv);

            poseStack.popPose();


            renderTopTextBottomText(tile, poseStack, buffer, light);

            poseStack.popPose();
        }
    }


}
