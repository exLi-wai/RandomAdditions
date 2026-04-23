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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = FluidStackRenderer.class, remap = false)
public class MixinFluidStackRenderer {

    //直接就是一个飞天大mixin
    @Inject(method = "getTooltip*", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(Minecraft minecraft, FluidStack fluidStack, ITooltipFlag tooltipFlag, CallbackInfoReturnable<List<String>> cir) {
        if (fluidStack == null || fluidStack.getFluid() == null) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        ItemStack terminal = aeUtil.getWirelessTerminalFromPlayer(player);
        if (terminal == null || terminal.isEmpty()) return;

        IGrid grid = aeUtil.getGridFromTerminal(terminal, player, player.getPosition());
        if (grid == null) {
            grid = aeUtil.getGridFromTerminalNBT(terminal, player);
        }
        if (grid == null) return;

        long fluidCount = aeUtil.getFluidCountInGrid(grid, fluidStack.getFluid());
        
        if (fluidCount > 0) {
            List<String> tooltip = cir.getReturnValue();
            String countInfo = I18n.format("random_additions.fluid_storage.count",
                String.format("%.2f", (double) fluidCount), "mB");
            tooltip.add("§7" + countInfo);
            cir.setReturnValue(tooltip);
        }
    }
}
