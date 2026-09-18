package net.mehvahdjukaar.advframes.client;

import com.mojang.math.Transformation;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AdvancementFrameModel implements CustomBlockModel {

    private final BlockStateModel frame;

    public AdvancementFrameModel(BlockStateModel frame) {
        this.frame = frame;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        emitter.emitAll(this.frame, level, pos, state, random);
        if (state == null) return;

        Identifier iconId = state.getValue(AdvancementFrameBlock.TYPE).getModel();
        BlockStateModel icon = iconId == null ? null : ClientHelper.getStandaloneModel(iconId);
        if (icon == null) return;

        Matrix4f matrix = new Matrix4f()
                .rotation(RotHlpr.rot(state.getValue(AdvancementFrameBlock.FACING)))
                .rotate(RotHlpr.Y180)
                .translate(0, 0, -7.5f / 16f);
        emitter.transform(new Transformation(matrix));
        emitter.emitAll(CustomBlockModel.collectQuads(icon, level, pos, state, random));
        emitter.transform((Matrix4f) null);
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.frame.particleMaterial().sprite();
    }
}
