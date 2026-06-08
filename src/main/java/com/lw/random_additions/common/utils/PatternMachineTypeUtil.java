package com.lw.random_additions.common.utils;

import com.lw.random_additions.common.integration.jei.JeiPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class PatternMachineTypeUtil {

    public static final String NBT_KEY = "randomAdditions.jeiMachineType";

    private static final ThreadLocal<String> CURRENT_JEI_MACHINE_TYPE = new ThreadLocal<>();

    private PatternMachineTypeUtil() {
    }

    public static String machineType(final String title, final String uid) {
        String machineType = sanitize(title);
        if (machineType.isEmpty()) {
            machineType = sanitize(uid);
        }
        return machineType;
    }

    public static String read(final NBTTagCompound tag) {
        if (tag == null || !tag.hasKey(NBT_KEY, 8)) {
            return "";
        }

        return sanitize(tag.getString(NBT_KEY));
    }

    public static void write(final NBTTagCompound tag, final String machineType) {
        if (tag == null) {
            return;
        }

        final String sanitized = sanitize(machineType);
        if (sanitized.isEmpty()) {
            tag.removeTag(NBT_KEY);
        } else {
            tag.setString(NBT_KEY, sanitized);
        }
    }

    public static void setCurrentJeiMachineType(final String machineType) {
        final String sanitized = sanitize(machineType);
        if (sanitized.isEmpty()) {
            CURRENT_JEI_MACHINE_TYPE.remove();
        } else {
            CURRENT_JEI_MACHINE_TYPE.set(sanitized);
        }
    }

    public static void writeAndClearCurrentJeiMachineType(final NBTTagCompound tag) {
        try {
            write(tag, CURRENT_JEI_MACHINE_TYPE.get());
        } finally {
            CURRENT_JEI_MACHINE_TYPE.remove();
        }
    }

    public static String getCurrentJeiMachineType() {
        return sanitize(CURRENT_JEI_MACHINE_TYPE.get());
    }

    public static void clearCurrentJeiMachineType() {
        CURRENT_JEI_MACHINE_TYPE.remove();
    }

    public static void writeToItemStackTag(final NBTTagCompound stackNbt, final String machineType) {
        if (stackNbt == null) {
            return;
        }

        final String sanitized = sanitize(machineType);
        if (sanitized.isEmpty()) {
            removeFromItemStackTag(stackNbt);
            return;
        }

        final NBTTagCompound itemTag = stackNbt.hasKey("tag", 10) ? stackNbt.getCompoundTag("tag") : new NBTTagCompound();
        itemTag.setString(NBT_KEY, sanitized);
        stackNbt.setTag("tag", itemTag);
    }

    public static void writeToItemStack(final ItemStack stack, final String machineType) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        final String sanitized = sanitize(machineType);
        if (sanitized.isEmpty()) {
            return;
        }

        final NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        tag.setString(NBT_KEY, sanitized);
        stack.setTagCompound(tag);
    }

    public static String readFromItemStack(final ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        return read(stack.getTagCompound());
    }

    public static String readFromStackList(final NBTTagCompound encodedValue, final String... keys) {
        if (encodedValue == null) {
            return "";
        }

        for (final String key : keys) {
            final NBTTagList stacks = encodedValue.getTagList(key, 10);
            for (int i = 0; i < stacks.tagCount(); i++) {
                final String machineType = readFromStackNbt(stacks.getCompoundTagAt(i));
                if (!machineType.isEmpty()) {
                    return machineType;
                }
            }
        }

        return "";
    }

    public static void stripFromEncodedPattern(final NBTTagCompound encodedValue) {
        if (encodedValue == null) {
            return;
        }

        stripFromStackList(encodedValue.getTagList("in", 10));
        stripFromStackList(encodedValue.getTagList("out", 10));
        stripFromStackList(encodedValue.getTagList("Inputs", 10));
        stripFromStackList(encodedValue.getTagList("Outputs", 10));
    }

    private static void stripFromStackList(final NBTTagList stacks) {
        for (int i = 0; i < stacks.tagCount(); i++) {
            removeFromItemStackTag(stacks.getCompoundTagAt(i));
        }
    }

    private static void removeFromItemStackTag(final NBTTagCompound stackNbt) {
        if (stackNbt == null || !stackNbt.hasKey("tag", 10)) {
            return;
        }

        final NBTTagCompound itemTag = stackNbt.getCompoundTag("tag");
        itemTag.removeTag(NBT_KEY);
        if (itemTag.getKeySet().isEmpty()) {
            stackNbt.removeTag("tag");
        } else {
            stackNbt.setTag("tag", itemTag);
        }
    }

    private static String readFromStackNbt(final NBTTagCompound stackNbt) {
        if (stackNbt == null || !stackNbt.hasKey("tag", 10)) {
            return "";
        }

        return read(stackNbt.getCompoundTag("tag"));
    }

    public static String machineType(final IRecipeCategory recipeCategory) {
        final IJeiRuntime runtime = JeiPlugin.getRuntime();
        if (runtime != null) {
            final List<Object> catalysts = runtime.getRecipeRegistry().getRecipeCatalysts(recipeCategory);
            for (final Object catalyst : catalysts) {
                final String name = ingredientName(catalyst);
                if (!name.isEmpty()) {
                    return name;
                }
            }
        }

        return machineType(recipeCategory.getTitle(), recipeCategory.getUid());
    }

    public static String ingredientName(final Object ingredient) {
        if (ingredient instanceof ItemStack) {
            final ItemStack stack = (ItemStack) ingredient;
            if (!stack.isEmpty()) {
                return sanitize(stack.getDisplayName());
            }
        }

        if (ingredient instanceof FluidStack) {
            return sanitize(((FluidStack) ingredient).getLocalizedName());
        }

        final IIngredientRegistry ingredientRegistry = JeiPlugin.getIngredientRegistry();
        if (ingredientRegistry != null) {
            try {
                final IIngredientHelper<Object> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
                return sanitize(ingredientHelper.getDisplayName(ingredient));
            } catch (final RuntimeException ignored) {
            }
        }

        return "";
    }

    public static String sanitize(final String text) {
        if (text == null) {
            return "";
        }

        final String stripped = TextFormatting.getTextWithoutFormattingCodes(text);
        final String value = (stripped == null ? text : stripped).trim();
        if (value.length() <= 128) {
            return value;
        }
        return value.substring(0, 128);
    }
}
