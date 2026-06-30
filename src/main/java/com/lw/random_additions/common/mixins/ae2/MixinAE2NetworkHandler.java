package com.lw.random_additions.common.mixins.ae2;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.packets.PacketValueConfig;
import com.lw.random_additions.api.PatternUploadContainer;
import com.lw.random_additions.common.network.PacketPatternUploadRequest;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = appeng.core.sync.network.NetworkHandler.class, remap = false, priority = 2000)
public abstract class MixinAE2NetworkHandler {

    @Unique
    private static final String RandomAdditions$PATTERN_TERMINAL_ENCODE = "PatternTerminal.Encode";

    @Unique
    private static long RandomAdditions$lastPatternUploadRequestAt;

    @Unique
    private static long RandomAdditions$suppressPatternEncodeUntil;

    @Inject(method = "sendToServer", at = @At("HEAD"), cancellable = true)
    private void RandomAdditions$interceptPatternUploadEncodePacket(final AppEngPacket packet, final CallbackInfo ci) {
        if (!(packet instanceof PacketValueConfig)) {
            return;
        }

        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || !(minecraft.player.openContainer instanceof PatternUploadContainer)) {
            return;
        }

        final PacketValueConfig valuePacket = (PacketValueConfig) packet;
        final String name = this.RandomAdditions$getPacketValueName(valuePacket);
        if (!RandomAdditions$PATTERN_TERMINAL_ENCODE.equals(name)) {
            return;
        }

        final long now = System.currentTimeMillis();
        final boolean rightClick = this.RandomAdditions$isRightMouseEncodeClick();
        if (!rightClick && now > RandomAdditions$suppressPatternEncodeUntil) {
            return;
        }

        if (rightClick) {
            RandomAdditions$suppressPatternEncodeUntil = now + 1500L;
            if (now - RandomAdditions$lastPatternUploadRequestAt > 500L) {
                RandomAdditions$lastPatternUploadRequestAt = now;
                com.lw.random_additions.common.network.NetworkHandler.CHANNEL.sendToServer(new PacketPatternUploadRequest());
            }
        }
        ci.cancel();
    }

    @Unique
    private boolean RandomAdditions$isRightMouseEncodeClick() {
        return Mouse.getEventButton() == 1 || Mouse.isButtonDown(1);
    }

    @Unique
    private String RandomAdditions$getPacketValueName(final PacketValueConfig packet) {
        return ((AccessorPacketValueConfig) packet).RandomAdditions$getName();
    }
}
