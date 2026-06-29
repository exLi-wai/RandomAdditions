package com.lw.random_additions.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPatternUploadSelect implements IMessage {

    private int index;

    public PacketPatternUploadSelect() {
    }

    public PacketPatternUploadSelect(int index) {
        this.index = index;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.index);
    }

    public static class Handler implements IMessageHandler<PacketPatternUploadSelect, IMessage> {

        @Override
        public IMessage onMessage(PacketPatternUploadSelect message, MessageContext ctx) {
            PatternUploadPacketHandlers.scheduleContainerAction(ctx,
                    (player, container) -> container.RandomAdditions$sendPatternToTarget(player, message.index));
            return null;
        }
    }
}
