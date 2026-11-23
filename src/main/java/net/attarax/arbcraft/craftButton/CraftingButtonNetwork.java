package net.attarax.arbcraft.craftButton;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registers the packet and opens a crafting table menu on the server when it arrives. */
@EventBusSubscriber(modid = ArbCraft.MODID)
public class CraftingButtonNetwork {

    /** Minimum player (experience) level needed to use the crafting button. */
    public static final int REQUIRED_LEVEL = 100;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                OpenCraftingPayload.TYPE,
                OpenCraftingPayload.STREAM_CODEC,
                CraftingButtonNetwork::handle);
    }

    private static void handle(OpenCraftingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (player.experienceLevel < REQUIRED_LEVEL) return; // the server enforces it, not just the UI

            // The menu needs a real level + position: with ContainerLevelAccess.NULL, access.execute()
            // does nothing, so the recipe result was never computed and the grid items were never
            // returned on close. A real block isn't there though, so stillValid is overridden
            // to keep the menu open wherever the player is.
            ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, p) -> new CraftingMenu(containerId, inventory, access) {
                        @Override
                        public boolean stillValid(Player player) {
                            return true;
                        }
                    },
                    Component.translatable("container.crafting")));
        });
    }
}