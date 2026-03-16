package com.lw.random_additions.config;

import com.lw.random_additions.Tags;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID , name = "RandomAdditions/RandomAdditionsConfig")
@Config.LangKey("RandomAdditions.config.title")
public class RandomAdditionsConfig {

    @Config.Comment({
            "无法溶解在 坩埚/源质冶炼炉 中的物品",
            "格式: 物品ID",
            "示例: minecraft:dirt"
    })
    @Config.Name("神秘坩埚不可溶解物品白名单")
    @Config.RequiresMcRestart
    public static String[] CrucibleInsolubleWhitelist = {
            "minecraft:dirt",
            "minecraft:grass"
    };

    @Config.Name("神秘源质冶炼炉不可溶解物品白名单")
    @Config.RequiresMcRestart
    public static String[] SmelterInsolubleWhitelist = {
            "minecraft:dirt",
            "minecraft:grass"
    };

    public static boolean crucibleWhitelist(ItemStack item) {
        for (String itemId : CrucibleInsolubleWhitelist) {
            if (item.getItem().getRegistryName().toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean TileSmelterWhitelist(ItemStack item) {
        for (String itemId : SmelterInsolubleWhitelist) {
            if (item.getItem().getRegistryName().toString().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
