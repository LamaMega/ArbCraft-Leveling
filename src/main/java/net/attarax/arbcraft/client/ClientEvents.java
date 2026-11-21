package net.attarax.arbcraft.client;

import net.attarax.arbcraft.ArbCraft;
import net.attarax.arbcraft.blocks.ModBlocks;
import net.attarax.arbcraft.client.ExperienceConverterScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = ArbCraft.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModBlocks.EXPERIENCE_CONVERTER_MENU.get(),
                ExperienceConverterScreen::new
        );
    }
}