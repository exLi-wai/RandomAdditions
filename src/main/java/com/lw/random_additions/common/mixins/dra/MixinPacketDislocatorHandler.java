package com.lw.random_additions.common.mixins.dra;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.brandon3055.draconicevolution.network.PacketDislocator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.network.PacketDislocator$Handler")
public class MixinPacketDislocatorHandler {

    @Unique
    private static Field randomAdditions$functionField;

    @Unique
    private byte randomAdditions$currentFunction = -1;

    @Inject(method = "handleMessage*", at = @At("HEAD"), remap = false)
    public void randomAdditions$captureFunction(
            PacketDislocator message,
            MessageContext ctx,
            CallbackInfoReturnable<IMessage> cir) {
        if (randomAdditions$functionField == null) {
            try {
                randomAdditions$functionField = message.getClass().getDeclaredField("function");
                randomAdditions$functionField.setAccessible(true);
            } catch (Exception e) {
                randomAdditions$currentFunction = -1;
                return;
            }
        }
        try {
            randomAdditions$currentFunction = randomAdditions$functionField.getByte(message);
        } catch (Exception e) {
            randomAdditions$currentFunction = -1;
        }
    }

    @Redirect(
        method = "handleMessage*",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/brandon3055/brandonscore/handlers/HandHelper;getItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/Item;)Lnet/minecraft/item/ItemStack;",
            remap = false
        )
    )
    public ItemStack randomAdditions$getItem(EntityPlayer player, Item item) {
        boolean checkFuel = (randomAdditions$currentFunction == 8);

        ItemStack main = player.getHeldItemMainhand();
        if (!main.isEmpty() && main.getItem() == item && (!checkFuel || randomAdditions$hasFuel(main))) return main;
        ItemStack off = player.getHeldItemOffhand();
        if (!off.isEmpty() && off.getItem() == item && (!checkFuel || randomAdditions$hasFuel(off))) return off;
        if (Loader.isModLoaded("baubles")) {
            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
            for (int i = 0; i < baubles.getSlots(); i++) {
                ItemStack s = baubles.getStackInSlot(i);
                if (!s.isEmpty() && s.getItem() == item && (!checkFuel || randomAdditions$hasFuel(s))) return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private static boolean randomAdditions$hasFuel(ItemStack stack) {
        if (stack.getTagCompound() != null) {
            return !stack.hasTagCompound()
                || !stack.getTagCompound().hasKey("Fuel")
                || stack.getTagCompound().getInteger("Fuel") > 0;
        }
        return false;
    }
}
