package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.stats.Stat;

public class StatFrameBlockTileRenderer extends BaseFrameTileRenderer<StatFrameBlockTile> {

    public StatFrameBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StatFrameBlockTile tile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int packedOverlay) {

        Stat<?> stat = tile.getStat();
        if (stat != null) {


            renderTopTextBottomText(tile, poseStack, buffer, light);
        }
    }

}