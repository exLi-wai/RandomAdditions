package com.lw.random_additions.cilent.handler;

import com.lw.random_additions.network.NetworkHandler;
import com.lw.random_additions.network.WirelessInput;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyHandler {

    public static KeyBinding WirelessInputKey;

    public static void init() {
        WirelessInputKey = new KeyBinding(
                "key.random_additions.wireless_input",
                KeyConflictContext.UNIVERSAL,
                KeyModifier.ALT,
                Keyboard.KEY_V,
                "key.random_additions"
        );
        ClientRegistry.registerKeyBinding(WirelessInputKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WirelessInputKey.isPressed()) {
            NetworkHandler.WirelessDeposit.sendToServer(new WirelessInput(0, 0));
        }
    }
}

