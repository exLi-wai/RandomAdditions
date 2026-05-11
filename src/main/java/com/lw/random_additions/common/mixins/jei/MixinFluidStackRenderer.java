package com.lw.random_additions.common.mixins.jei;

import appeng.api.networking.IGrid;
import com.lw.random_additions.common.util.aeUtil;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidStackRenderer.class, remap = false)
public class MixinFluidStackRenderer {

    @Unique
    private static long randomAdditions$lastQueryTime = 0;
    @Unique
    private static String randomAdditions$lastCacheKey = "";
    @Unique
    private static long randomAdditions$cachedCount = 0;
    @Unique
    private static boolean randomAdditions$cachedCraftable = false;

    //直接就是一个飞天大mixin
    @Inject(method = "getTooltip*", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(Minecraft minecraft, FluidStack fluidStack, ITooltipFlag tooltipFlag, CallbackInfoReturnable<List<String>> cir) {
        if (fluidStack == null || fluidStack.getFluid() == null) return;

        String fluidName = fluidStack.getFluid().getName();
        long now = System.currentTimeMillis();
        boolean hitCache = now - randomAdditions$lastQueryTime < 1000 && randomAdditions$lastCacheKey.equals(fluidName);

        if (hitCache) {
            if (randomAdditions$cachedCraftable || randomAdditions$cachedCount > 0) {
                List<String> tooltip = cir.getReturnValue();
                randomAdditions$addStorageInfo(tooltip, randomAdditions$cachedCraftable, randomAdditions$cachedCount);
                cir.setReturnValue(tooltip);
            }
            return;
        }

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);
        if (terminal.isEmpty()) return;

        IGrid grid = aeUtil.getGridFromTerminal(terminal, player, player.getPosition());
        if (grid == null) {
            grid = aeUtil.getGridFromTerminalNBT(terminal, player);
        }
        if (grid == null) return;

        long fluidCount = aeUtil.getFluidCountInGrid(grid, fluidStack.getFluid());
        boolean craftable = Loader.isModLoaded("ae2fc") && aeUtil.isFluidCraftable(grid, fluidStack.getFluid());

        randomAdditions$lastQueryTime = now;
        randomAdditions$lastCacheKey = fluidName;
        randomAdditions$cachedCount = fluidCount;
        randomAdditions$cachedCraftable = craftable;

        if (craftable || fluidCount > 0) {
            List<String> tooltip = cir.getReturnValue();
            randomAdditions$addStorageInfo(tooltip, craftable, fluidCount);
            cir.setReturnValue(tooltip);
        }
    }

    @Unique
    private static void randomAdditions$addStorageInfo(List<String> tooltip, boolean craftable, long count) {
        StringBuilder sb = new StringBuilder();
        if (craftable) {
            sb.append("§a").append(I18n.format("random_additions.me_storage.craftable"));
        }
        if (count > 0) {
            if (sb.length() > 0) sb.append(" §7");
            else sb.append("§7");
            sb.append(I18n.format("random_additions.fluid_storage.count",
                    String.format("%.2f", (double) count), "mB"));
        }
        if (sb.length() > 0) {
            tooltip.add(sb.toString());
        }
    }
}
