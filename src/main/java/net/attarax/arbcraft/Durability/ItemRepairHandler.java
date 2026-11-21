package net.attarax.arbcraft.Durability;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static net.minecraft.world.entity.LivingEntity.ARMOR_SLOTS;

@EventBusSubscriber(modid = ArbCraft.MODID)
public class ItemRepairHandler {

    // Configuration: 1 XP point = 2 durability points repaired
    private static final int DURABILITY_PER_XP = 2;

    private static final int ARMOR_REPAIR_INTERVAL_TICKS = 2400;
    private static final int ARMOR_REPAIR_AMOUNT = 1;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };


    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        // Only process on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if player is sneaking (Shift held)
        if (!player.isShiftKeyDown()) {
            return;
        }

        // Get the item in main hand
        ItemStack heldItem = player.getMainHandItem();

        // Check if item exists and is damageable
        if (heldItem.isEmpty() || !heldItem.isDamageableItem()) {
            return;
        }

        // Check if item actually needs repair
        int currentDamage = heldItem.getDamageValue();
        if (currentDamage == 0) {
            player.displayClientMessage(Component.literal("§aItem is already at full durability!"), true);
            return;
        }

        // Get total XP points
        int totalXP = getTotalExperience(player);

        // Check if player has XP
        if (totalXP <= 0) {
            player.displayClientMessage(Component.literal("§cNot enough experience points!"), true);
            return;
        }

        // Calculate how much we can repair
        int maxRepairAmount = totalXP * DURABILITY_PER_XP;
        int actualRepairAmount = Math.min(currentDamage, maxRepairAmount);
        int xpNeeded = (int) Math.ceil(actualRepairAmount / (double) DURABILITY_PER_XP);

        // Repair the item
        heldItem.setDamageValue(currentDamage - actualRepairAmount);

        // Remove experience points
        removeExperience(player, xpNeeded);

        // Play anvil sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5F, 1.0F);


        // Cancel the event to prevent normal right-click behavior
        event.setCanceled(true);
    }

    /**
     * Gets the total experience points a player has
     */
    private static int getTotalExperience(Player player) {
        int level = player.experienceLevel;
        int points = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());

        // Calculate XP from levels
//        if (level >= 32) {
//            points += (9 * level * level - 325 * level + 4440) / 2;
//        } else if (level >= 17) {
//            points += (5 * level * level - 162 * level + 2220) / 2;
//        } else {
//            points += level * level + 6 * level;
//        }
        //1.3.6.6 Fixed Repair EXP Formula
        if (level >= 32) {
            points += (9 * level * level - 325 * level + 4440) / 2;
        } else if (level >= 17) {
            points += (5 * level * level - 81 * level + 720) / 2;
        } else {
            points += level * level + 6 * level;
        }
        return points;
    }

    private static void removeExperience(Player player, int amount) {
        int totalXP = getTotalExperience(player);
        int newTotalXP = Math.max(0, totalXP - amount);

        // Reset player XP
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;

        // Give back the remaining XP
        player.giveExperiencePoints(newTotalXP);
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only process on server side
        if (player.level().isClientSide) {
            return;
        }

        // Use the player's own tick count so this fires once every interval, per player,
        // without needing any external/static counters.
        if (player.tickCount % ARMOR_REPAIR_INTERVAL_TICKS != 0) {
            return;
        }
        if (player.experienceLevel < 200)
        {
            return;
        }

        repairArmor(player);
    }

    private static void repairArmor(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armorPiece = player.getItemBySlot(slot);

            if (armorPiece.isEmpty() || !armorPiece.isDamageableItem()) {
                continue;
            }

            int currentDamage = armorPiece.getDamageValue();
            if (currentDamage <= 0) {
                continue;
            }

            int newDamage = Math.max(0, currentDamage - ARMOR_REPAIR_AMOUNT);
            armorPiece.setDamageValue(newDamage);
        }
    }

}