package com.lw.random_additions.common.mixins.ae2;

import appeng.container.implementations.ContainerPatternEncoder;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import com.lw.random_additions.api.PatternMachineType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContainerPatternEncoder.class, remap = false)
public abstract class MixinContainerPatternTerm implements PatternMachineType {

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Unique
    private long RandomAdditions$jeiMachineTypeSetAt = 0L;

    @Unique
    private String RandomAdditions$jeiRecipeSignature = "";

    @Shadow
    public abstract boolean isCraftingMode();

    @Shadow
    protected abstract ItemStack[] getInputs();

    @Shadow
    protected abstract ItemStack[] getOutputs();

    @Override
    public void RandomAdditions$setJeiMachineType(final String machineType) {
        this.RandomAdditions$jeiMachineType = PatternMachineTypeUtil.sanitize(machineType);
        this.RandomAdditions$jeiMachineTypeSetAt = this.RandomAdditions$jeiMachineType.isEmpty() ? 0L : System.currentTimeMillis();
        this.RandomAdditions$jeiRecipeSignature = this.RandomAdditions$jeiMachineType.isEmpty() ? "" : this.RandomAdditions$currentRecipeSignature();
    }

    @Override
    public String RandomAdditions$getJeiMachineType() {
        return this.RandomAdditions$jeiMachineType;
    }

    @Override
    public boolean RandomAdditions$hasFreshJeiMachineType() {
        return !this.RandomAdditions$jeiMachineType.isEmpty()
                && this.RandomAdditions$jeiMachineTypeSetAt > 0L
                && this.RandomAdditions$jeiRecipeSignature.equals(this.RandomAdditions$currentRecipeSignature());
    }

    @Override
    public void RandomAdditions$clearJeiMachineType() {
        this.RandomAdditions$jeiMachineType = "";
        this.RandomAdditions$jeiMachineTypeSetAt = 0L;
        this.RandomAdditions$jeiRecipeSignature = "";
    }

    @ModifyArg(
            method = "encode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"),
            index = 0
    )
    private NBTTagCompound RandomAdditions$writeMachineTypeToPattern(final NBTTagCompound encodedValue) {
        if (!this.isCraftingMode() && this.RandomAdditions$hasFreshJeiMachineType()) {
            PatternMachineTypeUtil.write(encodedValue, this.RandomAdditions$jeiMachineType);
        }
        return encodedValue;
    }

    @Inject(method = "encode", at = @At("RETURN"))
    private void RandomAdditions$clearMachineTypeAfterEncode(final CallbackInfo ci) {
        this.RandomAdditions$clearJeiMachineType();
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void RandomAdditions$clearMachineType(final CallbackInfo ci) {
        this.RandomAdditions$clearJeiMachineType();
    }

    @Unique
    private String RandomAdditions$currentRecipeSignature() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.isCraftingMode() ? "crafting" : "processing");
        builder.append('|');
        this.RandomAdditions$appendStacks(builder, this.getInputs());
        builder.append('|');
        this.RandomAdditions$appendStacks(builder, this.getOutputs());
        return builder.toString();
    }

    @Unique
    private void RandomAdditions$appendStacks(final StringBuilder builder, final ItemStack[] stacks) {
        if (stacks == null) {
            builder.append("null");
            return;
        }

        for (final ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                builder.append("empty;");
                continue;
            }

            final ResourceLocation id = stack.getItem().getRegistryName();
            builder.append(id == null ? "" : id.toString());
            builder.append(',');
            builder.append(stack.getMetadata());
            builder.append(',');
            builder.append(stack.getCount());
            builder.append(',');
            builder.append(stack.hasTagCompound() ? stack.getTagCompound().toString() : "");
            builder.append(';');
        }
    }
}
