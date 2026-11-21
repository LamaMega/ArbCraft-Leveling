package net.attarax.arbcraft;

import net.attarax.arbcraft.blocks.ModBlocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;


public class ModTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArbCraft.MODID);
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARB_CRAFT = REGISTRY.register("arb_craft",
//            () -> CreativeModeTab.builder().title(Component.translatable("item_group.arbcraft.arb_craft")).icon(() -> new ItemStack(ModBlocks.ORE_BLOCK_OF_ARBRIUM.get())).displayItems((parameters, tabData) -> {
//                tabData.accept(ModBlocks.ORE_BLOCK_OF_ARBRIUM.get().asItem());
//            }).build());
}
