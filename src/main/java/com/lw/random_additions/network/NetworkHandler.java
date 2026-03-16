package com.lw.random_additions.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

    private static int packetId = 0;
    public static final SimpleNetworkWrapper WirelessDeposit = NetworkRegistry.INSTANCE.newSimpleChannel("random_additions:wireless_input");

    public static void registerPackets() {
        WirelessDeposit.registerMessage(
                WirelessInput.Handler.class,
                WirelessInput.class,
                packetId++,
                Side.SERVER
        );
    }
}

