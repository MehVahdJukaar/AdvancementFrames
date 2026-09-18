package net.mehvahdjukaar.advframes.client;

import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

        Matrix4f matrix = new Matrix4f(getFacingRotation(state.getValue(AdvancementFrameBlock.FACING)).getMatrix());
        matrix.rotateY((float) Math.PI);
        matrix.translate(0, 0, -7.5f / 16f);
        emitter.transform(new Transformation(matrix));
        emitter.emitAll(icon, level, pos, state, random);
        emitter.transform((Matrix4f) null);
    }

    private static Transformation getFacingRotation(Direction facing) {
        var group = switch (facing) {
            case NORTH -> Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R0);
            case EAST -> Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R90);
            case SOUTH -> Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R180);
            case WEST -> Quadrant.fromXYAngles(Quadrant.R0, Quadrant.R270);
            case UP -> Quadrant.fromXYAngles(Quadrant.R270, Quadrant.R180);
            case DOWN -> Quadrant.fromXYAngles(Quadrant.R90, Quadrant.R180);
        };
        return BlockModelRotation.get(group).transformation();
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.frame.particleMaterial().sprite();
    }
}
