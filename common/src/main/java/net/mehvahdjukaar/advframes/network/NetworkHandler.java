package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class NetworkHandler {
    public static ChannelHandler CHANNEL = ChannelHandler.builder(AdvFrames.MOD_ID)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetAdvancementFramePacket.class,
                    ServerBoundSetAdvancementFramePacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetStatFramePacket.class,
                    ServerBoundSetStatFramePacket::new)
            .register(NetworkDir.PLAY_TO_CLIENT, ClientBoundSendStatsPacket.class,
                    ClientBoundSendStatsPacket::new)
            .register(NetworkDir.PLAY_TO_SERVER, ServerBoundRequestStatsPacket.class,
                    ServerBoundRequestStatsPacket::new)
            .build();


    public static void init() {
    }
}