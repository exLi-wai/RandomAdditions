package com.lw.random_additions.client;

import com.lw.random_additions.api.PatternUploadScreen;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetInfo;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class PatternUploadClient {

    private PatternUploadClient() {
    }

    public static void showTargets(List<PatternUploadTargetInfo> targets) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen instanceof PatternUploadScreen) {
            ((PatternUploadScreen) minecraft.currentScreen).RandomAdditions$showPatternUploadTargets(targets);
        }
    }
}
