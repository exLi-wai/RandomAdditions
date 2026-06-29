package com.lw.random_additions.common.integration.ae2.patternupload;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatternUploadGroup {

    private final PatternUploadGroupKey key;
    private final List<PatternUploadEndpoint> endpoints = new ArrayList<>();

    public PatternUploadGroup(PatternUploadGroupKey key) {
        this.key = key;
    }

    public void add(PatternUploadEndpoint endpoint) {
        this.endpoints.add(endpoint);
    }

    public String getKey() {
        return key.getKey();
    }

    public String getName() {
        int count = endpoints.size();
        return count > 1 ? key.getDisplayName() + " (" + count + ")" : key.getDisplayName();
    }

    public ItemStack getIcon() {
        return key.getStaticIcon();
    }

    public int getTotalCount() {
        return endpoints.size();
    }

    public int getFreeCount(ItemStack encodedPattern) {
        return this.getInsertableEndpoints(encodedPattern).size();
    }

    public boolean supports(ItemStack encodedPattern) {
        for (PatternUploadEndpoint endpoint : endpoints) {
            if (endpoint.supportsPatternType(encodedPattern)) {
                return true;
            }
        }
        return false;
    }

    public boolean insert(ItemStack encodedPattern) {
        for (PatternUploadEndpoint endpoint : this.getInsertableEndpoints(encodedPattern)) {
            if (endpoint.insert(encodedPattern)) {
                return true;
            }
        }
        return false;
    }

    public List<PatternUploadEndpoint> getInsertableEndpoints(ItemStack encodedPattern) {
        List<PatternUploadEndpoint> insertable = new ArrayList<>();
        for (PatternUploadEndpoint endpoint : endpoints) {
            if (endpoint.canAccept(encodedPattern)) {
                insertable.add(endpoint);
            }
        }
        return insertable;
    }

    public PatternUploadTargetInfo toInfo(ItemStack encodedPattern) {
        int free = getFreeCount(encodedPattern);
        String search = (getName() + " " + key.getKey() + " " + key.getSearchText()).toLowerCase(Locale.ROOT);
        return new PatternUploadTargetInfo(getName(), getIcon(), free == 0, free, getTotalCount(), search);
    }
}
