
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.attarax.arbcraft.items;

import net.attarax.arbcraft.ArbCraft;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

//import net.mcreator.arbcraft.item.ShovelOfMotalItem;
//import net.attarax.arbcraft.items.PickaxeOfHolyMotal;
//import net.mcreator.arbcraft.item.IngotOfMotalItem;
//import net.mcreator.arbcraft.item.IngotOfHolyMotalItem;
//import net.mcreator.arbcraft.item.HolySwordOfMotalItem;
//import net.mcreator.arbcraft.ArbcraftMod;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(ArbCraft.MODID);
    //public static final DeferredItem<Item> BLOCK_OF_MOTAL = block(ArbcraftModBlocks.BLOCK_OF_MOTAL);
    //public static final DeferredItem<Item> INGOT_OF_MOTAL = register("ingot_of_motal", IngotOfMotalItem::new);
    //public static final DeferredItem<Item> HOLY_SWORD_OF_MOTAL = register("holy_sword_of_motal", HolySwordOfMotalItem::new);
   // public static final DeferredItem<Item> SHOVEL_OF_MOTAL = register("shovel_of_motal", ShovelOfMotalItem::new);
//    public static final DeferredItem<Item> PICKAXE_OF_HOLY_MOTAL = register("pickaxe_of_holy_motal", PickaxeOfHolyMotal::new);
////    public static final DeferredItem<Item> INGOT_OF_HOLY_MOTAL = register("ingot_of_holy_motal", IngotOfHolyMotalItem::new);
//    public static final DeferredItem<Item> ORE_BLOCK_OF_ARBRIUM = block(ArbCraft.ORE_BLOCK_OF_ARBRIUM);

    // Start of user code block custom items
    // End of user code block custom items
    private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
        return REGISTRY.registerItem(name, supplier, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return REGISTRY.registerItem(block.getId().getPath(), properties -> new BlockItem(block.get(), properties), new Item.Properties());
    }
}
