package com.lw.random_additions.common.integration.ae2.patternupload;

import appeng.api.networking.IGrid;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface PatternUploadEndpointFinder {

    void findEndpoints(IGrid grid, ItemStack encodedPattern, List<PatternUploadEndpoint> endpoints);
}
