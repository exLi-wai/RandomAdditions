package com.lw.random_additions.common.integration.ae2.patternupload;

import appeng.api.networking.IGrid;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PatternUploadTargetFinder {

    private static final List<PatternUploadEndpointFinder> FINDERS = Collections.singletonList(new InterfaceEndpointFinder());

    private PatternUploadTargetFinder() {
    }

    public static List<PatternUploadGroup> findGroups(IGrid grid, ItemStack encodedPattern) {
        Map<String, PatternUploadGroup> groups = new LinkedHashMap<>();
        if (grid == null) {
            return new ArrayList<>();
        }

        List<PatternUploadEndpoint> endpoints = new ArrayList<>();
        for (PatternUploadEndpointFinder finder : FINDERS) {
            finder.findEndpoints(grid, encodedPattern, endpoints);
        }

        for (PatternUploadEndpoint endpoint : endpoints) {
            PatternUploadGroup group = groups.computeIfAbsent(
                    endpoint.getGroupKey().getKey(),
                    key -> new PatternUploadGroup(endpoint.getGroupKey())
            );
            group.add(endpoint);
        }

        return getPatternUploadGroups(encodedPattern, groups);
    }

    @Nonnull
    private static List<PatternUploadGroup> getPatternUploadGroups(ItemStack encodedPattern, Map<String, PatternUploadGroup> groups) {
        List<PatternUploadGroup> result = new ArrayList<>(groups.values());
        result.sort((left, right) -> {
            int freeCompare = Boolean.compare(right.getFreeCount(encodedPattern) > 0, left.getFreeCount(encodedPattern) > 0);
            if (freeCompare != 0) {
                return freeCompare;
            }
            int free = Integer.compare(right.getFreeCount(encodedPattern), left.getFreeCount(encodedPattern));
            if (free != 0) {
                return free;
            }
            return left.getName().compareToIgnoreCase(right.getName());
        });
        return result;
    }
}
