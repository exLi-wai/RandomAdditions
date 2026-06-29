package com.lw.random_additions.common.integration.ae2.patternupload;

import net.minecraft.item.ItemStack;

public class PatternUploadGroupKey {

    private final String key;
    private final String displayName;
    private final ItemStack staticIcon;
    private final String searchText;

    public PatternUploadGroupKey(String key, String displayName, ItemStack staticIcon, String searchText) {
        this.key = key == null || key.isEmpty() ? "unknown" : key;
        this.displayName = displayName == null || displayName.isEmpty() ? "ME Interface" : displayName;
        this.staticIcon = sanitizeIcon(staticIcon);
        this.searchText = searchText == null || searchText.isEmpty() ? this.displayName : searchText;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getStaticIcon() {
        return staticIcon.copy();
    }

    public String getSearchText() {
        return searchText;
    }

    public static ItemStack sanitizeIcon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        copy.setTagCompound(null);
        return copy;
    }
}
