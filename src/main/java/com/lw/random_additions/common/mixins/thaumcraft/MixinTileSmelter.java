package com.lw.random_additions.common.mixins.thaumcraft;

import com.lw.random_additions.common.config.RandomAdditionsConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.tiles.essentia.TileSmelter;

@Mixin(TileSmelter.class)
public class MixinTileSmelter {
    @Inject(
            method = "canSmelt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCanSmelt(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = ((TileSmelter)(Object)this).getStackInSlot(0);

        if (!stack.isEmpty() && RandomAdditionsConfig.TileSmelterWhitelist(stack)) {
            cir.setReturnValue(false);
        }
    }
}
