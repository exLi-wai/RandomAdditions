package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.lw.random_additions.api.PatternMachineType;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerFluidPatternEncoder.class, remap = false)
public abstract class MixinFluidPatternEncoderMachineType implements PatternMachineType {

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Unique
    private long RandomAdditions$jeiMachineTypeSetAt = 0L;

    @Override
    public void RandomAdditions$setJeiMachineType(final String machineType) {
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.sanitize(machineType);
        this.RandomAdditions$jeiMachineTypeSetAt = this.RandomAdditions$jeiMachineType.isEmpty() ? 0L : System.currentTimeMillis();
    }

    @Override
    public String RandomAdditions$getJeiMachineType() {
        return this.RandomAdditions$jeiMachineType;
    }

    @Override
    public boolean RandomAdditions$hasFreshJeiMachineType() {
        return !this.RandomAdditions$jeiMachineType.isEmpty()
                && this.RandomAdditions$jeiMachineTypeSetAt > 0L
                && System.currentTimeMillis() - this.RandomAdditions$jeiMachineTypeSetAt <= 30000L;
    }

    @Override
    public void RandomAdditions$clearJeiMachineType() {
        this.RandomAdditions$jeiMachineType = "";
        this.RandomAdditions$jeiMachineTypeSetAt = 0L;
    }

    @Inject(method = "encodePattern", at = @At("HEAD"))
    private void RandomAdditions$setMachineTypeForEncoding(final CallbackInfo ci) {
        if (this.RandomAdditions$hasFreshJeiMachineType()) {
            PatternMachineTypeUtil.setCurrentJeiMachineType(this.RandomAdditions$jeiMachineType);
        } else {
            PatternMachineTypeUtil.clearCurrentJeiMachineType();
        }
    }

    @Inject(method = "encodePattern", at = @At("RETURN"))
    private void RandomAdditions$clearMachineTypeAfterEncoding(final CallbackInfo ci) {
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
        this.RandomAdditions$clearJeiMachineType();
    }
}
