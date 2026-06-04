package com.lw.random_additions.common.network;

import com.lw.random_additions.common.init.Mods;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

public class NetworkHandler {

    private static int packetId = 0;
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("ra");

    public static void registerPackets() {
        CHANNEL.registerMessage(PacketWirelessInput.Handler.class, PacketWirelessInput.class, packetId++, Side.SERVER);
        if(Mods.RD.isLoaded()){
            CHANNEL.registerMessage(PacketTimeBottle.Handler.class, PacketTimeBottle.class, packetId++, Side.SERVER);
        }

    }
}

