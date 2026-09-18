package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ServerBoundSetStatFramePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetStatFramePacket> CODEC = Message.makeType(
            AdvFrames.res("set_stat_frame"), ServerBoundSetStatFramePacket::new);

    private final BlockPos pos;
    public final Identifier statValue;
    public final Identifier statType;

    public ServerBoundSetStatFramePacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.statValue = buf.readIdentifier();
        this.statType = buf.readIdentifier();
    }

    public <T> ServerBoundSetStatFramePacket(BlockPos pos, StatType<T> stat, T obj) {
        this.pos = pos;
        this.statValue = stat.getRegistry().getKey(obj);
        this.statType = BuiltInRegistries.STAT_TYPE.getKey(stat);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeIdentifier(this.statValue);
        buf.writeIdentifier(this.statType);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = (ServerLevel) serverPlayer.level();
            BlockPos pos = this.pos;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof StatFrameBlockTile te) {
               var stat =  BuiltInRegistries.STAT_TYPE.getValue(statType);
               if(stat != null) {
                   te.setStat(stat, statValue, serverPlayer);
                   te.updateStatValue();
                   //updates client
                   tile.setChanged();
                   level.sendBlockUpdated(pos,tile.getBlockState(),tile.getBlockState(), 3);
               }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}