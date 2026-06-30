package com.lw.random_additions.common.integration.ae2.patternupload;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.helpers.IInterfaceHost;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InterfaceEndpointFinder implements PatternUploadEndpointFinder {

    private static final String[] AE_NAMESPACES = {
            "appliedenergistics2:",
            "ae2fc:",
            "ae2fluidcraft:"
    };

    private static final String[] AE_CLASS_PREFIXES = {
            "appeng.",
            "com.glodblock."
    };

    private static final String[] MOLECULAR_ASSEMBLER_MARKERS = {
            "molecular_assembler",
            "molecularassembler"
    };

    private static final String[] AE2FC_PATTERN_TARGET_TYPE_MARKERS = {
            "fluid",
            "dual",
            "trio"
    };

    private static final String[] PATTERN_TARGET_MARKERS = {
            "interface",
            "pattern"
    };

    private static final String[] AE_INFRASTRUCTURE_MARKERS = {
            "controller",
            "cable",
            "energy",
            "interface",
            "terminal",
            "drive",
            "cell"
    };

    @Override
    public void findEndpoints(IGrid grid, ItemStack encodedPattern, List<PatternUploadEndpoint> endpoints) {
        if (grid == null) {
            return;
        }
        for (Class<? extends IGridHost> machineClass : grid.getMachinesClasses()) {
            for (IGridNode node : grid.getMachines(machineClass)) {
                if (node == null || !node.isActive()) {
                    continue;
                }
                IGridHost machine = node.getMachine();
                if (machine instanceof IInterfaceHost) {
                    IInterfaceHost host = (IInterfaceHost) machine;
                    InterfacePatternUploadEndpoint endpoint = new InterfacePatternUploadEndpoint(host, resolveGroupKey(host, encodedPattern));
                    if (endpoint.supportsPatternType(encodedPattern)) {
                        endpoints.add(endpoint);
                    }
                }
            }
        }
    }

    private static PatternUploadGroupKey resolveGroupKey(IInterfaceHost host, ItemStack encodedPattern) {
        PatternUploadGroupKey adjacent = resolveAdjacentTarget(host);
        if (adjacent != null) {
            return adjacent;
        }

        String name = host.getInterfaceDuality().getTermName();
        if (name == null || name.isEmpty()) {
            name = PatternUploadPatternTypes.isFluidPattern(encodedPattern) ? "Fluid Interface" : "ME Interface";
        }
        String key = name.equals("ME Interface") || name.equals("Fluid Interface")
                ? "ae_interface:" + (PatternUploadPatternTypes.isFluidPattern(encodedPattern) ? "fluid" : "item")
                : "named_interface:" + name.toLowerCase(Locale.ROOT);
        return new PatternUploadGroupKey(key, name, getIcon(host), name + " " + key);
    }

    private static ItemStack getIcon(IInterfaceHost host) {
        TileEntity tile = host.getTileEntity();
        if (tile == null || tile.getWorld() == null) {
            return ItemStack.EMPTY;
        }
        return getBlockIcon(tile.getWorld(), tile.getPos());
    }

    private static PatternUploadGroupKey resolveAdjacentTarget(IInterfaceHost host) {
        TileEntity tile = host.getTileEntity();
        if (tile == null || tile.getWorld() == null) {
            return null;
        }

        List<AdjacentTargetCandidate> candidates = new ArrayList<>();
        World world = tile.getWorld();
        for (EnumFacing facing : host.getTargets()) {
            BlockPos pos = tile.getPos().offset(facing);
            TileEntity adjacent = world.getTileEntity(pos);
            if (adjacent != null) {
                candidates.add(createCandidate(world, pos, adjacent));
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        int bestPriority = candidates.stream()
                .mapToInt(AdjacentTargetCandidate::getPriority)
                .max()
                .orElse(AdjacentTargetCandidate.AE_INFRASTRUCTURE_PRIORITY);
        if (bestPriority <= AdjacentTargetCandidate.AE_INFRASTRUCTURE_PRIORITY) {
            return null;
        }

        List<AdjacentTargetCandidate> best = candidates.stream()
                .filter(candidate -> candidate.getPriority() == bestPriority)
                .collect(Collectors.toList());
        if (best.stream().map(candidate -> candidate.key.getKey()).distinct().count() > 1 && bestPriority < 100) {
            return null;
        }

        return best.stream()
                .max(Comparator.comparingInt(AdjacentTargetCandidate::getPriority))
                .map(AdjacentTargetCandidate::getKey)
                .orElse(null);
    }

    private static AdjacentTargetCandidate createCandidate(World world, BlockPos pos, TileEntity tile) {
        ItemStack icon = getBlockIcon(world, pos);
        String name = icon.isEmpty() ? tile.getClass().getSimpleName() : icon.getDisplayName();
        String registry = getBlockRegistryName(world, pos, tile);
        String className = tile.getClass().getName().toLowerCase(Locale.ROOT);
        String searchText = name + " " + registry + " " + tile.getClass().getName();

        if (isMolecularAssembler(registry, className)) {
            return new AdjacentTargetCandidate(
                    new PatternUploadGroupKey("ae_molecular_assembler:" + registry, name, icon, searchText),
                    100
            );
        }
        if (isAe2FcPatternTarget(registry, className)) {
            return new AdjacentTargetCandidate(
                    new PatternUploadGroupKey("ae2fc_pattern_target:" + registry, name, icon, searchText),
                    90
            );
        }
        if (isAeInfrastructure(registry, className)) {
            return new AdjacentTargetCandidate(
                    new PatternUploadGroupKey("ae_infrastructure:" + registry, name, icon, searchText),
                    AdjacentTargetCandidate.AE_INFRASTRUCTURE_PRIORITY
            );
        }
        return new AdjacentTargetCandidate(
                new PatternUploadGroupKey("tile_target:" + registry, name, icon, searchText),
                60
        );
    }

    private static boolean isMolecularAssembler(String registry, String className) {
        return containsAny(registry, className, MOLECULAR_ASSEMBLER_MARKERS);
    }

    private static boolean isAe2FcPatternTarget(String registry, String className) {
        return containsAny(registry, className, AE2FC_PATTERN_TARGET_TYPE_MARKERS)
                && containsAny(registry, className, PATTERN_TARGET_MARKERS);
    }

    private static boolean isAeInfrastructure(String registry, String className) {
        if (!startsWithAny(registry, AE_NAMESPACES) && !startsWithAny(className, AE_CLASS_PREFIXES)) {
            return false;
        }
        if (isMolecularAssembler(registry, className) || isAe2FcPatternTarget(registry, className)) {
            return false;
        }
        return containsAny(registry, className, AE_INFRASTRUCTURE_MARKERS);
    }

    private static boolean containsAny(String left, String right, String[] markers) {
        for (String marker : markers) {
            if (left.contains(marker) || right.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithAny(String value, String[] prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack getBlockIcon(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return PatternUploadGroupKey.sanitizeIcon(new ItemStack(item, 1, 0));
    }

    private static String getBlockRegistryName(World world, BlockPos pos, TileEntity tile) {
        ResourceLocation registryName = world.getBlockState(pos).getBlock().getRegistryName();
        return registryName == null ? tile.getClass().getName().toLowerCase(Locale.ROOT) : registryName.toString().toLowerCase(Locale.ROOT);
    }

    private static class AdjacentTargetCandidate {

        private static final int AE_INFRASTRUCTURE_PRIORITY = -10;

        private final PatternUploadGroupKey key;
        private final int priority;

        private AdjacentTargetCandidate(PatternUploadGroupKey key, int priority) {
            this.key = key;
            this.priority = priority;
        }

        private PatternUploadGroupKey getKey() {
            return key;
        }

        private int getPriority() {
            return priority;
        }
    }
}
