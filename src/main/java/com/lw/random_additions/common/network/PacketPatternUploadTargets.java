package com.lw.random_additions.common.network;

import com.lw.random_additions.RandomAdditions;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetInfo;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketPatternUploadTargets implements IMessage {

    private final List<PatternUploadTargetInfo> targets = new ArrayList<>();

    public PacketPatternUploadTargets() {
    }

    public PacketPatternUploadTargets(List<PatternUploadTargetInfo> targets) {
        this.targets.addAll(targets);
    }

    public List<PatternUploadTargetInfo> getTargets() {
        return new ArrayList<>(this.targets);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.targets.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.targets.add(PatternUploadTargetInfo.readFrom(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.targets.size());
        for (PatternUploadTargetInfo target : this.targets) {
            target.writeTo(buf);
        }
    }

    public static class Handler implements IMessageHandler<PacketPatternUploadTargets, IMessage> {

        @Override
        public IMessage onMessage(PacketPatternUploadTargets message, MessageContext ctx) {
            RandomAdditions.proxy.handlePatternUploadTargets(message.getTargets());
            return null;
        }
    }
}
