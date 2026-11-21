package net.attarax.arbcraft.CreativeTab;

import net.attarax.arbcraft.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "arbcraft");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARBCRAFT_TAB =
            CREATIVE_MODE_TABS.register("arbcraft_tab", () -> CreativeModeTab.builder()
                    //Experience Converter
                    .title(Component.translatable("itemGroup.arbcraft"))
                    .icon(() -> new ItemStack(ModBlocks.EXPERIENCE_CONVERTER_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        // Changed: Call .get() to extract the actual item
                        output.accept(ModBlocks.EXPERIENCE_CONVERTER_ITEM.get());
                        //output.accept(ModBlocks.CRUSHER_BLOCK_ITEM.get());
                    })
                    //Crusher
//                    .title(Component.translatable("itemGroup.arbcraft"))
//                    .icon(() -> new ItemStack(ModBlocks.CRUSHER_BLOCK_ITEM.get()))
//                    .displayItems((parameters, output) -> {
//
//                    })
                    //

                    .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}