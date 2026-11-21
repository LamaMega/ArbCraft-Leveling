package net.attarax.arbcraft.events;

import net.attarax.arbcraft.config.ExperienceConverterConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.File;

@EventBusSubscriber
public class ConfigReloadHandler {
    private static int tickCounter = 0; // int MAX = 2.147B
    private static long lastModified = 0; //long MAX =

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        tickCounter++;

        if (tickCounter < 200) { // 10 seconds
            return;
        }

        tickCounter = 0;

        //File Modification Check: It was used to reload the config file only if the config file was edited after the game was started
//        File file = ExperienceConverterConfig.getConfigFile();
//
//        long modified = file.lastModified();
//
//        if (modified != lastModified) {
//
//            lastModified = modified;
//
//            ExperienceConverterConfig.load();
//
//            System.out.println("[ArbCraft] Config reloaded");
//        }
        ExperienceConverterConfig.load();

    }

    public static void initialize() {
        lastModified = ExperienceConverterConfig
                .getConfigFile()
                .lastModified();
    }
}