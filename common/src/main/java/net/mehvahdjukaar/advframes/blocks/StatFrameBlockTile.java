package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
        this.setOwner(new ResolvableProfile(player.getGameProfile()));
    }

    @Nullable
    public Stat<?> getStat() {
        return stat;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.stat != null) {
            tag.putString("Stat", BuiltInRegistries.STAT_TYPE.getKey(stat.getType()).toString());
            tag.putString("StatKey", getStatKey(stat).toString());
            tag.putInt("Value", value);
        }
    }

    private static <T> ResourceLocation getStatKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.stat = null;
        if (tag.contains("Stat") && tag.contains("StatKey")) {
            var statValue = ResourceLocation.tryParse(tag.getString("StatKey"));
            var type = BuiltInRegistries.STAT_TYPE.get(ResourceLocation.tryParse(tag.getString("Stat")));
            this.stat = getInstance(statValue, type);
            this.value = tag.getInt("Value");
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
        if ((level.getGameTime() + 1) % (5 * 20) == 0) {
            tile.updateStatValue();
        }
    }

    public void updateStatValue() {
        var owner = this.getOwner();
        if (this.stat != null && owner != null && owner.id().isPresent()) {
            var player = level.getPlayerByUUID(owner.id().get());
            if (player instanceof ServerPlayer serverPlayer && isWithinUpdateRange(serverPlayer)) {
                var stats = serverPlayer.getStats();
                int newValue = stats.getValue(this.stat);
                if(newValue != this.value){
                    this.value = newValue;
                    this.setChanged();
                    BlockState state = getBlockState();
                    level.setBlockAndUpdate(worldPosition, state.setValue(StatFrameBlock.TRIGGERED, true));
                    level.scheduleTick(worldPosition, state.getBlock(), 2);
                }
            }
        }
    }


    private boolean isWithinUpdateRange(ServerPlayer player) {
        int range = CommonConfigs.STAT_UPDATE_RANGE.get();
        if (range >= CommonConfigs.INFINITE_STAT_RANGE) return true;
        BlockPos playerPos = player.blockPosition();
        int dx = Math.abs(SectionPos.blockToSectionCoord(playerPos.getX()) - SectionPos.blockToSectionCoord(worldPosition.getX()));
        int dz = Math.abs(SectionPos.blockToSectionCoord(playerPos.getZ()) - SectionPos.blockToSectionCoord(worldPosition.getZ()));
        return range > 0 && Math.max(dx, dz) <= range;
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
