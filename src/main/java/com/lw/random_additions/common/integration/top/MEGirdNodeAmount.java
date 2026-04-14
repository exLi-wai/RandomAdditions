package com.lw.random_additions.common.integration.top;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import com.lw.random_additions.Tags;
import com.lw.random_additions.common.util.aeUtil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class MEGirdNodeAmount implements IProbeInfoProvider {
    @Override
    public String getID() {
        return Tags.MOD_ID + ":me_gird_node_amount";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        TileEntity tileEntity = world.getTileEntity(iProbeHitData.getPos());
        if (tileEntity == null) return;
    
        IGrid grid = getGridFromTileEntity(tileEntity, iProbeHitData);
        if (grid == null) return;
    
        int nodeCount = grid.getNodes().size();
        String nodeInfo = new TextComponentTranslation("random_additions.me_grid.node_count", nodeCount).getFormattedText();
        iProbeInfo.text(nodeInfo);
    }
    
    private IGrid getGridFromTileEntity(TileEntity tileEntity, IProbeHitData hitData) {
        if (tileEntity instanceof IPartHost) {
            return getGridFromPartHost((IPartHost) tileEntity, hitData);
        }
        return aeUtil.getGridFromBlock(tileEntity);
    }
    
    private IGrid getGridFromPartHost(IPartHost host, IProbeHitData hitData) {
        IPart part = host.getPart(AEPartLocation.fromFacing(hitData.getSideHit()));

        if (part == null) {
            part = host.getPart(AEPartLocation.INTERNAL);
        }
            
        if (part != null) {
            IGridNode node = part.getGridNode();
            return node != null ? node.getGrid() : null;
        }

        IGridNode hostNode = ((IGridHost) host).getGridNode(null);
        return hostNode != null ? hostNode.getGrid() : null;
    }
}