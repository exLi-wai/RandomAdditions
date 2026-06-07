package com.lw.random_additions.common.integration.top;

import com.lw.random_additions.common.config.RandomAdditionsConfig;
import com.lw.random_additions.common.init.Mods;
import mcjty.theoneprobe.TheOneProbe;

public class TheOneProbeCompat {

    public static void register(){
        if (Mods.TOP.isLoaded() && RandomAdditionsConfig.COMPATIBILITY.EnableMEGirdNodeAmount){
            TheOneProbe.theOneProbeImp.registerProvider(new MEGirdNodeAmount());
        }
        if (Mods.TOP.isLoaded() && RandomAdditionsConfig.COMPATIBILITY.EnableMEStorageInfoProvider){
            TheOneProbe.theOneProbeImp.registerProvider(new MEStorageInfoProvider());
        }
    }
}
