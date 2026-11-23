package net.attarax.arbcraft.craftButton;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Adds a toggle button next to the player inventory and crafting table screens:
 *   inventory screen      -> "Crafting"  opens a crafting table menu
 *   crafting table screen -> "Inventory" closes it and goes back to the player inventory
 */
@EventBusSubscriber(modid = ArbCraft.MODID, value = Dist.CLIENT)
public class CraftingButtonClient {

    private static final int BUTTON_WIDTH = 56;
    private static final int BUTTON_HEIGHT = 20;

    private static Button button;

    private static boolean isSupported(Object screen) {
        return screen instanceof InventoryScreen || screen instanceof CraftingScreen;
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !isSupported(screen)) return;

        boolean inCraftingScreen = screen instanceof CraftingScreen;
        button = Button.builder(
                        Component.literal(inCraftingScreen ? "Inventory" : "Crafting"),
                        b -> {
                            if (inCraftingScreen) {
                                backToInventory();
                            } else {
                                PacketDistributor.sendToServer(new OpenCraftingPayload());
                            }
                        })
                .bounds(screen.getGuiLeft() + screen.getXSize() + 40, screen.getGuiTop() + 80,
                        BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        button.visible = hasRequiredLevel();
        event.addListener(button);
    }

    private static boolean hasRequiredLevel() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.experienceLevel >= CraftingButtonNetwork.REQUIRED_LEVEL;
    }

    /** Closes the crafting menu (the server returns the grid items) and reopens the inventory. */
    private static void backToInventory() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        player.closeContainer();
        mc.setScreen(new InventoryScreen(player));
    }

    /** The recipe book moves the screen sideways without re-initialising, so follow it. */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (button == null) return;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !isSupported(screen)) return;

        button.setPosition(screen.getGuiLeft() + screen.getXSize() + 40, screen.getGuiTop() + 80);
        button.visible = hasRequiredLevel(); // the level can change while the screen is open
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (isSupported(event.getScreen())) button = null;
    }
}