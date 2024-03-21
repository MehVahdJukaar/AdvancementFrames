package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.moonlight.api.client.model.BakedQuadsTransformer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
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
            Matrix4f matrix = transform.getRotation().getMatrix();
            matrix.rotateY((float) Math.PI);
            matrix.translate(0, 0, -7.5f / 16f);
            BakedQuadsTransformer transformer = BakedQuadsTransformer.create()
                    .applyingTransform(matrix);
            BakedModel frame = ClientHelper.getModel(Minecraft.getInstance().getModelManager(), model);
            quads.addAll(transformer.transformAll(frame.getQuads(state, side, rand)));
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
