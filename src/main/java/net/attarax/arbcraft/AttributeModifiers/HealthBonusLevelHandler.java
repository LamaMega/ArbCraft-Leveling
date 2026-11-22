package net.attarax.arbcraft.AttributeModifiers;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class HealthBonusLevelHandler {

    // ResourceLocation for the attack speed modifier - replace "yourmodid" with your actual mod ID
    private static final ResourceLocation HEALTH_BONUS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("arbcraft", "level_health_bonus");

    public static double healthModifier = 0.025; //0.07 to 0.05

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side and every 20 ticks (1 second)
        if (player.level().isClientSide || player.tickCount % 20 != 0) {
            return;
        }

        updateHealthBoostModifier(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateHealthBoostModifier(event.getEntity());
    }

    private static void updateHealthBoostModifier(Player player) {
        int playerLevel = player.experienceLevel;

        // Remove existing modifier if present
        if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(HEALTH_BONUS_MODIFIER_ID) != null) {
            player.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_BONUS_MODIFIER_ID);
        }

        // Calculate Health bonus (adjust formula as needed)
        double maxHealthBonus = calculateHealthBoost(playerLevel);

        if (maxHealthBonus > 0) {
            // Create and apply the modifier
            AttributeModifier modifier = new AttributeModifier(
                    HEALTH_BONUS_MODIFIER_ID,
                    maxHealthBonus,
                    AttributeModifier.Operation.ADD_VALUE
            );

            player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(modifier);
        }
    }

    private static double calculateHealthBoost(int level) {
        // Example formulas - adjust these to your preference:

        // Linear scaling: 0.1 attack speed per level
        return Math.min(level * healthModifier, 100000.0); //level * 0.001; 0.0334

        // Logarithmic scaling (slower growth at higher levels):
        // return Math.log(level + 1) * 0.5;

        // Tiered scaling:
        // if (level >= 50) return 10.0;
        // if (level >= 30) return 5.0;
        // if (level >= 10) return 2.0;
        // return level * 0.2;

        // Capped linear scaling:
        // return Math.min(level * 0.15, 10.0); // Max 10 attack speed bonus
    }
}