package com.lw.random_additions.common.mixins.ae2;

import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
        if (recipeLayout != null && recipeLayout.getRecipeCategory() != null) {
            PatternMachineTypeUtil.setCurrentJeiMachineType(PatternMachineTypeUtil.machineType(recipeLayout.getRecipeCategory()));
        }
    }

    @ModifyArg(
            method = "transferRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/sync/packets/PacketJEIRecipe;<init>(Lnet/minecraft/nbt/NBTTagCompound;)V"
            ),
            index = 0
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToRecipe(final NBTTagCompound recipe) {
        PatternMachineTypeUtil.write(recipe, PatternMachineTypeUtil.getCurrentJeiMachineType());
        return recipe;
    }

    @Inject(method = "transferRecipe", at = @At("RETURN"))
    private void RandomAdditions$clearCapturedMachineType(final Container container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer, final CallbackInfoReturnable<IRecipeTransferError> cir) {
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
    }
}
