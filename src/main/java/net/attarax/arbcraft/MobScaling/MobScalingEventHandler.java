package net.attarax.arbcraft.MobScaling;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;

public class MobScalingEventHandler {

    private static final double HP_SCALING = 0.003; //0.05 to 0.01
    //private static final double DAMAGE_SCALING = 0.005;

    public static double MAX_HP_MULTIPLIER = 1.0;
    //private static final double MAX_DAMAGE_MULTIPLIER = 1.0;

    private static final String SCALED_TAG = "arbcraft_scaled";

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {

        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (!isHostileMobOrBoss(livingEntity)) {
            return;
        }

        // Prevent scaling the same mob multiple times
        if (livingEntity.getPersistentData().getBoolean(SCALED_TAG)) {
            return;
        }

        livingEntity.getPersistentData().putBoolean(SCALED_TAG, true);

        ServerLevel level = (ServerLevel) event.getLevel();
        scaleMobAttributes(livingEntity, level);
    }

    private boolean isHostileMobOrBoss(LivingEntity entity) {
        return entity instanceof Monster
                || entity instanceof Enemy
                || entity instanceof EnderDragon
                || entity instanceof WitherBoss;
    }

    private void scaleMobAttributes(LivingEntity entity, ServerLevel level) {

        double playerExpFactor = getAveragePlayerExpFactor(level);

        double hpMultiplier = Math.min(
                1.0 + (playerExpFactor * HP_SCALING),
                MAX_HP_MULTIPLIER
        );

//        double damageMultiplier = Math.min(
//                1.0 + (playerExpFactor * DAMAGE_SCALING),
//                MAX_DAMAGE_MULTIPLIER
//        );

        // Health Scaling
        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double newHealth = healthAttr.getBaseValue() * hpMultiplier;
            healthAttr.setBaseValue(newHealth);
            entity.setHealth((float) newHealth);
        }

//        // Damage Scaling
//        AttributeInstance attackAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
//        if (attackAttr != null) {
//            double newDamage = attackAttr.getBaseValue() * damageMultiplier;
//            attackAttr.setBaseValue(newDamage);
//        }
    }

    private double getAveragePlayerExpFactor(ServerLevel level) {

        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();

        if (players.isEmpty()) {
            return 0.0;
        }

        double totalExp = 0.0;

        for (ServerPlayer player : players) {
            totalExp += getTotalExperience(player);
        }

        return (totalExp / players.size() / 2.0) * 0.01;
    }

    private int getTotalExperience(ServerPlayer player) {

        int totalExp = player.totalExperience;
        totalExp += Math.round(
                player.experienceProgress *
                        getExpToNext(player.experienceLevel)
        );

        return totalExp;
    }

    private int getExpToNext(int level) {

        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 7 * level - 158;
        }
    }
}