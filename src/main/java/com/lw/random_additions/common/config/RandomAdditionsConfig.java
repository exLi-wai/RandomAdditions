package com.lw.random_additions.common.config;

import com.lw.random_additions.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID , name = "RandomAdditions/RandomAdditionsConfig")
@Config.LangKey("RandomAdditions.config.title")
public class RandomAdditionsConfig {

    @Config.Name("thaumcraft")
    public static final Thaumcraft THAUMCRAFT = new Thaumcraft();

    @Config.Name("compatibility")
    public static final Compatibility COMPATIBILITY = new Compatibility();


    public static final class Thaumcraft{

        @Config.Comment({
                "CannotDissolveIn  Thaumcraft Crucible/EssentiaSmeltery Items",
                "format: Item ID",
                "example: minecraft:dirt"
        })
        @Config.Name("Thaumcraft Crucible Insoluble Item Whitelist")
        @Config.RequiresMcRestart
        public String[] CrucibleInsolubleWhitelist = {
                "minecraft:dirt",
                "minecraft:grass"
        };

        @Config.Name("Thaumcraft Essentia Smeltery Insoluble Item Whitelist")
        @Config.RequiresMcRestart
        public String[] SmelterInsolubleWhitelist = {
                "minecraft:dirt",
                "minecraft:grass"
        };

    @Config.Name("Thaumonomicon Submissions Are Not Allowed TinkersConstruct Tools")
    @Config.RequiresMcRestart
    public boolean CheckEnchantedPlaceholder = true;

    }

    public static final class Compatibility{
        @Config.Name("enableMEGirdNodeAmount")
        @Config.Comment("Enable The One Probe compatibility MEGirdNodeAmount")
        public boolean EnableMEGirdNodeAmount = true;

        @Config.Name("enableMEStorageInfoProvider")
        @Config.Comment("Enable The One Probe compatibility MEStorageInfoProvider")
        public boolean EnableMEStorageInfoProvider = true;

        @Config.Name("enable MEStorageInfoProvider in onItemTooltip")
        @Config.Comment("enable MEStorageInfoProvider in onItemTooltip")
        public boolean EnableMEStorageInfoProviderInOnItemTooltip = true;
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
