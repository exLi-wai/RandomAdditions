package com.lw.random_additions.common.mixins.ae2fc;

import appeng.api.storage.data.IAEItemStack;
import com.glodblock.github.util.FluidPatternDetails;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidPatternDetails.class, remap = false)
public abstract class MixinFluidPatternDetails {

    @Shadow
    private IAEItemStack[] outputs;

    @Shadow
    private IAEItemStack[] outputsCond;

    @ModifyArg(
            method = "writeToStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"),
            index = 0
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToPattern(final NBTTagCompound encodedValue) {
        PatternMachineTypeUtil.write(encodedValue, PatternMachineTypeUtil.getCurrentJeiMachineType());
        PatternMachineTypeUtil.stripFromEncodedPattern(encodedValue);
        return encodedValue;
    }

    @Inject(method = "setOutputs", at = @At("RETURN"))
    private void RandomAdditions$stripMachineTypeFromOutputs(final IAEItemStack[] outputs, final CallbackInfoReturnable<Boolean> cir) {
        PatternMachineTypeUtil.stripFromAeItemStacks(this.outputs);
        PatternMachineTypeUtil.stripFromAeItemStacks(this.outputsCond);
    }
}
