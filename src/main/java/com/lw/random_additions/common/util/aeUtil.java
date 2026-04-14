package com.lw.random_additions.common.util;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.misc.TileSecurityStation;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.google.common.collect.ImmutableCollection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;

public class aeUtil {

    /**
     * 获取玩家背包中的无线终端
     */
    @Nullable
    public static ItemStack getWirelessTerminalFromPlayer(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof ToolWirelessTerminal) {
                return stack;
            }
        }

        if (Loader.isModLoaded("baubles")) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
            if (baubles != null) {
                for (int i = 0; i < baubles.getSlots(); i++) {
                    ItemStack stack = baubles.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof ToolWirelessTerminal) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取 AE2 网格
     */
    @Nullable
    public static IGrid getGridFromTerminal(ItemStack terminal, EntityPlayer player, BlockPos pos) {
        if (terminal.isEmpty()) return null;

        IWirelessTermRegistry registry = AEApi.instance().registries().wireless();
        if (!registry.isWirelessTerminal(terminal)) return null;

        IWirelessTermHandler handler = registry.getWirelessTerminalHandler(terminal);
        if (handler == null) return null;

        String key = handler.getEncryptionKey(terminal);
        if (key == null || key.isEmpty()) return null;

        long parsedKey;
        try {
            parsedKey = Long.parseLong(key);
        } catch (NumberFormatException e) {
            return null;
        }

        ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);

        if (!(securityStation instanceof TileSecurityStation)) return null;

        WirelessTerminalGuiObject obj = new WirelessTerminalGuiObject(
                (ToolWirelessTerminal) terminal.getItem(),
                terminal,
                player,
                player.world,
                pos.getX(), pos.getY(), pos.getZ()
        );

        if (!obj.rangeCheck()) return null;

        IGridNode node = obj.getActionableNode();
        return node != null ? node.getGrid() : null;
    }

    /**
     * 检查玩家是否具有指定权限
     * @param player 玩家
     * @param grid AE2 网格
     * @param permission 权限
     * @return 是否具有指定权限
     */
    public static boolean securityCheck(EntityPlayer player, IGrid grid, SecurityPermissions permission) {
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        return securityGrid != null && securityGrid.hasPermission(player, permission);
    }

    public static boolean isCraftable(IGrid grid, ItemStack stack) {
        try {
            ICraftingGrid craftingGrid = grid.getCache(ICraftingGrid.class);
            if (stack.isEmpty()) return false;

            IAEItemStack aeStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)
                    .createStack(stack);

            ImmutableCollection<ICraftingPatternDetails> patterns = craftingGrid.getCraftingFor(aeStack, null, 1, null);

            return patterns != null && !patterns.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }


    @Nullable
    public static IGrid getGridFromBlock(TileEntity tileEntity) {
        if (tileEntity == null) return null;

        if (!(tileEntity instanceof IGridHost)) return null;

        IGridHost gridHost = (IGridHost) tileEntity;
        IGridNode node = gridHost.getGridNode(null);

        return node != null ? node.getGrid() : null;
    }
}
