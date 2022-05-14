package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.mehvahdjukaar.advframes.init.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class AdvancementFrameBlockTile extends BlockEntity {

    private String advancementId = null;
    private DisplayInfo advancement;
    private GameProfile owner;

    public AdvancementFrameBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.ADVANCEMENT_FRAME_TILE.get(), pos, state);
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
            if (title instanceof TranslatableComponent translatableComponent) {
                tag.putString("Title", translatableComponent.getKey());
            } else {
                tag.putString("Title", title.getString());
            }
            Component description = advancement.getDescription();
            if (description instanceof TranslatableComponent translatableComponent) {
                tag.putString("Description", translatableComponent.getKey());
            } else {
                tag.putString("Description", description.getString());
            }
            tag.put("Icon", advancement.getIcon().save(new CompoundTag()));
            tag.putInt("FrameType", advancement.getFrame().ordinal());
            cmp.put("Advancement", tag);
        }
        if (this.owner != null) {
            cmp.putUUID("PlayerID", owner.getId());
        }
    }

    @Override
    public void load(CompoundTag cmp) {
        super.load(cmp);
        this.advancementId = null;
        if (cmp.contains("PlayerID")) {
            UUID id = cmp.getUUID("PlayerID");
            this.setOwner(new GameProfile(id, null));
        }
        if (cmp.contains("Advancement")) {
            CompoundTag tag = cmp.getCompound("Advancement");
            if (cmp.contains("ID")) {
                this.advancementId = tag.getString("ID");
            }
            TranslatableComponent title = new TranslatableComponent(tag.getString("Title"));
            TranslatableComponent description = new TranslatableComponent(tag.getString("Description"));
            ItemStack icon = ItemStack.of(tag.getCompound("Icon"));
            FrameType type = FrameType.values()[tag.getInt("FrameType")];
            this.advancement = new DisplayInfo(icon, title, description, null, type, false, true, true);
        }
    }

    public ChatFormatting getColor() {
        var v = this.getAdvancement().getFrame();
        if (v == FrameType.GOAL) {
            return ChatFormatting.AQUA;
        }
        return v.getChatColor();
    }

    public DisplayInfo getAdvancement() {
        return advancement;
    }

    public GameProfile getOwner() {
        return owner;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (this.advancement != null) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }


    public void setOwner(@Nullable GameProfile input) {
        if (this.owner == null) {
            if (input == null || !(input.isComplete())) {
                synchronized (this) {
                    this.owner = input;
                }

                updateGameProfile(this.owner, (gameProfile) -> this.owner = gameProfile);
            }
        }
    }

    private static GameProfileCache profileCache;
    @Nullable
    private static MinecraftSessionService sessionService;
    @Nullable
    private static Executor mainThreadExecutor;

    public static void updateGameProfile(@Nullable GameProfile gameProfile, Consumer<GameProfile> consumer) {
        if (gameProfile != null && gameProfile.getId() != null && (!gameProfile.isComplete() || gameProfile.getName() == null) && profileCache != null && sessionService != null) {
            Optional<GameProfile> profile = profileCache.get(gameProfile.getId());
            Util.backgroundExecutor().execute(() -> Util.ifElse(profile, (p) -> {
                if (p.getName() == null) {
                    p = sessionService.fillProfileProperties(p, true);
                }
                GameProfile finalGp = p;
                mainThreadExecutor.execute(() -> {
                    profileCache.add(finalGp);
                    consumer.accept(finalGp);
                });
            }, () -> mainThreadExecutor.execute(() -> consumer.accept(gameProfile))));
        } else {
            consumer.accept(gameProfile);
        }
    }

    public static void setup(GameProfileCache p_196701_, MinecraftSessionService p_196702_, Executor p_196703_) {
        profileCache = p_196701_;
        sessionService = p_196702_;
        mainThreadExecutor = p_196703_;
    }

    public static void clear() {
        profileCache = null;
        sessionService = null;
        mainThreadExecutor = null;
    }

}
