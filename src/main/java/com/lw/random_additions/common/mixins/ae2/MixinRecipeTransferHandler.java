package com.lw.random_additions.common.mixins.ae2;

import appeng.helpers.ItemStackHelper;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import com.lw.random_additions.common.integration.jei.JeiPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
            final String machineType = RandomAdditions$getCatalystMachineName(recipeLayout.getRecipeCategory());
            PatternMachineTypeUtil.setCurrentJeiMachineType(machineType);
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

    @Unique
    private String RandomAdditions$getCatalystMachineName(final IRecipeCategory recipeCategory) {
        final IJeiRuntime runtime = JeiPlugin.getRuntime();
        if (runtime != null) {
            runtime.getRecipeRegistry();
            final List<Object> catalysts = runtime.getRecipeRegistry().getRecipeCatalysts(recipeCategory);
            for (final Object catalyst : catalysts) {
                final String name = RandomAdditions$getIngredientName(catalyst);
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }

        return PatternMachineTypeUtil.machineType(recipeCategory.getTitle(), recipeCategory.getUid());
    }

    @Unique
    private String RandomAdditions$getIngredientName(final Object ingredient) {
        if (ingredient instanceof ItemStack) {
            final ItemStack stack = (ItemStack) ingredient;
            if (!stack.isEmpty()) {
                return PatternMachineTypeUtil.sanitize(stack.getDisplayName());
            }
        }

        if (ingredient instanceof FluidStack) {
            return PatternMachineTypeUtil.sanitize(((FluidStack) ingredient).getLocalizedName());
        }

        final IIngredientRegistry ingredientRegistry = JeiPlugin.getIngredientRegistry();
        if (ingredientRegistry != null) {
            try {
                final IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
                return PatternMachineTypeUtil.sanitize(ingredientHelper.getDisplayName(ingredient));
            } catch (final RuntimeException ignored) {
            }
        }

        return "";
    }
}
