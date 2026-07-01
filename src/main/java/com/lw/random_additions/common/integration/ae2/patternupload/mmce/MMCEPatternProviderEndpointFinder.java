package com.lw.random_additions.common.integration.ae2.patternupload.mmce;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadEndpoint;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadEndpointFinder;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MMCEPatternProviderEndpointFinder implements PatternUploadEndpointFinder {

    @Override
    public void findEndpoints(final IGrid grid, final ItemStack encodedPattern, final List<PatternUploadEndpoint> endpoints) {
        if (grid == null) {
            return;
        }

        for (final IGridNode node : grid.getMachines(MEPatternProvider.class)) {
            if (node == null || !node.isActive() || !(node.getMachine() instanceof MEPatternProvider)) {
                continue;
            }
            final MEPatternProvider provider = (MEPatternProvider) node.getMachine();
            final MMCEPatternProviderEndpoint endpoint = new MMCEPatternProviderEndpoint(provider);
            if (endpoint.supportsPatternType(encodedPattern)) {
                endpoints.add(endpoint);
            }
        }
    }
}
