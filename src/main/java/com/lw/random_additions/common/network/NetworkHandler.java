package com.lw.random_additions.common.network;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

public class NetworkHandler {

    private static int packetId = 0;
    public static final SimpleNetworkWrapper WirelessDeposit = NetworkRegistry.INSTANCE.newSimpleChannel("random_additions:wireless_input");

    public static void registerPackets() {
        WirelessDeposit.registerMessage(
                PacketWirelessInput.Handler.class,
                PacketWirelessInput.class,
                packetId++,
                Side.SERVER
        );

        if(Loader.isModLoaded("randomthings")){
            WirelessDeposit.registerMessage(
                    PacketTimeBottle.Handler.class,
                    PacketTimeBottle.class,
                    packetId++,
                    Side.SERVER
            );
        }

    }
}

