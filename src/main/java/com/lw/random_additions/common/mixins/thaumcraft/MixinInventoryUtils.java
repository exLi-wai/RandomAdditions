package com.lw.random_additions.common.mixins.thaumcraft;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.ToolCore;
import thaumcraft.common.lib.utils.InventoryUtils;

@Mixin(InventoryUtils.class)
public class MixinInventoryUtils {
    
    @Inject(
            method = "checkEnchantedPlaceholder",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void checkEnchantedPlaceholder(ItemStack stack, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty() && stack2 != null && !stack2.isEmpty()) {
            if (stack.getItem() instanceof ToolCore || stack2.getItem() instanceof ToolCore) {
                cir.setReturnValue(false);
            }
        }
    }
}
