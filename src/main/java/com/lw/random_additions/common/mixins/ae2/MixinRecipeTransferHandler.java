package com.lw.random_additions.common.mixins.ae2;

import appeng.helpers.ItemStackHelper;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.integration.modules.jei.RecipeTransferHandler", remap = false)
public abstract class MixinRecipeTransferHandler {

    @Inject(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/ItemStackHelper;stackToNBT(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NBTTagCompound;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void RandomAdditions$captureJeiMachineType(final Container container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer, final CallbackInfoReturnable<IRecipeTransferError> cir) {
        if (recipeLayout != null && recipeLayout.getRecipeCategory() != null) {
            PatternMachineTypeUtil.setCurrentJeiMachineType(PatternMachineTypeUtil.machineType(recipeLayout.getRecipeCategory()));
        }
    }

    @Redirect(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/helpers/ItemStackHelper;stackToNBT(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NBTTagCompound;",
                    ordinal = 0
            )
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToOutput(final ItemStack output) {
        final NBTTagCompound tag = ItemStackHelper.stackToNBT(output);
        PatternMachineTypeUtil.writeToItemStackTag(tag, PatternMachineTypeUtil.getCurrentJeiMachineType());
        return tag;
    }

    @Inject(method = "transferRecipe", at = @At("RETURN"))
    private void RandomAdditions$clearCapturedMachineType(final Container container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer, final CallbackInfoReturnable<IRecipeTransferError> cir) {
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
    }
}
