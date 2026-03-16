package com.lw.random_additions;

import com.lw.random_additions.cilent.handler.MEStorageInfoProvider;
import com.lw.random_additions.common.handler.KeyHandler;
import com.lw.random_additions.common.handler.NetworkHandler;
import mcjty.theoneprobe.TheOneProbe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class RandomAdditions {

    public static String MOD_ID = "RandomAdditions";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        NetworkHandler.registerPackets();
        TheOneProbe.theOneProbeImp.registerProvider(new MEStorageInfoProvider());

        if (event.getSide() == Side.CLIENT) {
            KeyHandler.init();
            MinecraftForge.EVENT_BUS.register(new KeyHandler());
        }
    }
}
