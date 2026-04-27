package com.lw.random_additions.common.integration.top;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import com.lw.random_additions.Tags;
import com.lw.random_additions.common.util.aeUtil;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class MEStorageInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "random_additions:me_storage_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData iProbeHitData) {
        ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);
        if (terminal.isEmpty()) return;
    
        IGrid grid = aeUtil.getGridFromTerminal(terminal, player, player.getPosition());
        if (grid == null) return;
    
        long count = 0;
        String displayUnit = "";
        ItemStack targetStack = null;
    
        if (isFluidBlock(blockState)) {
            Fluid fluid = getFluidFromBlock(blockState);
            if (fluid != null) {
                count = aeUtil.getFluidCountInGrid(grid, fluid);
                displayUnit = "mB";
            }
        } else {
            targetStack = iProbeHitData.getPickBlock();
            if (targetStack == null || targetStack.isEmpty()) {
                targetStack = getTargetItemStack(world, blockState, iProbeHitData);
            }
            if (!targetStack.isEmpty()) {
                count = getItemCountInGridByItemStack(grid, targetStack);
            }
        }
    
        StringBuilder infoBuilder = new StringBuilder();

        if (!isFluidBlock(blockState)&& !targetStack.isEmpty() && aeUtil.isCraftable(grid, targetStack)) {
            String isCraftable = new TextComponentTranslation("random_additions.me_storage.craftable").getFormattedText();
            infoBuilder.append(TextStyleClass.OK).append(isCraftable).append(" ");
        }

        if (displayUnit.isEmpty()) {
            String countInfo = new TextComponentTranslation("random_additions.me_storage.count", count).getFormattedText();
            infoBuilder.append(TextStyleClass.INFO).append(countInfo);
        } else {
            String fluidInfo = new TextComponentTranslation("random_additions.fluid_storage.count", String.format("%.2f", (double) count), displayUnit).getFormattedText();
            infoBuilder.append(TextStyleClass.INFO).append(fluidInfo);
        }
    
        iProbeInfo.text(infoBuilder.toString());
    }

    private static ItemStack getTargetItemStack(World world, IBlockState state, IProbeHitData hitData) {
        Block block = state.getBlock();
        TileEntity te = world.getTileEntity(hitData.getPos());

        if (te instanceof IPartHost) {
            IPartHost host = (IPartHost) te;
            AEPartLocation location = AEPartLocation.fromFacing(hitData.getSideHit());
            IPart part = host.getPart(location);
            if (part != null) {
                return part.getItemStack(PartItemStack.PICK);
            }
        }
        return block.getItem(world, hitData.getPos(), state);
    }

    /**
     * 获取物品存储列表
     */
    private static IItemList<IAEItemStack> getItemStorageList(IGrid grid) {
        if (grid == null) return null;
        
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        IMEMonitor<IAEItemStack> monitor = storage.getInventory(channel);
        
        return monitor != null ? monitor.getStorageList() : null;
    }

    public static long getItemCountInGridByItemStack(IGrid grid, ItemStack targetStack) {
        if (targetStack.isEmpty()) return 0;

        IItemList<IAEItemStack> list = getItemStorageList(grid);
        if (list == null) return 0;

        List<IAEItemStack> snapshot = new ArrayList<>();
        for(IAEItemStack stack :list){
            snapshot.add(stack);
        }

        long total = 0;

        for (IAEItemStack aeStack : snapshot) {
            ItemStack stack = aeStack.createItemStack();
            if (stack.getItem() == targetStack.getItem() && stack.getMetadata() == targetStack.getMetadata()) {
                total += aeStack.getStackSize();
            }
        }
        return total;
    }

    private static boolean isFluidBlock(IBlockState state) {
        Block block = state.getBlock();

        if (block instanceof BlockLiquid) return true;
        if (block instanceof IFluidBlock) return true;

        return state.getMaterial().isLiquid();
    }

    private static Fluid getFluidFromBlock(IBlockState state) {
        Block block = state.getBlock();

        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).getFluid();
        }

        if (block instanceof BlockLiquid) {
            if (state.getMaterial() == Material.WATER) {
                return FluidRegistry.WATER;
            } else if (state.getMaterial() == Material.LAVA) {
                return FluidRegistry.LAVA;
            }
        }

        if (state.getMaterial().isLiquid()) {

        }
        return null;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
    public static class EventMEStorageTooltip {

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void onItemTooltip(ItemTooltipEvent event) {
            EntityPlayer player = event.getEntityPlayer();
            if (player == null) return;

            ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);
            if (terminal == null || terminal.isEmpty()) return;

            IGrid grid = aeUtil.getGridFromTerminal(terminal, player, player.getPosition());
            if (grid == null) {
                grid = aeUtil.getGridFromTerminalNBT(terminal, player);
                if (grid == null) return;
            }

            ItemStack itemStack = event.getItemStack();
            if (itemStack.isEmpty()) return;

            StringBuilder tooltipText = new StringBuilder();

            long count;
            if (itemStack.hasTagCompound()) {
                count = getItemCountWithNBT(grid, itemStack);
            } else {
                count = MEStorageInfoProvider.getItemCountInGridByItemStack(grid, itemStack);
            }

            if (aeUtil.isCraftable(grid, itemStack)) {
                String isCraftable = I18n.format("random_additions.me_storage.craftable");
                tooltipText.append("§a").append(isCraftable).append(" ");
            }
                
            if (count > 0) {
                String countInfo = I18n.format("random_additions.me_storage.count", count);
                tooltipText.append("§7").append(countInfo);
            }
            
            if (tooltipText.length() > 0) {
                event.getToolTip().add(tooltipText.toString());
            }
        }

        /**
         * 匹配 NBT 然后计数
         */
        private static long getItemCountWithNBT(IGrid grid, ItemStack targetStack) {
            if (targetStack.isEmpty() || !targetStack.hasTagCompound()) return 0;

            IItemList<IAEItemStack> list = MEStorageInfoProvider.getItemStorageList(grid);
            if (list == null) return 0;

            List<IAEItemStack> snapshot = new ArrayList<>();
            for (IAEItemStack stack : list) {
                snapshot.add(stack);
            }

            long total = 0;

            for (IAEItemStack aeStack : snapshot) {
                ItemStack stack = aeStack.createItemStack();
                if (targetStack.getTagCompound() != null && stack.getItem() == targetStack.getItem() &&
                        stack.getMetadata() == targetStack.getMetadata() &&
                        stack.hasTagCompound() &&
                        stack.getTagCompound().equals(targetStack.getTagCompound())) {
                    total += aeStack.getStackSize();
                }
            }
            return total;
        }

    }
}

