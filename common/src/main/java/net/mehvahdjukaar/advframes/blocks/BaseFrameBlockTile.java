package net.mehvahdjukaar.advframes.blocks;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class BaseFrameBlockTile extends BlockEntity {

    protected GameProfile owner;

    protected BaseFrameBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag cmp) {
        super.saveAdditional(cmp);
        if (this.owner != null) {
            cmp.putUUID("PlayerID", owner.getId());
        }
    }

    @Override
    public void load(CompoundTag cmp) {
        super.load(cmp);
        if (cmp.contains("PlayerID")) {
            UUID id = cmp.getUUID("PlayerID");
            this.setOwner(new GameProfile(id, null));
        }
    }

    public GameProfile getOwner() {
        return owner;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
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

    // Copied from skull block tile. Needed otherwise stuff doesnt work properly. forgot why
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

    public static void setup(GameProfileCache gameProfileCache, MinecraftSessionService minecraftSessionService, Executor executor) {
        profileCache = gameProfileCache;
        sessionService = minecraftSessionService;
        mainThreadExecutor = executor;
    }

    public static void clear() {
        profileCache = null;
        sessionService = null;
        mainThreadExecutor = null;
    }

    @Nullable
    public Component getOwnerName() {
        if (owner != null) {
            String name = owner.getName();
            if (name == null) return null;
            return Component.literal(name);
        }
        return null;
    }

    @Nullable
    public abstract Component getTitle();

    public abstract ChatFormatting getTitleColor();

    public abstract boolean isEmpty();

}
