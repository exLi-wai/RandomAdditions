package com.lw.random_additions.common.mixins.ae2;

import appeng.core.sync.packets.PacketValueConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PacketValueConfig.class, remap = false)
public interface AccessorPacketValueConfig {

    @Accessor("Name")
    String RandomAdditions$getName();
}
