package net.mehvahdjukaar.advframes.blocks;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class AdvancementFrameBlockTile extends BaseFrameBlockTile {

    @Nullable
    private Identifier advancementId = null;
    @Nullable
    private DisplayInfo advancementDisplay = null;

    public AdvancementFrameBlockTile(BlockPos pos, BlockState state) {
        super(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), pos, state);
    }

    public void setAdvancement(AdvancementHolder advancement, ServerPlayer player) {
        this.advancementDisplay = advancement.value().display().orElse(null);
        this.advancementId = advancement.id();
        this.setOwner(ResolvableProfile.createResolved(player.getGameProfile()));
    }


    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.advancementDisplay != null) {
            if (this.level instanceof ServerLevel server && this.owner != null && this.advancementId != null) {
                AdvancementHolder advancement = server.getServer().getAdvancements().get(this.advancementId);
                Player player = this.level.getPlayerByUUID(this.getOwnerId());
                if (advancement == null || (player instanceof ServerPlayer sp && !sp.getAdvancements()
                        .getOrStartProgress(advancement).isDone())) {
                    return;
                }
            }
            output.store("Advancement", DisplayInfo.CODEC, this.advancementDisplay);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.advancementId = null;
        input.read("Advancement", DisplayInfo.CODEC).ifPresent(a -> this.advancementDisplay = a);
        //remove
        if (level != null) {
            var t = AdvancementFrameBlock.Type.get(advancementDisplay);
            if (getBlockState().getValue(AdvancementFrameBlock.TYPE) != t) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AdvancementFrameBlock.TYPE, t));
            }
        }

    }

    @Override
    public ChatFormatting getTitleColor() {
        var v = this.getAdvancement().getType();
        if (v == AdvancementType.GOAL) {
            return ChatFormatting.AQUA;
        }
        return v.getChatColor();
    }

    @Override
    public boolean isEmpty() {
        return advancementDisplay != null;
    }

    @Nullable
    public DisplayInfo getAdvancement() {
        return advancementDisplay;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.advancementDisplay != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Nullable
    @Override
    public Component getTitle() {
        if (advancementDisplay != null) return advancementDisplay.getTitle();
        return null;
    }

}
