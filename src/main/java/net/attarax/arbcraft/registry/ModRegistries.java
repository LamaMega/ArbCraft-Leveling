package net.attarax.arbcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {
    public static final String MOD_ID = "arbcraft";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENU_TYPES.register(bus);
    }
}






























//package net.attarax.arbcraft.registry;
//
//import net.attarax.arbcraft.blocks.ExperienceConverterBlock;
//import net.attarax.arbcraft.blockentity.ExperienceConverterBlockEntity;
//import net.attarax.arbcraft.menu.ExperienceConverterMenu;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.world.inventory.MenuType;
//import net.minecraft.world.item.BlockItem;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockBehaviour;
//import net.neoforged.bus.api.IEventBus;
//import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
//import net.neoforged.neoforge.registries.DeferredHolder;
//import net.neoforged.neoforge.registries.DeferredRegister;
//
//public class ModRegistries {
//    public static final String MOD_ID = "arbcraft";
//
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);
//    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
//    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);
//
//    public static void register(IEventBus bus) {
//        BLOCKS.register(bus);
//        ITEMS.register(bus);
//        BLOCK_ENTITIES.register(bus);
//        MENU_TYPES.register(bus);
//    }
//}
//
//class ModBlocks {
//    public static final DeferredHolder<Block, ExperienceConverterBlock> EXPERIENCE_CONVERTER =
//            ModRegistries.BLOCKS.register("experience_converter",
//                    () -> new ExperienceConverterBlock(BlockBehaviour.Properties.of()
//                            .strength(3.5f)
//                            .requiresCorrectToolForDrops()));
//
//    static {
//        ModRegistries.ITEMS.register("experience_converter",
//                () -> new BlockItem(EXPERIENCE_CONVERTER.get(), new Item.Properties()));
//    }
//}
//
//class ModBlockEntities {
//    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExperienceConverterBlockEntity>> EXPERIENCE_CONVERTER =
//            ModRegistries.BLOCK_ENTITIES.register("experience_converter",
//                    () -> BlockEntityType.Builder.of(ExperienceConverterBlockEntity::new,
//                            ModBlocks.EXPERIENCE_CONVERTER.get()).build(null));
//}
//
//class ModMenuTypes {
//    public static final DeferredHolder<MenuType<?>, MenuType<ExperienceConverterMenu>> EXPERIENCE_CONVERTER =
//            ModRegistries.MENU_TYPES.register("experience_converter",
//                    () -> IMenuTypeExtension.create(ExperienceConverterMenu::create));
//}