package com.lw.random_additions.common.mixins.ae2;

import appeng.client.gui.AEBaseGui;
import appeng.integration.modules.jei.JEIPlugin;
import mezz.jei.api.IBookmarkOverlay;
import mezz.jei.api.IJeiRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseGui.class, remap = false)
public abstract class MixinAEBaseGui {

    @Inject(method = "bookmarkedJEIghostItem", at = @At("HEAD"), cancellable = true)
    private void skipInvalidJeiBookmarkOverlay(final int mouseX, final int mouseY, final CallbackInfo ci) {
        final IJeiRuntime runtime = JEIPlugin.runtime;
        if (runtime == null || JEIPlugin.aeGuiHandler == null) {
            ci.cancel();
        }
    }
}
