package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.network.CPacketLoadPattern;
import com.lw.random_additions.api.PatternMachineType;
import com.lw.random_additions.api.PatternMachineTypePacket;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CPacketLoadPattern.Handler.class, remap = false)
public abstract class MixinCPacketLoadPatternHandler {

    @Inject(method = "lambda$onMessage$0", at = @At("HEAD"))
    private static void RandomAdditions$setMachineTypeOnContainer(final EntityPlayerMP player, final CPacketLoadPattern message, final CallbackInfo ci) {
        final String machineType = PatternMachineTypeUtil.sanitize(((PatternMachineTypePacket) message).RandomAdditions$getJeiMachineType());
        final Container container = player.openContainer;
        if (container instanceof PatternMachineType) {
            final PatternMachineType patternMachineType = (PatternMachineType) container;
            if (machineType.isEmpty()) {
                patternMachineType.RandomAdditions$clearJeiMachineType();
            } else {
                patternMachineType.RandomAdditions$setJeiMachineType(machineType);
            }
        }
    }
}
