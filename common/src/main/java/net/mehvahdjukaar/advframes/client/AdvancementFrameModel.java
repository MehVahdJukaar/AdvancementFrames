package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancementFrameModel implements CustomBakedModel {

    private final BakedModel model;
    private final ModelState transform;

    public AdvancementFrameModel(BakedModel bakedModel, ModelState state) {
        this.model = bakedModel;
        this.transform = state;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side,
                                         RandomSource rand, RenderType renderType,
                                         ExtraModelData data) {

        List<BakedQuad> quads = new ArrayList<>();
        quads.addAll(model.getQuads(state, side, rand));
        Direction back = state.getValue(AdvancementFrameBlock.FACING).getOpposite();
        var model = state.getValue(AdvancementFrameBlock.TYPE).getModel();
        if (model != null) {
            BakedModel frame = ClientHelper.getModel(Minecraft.getInstance().getModelManager(), model);
            for (var q : frame.getQuads(state, side, rand)) {
                int[] v = Arrays.copyOf(q.getVertices(), q.getVertices().length);
                Matrix4f matrix = transform.getRotation().getMatrix();
                matrix.rotateY((float) Math.PI);
                matrix.translate(0, 0, -7.5f / 16f);
                VertexUtil.transformVertices(v, matrix);
                Direction dir = Direction.rotate(matrix, q.getDirection());
                if (dir != back) quads.add(new BakedQuad(v, q.getTintIndex(), dir, q.getSprite(), q.isShade()));
            }
        }
        return quads;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData data) {
        return model.getParticleIcon();
    }


    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
}
