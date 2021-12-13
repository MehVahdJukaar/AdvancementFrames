package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetAdvancementFramePacket {
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

    public static void buffer(ServerBoundSetAdvancementFramePacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeResourceLocation(message.advancementId);
    }

    public static void handler(ServerBoundSetAdvancementFramePacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
        ctx.get().enqueueWork(() -> {
            ServerLevel level = (ServerLevel) player.level;
            BlockPos pos = message.pos;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AdvancementFrameBlockTile te) {
                Advancement advancement = level.getServer().getAdvancements().getAdvancement(message.advancementId);
                if(advancement != null){
                    te.setAdvancement(advancement, player);
                    //updates client
                    BlockState state = level.getBlockState(pos);
                    level.sendBlockUpdated(pos, state, state, 3);
                    tile.setChanged();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}