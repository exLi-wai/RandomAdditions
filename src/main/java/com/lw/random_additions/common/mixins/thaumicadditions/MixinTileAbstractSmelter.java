package com.lw.random_additions.common.mixins.thaumicadditions;

import com.lw.random_additions.common.utils.ThaumicUtil;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.thaumicadditions.tiles.TileAbstractSmelter;

@Mixin(TileAbstractSmelter.class)
public class MixinTileAbstractSmelter {
    @Inject(
            method = "canSmelt",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onCanSmelt(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = ((TileAbstractSmelter)(Object)this).getStackInSlot(0);

        if (!stack.isEmpty() && ThaumicUtil.TileSmelterWhitelist(stack)) {
            cir.setReturnValue(false);
        }
    }

}
