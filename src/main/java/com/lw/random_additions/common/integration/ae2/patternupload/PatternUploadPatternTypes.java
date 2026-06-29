package com.lw.random_additions.common.integration.ae2.patternupload;

import appeng.helpers.IInterfaceHost;
import net.minecraft.item.ItemStack;

public final class PatternUploadPatternTypes {

    private PatternUploadPatternTypes() {
    }

    public static boolean hostSupports(IInterfaceHost host, ItemStack encodedPattern) {
        if (encodedPattern == null || encodedPattern.isEmpty()) {
            return false;
        }
        String hostClass = host.getClass().getName().toLowerCase();
        String dualityClass = host.getInterfaceDuality().getClass().getName().toLowerCase();
        boolean mixedHost = containsMixedMarker(hostClass) || containsMixedMarker(dualityClass);
        boolean fluidHost = hostClass.contains("fluid") || dualityClass.contains("fluid");
        if (isFluidPattern(encodedPattern)) {
            return fluidHost || mixedHost;
        }
        return !fluidHost || mixedHost;
    }

    public static boolean isFluidPattern(ItemStack encodedPattern) {
        if (encodedPattern == null || encodedPattern.isEmpty()) {
            return false;
        }
        String itemClass = encodedPattern.getItem().getClass().getName().toLowerCase();
        String registryName = encodedPattern.getItem().getRegistryName() == null
                ? ""
                : encodedPattern.getItem().getRegistryName().toString().toLowerCase();
        return itemClass.contains("fluid")
                || itemClass.contains("largeencodedpattern")
                || registryName.contains("fluid")
                || registryName.contains("large_encoded_pattern");
    }

    private static boolean containsMixedMarker(String value) {
        return value.contains("dual") || value.contains("trio");
    }
}
