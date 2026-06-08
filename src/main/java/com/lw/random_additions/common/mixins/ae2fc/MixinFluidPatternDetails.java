package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.util.FluidPatternDetails;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = FluidPatternDetails.class, remap = false)
public abstract class MixinFluidPatternDetails {

    @ModifyArg(
            method = "writeToStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"),
            index = 0
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToPattern(final NBTTagCompound encodedValue) {
        final String machineType = PatternMachineTypeUtil.readFromStackList(encodedValue, "Outputs", "out");
        if (!machineType.isEmpty()) {
            PatternMachineTypeUtil.write(encodedValue, machineType);
        }
        PatternMachineTypeUtil.stripFromEncodedPattern(encodedValue);
        return encodedValue;
    }
}
