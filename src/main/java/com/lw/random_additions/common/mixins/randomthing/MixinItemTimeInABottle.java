package com.lw.random_additions.common.mixins.randomthing;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import lumien.randomthings.item.ItemTimeInABottle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(ItemTimeInABottle.class)
public class MixinItemTimeInABottle implements IBauble {

    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.TRINKET;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (!player.world.isRemote) {
            itemstack.getItem().onUpdate(itemstack, player.world, player, 0, false);
        }
    }
}
