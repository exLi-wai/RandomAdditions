package com.lw.random_additions.common.mixins.ae2fc;

import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.util.Util;
import com.lw.random_additions.api.PatternMachineTypePacket;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CPacketLoadPattern.class, remap = false)
public abstract class MixinCPacketLoadPattern implements PatternMachineTypePacket {

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Inject(method = "<init>(Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;Ljava/util/List;Z)V", at = @At("RETURN"))
    private void RandomAdditions$captureMachineType(final Int2ObjectMap<ItemStack[]> crafting, final List<ItemStack> output, final boolean compress, final CallbackInfo ci) {
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.getCurrentJeiMachineType();
        PatternMachineTypeUtil.clearCurrentJeiMachineType();
    }

    @Override
    public String RandomAdditions$getJeiMachineType() {
        return this.RandomAdditions$jeiMachineType;
    }

    @ModifyArg(
            method = "toBytes",
            at = @At(value = "INVOKE", target = "Lcom/glodblock/github/util/Util;writeNBTToBytes(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NBTTagCompound;)V"),
            index = 1
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToPacket(final NBTTagCompound tag) {
        PatternMachineTypeUtil.write(tag, this.RandomAdditions$jeiMachineType);
        return tag;
    }

    @Redirect(
            method = "fromBytes",
            at = @At(value = "INVOKE", target = "Lcom/glodblock/github/util/Util;readNBTFromBytes(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/NBTTagCompound;")
    )
    private NBTTagCompound RandomAdditions$readMachineTypeFromPacket(final ByteBuf from) {
        final NBTTagCompound tag = Util.readNBTFromBytes(from);
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.read(tag);
        return tag;
    }
}
