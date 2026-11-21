

package net.attarax.arbcraft.client;

import net.attarax.arbcraft.config.ExperienceConverterConfig;
import net.attarax.arbcraft.menu.ExperienceConverterMenu;
import net.attarax.arbcraft.network.WithdrawExperiencePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Renders the Experience Converter GUI using programmatic fills (no texture file needed).
 *
 * Layout (176 × 196, all coordinates relative to leftPos/topPos):
 *
 *  y= 6   Title                              XP: NNNN
 *  y=35   [  input slot  ]
 *  y=53   ████████ progress bar ░░░░░░░░░░
 *  y=57   Lvl 0 / 20                    80t · +0% XP
 *  y=68   [ ↑ Upgrade ] ← tooltip shows cost
 *  y=89   ─────────────── divider ───────────────
 *  y=98   [ player inventory rows ]
 *  y=156  [ hotbar ]
 */
public class ExperienceConverterScreen extends AbstractContainerScreen<ExperienceConverterMenu> {

    // ── Vanilla-palette fills ─────────────────────────────────────────────────
    private static final int COL_PANEL   = 0xFFC6C6C6;  // main grey panel
    private static final int COL_HILIGHT = 0xFFFFFFFF;  // raised / inset highlight
    private static final int COL_SHADOW  = 0xFF555555;  // raised / inset shadow
    private static final int COL_DARK    = 0xFF373737;  // deepest slot edge
    private static final int COL_SLOT    = 0xFF8B8B8B;  // slot interior

    private Button upgradeButton;

    // ─────────────────────────────────────────────────────────────────────────

    public ExperienceConverterScreen(ExperienceConverterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = 176;
        this.imageHeight = 196;
    }

//    @Override
//    protected void init() {
//        super.init();
//        this.titleLabelY     = 6;
//        this.inventoryLabelY = 86;
//
//        this.upgradeButton = Button.builder(
//                        Component.literal("\u2191 Upgrade"),
//                        btn -> Minecraft.getInstance().gameMode
//                                .handleInventoryButtonClick(this.menu.containerId, 0))
//                .bounds(this.leftPos + 60, this.topPos + 68, 56, 16)
//                .build();
//        this.addRenderableWidget(this.upgradeButton);
//        // Withdraw All button
//        this.addRenderableWidget(Button.builder(
//                        Component.literal("Withdraw All"),
//                        button -> {
//                            PacketDistributor.sendToServer(new WithdrawExperiencePacket(
//                                    menu.getBlockEntity().getBlockPos(), -1));
//                        })
//                .bounds(this.inventoryLabelX + 8, this.inventoryLabelY + 60, 75, 20)
//                .build());
//
//        // Withdraw 100 button
//        this.addRenderableWidget(Button.builder(
//                        Component.literal("Withdraw 100"),
//                        button -> {
//                            PacketDistributor.sendToServer(new WithdrawExperiencePacket(
//                                    menu.getBlockEntity().getBlockPos(), 100));
//                        })
//                .bounds(this.inventoryLabelX + 93, this.inventoryLabelY + 60, 75, 20)
//                .build());
//    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY     = 6;
        this.inventoryLabelY = 86;

        this.upgradeButton = Button.builder(
                        Component.literal("\u2191 Upgrade"),
                        btn -> Minecraft.getInstance().gameMode
                                .handleInventoryButtonClick(this.menu.containerId, 0))
                .bounds(this.leftPos + 60, this.topPos + 68, 56, 16)
                .build();
        this.addRenderableWidget(this.upgradeButton);

        // --- Anchor for your existing MAX button (set these to match your MAX button)
        int maxX = this.leftPos + 60;   // <-- change to your MAX button X
        int maxY = this.topPos + 68;     // <-- change to your MAX button Y
        int maxWidth = 56;               // <-- change to your MAX button width
        final int spacing = 4;
        final int withdrawW = 56;
        final int withdrawH = 16;

// Withdraw All (left of MAX)
        this.addRenderableWidget(Button.builder(
                        Component.literal("Withdraw All"),
                        button -> PacketDistributor.sendToServer(new WithdrawExperiencePacket(
                                menu.getBlockEntity().getBlockPos(), -1)))
                .bounds(maxX - withdrawW - spacing, maxY, withdrawW, withdrawH)
                .build());

// Withdraw 100 (right of MAX)
        this.addRenderableWidget(Button.builder(
                        Component.literal("Withdraw 100"),
                        button -> PacketDistributor.sendToServer(new WithdrawExperiencePacket(
                                menu.getBlockEntity().getBlockPos(), 100)))
                .bounds(maxX + maxWidth + spacing, maxY, withdrawW, withdrawH)
                .build());

    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    protected void containerTick() {
        super.containerTick();

        int     level    = this.menu.getUpgradeLevel();
        int     cost     = this.menu.getUpgradeCost();
        int     maxLevel = ExperienceConverterConfig.getMaxUpgradeLevel();
        boolean atMax    = maxLevel > 0 && level >= maxLevel;

        if (atMax) {
            this.upgradeButton.setMessage(Component.literal("\u2606 MAX"));
            this.upgradeButton.active = false;
            this.upgradeButton.setTooltip(Tooltip.create(Component.literal("Fully upgraded!")));
        } else {
            this.upgradeButton.setMessage(Component.literal("\u2191 Upgrade"));
            this.upgradeButton.active = true;
            String mat = ExperienceConverterConfig.getUpgradeMaterial().getDescription().getString();
            this.upgradeButton.setTooltip(Tooltip.create(
                    Component.literal("Cost: " + cost + " \u00d7 " + mat + " In Inventory")));
        }
    }

    // ── Background ───────────────────────────────────────────────────────────

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // ── Main panel ────────────────────────────────────────────────────────
        g.fill(x, y, x + w, y + h, COL_PANEL);

        // Raised-border effect (1 px each side)
        g.fill(x,         y,         x + w,     y + 1,     COL_HILIGHT); // top
        g.fill(x,         y,         x + 1,     y + h,     COL_HILIGHT); // left
        g.fill(x,         y + h - 1, x + w,     y + h,     COL_SHADOW);  // bottom
        g.fill(x + w - 1, y,         x + w,     y + h,     COL_SHADOW);  // right

        // ── Input slot ────────────────────────────────────────────────────────
        drawSunkenSlot(g, x + 80, y + 35);

        // ── Progress bar (below input slot) ───────────────────────────────────
        drawProgressBar(g, x + 71, y + 53);

        // ── Divider between block section and inventory ────────────────────────
        int divY = y + 91;
        g.fill(x + 7, divY,     x + w - 7, divY + 1, COL_SHADOW);
        g.fill(x + 7, divY + 1, x + w - 7, divY + 2, COL_HILIGHT);

        // ── Player inventory slots ─────────────────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSunkenSlot(g, x + 8 + col * 18, y + 98 + row * 18);
            }
        }

        // ── Hotbar slots ──────────────────────────────────────────────────────
        for (int col = 0; col < 9; col++) {
            drawSunkenSlot(g, x + 8 + col * 18, y + 156);
        }
    }

    /**
     * Draws a single 16 × 16 sunken slot at (slotX, slotY).
     * slotX/slotY should be the exact same values passed to new Slot(...) in the menu.
     */
    private void drawSunkenSlot(GuiGraphics g, int sx, int sy) {
        g.fill(sx - 1, sy - 1, sx + 16, sy,      COL_DARK);    // top shadow
        g.fill(sx - 1, sy - 1, sx,      sy + 16, COL_DARK);    // left shadow
        g.fill(sx - 1, sy + 16, sx + 17, sy + 17, COL_HILIGHT); // bottom highlight
        g.fill(sx + 16, sy - 1, sx + 17, sy + 17, COL_HILIGHT); // right highlight
        g.fill(sx, sy, sx + 16, sy + 16, COL_SLOT);             // interior
    }

    /** Simple green-fill progress bar, 34 × 4 px. */
    private void drawProgressBar(GuiGraphics g, int bx, int by) {
        int progress    = this.menu.getConversionProgress();
        int maxProgress = this.menu.getMaxProgress();
        int barW = 34;
        int barH = 4;

        // Track (dark background)
        g.fill(bx, by, bx + barW, by + barH, COL_DARK);

        // Filled portion (green)
        if (maxProgress > 0 && progress > 0) {
            int filled = (int) (barW * ((float) progress / maxProgress));
            g.fill(bx, by, bx + filled, by + barH, 0xFF55FF55);
        }
    }

    // ── Labels (translated to GUI-local coords by vanilla before call) ────────

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY); // title + "Inventory"

        int level      = this.menu.getUpgradeLevel();
        int maxLevel   = ExperienceConverterConfig.getMaxUpgradeLevel();
        int storedXP   = this.menu.getStoredExperience();
        int convTime   = this.menu.getMaxProgress();
        int bonusPct   = level * ExperienceConverterConfig.getUpgradeExpBonusPct();

        // Stored XP — right-aligned next to the title
        String xpStr = "XP: " + storedXP;
        g.drawString(this.font, xpStr,
                this.imageWidth - this.font.width(xpStr) - 8, this.titleLabelY,
                0x55FF55, false);

        // Level (left) and stats (right) on the same row
        String levelStr = (maxLevel > 0)
                ? "Lvl " + level + " / " + maxLevel
                : "Lvl " + level;
        String statsStr = convTime + "t  \u00b7  +" + bonusPct + "% XP";

        g.drawString(this.font, levelStr, 8, 57, 0xFFD700, false);
        g.drawString(this.font, statsStr,
                this.imageWidth - this.font.width(statsStr) - 8, 57, 0x55FFFF, false);
    }

    // ── Full render ───────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }
}




















































//package net.attarax.arbcraft.client;
//
//import net.attarax.arbcraft.menu.ExperienceConverterMenu;
//import net.attarax.arbcraft.network.WithdrawExperiencePacket;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Inventory;
//import net.neoforged.neoforge.network.PacketDistributor;
//
//public class ExperienceConverterScreen extends AbstractContainerScreen<ExperienceConverterMenu> {
//
//    private static final ResourceLocation TEXTURE =
//            ResourceLocation.fromNamespaceAndPath("arbcraft", "textures/gui/experience_converter.png");
//
//    public ExperienceConverterScreen(ExperienceConverterMenu menu, Inventory playerInventory, Component title) {
//        super(menu, playerInventory, title);
//        this.imageHeight = 180;
//        this.imageWidth = 176;
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//
//        int x = (this.width - this.imageWidth) / 2;
//        int y = (this.height - this.imageHeight) / 2;
//
//        // Withdraw All button
//        this.addRenderableWidget(Button.builder(
//                        Component.literal("Withdraw All"),
//                        button -> {
//                            PacketDistributor.sendToServer(new WithdrawExperiencePacket(
//                                    menu.getBlockEntity().getBlockPos(), -1));
//                        })
//                .bounds(x + 8, y + 60, 75, 20)
//                .build());
//
//        // Withdraw 100 button
//        this.addRenderableWidget(Button.builder(
//                        Component.literal("Withdraw 100"),
//                        button -> {
//                            PacketDistributor.sendToServer(new WithdrawExperiencePacket(
//                                    menu.getBlockEntity().getBlockPos(), 100));
//                        })
//                .bounds(x + 93, y + 60, 75, 20)
//                .build());
//    }
//
//    @Override
//  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//    int x = (this.width - this.imageWidth) / 2;
//    int y = (this.height - this.imageHeight) / 2;
//
//    // Render gray background
//    guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF8B8B8B);
//
//    // Render item slot with depth (recessed look)
//    int slotX = x + 79;  // 80 - 1 (slot is at 80, we draw border 1 pixel before)
//    int slotY = y + 34;  // 35 - 1
//    int slotSize = 18;   // 16 + 2 for border
//
//    // Dark shadow (top and left - makes it look recessed)
//    guiGraphics.fill(slotX, slotY, slotX + slotSize, slotY + 1, 0xFF373737);  // Top shadow
//    guiGraphics.fill(slotX, slotY, slotX + 1, slotY + slotSize, 0xFF373737);  // Left shadow
//
//    // Light highlight (bottom and right - adds depth)
//    guiGraphics.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, 0xFFFFFFFF);  // Bottom highlight
//    guiGraphics.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, 0xFFFFFFFF);  // Right highlight
//
//    // Inner dark background for the slot
//    guiGraphics.fill(slotX + 1, slotY + 1, slotX + slotSize - 1, slotY + slotSize - 1, 0xFF4A4A4A);
//
//    // Render progress bar if converting
//    if (menu.getConversionProgress() > 0) {
//        int progress = menu.getConversionProgress() * 24 / menu.getMaxProgress();
//
//        // Progress bar background (darker)
//        guiGraphics.fill(x + 76, y + 53, x + 100, y + 59, 0xFF2A2A2A);
//
//        // Progress bar border
//        guiGraphics.fill(x + 75, y + 52, x + 101, y + 53, 0xFF000000);  // Top
//        guiGraphics.fill(x + 75, y + 63, x + 101, y + 60, 0xFFFFFFFF);  // Bottom
//        guiGraphics.fill(x + 75, y + 52, x + 76, y + 60, 0xFF000000);   // Left
//        guiGraphics.fill(x + 100, y + 52, x + 101, y + 60, 0xFFFFFFFF); // Right
//
//        // Progress fill (green with gradient effect)
//        if (progress > 0) {
//            guiGraphics.fill(x + 76, y + 53, x + 76 + progress, y + 59, 0xFF00DD00);
//            // Add lighter top for gradient effect
//            guiGraphics.fill(x + 76, y + 53, x + 76 + progress, y + 55, 0xFF00FF00);
//        }
//    }
//}
//
//    @Override
//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
//        super.render(guiGraphics, mouseX, mouseY, partialTick);
//        this.renderTooltip(guiGraphics, mouseX, mouseY);
//
//        // Render stored experience text
//        int x = (this.width - this.imageWidth) / 2;
//        int y = (this.height - this.imageHeight) / 2;
//        Component expText = Component.literal("Stored XP: " + menu.getStoredExperience());
//        guiGraphics.drawString(this.font, expText, x + 8, y + 20, 0x404040, false);
//    }
//
//    @Override
//    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        // Render the title
//        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
//
//        // Move inventory label down to not overlap with buttons
//        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY + 14, 0x404040, false);
//    }
//}