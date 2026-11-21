package net.attarax.arbcraft.blocks;

import net.attarax.arbcraft.blockentity.CrusherBlockEntity;
import net.attarax.arbcraft.blockentity.ExperienceConverterBlockEntity;
import net.attarax.arbcraft.menu.CrusherMenu;
import net.attarax.arbcraft.menu.ExperienceConverterMenu;
import net.attarax.arbcraft.registry.ModRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlocks {

    // Experience Converter Block
    public static final DeferredHolder<Block, ExperienceConverterBlock> EXPERIENCE_CONVERTER =
            ModRegistries.BLOCKS.register("experience_converter",
                    () -> new ExperienceConverterBlock(BlockBehaviour.Properties.of()
                            .strength(30.0f, 1200.0f)  // High blast resistance (1200 = obsidian level)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST)));  // Optional: adds metal sound

    public static final DeferredHolder<Item, BlockItem> EXPERIENCE_CONVERTER_ITEM =
            ModRegistries.ITEMS.register("experience_converter",
                    () -> new BlockItem(EXPERIENCE_CONVERTER.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExperienceConverterBlockEntity>> EXPERIENCE_CONVERTER_BLOCK_ENTITY =
            ModRegistries.BLOCK_ENTITIES.register("experience_converter",
                    () -> BlockEntityType.Builder.of(ExperienceConverterBlockEntity::new,
                            EXPERIENCE_CONVERTER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ExperienceConverterMenu>> EXPERIENCE_CONVERTER_MENU =
            ModRegistries.MENU_TYPES.register("experience_converter",
                    () -> IMenuTypeExtension.create(ExperienceConverterMenu::create));

    // Crusher Block - Using consistent ModRegistries pattern
    public static final DeferredHolder<Block, CrusherBlock> CRUSHER_BLOCK =
            ModRegistries.BLOCKS.register("crusher", () ->
                    new CrusherBlock(BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final DeferredHolder<Item, BlockItem> CRUSHER_BLOCK_ITEM =
            ModRegistries.ITEMS.register("crusher", () ->
                    new BlockItem(CRUSHER_BLOCK.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrusherBlockEntity>> CRUSHER_BLOCK_ENTITY =
            ModRegistries.BLOCK_ENTITIES.register("crusher", () ->
                    BlockEntityType.Builder.of(CrusherBlockEntity::new, CRUSHER_BLOCK.get()).build(null));

    // FIXED: Use CrusherMenu::create instead of CrusherMenu::new
    public static final DeferredHolder<MenuType<?>, MenuType<CrusherMenu>> CRUSHER_MENU =
            ModRegistries.MENU_TYPES.register("crusher", () ->
                    IMenuTypeExtension.create(CrusherMenu::create));

    public static void init() {
        // This method is called to ensure the class is loaded
    }
}