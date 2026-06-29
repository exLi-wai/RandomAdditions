package com.lw.random_additions.common.mixins.ae2;

import appeng.api.config.ActionItems;
import appeng.client.gui.widgets.GuiImgButton;
import com.lw.random_additions.api.PatternUploadScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GuiImgButton.class, remap = false)
public abstract class MixinGuiImgButton {

    @Shadow
    @Final
    private Enum buttonSetting;

    @Inject(method = "getMessage", at = @At("RETURN"), cancellable = true)
    private void RandomAdditions$appendPatternUploadHint(final CallbackInfoReturnable<String> cir) {
        if (this.buttonSetting == ActionItems.ENCODE
                && Minecraft.getMinecraft().currentScreen instanceof PatternUploadScreen) {
            cir.setReturnValue(cir.getReturnValue() + "\n" + I18n.format("random_additions.ae2.pattern_upload.hint"));
        }
    }
}
