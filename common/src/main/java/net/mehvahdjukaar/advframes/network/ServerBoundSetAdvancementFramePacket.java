package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ServerBoundSetAdvancementFramePacket implements Message {
    private final BlockPos pos;
    public final ResourceLocation advancementId;

    public ServerBoundSetAdvancementFramePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.advancementId = buf.readResourceLocation();
    }

    public ServerBoundSetAdvancementFramePacket(BlockPos pos, Advancement advancement) {
        this.pos = pos;
        this.advancementId = advancement.getId();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.advancementId);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        if (context.getSender() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockPos pos = this.pos;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AdvancementFrameBlockTile te) {
                Advancement advancement = level.getServer().getAdvancements().getAdvancement(this.advancementId);
                if (advancement != null) {
                    te.setAdvancement(advancement, serverPlayer);
                    //updates client
                    BlockState state = level.getBlockState(pos);
                    level.sendBlockUpdated(pos, state, state, 3);
                    tile.setChanged();
                }
            }
        }
    }
}