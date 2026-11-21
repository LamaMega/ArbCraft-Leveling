package net.attarax.arbcraft.components;

import com.mojang.serialization.Codec;
import net.attarax.arbcraft.ArbCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ArbCraft.MODID);

    /**
     * Stores how many durability upgrades have been applied to an item.
     * Persists across saves via Codec.INT serialization.
     */
    public static final Supplier<DataComponentType<Integer>> UPGRADE_COUNT =
            DATA_COMPONENT_TYPES.register("upgrade_count",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}