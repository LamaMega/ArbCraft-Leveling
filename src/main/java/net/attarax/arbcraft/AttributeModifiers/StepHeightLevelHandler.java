package net.attarax.arbcraft.AttributeModifiers;


import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class StepHeightLevelHandler {

    // ResourceLocation for the attack speed modifier - replace "yourmodid" with your actual mod ID
    private static final ResourceLocation STEP_HEIGHT_BONUS_ID = ResourceLocation.fromNamespaceAndPath("arbcraft", "level_step_height_bonus");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side and every 20 ticks (1 second)
        if (player.level().isClientSide || player.tickCount % 20 != 0) {
            return;
        }

        updateStepHeightBonus(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateStepHeightBonus(event.getEntity());
    }

    private static void updateStepHeightBonus(Player player) {
        int playerLevel = player.experienceLevel;

        // Remove existing modifier if present
        if (player.getAttribute(Attributes.STEP_HEIGHT).getModifier(STEP_HEIGHT_BONUS_ID) != null) {
            player.getAttribute(Attributes.STEP_HEIGHT).removeModifier(STEP_HEIGHT_BONUS_ID);
        }

        // Calculate Health bonus (adjust formula as needed)
        double stepHeightBonus = calculateStepHeightBonus(playerLevel);

        if (stepHeightBonus > 0) {
            // Create and apply the modifier
            AttributeModifier modifier = new AttributeModifier(
                    STEP_HEIGHT_BONUS_ID,
                    stepHeightBonus,
                    AttributeModifier.Operation.ADD_VALUE
            );

            player.getAttribute(Attributes.STEP_HEIGHT).addPermanentModifier(modifier);
        }
    }

    private static double calculateStepHeightBonus(int level) {
        // Example formulas - adjust these to your preference:

        if(level >= 500)
        {
            return 1;
        }
        else{
            return 0;
        }
//        if (level > 4000){
//            return 3;  //Math.min(level * 0.002, 1);
//        }else {
//            return 0;
//        }
        // Linear scaling: 0.1 attack speed per level
//        return Math.min(level * 0.1, 0.3); //level * 0.001;

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