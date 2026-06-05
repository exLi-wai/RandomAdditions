package com.lw.random_additions.common.mixins.ae2;

import appeng.container.implementations.ContainerPatternEncoder;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketJEIRecipe;
import com.lw.random_additions.api.PatternMachineType;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

@Mixin(value = PacketJEIRecipe.class, remap = false)
public abstract class MixinPacketJEIRecipe {

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"))
    private void RandomAdditions$writeMachineType(final NBTTagCompound recipe, final CallbackInfo ci) {
        PatternMachineTypeUtil.writeAndClearCurrentJeiMachineType(recipe);
    }

    @Redirect(
            method = "<init>(Lio/netty/buffer/ByteBuf;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompressedStreamTools;readCompressed(Ljava/io/InputStream;)Lnet/minecraft/nbt/NBTTagCompound;"
            )
    )
    private NBTTagCompound RandomAdditions$readMachineType(final InputStream stream) throws IOException {
        final NBTTagCompound recipe = CompressedStreamTools.readCompressed(stream);
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.read(recipe);
        return recipe;
    }

    @Inject(method = "serverPacketData", at = @At("TAIL"))
    private void RandomAdditions$applyMachineType(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player, final CallbackInfo ci) {
        if (!this.RandomAdditions$jeiMachineType.isEmpty()
                && player.openContainer instanceof ContainerPatternEncoder
                && player.openContainer instanceof PatternMachineType) {
            ((PatternMachineType) player.openContainer).RandomAdditions$setJeiMachineType(this.RandomAdditions$jeiMachineType);
        }
    }
}
