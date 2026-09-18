package net.mehvahdjukaar.advframes.blocks;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    public <T> void setStat(StatType<T> stat, Identifier objId, ServerPlayer player) {
        this.stat = stat.get(Objects.requireNonNull(stat.getRegistry().getValue(objId)));
        this.setOwner(ResolvableProfile.createResolved(player.getGameProfile()));
    }

    @Nullable
    public Stat<?> getStat() {
        return stat;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.stat != null) {
            output.store("Stat", Identifier.CODEC, BuiltInRegistries.STAT_TYPE.getKey(stat.getType()));
            output.store("StatKey", Identifier.CODEC, getStatKey(stat));
            output.putInt("Value", value);
        }
    }

    private static <T> Identifier getStatKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.stat = null;
        var statType = input.read("Stat", Identifier.CODEC);
        var statValue = input.read("StatKey", Identifier.CODEC);
        if (statType.isPresent() && statValue.isPresent()) {
            var type = BuiltInRegistries.STAT_TYPE.getValue(statType.get());
            this.stat = getInstance(statValue.get(), type);
            this.value = input.getIntOr("Value", 0);
        }
    }

    @Nullable
    private <T> Stat<T> getInstance(Identifier id, StatType<T> type) {
        if (type == null) return null;
        T value = type.getRegistry().getValue(id);
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
        UUID ownerId = this.getOwnerId();
        if (this.stat != null && ownerId != null) {
            var player = level.getPlayerByUUID(ownerId);
            if (player instanceof ServerPlayer serverPlayer) {
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
