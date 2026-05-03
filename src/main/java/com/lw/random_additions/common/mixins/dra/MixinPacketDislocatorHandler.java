package com.lw.random_additions.common.mixins.dra;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.network.PacketDislocator$Handler")
public class MixinPacketDislocatorHandler {

    @Redirect(
        method = "handleMessage",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/brandon3055/brandonscore/handlers/HandHelper;getItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)Lnet/minecraft/item/ItemStack;",
            remap = false
        )
    )
    public ItemStack randomAdditions$getItem(EntityPlayer player, Item item) {
        ItemStack main = player.getHeldItemMainhand();
        if (!main.isEmpty() && main.getItem() == item) return main;
        ItemStack off = player.getHeldItemOffhand();
        if (!off.isEmpty() && off.getItem() == item) return off;
        if (!Loader.isModLoaded("baubles")) return null;
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack s = baubles.getStackInSlot(i);
            if (!s.isEmpty() && s.getItem() == item) return s;
        }
        return null;
    }
}
