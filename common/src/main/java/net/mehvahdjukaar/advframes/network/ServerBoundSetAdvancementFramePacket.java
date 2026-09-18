package net.mehvahdjukaar.advframes.network;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlock;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ServerBoundSetAdvancementFramePacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSetAdvancementFramePacket> CODEC =
            Message.makeType(AdvFrames.res("set_advancement_frame"), ServerBoundSetAdvancementFramePacket::new);

    private final BlockPos pos;
    public final Identifier advancementId;

    public ServerBoundSetAdvancementFramePacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.advancementId = buf.readIdentifier();
    }

    public ServerBoundSetAdvancementFramePacket(BlockPos pos, AdvancementHolder advancement) {
        this.pos = pos;
        this.advancementId = advancement.id();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeIdentifier(this.advancementId);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.level();
            BlockPos pos = this.pos;
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AdvancementFrameBlockTile te) {
                AdvancementHolder advancement = level.getServer().getAdvancements().get(this.advancementId);
                if (advancement != null) {
                    te.setAdvancement(advancement, serverPlayer);
                    //updates client
                    level.setBlockAndUpdate(pos, te.getBlockState().setValue(AdvancementFrameBlock.TYPE,
                            AdvancementFrameBlock.Type.get(advancement.value().display().orElse(null))));
                    tile.setChanged();
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}