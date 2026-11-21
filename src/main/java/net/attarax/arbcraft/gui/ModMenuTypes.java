package net.attarax.arbcraft.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, "arbcraft"); // Replace yourmodid

    public static final Supplier<MenuType<CustomMenu>> CUSTOM_MENU =
            MENU_TYPES.register("custom_menu", () -> new MenuType<>(CustomMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
    }