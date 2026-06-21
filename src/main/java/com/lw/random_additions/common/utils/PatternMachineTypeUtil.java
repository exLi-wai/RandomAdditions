package com.lw.random_additions.common.utils;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.lw.random_additions.common.integration.jei.JeiPlugin;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.nbt.NBTBase;
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
            stripFromStackTag(stacks.getCompoundTagAt(i));
        }
    }

    public static void stripFromStackTag(final NBTTagCompound stackNbt) {
        if (stackNbt == null) {
            return;
        }

        removeMachineTypeRecursive(stackNbt);
        removeEmptyCompound(stackNbt, "tag");
        removeEmptyCompound(stackNbt, "Tag");
    }

    public static void stripFromAeItemStacks(final IAEItemStack[] stacks) {
        if (stacks == null) {
            return;
        }

        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = stripFromAeItemStack(stacks[i]);
        }
    }

    private static IAEItemStack stripFromAeItemStack(final IAEItemStack stack) {
        if (stack == null) {
            return null;
        }

        final NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        stripFromStackTag(tag);

        final IAEItemStack clean = AEItemStack.fromNBT(tag);
        return clean == null ? stack : clean;
    }

    private static void removeMachineTypeRecursive(final NBTTagCompound tag) {
        tag.removeTag(NBT_KEY);

        for (final String key : tag.getKeySet().toArray(new String[0])) {
            final NBTBase value = tag.getTag(key);
            if (value instanceof NBTTagCompound) {
                removeMachineTypeRecursive((NBTTagCompound) value);
            } else if (value instanceof NBTTagList) {
                removeMachineTypeFromList((NBTTagList) value);
            }
        }
    }

    private static void removeMachineTypeFromList(final NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTBase value = list.get(i);
            if (value instanceof NBTTagCompound) {
                removeMachineTypeRecursive((NBTTagCompound) value);
            } else if (value instanceof NBTTagList) {
                removeMachineTypeFromList((NBTTagList) value);
            }
        }
    }

    private static void removeEmptyCompound(final NBTTagCompound parent, final String key) {
        if (parent.hasKey(key, 10) && parent.getCompoundTag(key).getKeySet().isEmpty()) {
            parent.removeTag(key);
        }
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
