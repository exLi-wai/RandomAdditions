package com.lw.random_additions.common.network;

import com.lw.random_additions.api.PatternUploadContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PatternUploadPacketHandlers {

    public static void scheduleContainerAction(final MessageContext ctx, final ContainerAction action) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (player.openContainer instanceof PatternUploadContainer) {
                action.accept(player, (PatternUploadContainer) player.openContainer);
            }
        });
    }

    public interface ContainerAction {

        void accept(EntityPlayerMP player, PatternUploadContainer container);
    }
}
