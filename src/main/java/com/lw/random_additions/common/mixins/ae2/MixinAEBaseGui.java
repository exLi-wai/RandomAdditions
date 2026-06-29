package com.lw.random_additions.common.mixins.ae2;

import appeng.client.gui.AEBaseGui;
import com.lw.random_additions.api.PatternUploadScreen;
import appeng.integration.modules.jei.JEIPlugin;
import mezz.jei.api.IBookmarkOverlay;
import mezz.jei.api.IJeiRuntime;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = AEBaseGui.class, remap = false)
public abstract class MixinAEBaseGui {

    @Inject(method = "bookmarkedJEIghostItem", at = @At("HEAD"), cancellable = true)
    private void skipInvalidJeiBookmarkOverlay(final int mouseX, final int mouseY, final CallbackInfo ci) {
        final IJeiRuntime runtime = JEIPlugin.runtime;
        if (runtime == null || JEIPlugin.aeGuiHandler == null) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void RandomAdditions$handlePatternUploadClick(final int mouseX, final int mouseY, final int mouseButton,
                                                          final CallbackInfo ci) {
        if ((Object) this instanceof PatternUploadScreen
                && ((PatternUploadScreen) (Object) this).RandomAdditions$handlePatternUploadMouseClick(mouseX, mouseY, mouseButton)) {
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void RandomAdditions$drawPatternUploadOverlay(final int mouseX, final int mouseY, final float partialTicks,
                                                          final CallbackInfo ci) {
        if ((Object) this instanceof PatternUploadScreen) {
            ((PatternUploadScreen) (Object) this).RandomAdditions$drawPatternUploadOverlay(mouseX, mouseY);
        }
    }

    @Inject(method = "mouseWheelEvent", at = @At("HEAD"), cancellable = true)
    private void RandomAdditions$handlePatternUploadWheel(final int mouseX, final int mouseY, final int wheelDelta,
                                                          final CallbackInfo ci) {
        if ((Object) this instanceof PatternUploadScreen
                && ((PatternUploadScreen) (Object) this).RandomAdditions$handlePatternUploadMouseWheel(wheelDelta)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void RandomAdditions$handlePatternUploadMouseInput(final CallbackInfo ci) throws IOException {
        if (!((Object) this instanceof PatternUploadScreen)) {
            return;
        }
        final PatternUploadScreen screen = (PatternUploadScreen) (Object) this;
        if (!screen.RandomAdditions$isPatternUploadOverlayVisible()) {
            return;
        }
        final int wheelDelta = Mouse.getEventDWheel();
        if (wheelDelta != 0 && screen.RandomAdditions$handlePatternUploadMouseWheel(wheelDelta)) {
            ci.cancel();
        }
    }
}
