package com.lw.random_additions.common.integration.ae2.patternupload.mmce;

import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadEndpoint;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadGroupKey;
import github.kasuminova.mmce.common.tile.MEPatternMirrorImage;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class MMCEPatternProviderEndpoint implements PatternUploadEndpoint {

    private final MEPatternProvider provider;
    private final PatternUploadGroupKey groupKey;

    public MMCEPatternProviderEndpoint(final MEPatternProvider provider) {
        this.provider = provider;
        this.groupKey = createGroupKey(provider);
    }

    @Override
    public PatternUploadGroupKey getGroupKey() {
        return this.groupKey;
    }

    @Override
    public boolean supportsPatternType(final ItemStack encodedPattern) {
        if (encodedPattern == null || encodedPattern.isEmpty()) {
            return false;
        }
        final IItemHandler patterns = this.provider.getPatterns();
        for (int slot = 0; slot < patterns.getSlots(); slot++) {
            if (patterns.isItemValid(slot, encodedPattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canAccept(final ItemStack encodedPattern) {
        return this.supportsPatternType(encodedPattern) && canInsertIntoPatterns(encodedPattern, true);
    }

    @Override
    public boolean insert(final ItemStack encodedPattern) {
        if (!this.canAccept(encodedPattern)) {
            return false;
        }
        final boolean inserted = canInsertIntoPatterns(encodedPattern, false);
        if (inserted) {
            this.provider.saveChanges();
            this.provider.notifyNeighbors();
        }
        return inserted;
    }

    private boolean canInsertIntoPatterns(final ItemStack encodedPattern, final boolean simulate) {
        final IItemHandler patterns = this.provider.getPatterns();
        ItemStack remainder = encodedPattern.copy();
        for (int slot = 0; slot < patterns.getSlots(); slot++) {
            if (remainder.isEmpty()) {
                return true;
            }
            if (!patterns.isItemValid(slot, remainder)) {
                continue;
            }
            remainder = patterns.insertItem(slot, remainder, simulate);
        }
        return remainder.isEmpty();
    }

    private static PatternUploadGroupKey createGroupKey(final MEPatternProvider provider) {
        final TileEntity controller = findLinkedController(provider);
        final ItemStack icon = getDisplayIcon(provider, controller);
        final String name = getDisplayName(provider, icon, controller);
        final String key = "mmce_pattern_provider:" + name.toLowerCase(Locale.ROOT);
        final String searchText = name + " " + key + " " + getPositionSearchText(provider, controller);
        return new PatternUploadGroupKey(key, name, icon, searchText);
    }

    private static String getDisplayName(final MEPatternProvider provider, final ItemStack icon, final TileEntity controller) {
        if (controller != null && !icon.isEmpty()) {
            return icon.getDisplayName();
        }
        if (provider.hasCustomInventoryName()) {
            final String customName = provider.getCustomInventoryName();
            if (customName != null && !customName.isEmpty()) {
                return customName;
            }
        }
        final ItemStack providerIcon = provider.getVisualItemStack();
        if (!providerIcon.isEmpty()) {
            return providerIcon.getDisplayName();
        }
        return "ME Pattern Provider";
    }

    private static ItemStack getDisplayIcon(final MEPatternProvider provider, final TileEntity controller) {
        if (controller != null) {
            final ItemStack controllerIcon = getBlockIcon(controller.getWorld(), controller.getPos());
            if (!controllerIcon.isEmpty()) {
                return controllerIcon;
            }
        }
        return provider.getVisualItemStack();
    }

    private static TileEntity findLinkedController(final MEPatternProvider provider) {
        final World world = provider.getWorld();
        if (world == null) {
            return null;
        }

        TileEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (final TileEntity tile : new ArrayList<>(world.loadedTileEntityList)) {
            if (!isMmceController(tile) || !isStructureFormed(tile) || !containsProviderComponent(tile, provider)) {
                continue;
            }
            final double distance = tile.getPos().distanceSq(provider.getPos());
            if (distance < bestDistance) {
                best = tile;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static boolean isMmceController(final TileEntity tile) {
        return tile != null
                && tile.getClass().getName().startsWith("hellfirepvp.modularmachinery.common.tiles.")
                && hasPublicMethod(tile, "getFoundComponents");
    }

    private static boolean isStructureFormed(final TileEntity controller) {
        final Object formed = invokePublic(controller, "isStructureFormed");
        return formed instanceof Boolean && (Boolean) formed;
    }

    private static boolean containsProviderComponent(final TileEntity controller, final MEPatternProvider provider) {
        return containsProviderComponentObject(invokePublic(controller, "getFoundComponents"), provider);
    }

    private static boolean containsProviderComponentObject(final Object value, final MEPatternProvider provider) {
        if (value instanceof Map) {
            final Map<?, ?> map = (Map<?, ?>) value;
            if (containsProviderComponent(map.keySet(), provider)) {
                return true;
            }
            for (final Object nested : map.values()) {
                if (containsProviderComponentObject(nested, provider)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsProviderComponent(final Collection<?> components, final MEPatternProvider provider) {
        for (final Object value : components) {
            if (!(value instanceof TileEntity)) {
                continue;
            }
            final TileEntity component = (TileEntity) value;
            if (component == provider) {
                return true;
            }
            if (component instanceof MEPatternMirrorImage
                    && provider.getPos().equals(((MEPatternMirrorImage) component).providerPos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPublicMethod(final Object target, final String methodName) {
        try {
            target.getClass().getMethod(methodName);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static Object invokePublic(final Object target, final String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static ItemStack getBlockIcon(final World world, final BlockPos pos) {
        if (world == null || pos == null) {
            return ItemStack.EMPTY;
        }
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return PatternUploadGroupKey.sanitizeIcon(new ItemStack(item, 1, block.damageDropped(state)));
    }

    private static String getPositionSearchText(final MEPatternProvider provider, final TileEntity controller) {
        final BlockPos pos = provider.getPos();
        final ResourceLocation registryName = provider.getBlockType() == null ? null : provider.getBlockType().getRegistryName();
        final String registry = registryName == null ? "" : registryName.toString();
        final String providerText = registry + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        if (controller == null) {
            return providerText;
        }
        final BlockPos controllerPos = controller.getPos();
        final ResourceLocation controllerRegistry = controller.getBlockType() == null
                ? null
                : controller.getBlockType().getRegistryName();
        final String controllerRegistryText = controllerRegistry == null ? "" : controllerRegistry.toString();
        return providerText + " " + controllerRegistryText + " "
                + controllerPos.getX() + " " + controllerPos.getY() + " " + controllerPos.getZ();
    }
}
