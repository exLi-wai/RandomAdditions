package com.lw.random_additions.common.init;

import net.minecraftforge.fml.common.Loader;

public enum Mods {
    DRA("draconicevolution"),
    BAUBLES("baubles"),
    RD("randomthings"),
    AE2FC("ae2fc"),
    MMCE("modularmachinery"),
    TC("tconstruct"),
    TOP("theoneprobe");

    public final String modid;

    Mods(String modid) {
        this.modid = modid;
    }

    public boolean isLoaded() {
        return Loader.isModLoaded(this.modid);
    }
}
