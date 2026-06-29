package com.lw.random_additions.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPatternUploadRequest implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketPatternUploadRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketPatternUploadRequest message, MessageContext ctx) {
            PatternUploadPacketHandlers.scheduleContainerAction(ctx,
                    (player, container) -> container.RandomAdditions$requestPatternUpload(player));
            return null;
        }
    }
}
