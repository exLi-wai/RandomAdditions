package com.lw.random_additions;

import com.lw.random_additions.common.network.NetworkHandler;
import com.lw.random_additions.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "required-after:appliedenergistics2"
)
public class RandomAdditions {

    @Mod.Instance(Tags.MOD_ID)
    public static RandomAdditions instance;

    @SidedProxy(
            clientSide = "com.lw.random_additions.proxy.ClientProxy",
            serverSide = "com.lw.random_additions.proxy.CommonProxy"
    )
    public static CommonProxy proxy;
    public static String MOD_ID = Tags.MOD_ID;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        NetworkHandler.registerPackets();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
