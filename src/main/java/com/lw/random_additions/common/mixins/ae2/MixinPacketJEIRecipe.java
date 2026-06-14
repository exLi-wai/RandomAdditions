package com.lw.random_additions.common.mixins.ae2;

import appeng.container.implementations.ContainerPatternEncoder;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketJEIRecipe;
import com.lw.random_additions.api.PatternMachineType;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Mixin(value = PacketJEIRecipe.class, remap = false)
public abstract class MixinPacketJEIRecipe {

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Inject(method = "<init>(Lio/netty/buffer/ByteBuf;)V", at = @At("RETURN"))
    private void RandomAdditions$readMachineType(final ByteBuf stream, final CallbackInfo ci) throws IOException {
        final ByteArrayInputStream bytes = ((PacketJEIRecipe) (Object) this).getPacketByteArray(stream);
        bytes.skip(stream.readerIndex());
        final NBTTagCompound recipe = CompressedStreamTools.readCompressed(bytes);
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.read(recipe);
    }

    @Inject(method = "serverPacketData", at = @At("HEAD"))
    private void RandomAdditions$setMachineTypeOnContainer(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player, final CallbackInfo ci) {
        final String machineType = PatternMachineTypeUtil.sanitize(this.RandomAdditions$jeiMachineType);
        final Container container = player.openContainer;
        if (container instanceof ContainerPatternEncoder && container instanceof PatternMachineType) {
            final PatternMachineType patternMachineType = (PatternMachineType) container;
            if (machineType.isEmpty()) {
                patternMachineType.RandomAdditions$clearJeiMachineType();
            } else {
                patternMachineType.RandomAdditions$setJeiMachineType(machineType);
            }
        }
    }
}
