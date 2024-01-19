package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancementFrameBlockTile extends BaseFrameBlockTile {

    private String advancementId = null;
    private DisplayInfo advancement;

    public AdvancementFrameBlockTile(BlockPos pos, BlockState state) {
        super(AdvFrames.ADVANCEMENT_FRAME_TILE.get(), pos, state);
    }

    public void setAdvancement(Advancement advancement, ServerPlayer player) {
        this.advancement = advancement.getDisplay();
        this.advancementId = advancement.getId().toString();
        this.setOwner(new GameProfile(player.getUUID(), null));
    }


    @Override
    protected void saveAdditional(CompoundTag cmp) {
        super.saveAdditional(cmp);
        if (this.advancement != null) {
            if (this.level instanceof ServerLevel server && this.owner != null && this.advancementId != null && !this.advancementId.isEmpty()) {
                Advancement a = server.getServer().getAdvancements().getAdvancement(new ResourceLocation(this.advancementId));
                Player player = this.level.getPlayerByUUID(this.owner.getId());
                if (a == null || (player instanceof ServerPlayer sp && !sp.getAdvancements().getOrStartProgress(a).isDone())) {
                    return;
                }
            }

            CompoundTag tag = new CompoundTag();
            if (this.advancementId != null) {
                cmp.putString("ID", this.advancementId);
            }
            Component title = advancement.getTitle();
            if (title instanceof MutableComponent mc && mc.getContents() instanceof TranslatableContents tc) {
                tag.putString("Title", tc.getKey());
            } else {
                tag.putString("Title", title.getString());
            }
            Component description = advancement.getDescription();
            if (description instanceof MutableComponent mc && mc.getContents() instanceof TranslatableContents tc) {
                tag.putString("Description", tc.getKey());
            } else {
                tag.putString("Description", description.getString());
            }
            tag.put("Icon", advancement.getIcon().save(new CompoundTag()));
            tag.putInt("FrameType", advancement.getFrame().ordinal());
            cmp.put("Advancement", tag);
        }
    }

    @Override
    public void load(CompoundTag cmp) {
        super.load(cmp);
        this.advancementId = null;
        if (cmp.contains("Advancement")) {
            CompoundTag tag = cmp.getCompound("Advancement");
            if (cmp.contains("ID")) {
                this.advancementId = tag.getString("ID");
            }
            Component title = Component.translatable(tag.getString("Title"));
            Component description = Component.translatable(tag.getString("Description"));
            ItemStack icon = ItemStack.of(tag.getCompound("Icon"));
            FrameType type = FrameType.values()[tag.getInt("FrameType")];
            this.advancement = new DisplayInfo(icon, title, description, null, type, false, true, true);
        }
        //remove
        if (level != null) {
            var t = AdvancementFrameBlock.Type.get(advancement);
            if (getBlockState().getValue(AdvancementFrameBlock.TYPE) != t) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AdvancementFrameBlock.TYPE, t));
            }
        }

    }

    @Override
    public ChatFormatting getTitleColor() {
        var v = this.getAdvancement().getFrame();
        if (v == FrameType.GOAL) {
            return ChatFormatting.AQUA;
        }
        return v.getChatColor();
    }

    @Override
    public boolean isEmpty() {
        return advancement != null;
    }

    public DisplayInfo getAdvancement() {
        return advancement;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.advancement != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Nullable
    @Override
    public Component getTitle() {
        if (advancement != null) return advancement.getTitle();
        return null;
    }

}
