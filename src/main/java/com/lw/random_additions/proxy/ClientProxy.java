package com.lw.random_additions.proxy;

import com.lw.random_additions.client.PatternUploadClient;
import com.lw.random_additions.client.handler.KeyHandler;
import com.lw.random_additions.common.init.Mods;
import com.lw.random_additions.common.integration.ae2.patternupload.PatternUploadTargetInfo;
import com.lw.random_additions.common.integration.tconstruct.ModRemoveInscription;
import com.lw.random_additions.common.integration.top.TheOneProbeCompat;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        KeyHandler.init();
        TheOneProbeCompat.register();
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        if(Mods.TC.isLoaded()){
            ModRemoveInscription.addTConstructBookEntry();
        }
    }

    @Override
    public void handlePatternUploadTargets(List<PatternUploadTargetInfo> targets) {
        Minecraft.getMinecraft().addScheduledTask(() -> PatternUploadClient.showTargets(targets));
    }
}
