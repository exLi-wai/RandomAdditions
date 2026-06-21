package com.lw.random_additions.common.mixins.ae2;

import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.PatternHelper;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternHelper.class, remap = false)
public abstract class MixinPatternHelper {

    @Mutable
    @Final
    @Shadow
    private IAEItemStack[] outputs;

    @Mutable
    @Final
    @Shadow
    private IAEItemStack[] condensedOutputs;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void RandomAdditions$stripMachineTypeFromOutputs(final ItemStack is, final World w, final CallbackInfo ci) {
        PatternMachineTypeUtil.stripFromAeItemStacks(this.outputs);
        PatternMachineTypeUtil.stripFromAeItemStacks(this.condensedOutputs);
    }
}
