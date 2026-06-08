package com.lw.random_additions.common.mixins.ae2;

import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.lw.random_additions.common.init.Mods;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemEncodedPattern.class, remap = false)
public abstract class MixinItemEncodedPattern {

    @Inject(method = "addCheckedInformation", at = @At("HEAD"))
    @SideOnly(Side.CLIENT)
    private void RandomAdditions$addMachineTypeTooltip(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips, final CallbackInfo ci) {
        final NBTTagCompound tag = stack.getTagCompound();
        if (RandomAdditions$isCraftingPattern(stack, tag)) {
            lines.add(TextFormatting.GRAY + I18n.format("random_additions.ae2.pattern.encoding_type.crafting"));
            return;
        }
        final String machineType = PatternMachineTypeUtil.read(tag);
        if (!machineType.isEmpty()) {
            lines.add(TextFormatting.GRAY + I18n.format("random_additions.ae2.pattern.machine_type", machineType));
        }
    }

    @Unique
    private boolean RandomAdditions$isCraftingPattern(final ItemStack stack, final NBTTagCompound tag) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (Mods.AE2FC.isLoaded()) {
            if (stack.getItem() instanceof ItemFluidCraftEncodedPattern) {
                return true;
            }
            if (stack.getItem() instanceof ItemFluidEncodedPattern || stack.getItem() instanceof ItemLargeEncodedPattern) {
                return false;
            }
        }

        return tag != null && tag.getBoolean("crafting");
    }
}
