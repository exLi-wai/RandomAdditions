package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.integration.jei.RecipeTransferBuilder;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import mezz.jei.api.gui.IRecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeTransferBuilder.class, remap = false)
public abstract class MixinRecipeTransferBuilder {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void RandomAdditions$captureMachineType(final IRecipeLayout recipe, final CallbackInfo ci) {
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
        if (recipe != null && recipe.getRecipeCategory() != null) {
            PatternMachineTypeUtil.setCurrentJeiMachineType(PatternMachineTypeUtil.machineType(recipe.getRecipeCategory()));
        }
    }
}
