package com.lw.random_additions.api;

import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetInfo;

import java.util.List;

public interface PatternUploadScreen {

    void RandomAdditions$showPatternUploadTargets(List<PatternUploadTargetInfo> targets);

    boolean RandomAdditions$handlePatternUploadMouseClick(int mouseX, int mouseY, int mouseButton);

    void RandomAdditions$drawPatternUploadOverlay(int mouseX, int mouseY);

    boolean RandomAdditions$handlePatternUploadMouseWheel(int wheelDelta);

    boolean RandomAdditions$handlePatternUploadKeyTyped(char typedChar, int keyCode);

    boolean RandomAdditions$isPatternUploadOverlayVisible();
}
