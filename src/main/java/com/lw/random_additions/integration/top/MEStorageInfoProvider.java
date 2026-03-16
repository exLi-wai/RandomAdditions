package com.lw.random_additions.integration.top;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import com.lw.random_additions.util.aeUtil;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;

public class MEStorageInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "random_additions:me_storage_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData iProbeHitData) {
        long count = 0;
        String displayUnit = "";

        ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);

        if (!terminal.isEmpty()) {
            IGrid grid = aeUtil.getGridFromTerminal(terminal, player, player.getPosition());
            if (grid != null) {
                if (isFluidBlock(blockState)) {

                    Fluid fluid = getFluidFromBlock(blockState);

                    if (fluid != null) {
                        count = getFluidCountInGrid(grid, fluid);
                        displayUnit = "mB";

                    }
                } else {
                    ItemStack targetStack = getTargetItemStack(world, blockState, iProbeHitData);
                    if (!targetStack.isEmpty()) {
                        count = getItemCountInGridByItemStack(grid, targetStack);
                        displayUnit = "";
                    }
                }
            }
        }

        if (displayUnit.isEmpty()) {
            String info = new TextComponentTranslation("random_additions.me_storage.count", count).getFormattedText();
            iProbeInfo.text(TextStyleClass.INFO + info);
        } else {
            String info = new TextComponentTranslation("random_additions.fluid_storage.count", String.format("%.2f", (double)count), displayUnit).getFormattedText();
            iProbeInfo.text(TextStyleClass.INFO + info);
        }
    }

    private ItemStack getTargetItemStack(World world, IBlockState state, IProbeHitData hitData) {
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

    private long getItemCountInGridByItemStack(IGrid grid, ItemStack targetStack) {
        long total = 0;

        if (targetStack.isEmpty()) return 0;

        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        IMEMonitor<IAEItemStack> monitor = storage.getInventory(channel);

        if (monitor == null) return 0;

        IItemList<IAEItemStack> list = monitor.getStorageList();

        for (IAEItemStack aeStack : list) {
            ItemStack stack = aeStack.createItemStack();
            if (stack.getItem() == targetStack.getItem() && stack.getMetadata() == targetStack.getMetadata()) {
                total += aeStack.getStackSize();
            }
        }
        return total;
    }

    private long getFluidCountInGrid(IGrid grid, Fluid targetFluid) {
        long total = 0;

        if (targetFluid == null) return 0;

        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        IFluidStorageChannel fluidChannel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
        IMEMonitor<IAEFluidStack> monitor = storage.getInventory(fluidChannel);

        if (monitor == null) return 0;

        IItemList<IAEFluidStack> fluidList = monitor.getStorageList();

        for (IAEFluidStack aeFluidStack : fluidList) {
            Fluid fluid = aeFluidStack.getFluid();
            if (fluid == targetFluid) {

                total += aeFluidStack.getStackSize();
            }
        }
        return total;
    }

    private boolean isFluidBlock(IBlockState state) {
        Block block = state.getBlock();

        if (block instanceof BlockLiquid) return true;
        if (block instanceof IFluidBlock) return true;

        return state.getMaterial().isLiquid();
    }

    private Fluid getFluidFromBlock(IBlockState state) {
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

}

