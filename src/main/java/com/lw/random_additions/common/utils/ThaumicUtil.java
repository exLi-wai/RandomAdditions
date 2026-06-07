package com.lw.random_additions.common.utils;

import com.lw.random_additions.common.config.RandomAdditionsConfig;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ThaumicUtil {
    public static boolean crucibleWhitelist(ItemStack item) {
        for (String itemId : RandomAdditionsConfig.THAUMCRAFT.CrucibleInsolubleWhitelist) {
            if (Objects.requireNonNull(item.getItem().getRegistryName()).toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean TileSmelterWhitelist(ItemStack item) {
        for (String itemId : RandomAdditionsConfig.THAUMCRAFT.SmelterInsolubleWhitelist) {
            if (Objects.requireNonNull(item.getItem().getRegistryName()).toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }
}
