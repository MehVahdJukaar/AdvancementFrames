package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

public class StatFrameBlockTile extends BaseFrameBlockTile {

    @Nullable
    private Stat<?> stat = null;
    private ResourceLocation statValue = null;

    public StatFrameBlockTile(BlockPos pos, BlockState state) {
        super(AdvFrames.STAT_FRAME_TILE.get(), pos, state);
    }

    public<T> void setStat(StatType<T> stat, ResourceLocation objId, ServerPlayer player) {
        this.stat = stat.get(Objects.requireNonNull(stat.getRegistry().get(objId)));
        this.statValue = objId;
        this.setOwner(new GameProfile(player.getUUID(), null));
    }

    @Nullable
    public Stat<?> getStat() {
        return stat;
    }

    @Override
    protected void saveAdditional(CompoundTag cmp) {
        super.saveAdditional(cmp);
        if (this.stat != null) {
            cmp.putString("Stat", BuiltInRegistries.STAT_TYPE.getKey(stat.getType()).toString());
            cmp.putString("StatKey", statValue.toString());
        }
    }

    @Override
    public void load(CompoundTag cmp) {
        super.load(cmp);
        this.stat = null;
        if (cmp.contains("Stat") && cmp.contains("StatKey")) {
            this.statValue = new ResourceLocation(cmp.getString("StatKey"));
            this.stat = (Stat<?>) BuiltInRegistries.STAT_TYPE.get(new ResourceLocation(cmp.getString("Stat")))
                    .getRegistry().get(statValue);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.stat != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }
}
