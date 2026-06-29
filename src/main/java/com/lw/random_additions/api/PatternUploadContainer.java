package com.lw.random_additions.api;

import net.minecraft.entity.player.EntityPlayerMP;

public interface PatternUploadContainer {

    void RandomAdditions$requestPatternUpload(EntityPlayerMP player);

    void RandomAdditions$sendPatternToTarget(EntityPlayerMP player, int index);
}
