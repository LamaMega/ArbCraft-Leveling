package net.attarax.arbcraft.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomScreen extends AbstractContainerScreen<CustomMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("arbcraft", "textures/gui/custom_gui.png");

    // Effects display constants
    private static final int EFFECT_ICON_SIZE = 16;
    private static final int EFFECT_SPACING = 20;
    private static final int EFFECTS_START_X = 10;
    private static final int EFFECTS_START_Y = 80;

    // Define effects and their corresponding levels
    private static final Map<Holder<MobEffect>, int[]> EFFECTS_AND_LEVELS = new LinkedHashMap<>();

    static {
//        EFFECTS_AND_LEVELS.put(MobEffects.DIG_SPEED,
//                new int[]{10}); // HASTE
//        EFFECTS_AND_LEVELS.put(MobEffects.HEALTH_BOOST,
//                new int[]{124});
//        EFFECTS_AND_LEVELS.put(MobEffects.DAMAGE_BOOST,
//                new int[]{60}); // STRENGTH
        EFFECTS_AND_LEVELS.put(MobEffects.MOVEMENT_SPEED,
                new int[]{500, 1000, 5000});
//        EFFECTS_AND_LEVELS.put(MobEffects.REGENERATION,
//                new int[]{500, 2500, 3500, 4500, 5000});
//        EFFECTS_AND_LEVELS.put(MobEffects.DAMAGE_RESISTANCE,
//                new int[]{100, 500, 1000});
        EFFECTS_AND_LEVELS.put(MobEffects.LUCK,
                new int[]{100, 200, 400, 600, 1000, 1500, 2000, 2500, 3500, 5000});
    }

    public CustomScreen(CustomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 640; // Made wider to accommodate effects list
        this.imageHeight = 240; // Made taller for all effects
    }

    @Override
    protected void init() {
        super.init();

        // Add a test button
        this.addRenderableWidget(Button.builder(
                Component.literal("Effects and Levels"),
                button -> {
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.sendSystemMessage(Component.literal("Nothing Happens!"));
                    }
                }
        ).bounds(this.leftPos + 240, this.topPos + 30, 160, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Simple colored background instead of texture for testing
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF8B8B8B);

        // Render effects section background
        guiGraphics.fill(this.leftPos + 5, this.topPos + 75, this.leftPos + this.imageWidth - 5, this.topPos + this.imageHeight - 5, 0xFF404040);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.literal("ArbCraft"), 8, 6, 0x404040, false);

        // Render effects title
        guiGraphics.drawString(this.font, Component.literal("Effects & Levels:"), 8, 62, 0xFFFFFF, false);

        // Render effects list
        renderEffectsList(guiGraphics);
    }

    private void renderEffectsList(GuiGraphics guiGraphics) {
        int maxEffectsPerColumn = 5;
        int effectIndex = 0;

        for (Map.Entry<Holder<MobEffect>, int[]> entry : EFFECTS_AND_LEVELS.entrySet()) {
            Holder<MobEffect> effectHolder = entry.getKey();
            MobEffect effect = effectHolder.value();
            int[] levels = entry.getValue();

            // Calculate position
            int column = effectIndex / maxEffectsPerColumn;
            int row = effectIndex % maxEffectsPerColumn;

            int x = EFFECTS_START_X + (column * 380);
            int y = EFFECTS_START_Y + (row * EFFECT_SPACING);

            // Render effect name (no icon space needed)
            String effectName = getShortEffectName(effect);
            guiGraphics.drawString(this.font, effectName, x, y + 2, 0xFFFFFF, false);

            // Render levels
            StringBuilder levelText = new StringBuilder();
            for (int i = 0; i < levels.length; i++) {
                levelText.append(levels[i]);
                if (i < levels.length - 1) {
                    levelText.append(",");
                }
            }

            guiGraphics.drawString(this.font, levelText.toString(), x, y + 12, 0xAAAAFF, false);

            effectIndex++;
        }
    }

    private String getShortEffectName(MobEffect effect) {
        // Return shortened effect names to fit better in GUI
//        if (effect == MobEffects.DIG_SPEED.value()) return "Haste";
//        if (effect == MobEffects.HEALTH_BOOST.value()) return "Health+ (max)";
//        if (effect == MobEffects.DAMAGE_BOOST.value()) return "Strength";
        if (effect == MobEffects.MOVEMENT_SPEED.value()) return "Speed";
//        if (effect == MobEffects.REGENERATION.value()) return "Health Regeneration";
//        if (effect == MobEffects.DAMAGE_RESISTANCE.value()) return "Resistance";
        if (effect == MobEffects.LUCK.value()) return "Luck";
        return "Effect"; // Fallback
    }
}