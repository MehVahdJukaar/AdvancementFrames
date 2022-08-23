package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;

public class NetworkHandler {
    public static ChannelHandler CHANNEL;

    public static void registerMessages() {
        CHANNEL = ChannelHandler.createChannel(AdvFrames.res("network"));

        CHANNEL.register(NetworkDir.PLAY_TO_SERVER, ServerBoundSetAdvancementFramePacket.class,
                ServerBoundSetAdvancementFramePacket::new);
    }
}