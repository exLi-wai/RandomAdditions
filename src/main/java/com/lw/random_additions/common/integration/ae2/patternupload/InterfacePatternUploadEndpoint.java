package com.lw.random_additions.common.integration.ae2.patternupload;

import appeng.api.config.Upgrades;
import appeng.helpers.IInterfaceHost;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class InterfacePatternUploadEndpoint implements PatternUploadEndpoint {

    private final IInterfaceHost host;
    private final PatternUploadGroupKey groupKey;

    public InterfacePatternUploadEndpoint(IInterfaceHost host, PatternUploadGroupKey groupKey) {
        this.host = host;
        this.groupKey = groupKey;
    }

    @Override
    public PatternUploadGroupKey getGroupKey() {
        return groupKey;
    }

    @Override
    public boolean supportsPatternType(ItemStack encodedPattern) {
        return !encodedPattern.isEmpty() && PatternUploadPatternTypes.hostSupports(host, encodedPattern);
    }

    @Override
    public boolean canAccept(ItemStack encodedPattern) {
        if (!supportsPatternType(encodedPattern)) {
            return false;
        }
        IItemHandler patterns = host.getInterfaceDuality().getPatterns();
        return insertIntoUnlockedSlot(patterns, encodedPattern, true).isEmpty();
    }

    @Override
    public boolean insert(ItemStack encodedPattern) {
        if (!canAccept(encodedPattern)) {
            return false;
        }
        IItemHandler patterns = host.getInterfaceDuality().getPatterns();
        ItemStack remainder = insertIntoUnlockedSlot(patterns, encodedPattern, false);
        if (!remainder.isEmpty()) {
            return false;
        }
        host.saveChanges();
        return true;
    }

    private ItemStack insertIntoUnlockedSlot(IItemHandler patterns, ItemStack encodedPattern, boolean simulate) {
        ItemStack remainder = encodedPattern.copy();
        int unlockedSlots = getUnlockedPatternSlots(patterns);
        for (int slot = 0; slot < unlockedSlots; slot++) {
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!patterns.isItemValid(slot, remainder)) {
                continue;
            }
            remainder = patterns.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    private int getUnlockedPatternSlots(IItemHandler patterns) {
        int expansionUpgrades = host.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION);
        return Math.min(patterns.getSlots(), 9 + expansionUpgrades * 9);
    }
}
