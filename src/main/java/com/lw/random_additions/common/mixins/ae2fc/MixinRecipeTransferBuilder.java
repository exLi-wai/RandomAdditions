package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.integration.jei.RecipeTransferBuilder;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipeTransferBuilder.class, remap = false)
public abstract class MixinRecipeTransferBuilder {

    @Unique
    private String RandomAdditions$machineType = "";

    @Inject(method = "<init>", at = @At("RETURN"))
    private void RandomAdditions$captureMachineType(final IRecipeLayout recipe, final CallbackInfo ci) {
        if (recipe != null && recipe.getRecipeCategory() != null) {
            this.RandomAdditions$machineType = PatternMachineTypeUtil.machineType(recipe.getRecipeCategory());
        }
    }

    @Inject(method = "getOutput", at = @At("RETURN"))
    private void RandomAdditions$writeMachineTypeToOutputs(final CallbackInfoReturnable<List<ItemStack>> cir) {
        if (this.RandomAdditions$machineType.isEmpty() || cir.getReturnValue() == null) {
            return;
        }

        for (final ItemStack output : cir.getReturnValue()) {
            PatternMachineTypeUtil.writeToItemStack(output, this.RandomAdditions$machineType);
        }
    }
}
