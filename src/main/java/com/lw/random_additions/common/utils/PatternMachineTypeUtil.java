package com.lw.random_additions.common.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

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
