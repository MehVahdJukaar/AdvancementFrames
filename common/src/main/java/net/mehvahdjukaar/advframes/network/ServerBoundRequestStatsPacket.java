package net.mehvahdjukaar.advframes.network;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

import java.util.HashSet;
import java.util.Set;

public class ServerBoundRequestStatsPacket implements Message {
    private final Set<Stat<?>> stats;

    public ServerBoundRequestStatsPacket(Set<Stat<?>> stats) {
        this.stats = new HashSet<>(stats);
    }

    public ServerBoundRequestStatsPacket(FriendlyByteBuf buf) {
        this.stats = buf.readCollection(HashSet::new, (b) -> {
            StatType<?> statType = b.readById(BuiltInRegistries.STAT_TYPE);
            return readStatCap(buf, statType);
        });
    }

    public static <T> Stat<T> readStatCap(FriendlyByteBuf buffer, StatType<T> statType) {
        return statType.get(buffer.readById(statType.getRegistry()));
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeCollection(this.stats, ServerBoundRequestStatsPacket::writeStatCap);
    }

    public static <T> void writeStatCap(FriendlyByteBuf buffer, Stat<T> stat) {
        buffer.writeId(BuiltInRegistries.STAT_TYPE, stat.getType());
        buffer.writeId(stat.getType().getRegistry(), stat.getValue());
    }

    @Override
    public void handle(ChannelHandler.Context context) {

        if (context.getSender() instanceof ServerPlayer serverPlayer) {
            ServerStatsCounter counter = serverPlayer.getStats();
            Object2IntMap<Stat<?>> object2IntMap = new Object2IntOpenHashMap<>();

            Set<Stat<?>> dirty = counter.dirty;
            stats.retainAll(dirty);
            for (Stat<?> stat : stats) {
                object2IntMap.put(stat, counter.getValue(stat));
            }
            dirty.removeAll(stats);

            NetworkHandler.CHANNEL.sendToClientPlayer(serverPlayer, new ClientBoundSendStatsPacket(object2IntMap));
        }
    }
}
