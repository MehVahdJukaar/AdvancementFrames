package net.mehvahdjukaar.advframes.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class BaseFrameBlockTile extends BlockEntity {

    protected ResolvableProfile owner;

    protected BaseFrameBlockTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("profile", ResolvableProfile.CODEC, this.owner);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        //TODO: remove
        input.read("PlayerID", UUIDUtil.CODEC).ifPresent(id -> this.setOwner(ResolvableProfile.createUnresolved(id)));
        input.read("profile", ResolvableProfile.CODEC).ifPresent(this::setOwner);
    }

    public ResolvableProfile getOwner() {
        return owner;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void setOwner(@Nullable ResolvableProfile owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Nullable
    public UUID getOwnerId() {
        return owner == null ? null : owner.partialProfile().id();
    }

    @Nullable
    public Component getOwnerName() {
        if (owner != null) {
            return owner.name().map(Component::literal).orElse(null);
        }
        return null;
    }

    @Nullable
    public abstract Component getTitle();

    public abstract ChatFormatting getTitleColor();

    public abstract boolean isEmpty();

}
