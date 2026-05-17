package com.lw.random_additions.cilent.handler;

import com.brandon3055.draconicevolution.DraconicEvolution;
import com.lw.random_additions.common.network.NetworkHandler;
import com.lw.random_additions.common.network.PacketTimeBottle;
import com.lw.random_additions.common.network.PacketWirelessInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyHandler {

    public static KeyBinding WirelessInputKey;
    public static KeyBinding OpenBaubleGUIKey;
    public static KeyBinding TimeBottleKey;

    public static void init() {
        WirelessInputKey = new KeyBinding(
                "key.random_additions.wireless_input",
                KeyConflictContext.UNIVERSAL,
                KeyModifier.ALT,
                Keyboard.KEY_V,
                "key.random_additions"
        );
        ClientRegistry.registerKeyBinding(WirelessInputKey);

        if (Loader.isModLoaded("draconicevolution")) {
            OpenBaubleGUIKey = new KeyBinding(
                    "key.random_additions.open_bauble_gui",
                    KeyConflictContext.UNIVERSAL,
                    KeyModifier.NONE,
                    Keyboard.KEY_G,
                    "key.random_additions"
            );
            ClientRegistry.registerKeyBinding(OpenBaubleGUIKey);
        }

        if(Loader.isModLoaded("randomthings")){
            TimeBottleKey = new KeyBinding(
                    "key.random_additions.time_bottle",
                    KeyConflictContext.UNIVERSAL,
                    KeyModifier.NONE,
                    Keyboard.KEY_C,
                    "key.random_additions"
            );
            ClientRegistry.registerKeyBinding(TimeBottleKey);
        }

    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (WirelessInputKey.isPressed()) {
            NetworkHandler.WirelessDeposit.sendToServer(new PacketWirelessInput(0, 0));
        }

        if (Loader.isModLoaded("draconicevolution")) {
            if (OpenBaubleGUIKey.isPressed()) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player == null || Minecraft.getMinecraft().currentScreen != null) return;

                FMLNetworkHandler.openGui(player, DraconicEvolution.instance, 3,
                        player.world,
                        (int) player.posX,
                        (int) player.posY,
                        (int) player.posZ);
            }
        }

        if(Loader.isModLoaded("randomthings")){
            if (TimeBottleKey.isPressed()) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (player == null) return;

                RayTraceResult ray = player.rayTrace(4.5D, 1.0F);
                if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos pos = ray.getBlockPos();
                    EnumFacing side = ray.sideHit;
                    float hitX = (float) (ray.hitVec.x - pos.getX());
                    float hitY = (float) (ray.hitVec.y - pos.getY());
                    float hitZ = (float) (ray.hitVec.z - pos.getZ());
                    NetworkHandler.WirelessDeposit.sendToServer(new PacketTimeBottle(pos, side, hitX, hitY, hitZ));
                }
            }
        }

    }
}

