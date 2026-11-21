package net.attarax.arbcraft.menu;


import net.attarax.arbcraft.gui.CustomMenu;
import net.attarax.arbcraft.menu.ExperienceConverterMenu;
import net.attarax.arbcraft.registry.ModRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {

    public static final DeferredHolder<MenuType<?>, MenuType<ExperienceConverterMenu>> EXPERIENCE_CONVERTER =
            ModRegistries.MENU_TYPES.register("experience_converter",
                    () -> IMenuTypeExtension.create(ExperienceConverterMenu::create));
    public static final DeferredHolder<MenuType<?>, MenuType<ExperienceConverterMenu>> CUSTOM_MENU =
            ModRegistries.MENU_TYPES.register("custom_menu",
                    () -> IMenuTypeExtension.create(ExperienceConverterMenu::create));
    public static void init() {
        // This method is called to ensure the class is loaded
    }

}

//package net.attarax.arbcraft.gui;
//
//import java.util.function.Supplier;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.flag.FeatureFlags;
//import net.minecraft.world.inventory.MenuType;
//import net.neoforged.neoforge.registries.DeferredRegister;
//
//public class ModMenuTypes {
//    public static final DeferredRegister<MenuType<?>> MENU_TYPES;
//    public static final Supplier<MenuType<CustomMenu>> CUSTOM_MENU;
//
//    public ModMenuTypes() {
//    }
//
//    static {
//        MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, "arbcraft");
//        CUSTOM_MENU = MENU_TYPES.register("custom_menu", () -> new MenuType(CustomMenu::new, FeatureFlags.VANILLA_SET));
//    }
//}
