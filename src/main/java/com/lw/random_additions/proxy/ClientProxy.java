package com.lw.random_additions.proxy;

import com.lw.random_additions.cilent.handler.KeyHandler;
import com.lw.random_additions.common.integration.top.MEGirdNodeAmount;
import com.lw.random_additions.common.integration.top.MEStorageInfoProvider;
import mcjty.theoneprobe.TheOneProbe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        KeyHandler.init();
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(this);
        TheOneProbe.theOneProbeImp.registerProvider(new MEStorageInfoProvider());
        TheOneProbe.theOneProbeImp.registerProvider(new MEGirdNodeAmount());
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

}
