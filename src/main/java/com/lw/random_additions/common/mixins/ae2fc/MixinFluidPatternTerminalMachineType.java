package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.lw.random_additions.api.PatternMachineType;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        ContainerFluidPatternTerminal.class,
        ContainerExtendedFluidPatternTerminal.class,
        ContainerWirelessFluidPatternTerminal.class
}, remap = false)
public abstract class MixinFluidPatternTerminalMachineType {

    @Inject(method = "encodeFluidPattern", at = @At("HEAD"))
    private void RandomAdditions$setMachineTypeForEncoding(final CallbackInfo ci) {
        final PatternMachineType patternMachineType = (PatternMachineType) this;
        if (patternMachineType.RandomAdditions$hasFreshJeiMachineType()) {
            PatternMachineTypeUtil.setCurrentJeiMachineType(patternMachineType.RandomAdditions$getJeiMachineType());
        } else {
            PatternMachineTypeUtil.clearCurrentJeiMachineType();
        }
    }

    @Inject(method = "encodeFluidPattern", at = @At("RETURN"))
    private void RandomAdditions$clearMachineTypeAfterEncoding(final CallbackInfo ci) {
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
        ((PatternMachineType) this).RandomAdditions$clearJeiMachineType();
    }
}
