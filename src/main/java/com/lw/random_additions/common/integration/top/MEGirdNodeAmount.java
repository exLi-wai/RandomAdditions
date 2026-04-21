package com.lw.random_additions.common.integration.top;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import com.lw.random_additions.Tags;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            IGrid grid = getGridFromPartHost((IPartHost) tileEntity, hitData);
            if (grid != null) return grid;
        }

        if (tileEntity instanceof IGridHost) {
            IGrid grid = getGridFromGridHost((IGridHost) tileEntity, hitData);
            if (grid != null) return grid;
        }

        return getGridViaReflection(tileEntity);
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

    private IGrid getGridFromGridHost(IGridHost host, IProbeHitData hitData) {

        AEPartLocation location = AEPartLocation.fromFacing(hitData.getSideHit());
        IGridNode node = host.getGridNode(location);
        if (node != null) {
            node.getGrid();
            return node.getGrid();
        }

        node = host.getGridNode(AEPartLocation.INTERNAL);
        if (node != null) {
            node.getGrid();
            return node.getGrid();
        }

        node = host.getGridNode(null);
        return node != null ? node.getGrid() : null;
    }

    /**
     * 反射从 TileEntity 中获取 IGrid 实例
     * @param tileEntity 要检查的 TileEntity 实例
     * @return 找到的 IGrid 实例，如果未找到或发生异常则返回 null
     */
    private IGrid getGridViaReflection(TileEntity tileEntity) {
        if (tileEntity == null) return null;
        
        try {
            Method getGridMethod = null;

            for (Method method : tileEntity.getClass().getMethods()) {
                String methodName = method.getName();
                if ((methodName.contains("Grid") || methodName.contains("grid")) && 
                    method.getParameterCount() == 0) {
                    
                    Class<?> returnType = method.getReturnType();

                    if (returnType.getName().equals("appeng.api.networking.IGrid")) {
                        getGridMethod = method;
                        break;
                    }
                    else if (returnType.getName().equals("appeng.api.networking.IGridNode")) {
                        Object node = method.invoke(tileEntity);
                        if (node != null) {
                            java.lang.reflect.Method getGrid = node.getClass().getMethod("getGrid");
                            return (IGrid) getGrid.invoke(node);
                        }

                    }
                }
            }
            if (getGridMethod != null) {
                return (IGrid) getGridMethod.invoke(tileEntity);
            }

            for (Field field : tileEntity.getClass().getDeclaredFields()) {
                String fieldName = field.getName();
                if (fieldName.contains("grid") || fieldName.contains("Grid")) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(tileEntity);
                    
                    if (fieldValue != null) {
                        if (fieldValue instanceof IGrid) {
                            return (IGrid) fieldValue;
                        }
                        else if (fieldValue.getClass().getName().equals("appeng.api.networking.IGridNode")) {
                            Method getGrid = fieldValue.getClass().getMethod("getGrid");
                            return (IGrid) getGrid.invoke(fieldValue);
                        }
                    }
                }
            }
            
        } catch (Exception ignored) {

        }
        return null;
    }
}