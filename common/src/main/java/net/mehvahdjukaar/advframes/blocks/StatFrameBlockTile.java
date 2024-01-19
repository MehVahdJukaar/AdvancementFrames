package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StatFrameBlockTile extends BaseFrameBlockTile {

    @Nullable
    private Stat<?> stat = null;
    private int value = 0;

    //Client stuff
    private float fontScale = 1;
    private List<FormattedCharSequence> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean needsVisualRefresh = true;

    public StatFrameBlockTile(BlockPos pos, BlockState state) {
        super(AdvFrames.STAT_FRAME_TILE.get(), pos, state);
    }

    public <T> void setStat(StatType<T> stat, ResourceLocation objId, ServerPlayer player) {
        this.stat = stat.get(Objects.requireNonNull(stat.getRegistry().get(objId)));
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
            cmp.putString("StatKey", getStatKey(stat).toString());
            cmp.putInt("Value", value);
        }
    }

    private static <T> ResourceLocation getStatKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    @Override
    public void load(CompoundTag cmp) {
        super.load(cmp);
        this.stat = null;
        if (cmp.contains("Stat") && cmp.contains("StatKey")) {
            var statValue = new ResourceLocation(cmp.getString("StatKey"));
            var type = BuiltInRegistries.STAT_TYPE.get(new ResourceLocation(cmp.getString("Stat")));
            this.stat = getInstance(statValue, type);
            this.value = cmp.getInt("Value");
        }
    }

    @Nullable
    private <T> Stat<T> getInstance(ResourceLocation id, StatType<T> type) {
        if (type == null) return null;
        T value = type.getRegistry().get(id);
        if (value == null) return null;
        return type.get(value);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Component getTitle() {
        return null;
    }

    @Override
    public ChatFormatting getTitleColor() {
        return ChatFormatting.WHITE;
    }

    @Override
    public boolean isEmpty() {
        return stat != null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.stat != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StatFrameBlockTile tile) {
        if ((level.getGameTime() + 1) % (15 * 20) == 0) {
            var owner = tile.getOwner();
            if (tile.stat != null && owner != null) {
                var player = level.getPlayerByUUID(owner.getId());
                if (player instanceof ServerPlayer serverPlayer) {
                    var stats = serverPlayer.getStats();
                    int newValue = stats.getValue(tile.stat);
                    if(newValue != tile.value){
                        tile.value = newValue;
                        tile.setChanged();
                        level.setBlockAndUpdate(pos, state.setValue(StatFrameBlock.TRIGGERED, true));
                        level.scheduleTick(pos, state.getBlock(), 2);
                    }
                }
            }
        }
    }


    public boolean needsVisualUpdate() {
        if (this.needsVisualRefresh) {
            this.needsVisualRefresh = false;
            return true;
        }
        return false;
    }


    public float getFontScale() {
        return this.fontScale;
    }

    public void setFontScale(float s) {
        this.fontScale = s;
    }

    public void setCachedPageLines(List<FormattedCharSequence> l) {
        this.cachedPageLines = l;
    }

    public List<FormattedCharSequence> getCachedLines() {
        return this.cachedPageLines;
    }

    public int getValue() {
        return value;
    }
}
