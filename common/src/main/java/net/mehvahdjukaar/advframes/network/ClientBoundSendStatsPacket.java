package net.mehvahdjukaar.advframes.network;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.player.Player;

import java.util.Iterator;
import java.util.Map;

public class ClientBoundSendStatsPacket implements Message {
    private final Object2IntMap<Stat<?>> stats;

    public ClientBoundSendStatsPacket(Object2IntMap<Stat<?>> stats) {
        this.stats = stats;
    }

    public ClientBoundSendStatsPacket(FriendlyByteBuf buf) {
        this.stats = buf.readMap(Object2IntOpenHashMap::new, (friendlyByteBuf2) -> {
            StatType<?> statType = friendlyByteBuf2.readById(BuiltInRegistries.STAT_TYPE);
            return ServerBoundRequestStatsPacket.readStatCap(buf, statType);
        }, FriendlyByteBuf::readVarInt);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeMap(this.stats, ServerBoundRequestStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        AdvFramesClient.updatePlayerStats(this.stats);
    }


}
