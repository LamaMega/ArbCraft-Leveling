package net.attarax.arbcraft.toollevel;

import com.mojang.serialization.Codec;
import net.attarax.arbcraft.ArbCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the data component that stores how many times a tool has been used.
 * The value is saved with the item and synced to the client (needed for the tooltip).
 */
public class ToolLevelComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(ArbCraft.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_USES =
            COMPONENTS.registerComponentType("tool_uses",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}