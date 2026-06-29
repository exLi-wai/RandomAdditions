package com.lw.random_additions.common.integration.ae2.patternupload;

import net.minecraft.item.ItemStack;

public interface PatternUploadEndpoint {

    PatternUploadGroupKey getGroupKey();

    boolean supportsPatternType(ItemStack encodedPattern);

    boolean canAccept(ItemStack encodedPattern);

    boolean insert(ItemStack encodedPattern);
}
