package net.attarax.arbcraft.client;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Replaces the vanilla heart row with a single red health bar that shows the numbers
 * ("current/max"), so high max health never stacks rows of hearts on top of each other.
 * Absorption is shown as a gold bar section on top of the red bar, with its own number.
 */
@EventBusSubscriber(modid = ArbCraft.MODID, value = Dist.CLIENT)
public class HealthBarOverlay {

    // ---- Look & size (all ARGB colors) ----
    private static final int BAR_WIDTH = 81;   // same width as 10 vanilla hearts
    private static final int BAR_HEIGHT = 11;
    /** Vertical space the bar takes up, so armor and other layers above it stay in place. */
    private static final int LAYER_HEIGHT = 10;

    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_BACKGROUND = 0xFF3A0A0A;
    private static final int COLOR_HEALTH = 0xFFD62020;
    private static final int COLOR_HEALTH_HIGHLIGHT = 0xFFFF5A5A;
    private static final int COLOR_POISON = 0xFF7A8C1E;
    private static final int COLOR_POISON_HIGHLIGHT = 0xFFA3B83A;
    private static final int COLOR_WITHER = 0xFF2B2B2B;
    private static final int COLOR_WITHER_HIGHLIGHT = 0xFF4A4A4A;
    private static final int COLOR_FROZEN = 0xFF5AA0D6;
    private static final int COLOR_FROZEN_HIGHLIGHT = 0xFF8CC8F0;
    private static final int COLOR_ABSORPTION = 0xFFE8B923;
    private static final int COLOR_ABSORPTION_HIGHLIGHT = 0xFFFFE066;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SHADOW = 0xFF000000;

    @SubscribeEvent
    public static void onRenderHealth(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;

        Minecraft mc = Minecraft.getInstance();
        // Same condition vanilla uses to draw the survival bars (hidden in creative / spectator)
        if (mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
        if (!(mc.getCameraEntity() instanceof Player player)) return;

        event.setCanceled(true); // hide the vanilla hearts

        GuiGraphics graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2 - 91;
        int y = graphics.guiHeight() - mc.gui.leftHeight;

        float health = Math.max(0.0F, player.getHealth());
        float maxHealth = Math.max(1.0F, player.getMaxHealth());
        float absorption = Math.max(0.0F, player.getAbsorptionAmount());

        int fill;
        int highlight;
        if (player.hasEffect(MobEffects.WITHER)) {
            fill = COLOR_WITHER;
            highlight = COLOR_WITHER_HIGHLIGHT;
        } else if (player.hasEffect(MobEffects.POISON)) {
            fill = COLOR_POISON;
            highlight = COLOR_POISON_HIGHLIGHT;
        } else if (player.isFullyFrozen()) {
            fill = COLOR_FROZEN;
            highlight = COLOR_FROZEN_HIGHLIGHT;
        } else {
            fill = COLOR_HEALTH;
            highlight = COLOR_HEALTH_HIGHLIGHT;
        }

        // Border + background
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BORDER);
        graphics.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, COLOR_BACKGROUND);

        int innerWidth = BAR_WIDTH - 2;
        int innerHeight = BAR_HEIGHT - 2;
        int innerX = x + 1;
        int innerY = y + 1;

        // Health fill
        int healthWidth = Mth.ceil(innerWidth * Math.min(1.0F, health / maxHealth));
        if (health > 0.0F) healthWidth = Math.max(1, healthWidth);
        drawFill(graphics, innerX, innerY, healthWidth, innerHeight, fill, highlight);

        // Absorption: gold section drawn from the right edge of the bar, scaled against max health
        if (absorption > 0.0F) {
            int absorptionWidth = Math.max(1, Mth.ceil(innerWidth * Math.min(1.0F, absorption / maxHealth)));
            drawFill(graphics, innerX + innerWidth - absorptionWidth, innerY, absorptionWidth, innerHeight,
                    COLOR_ABSORPTION, COLOR_ABSORPTION_HIGHLIGHT);
        }

        // Numbers: "health/max" (plus "+absorption" when there is any)
        String text = formatNumber(health) + "/" + formatNumber(maxHealth);
        if (absorption > 0.0F) text += " +" + formatNumber(absorption);
        drawCenteredText(mc, graphics, text, x + BAR_WIDTH / 2, y + BAR_HEIGHT / 2, BAR_WIDTH - 4);

        // The cancelled layer would normally move the layers above it up by its own height
        mc.gui.leftHeight += LAYER_HEIGHT;
    }

    /** A filled rectangle with a lighter strip along the top for a bit of depth. */
    private static void drawFill(GuiGraphics graphics, int x, int y, int width, int height, int color, int highlight) {
        if (width <= 0) return;
        graphics.fill(x, y, x + width, y + height, color);
        graphics.fill(x, y, x + width, y + Math.max(1, height / 3), highlight);
    }

    /** Text with a shadow, centered on (centerX, centerY), shrunk to fit maxWidth if it is too long. */
    private static void drawCenteredText(Minecraft mc, GuiGraphics graphics, String text,
                                         int centerX, int centerY, int maxWidth) {
        int textWidth = mc.font.width(text);
        float scale = textWidth > maxWidth ? (float) maxWidth / textWidth : 1.0F;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        int drawX = -textWidth / 2;
        int drawY = -mc.font.lineHeight / 2 + 1;
        graphics.drawString(mc.font, text, drawX + 1, drawY + 1, COLOR_TEXT_SHADOW, false);
        graphics.drawString(mc.font, text, drawX, drawY, COLOR_TEXT, false);
        graphics.pose().popPose();
    }

    /** Whole numbers only: 19.5 hp shows as 20 (rounded up, like vanilla hearts), never "19.5". */
    private static String formatNumber(float value) {
        return Integer.toString(Mth.ceil(value));
    }
}