package com.lw.random_additions.common.mixins.ae2;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.item.AEItemStack;
import com.lw.random_additions.api.PatternUploadContainer;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetFinder;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadGroup;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetInfo;
import com.lw.random_additions.common.network.NetworkHandler;
import com.lw.random_additions.common.network.PacketPatternUploadTargets;
import com.lw.random_additions.common.utils.PatternMachineTypeUtil;
import com.lw.random_additions.api.PatternMachineType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = ContainerPatternEncoder.class, remap = false)
public abstract class MixinContainerPatternTerm implements PatternMachineType, PatternUploadContainer {

    @Unique
    private static final long RandomAdditions$PATTERN_UPLOAD_TIMEOUT_MS = 15_000L;

    @Unique
    private String RandomAdditions$jeiMachineType = "";

    @Unique
    private long RandomAdditions$jeiMachineTypeSetAt = 0L;

    @Unique
    private String RandomAdditions$jeiRecipeSignature = "";

    @Unique
    private ItemStack RandomAdditions$pendingPatternUpload = ItemStack.EMPTY;

    @Unique
    private List<PatternUploadGroup> RandomAdditions$pendingPatternUploadTargets = new ArrayList<>();

    @Unique
    private long RandomAdditions$pendingPatternUploadAt = 0L;

    @Shadow
    public abstract boolean isCraftingMode();

    @Shadow
    protected abstract ItemStack[] getInputs();

    @Shadow
    protected abstract ItemStack[] getOutputs();

    @Shadow
    protected SlotRestrictedInput patternSlotIN;

    @Shadow
    protected SlotRestrictedInput patternSlotOUT;

    @Invoker("createItemTag")
    protected abstract NBTBase RandomAdditions$createItemTag(ItemStack stack);

    @Invoker("isSubstitute")
    protected abstract boolean RandomAdditions$isSubstitute();

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
        PatternMachineTypeUtil.stripFromEncodedPattern(encodedValue);
        return encodedValue;
    }

    @Inject(method = "encode", at = @At("RETURN"))
    private void RandomAdditions$clearMachineTypeAfterEncode(final CallbackInfo ci) {
        this.RandomAdditions$clearJeiMachineType();
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void RandomAdditions$clearMachineType(final CallbackInfo ci) {
        this.RandomAdditions$clearJeiMachineType();
        this.RandomAdditions$clearPendingPatternUpload();
    }

    @Override
    public void RandomAdditions$requestPatternUpload(final EntityPlayerMP player) {
        final ItemStack encodedPattern = this.RandomAdditions$encodePatternForUpload();
        if (encodedPattern.isEmpty()) {
            this.RandomAdditions$sendPatternUploadStatus(player, "invalid");
            return;
        }

        final IGrid grid = this.RandomAdditions$getGrid();
        if (grid == null) {
            this.RandomAdditions$sendPatternUploadStatus(player, "no_network");
            return;
        }

        final List<PatternUploadGroup> groups = PatternUploadTargetFinder.findGroups(grid, encodedPattern);
        if (groups.isEmpty()) {
            this.RandomAdditions$clearPendingPatternUpload();
            this.RandomAdditions$sendPatternUploadStatus(player, "no_targets");
            return;
        }

        this.RandomAdditions$pendingPatternUpload = encodedPattern.copy();
        this.RandomAdditions$pendingPatternUploadTargets = groups;
        this.RandomAdditions$pendingPatternUploadAt = System.currentTimeMillis();

        NetworkHandler.CHANNEL.sendTo(new PacketPatternUploadTargets(
                this.RandomAdditions$toPatternUploadTargetInfos(groups, encodedPattern)
        ), player);
    }

    @Override
    public void RandomAdditions$sendPatternToTarget(final EntityPlayerMP player, final int index) {
        if (this.RandomAdditions$isPendingPatternUploadSelectionInvalid(index)) {
            this.RandomAdditions$clearPendingPatternUpload();
            this.RandomAdditions$sendPatternUploadStatus(player, "expired");
            return;
        }

        final ItemStack encodedPattern = this.RandomAdditions$pendingPatternUpload.copy();
        final PatternUploadGroup pendingGroup = this.RandomAdditions$pendingPatternUploadTargets.get(index);
        final IGrid grid = this.RandomAdditions$getGrid();
        if (grid == null) {
            this.RandomAdditions$sendPatternUploadStatus(player, "no_network");
            return;
        }

        final PatternUploadGroup group = this.RandomAdditions$findCurrentPatternUploadGroup(grid, pendingGroup.getKey(), encodedPattern);
        if (group == null) {
            this.RandomAdditions$sendPatternUploadStatus(player, "no_targets");
            return;
        }

        if (group.getFreeCount(encodedPattern) <= 0) {
            this.RandomAdditions$sendPatternUploadStatus(player, "full");
            return;
        }

        if (!this.RandomAdditions$extractBlankPattern(grid)) {
            this.RandomAdditions$sendPatternUploadStatus(player, "no_blank");
            return;
        }

        if (!group.insert(encodedPattern)) {
            this.RandomAdditions$returnBlankPattern(grid);
            this.RandomAdditions$sendPatternUploadStatus(player, "failed");
            return;
        }

        this.RandomAdditions$clearPendingPatternUpload();
        this.RandomAdditions$sendPatternUploadStatus(player, "success");
    }

    @Unique
    private boolean RandomAdditions$isPendingPatternUploadSelectionInvalid(final int index) {
        return this.RandomAdditions$pendingPatternUpload.isEmpty()
                || System.currentTimeMillis() - this.RandomAdditions$pendingPatternUploadAt > RandomAdditions$PATTERN_UPLOAD_TIMEOUT_MS
                || index < 0
                || index >= this.RandomAdditions$pendingPatternUploadTargets.size();
    }

    @Unique
    private List<PatternUploadTargetInfo> RandomAdditions$toPatternUploadTargetInfos(final List<PatternUploadGroup> groups,
                                                                                    final ItemStack encodedPattern) {
        final List<PatternUploadTargetInfo> infos = new ArrayList<>(groups.size());
        for (final PatternUploadGroup group : groups) {
            infos.add(group.toInfo(encodedPattern));
        }
        return infos;
    }

    @Unique
    private void RandomAdditions$sendPatternUploadStatus(final EntityPlayerMP player, final String key) {
        player.sendStatusMessage(new TextComponentTranslation("random_additions.ae2.pattern_upload." + key), true);
    }

    @Unique
    private PatternUploadGroup RandomAdditions$findCurrentPatternUploadGroup(final IGrid grid, final String groupKey,
                                                                            final ItemStack encodedPattern) {
        final List<PatternUploadGroup> groups = PatternUploadTargetFinder.findGroups(grid, encodedPattern);
        for (final PatternUploadGroup group : groups) {
            if (group.getKey().equals(groupKey)) {
                return group;
            }
        }
        return null;
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

            builder.append(stack.getItem().getRegistryName());
            builder.append(',');
            builder.append(stack.getMetadata());
            builder.append(',');
            builder.append(stack.getCount());
            builder.append(',');
            builder.append(stack.hasTagCompound() ? stack.getTagCompound().toString() : "");
            builder.append(';');
        }
    }

    @Unique
    private ItemStack RandomAdditions$encodePatternForUpload() {
        if (this.RandomAdditions$isAe2FcFluidPatternTerminal()) {
            return this.RandomAdditions$encodeFluidPatternForUpload();
        }

        final ItemStack[] inputs = this.getInputs();
        final ItemStack[] outputs = this.getOutputs();
        if (inputs == null || outputs == null) {
            return ItemStack.EMPTY;
        }

        final Optional<ItemStack> encodedPatternStack = AEApi.instance().definitions().items().encodedPattern().maybeStack(1);
        if (!encodedPatternStack.isPresent()) {
            return ItemStack.EMPTY;
        }

        final NBTTagCompound encodedValue = new NBTTagCompound();
        final NBTTagList in = new NBTTagList();
        final NBTTagList out = new NBTTagList();

        for (final ItemStack input : inputs) {
            in.appendTag(this.RandomAdditions$createItemTag(input));
        }
        for (final ItemStack output : outputs) {
            out.appendTag(this.RandomAdditions$createItemTag(output));
        }

        encodedValue.setTag("in", in);
        encodedValue.setTag("out", out);
        encodedValue.setBoolean("crafting", this.isCraftingMode());
        encodedValue.setBoolean("substitute", this.RandomAdditions$isSubstitute());

        if (!this.isCraftingMode() && this.RandomAdditions$hasFreshJeiMachineType()) {
            PatternMachineTypeUtil.write(encodedValue, this.RandomAdditions$jeiMachineType);
        }
        PatternMachineTypeUtil.stripFromEncodedPattern(encodedValue);

        final ItemStack encodedPattern = encodedPatternStack.get();
        encodedPattern.setTagCompound(encodedValue);
        return encodedPattern;
    }

    @Unique
    private ItemStack RandomAdditions$encodeFluidPatternForUpload() {
        final ItemStack originalInput = this.patternSlotIN.getStack().copy();
        final ItemStack originalOutput = this.patternSlotOUT.getStack().copy();
        final ItemStack blankPattern = this.RandomAdditions$blankPatternItemStack();
        if (blankPattern.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try {
            this.patternSlotOUT.putStack(blankPattern.copy());
            ((ContainerPatternEncoder) (Object) this).encode();
            final ItemStack encoded = this.patternSlotOUT.getStack().copy();
            if (encoded.isEmpty() || AEApi.instance().definitions().materials().blankPattern().isSameAs(encoded)) {
                return ItemStack.EMPTY;
            }
            return encoded;
        } finally {
            this.patternSlotIN.putStack(originalInput);
            this.patternSlotOUT.putStack(originalOutput);
        }
    }

    @Unique
    private boolean RandomAdditions$isAe2FcFluidPatternTerminal() {
        final String className = ((Object) this).getClass().getName();
        return className.startsWith("com.glodblock.github.client.container.")
                && className.contains("FluidPatternTerminal");
    }

    @Unique
    private IGrid RandomAdditions$getGrid() {
        final IGridNode node = ((ContainerPatternEncoder) (Object) this).getNetworkNode();
        if (node == null) {
            return null;
        }
        return node.getGrid();
    }

    @Unique
    private boolean RandomAdditions$extractBlankPattern(final IGrid grid) {
        final IMEInventory<IAEItemStack> inventory = this.RandomAdditions$getNetworkInventory(grid);
        final IAEItemStack blank = this.RandomAdditions$blankPatternStack();
        if (inventory == null || blank == null) {
            return false;
        }
        final IAEItemStack extracted = inventory.extractItems(blank, Actionable.MODULATE,
                ((ContainerPatternEncoder) (Object) this).getActionSource());
        return extracted != null && extracted.getStackSize() > 0;
    }

    @Unique
    private void RandomAdditions$returnBlankPattern(final IGrid grid) {
        final IMEInventory<IAEItemStack> inventory = this.RandomAdditions$getNetworkInventory(grid);
        final IAEItemStack blank = this.RandomAdditions$blankPatternStack();
        if (inventory != null && blank != null) {
            inventory.injectItems(blank, Actionable.MODULATE, ((ContainerPatternEncoder) (Object) this).getActionSource());
        }
    }

    @Unique
    private IMEInventory<IAEItemStack> RandomAdditions$getNetworkInventory(final IGrid grid) {
        final IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (storageGrid == null) {
            return null;
        }
        final IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        return storageGrid.getInventory(channel);
    }

    @Unique
    private IAEItemStack RandomAdditions$blankPatternStack() {
        final ItemStack blank = this.RandomAdditions$blankPatternItemStack();
        if (blank.isEmpty()) {
            return null;
        }
        final AEItemStack stack = AEItemStack.fromItemStack(blank);
        return stack == null ? null : stack.setStackSize(1);
    }

    @Unique
    private ItemStack RandomAdditions$blankPatternItemStack() {
        final IItemDefinition blankPattern = AEApi.instance().definitions().materials().blankPattern();
        final Optional<ItemStack> blankPatternStack = blankPattern.maybeStack(1);
        return blankPatternStack.orElse(ItemStack.EMPTY);
    }

    @Unique
    private void RandomAdditions$clearPendingPatternUpload() {
        this.RandomAdditions$pendingPatternUpload = ItemStack.EMPTY;
        this.RandomAdditions$pendingPatternUploadTargets = new ArrayList<>();
        this.RandomAdditions$pendingPatternUploadAt = 0L;
    }
}
