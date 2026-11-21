package net.attarax.arbcraft.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.attarax.arbcraft.ArbCraft;
import net.attarax.arbcraft.components.ModDataComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;


@EventBusSubscriber(modid = "arbcraft")
public class ModCommands {

    // -----------------------------------------------------------------------
    // Tool Upgrade Balance Variables — adjust these as needed
    // -----------------------------------------------------------------------
    private static final int XP_COST_PER_UPGRADE   = 10;   // XP levels consumed per upgrade
    private static final int DURABILITY_PER_UPGRADE = 25;   // Max durability added per upgrade
    private static int MAX_UPGRADES                 = 1000; // Hard cap of upgrades per item
    // -----------------------------------------------------------------------

    public static void setMaxUpgrades(int value) {
        MAX_UPGRADES = value;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {

        // --- Tool Upgrade Command ---
        // Usage: /arbcraft_durability_upgrade          → upgrades once
        // Usage: /arbcraft_durability_upgrade <amount> → upgrades up to <amount> times
        event.getDispatcher().register(
                Commands.literal("arbcraft_durability_upgrade")
                        .requires(source -> true)
                        // No argument: upgrade exactly once
                        .executes(context -> {
                            if (!(context.getSource().getEntity() instanceof Player player)) {
                                context.getSource().sendFailure(
                                        Component.literal("This command can only be run by a player."));
                                return 0;
                            }
                            return applyToolUpgrade(player, 1);
                        })
                        // With argument: upgrade up to <amount> times
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000000))
                                .executes(context -> {
                                    if (!(context.getSource().getEntity() instanceof Player player)) {
                                        context.getSource().sendFailure(
                                                Component.literal("This command can only be run by a player."));
                                        return 0;
                                    }
                                    int requested = IntegerArgumentType.getInteger(context, "amount");
                                    return applyToolUpgrade(player, requested);
                                })
                        )
        );

        // --- Max Upgrade Config Command ---
        event.getDispatcher().register(
                Commands.literal("arbcraft_durability_max_upgrade")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("MaxUpgrade")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ModCommands.setMaxUpgrades(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aMax Tool Durability Upgrade has been set to: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
        );

        // --- Config Command ---
        event.getDispatcher().register(
                Commands.literal("arbcraft_config")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("ExpReward")
                                .then(Commands.argument("status", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean status = BoolArgumentType.getBool(context, "status");
                                            ArbCraft.setBlockExpStatus(status);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aExp Reward Per Block has been set to: " + status),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("normalexp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ArbCraft.setNormalBlockExp(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aNormal block exp set to: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("oreexp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ArbCraft.setOreBlockExp(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aOre block exp set to: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("bonusBlockExp")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ArbCraft.setBonusBlockExp(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aBonus block exp set to: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("bonusTickMultiplier")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ArbCraft.setBonusTickMultiplier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aSet the bonus tick Multiplier per 100 Levels: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("increasedBonusTickMultiplier")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000))
                                        .executes(context -> {
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            ArbCraft.setIncreasedBonusTickMultiplier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aSet the bonus tick Multiplier per 100 Levels after Level 5000: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("effectsEnabled")
                                .then(Commands.argument("status", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean status = BoolArgumentType.getBool(context, "status");
                                            ArbCraft.setEffectsEnabled(status);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§Effects enabled has been set to: " + status),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("view")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§6Current Exp Settings:\n" +
                                                    "§7Normal Blocks Exp: §e" + ArbCraft.getNormalBlockExp() + "\n" +
                                                    "§7Ore Blocks Exp: §e" + ArbCraft.getOreBlockExp() + "\n" +
                                                    "§7Bonus Blocks Exp: §e" + ArbCraft.getBonusBlockExp() + "\n" +
                                                    "§7Bonus Tick Multiplier: §e" + ArbCraft.getBonusTickMultiplier() + "\n" +
                                                    "§7Increased Bonus Tick Multiplier After 5000: §e" + ArbCraft.getIncreasedBonusTickMultiplier()+ "\n" +
                                                    "§7Effects Enabled: §e" +ArbCraft.getEffectsEnabled()
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );

        // --- Attribute Config Command ---
        event.getDispatcher().register(
                Commands.literal("arbcraft_attribute_config")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("hpPerLevel")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 100))
                                        .executes(context -> {
                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                            ArbCraft.setHealthModifier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aHp Per Level Set To: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("damagePerLevel")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1000))
                                        .executes(context -> {
                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                            ArbCraft.setDamageModifier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aDamage Per Level Set To: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("efficiencyPerLevel")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1000))
                                        .executes(context -> {
                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                            ArbCraft.setEfficiencyModifier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aEfficiency Per Level Set To: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("attackSpeedPerLevel")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 1000))
                                        .executes(context -> {
                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                            ArbCraft.setAttackSpeedModifier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aAttack Speed Per Level Set To: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("mobMaxHpMultiplier")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(1, 100000))
                                        .executes(context -> {
                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                            ArbCraft.setMaxHpMultiplier(amount);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§aU just multiplied every Hostile Mob's (bosses included) Max HP by: " + amount),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("view")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§6Current Attribute Settings:\n" +
                                                    "§7(Default:0.07) Health Per Level: §e" + ArbCraft.getHealthModifier() + "\n" +
                                                    "§7(Default:0.07) Damage Per Level: §e" + ArbCraft.getDamageModifier() + "\n" +
                                                    "§7(Default:0.03) Efficiency Per Level: §e" + ArbCraft.getEfficiencyModifier() + "\n" +
                                                    "§7(Default:0.0003) Attack Speed Per Level: §e" + ArbCraft.getAttackSpeedModifier()),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
        event.getDispatcher()
        .register(
                Commands.literal("arbcraft_collectAllIDrops")
                        .requires(source -> source.hasPermission(0)) // allow any player to run it; raise this if you want it restricted
                        .executes(ModCommands::collectAllDrops)
        );

    }

    /**
     * Applies up to {@code requested} durability upgrades to the item the player is holding.
     *
     * The actual number of upgrades applied is capped by three limits:
     *   1. How many upgrades are still available before hitting MAX_UPGRADES
     *   2. How many upgrades the player can afford with their current XP levels
     *   3. The number the player requested
     *
     * If fewer upgrades are applied than requested, the player is informed why.
     */
    private static int applyToolUpgrade(Player player, int requested) {
        ItemStack heldItem = player.getMainHandItem();

        // Must be holding a damageable item
        if (heldItem.isEmpty() || !heldItem.has(DataComponents.MAX_DAMAGE)) {
            player.sendSystemMessage(Component.literal(
                    "§cYou must hold a tool, weapon, or piece of equipment to upgrade!"));
            return 0;
        }

        int currentUpgrades = heldItem.getOrDefault(ModDataComponents.UPGRADE_COUNT.get(), 0);

        // Already at the cap — nothing to do
        if (currentUpgrades >= MAX_UPGRADES) {
            player.sendSystemMessage(Component.literal(
                    "§cThis item has already reached the maximum upgrade limit of §e"
                            + MAX_UPGRADES + "§c!"));
            return 0;
        }

        // Must have at least enough XP for 1 upgrade to do anything
        if (player.experienceLevel < XP_COST_PER_UPGRADE) {
            player.sendSystemMessage(Component.literal(
                    "§cYou need at least §e" + XP_COST_PER_UPGRADE
                            + " §cexperience levels to upgrade! You currently have §e"
                            + player.experienceLevel + "§c."));
            return 0;
        }

        // Calculate the real number of upgrades we can actually apply
        int upgradesLeftOnItem = MAX_UPGRADES - currentUpgrades;
        int upgradesAffordable = player.experienceLevel / XP_COST_PER_UPGRADE;
        int actualUpgrades     = Math.min(requested, Math.min(upgradesLeftOnItem, upgradesAffordable));

        // Apply all upgrades in one shot
        int newMaxDamage    = heldItem.get(DataComponents.MAX_DAMAGE) + (DURABILITY_PER_UPGRADE * actualUpgrades);
        int newUpgradeCount = currentUpgrades + actualUpgrades;

        heldItem.set(DataComponents.MAX_DAMAGE, newMaxDamage);
        heldItem.set(ModDataComponents.UPGRADE_COUNT.get(), newUpgradeCount);
        player.giveExperienceLevels(-(XP_COST_PER_UPGRADE * actualUpgrades));

        // Build feedback — inform the player if they received fewer upgrades than requested
        StringBuilder msg = new StringBuilder();
        msg.append("§aUpgrade applied! §7(")
                .append(newUpgradeCount).append("§7/§f").append(MAX_UPGRADES).append("§7) ")
                .append("§7New max durability: §f").append(newMaxDamage)
                .append(" §7| XP remaining: §f").append(player.experienceLevel);

        if (actualUpgrades < requested) {
            msg.append("\n§e⚠ Only ").append(actualUpgrades)
                    .append(" of ").append(requested).append(" upgrades were applied. ");

            if (upgradesAffordable <= upgradesLeftOnItem) {
                msg.append("§7(Not enough XP levels for more.)");
            } else {
                msg.append("§7(Item upgrade limit reached.)");
            }
        }

        player.sendSystemMessage(Component.literal(msg.toString()));
        return 1;
    }
    /// DROP COLLECT
    // Radius (in blocks) to search for item drops around the player
    private static final double COLLECT_RADIUS = 1000.0D;

    private static int collectAllDrops(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Player player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("This command can only be run server-side."));
            return 0;
        }

        BlockPos playerPos = player.blockPosition();

        // Build a bounding box of radius COLLECT_RADIUS centered on the player
        AABB searchBox = new AABB(
                playerPos.getX() - COLLECT_RADIUS, playerPos.getY() - COLLECT_RADIUS, playerPos.getZ() - COLLECT_RADIUS,
                playerPos.getX() + COLLECT_RADIUS, playerPos.getY() + COLLECT_RADIUS, playerPos.getZ() + COLLECT_RADIUS
        );

        // Grab every ItemEntity in range that is still alive
        List<ItemEntity> itemEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, searchBox, ItemEntity::isAlive);

        int collectedCount = 0;
        int droppedCount = 0;

        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();

            if (stack.isEmpty()) {
                continue;
            }

            // Try to add the item to the player's inventory
            boolean added = player.getInventory().add(stack);

            if (added && stack.isEmpty()) {
                // Fully absorbed into the inventory
                itemEntity.discard();
                collectedCount++;
            } else {
                // Inventory couldn't take it (fully or partially) - drop remainder in front of the player instead
                itemEntity.discard();

                if (!stack.isEmpty()) {
                    dropInFrontOfPlayer(player, stack);
                    droppedCount++;
                } else {
                    collectedCount++;
                }
            }
        }

        int finalCollected = collectedCount;
        int finalDropped = droppedCount;
        source.sendSuccess(() -> Component.literal(
                "Collected " + finalCollected + " item stack(s)"
                        + (finalDropped > 0 ? ", dropped " + finalDropped + " stack(s) in front of you (inventory full)." : ".")
        ), false);

        return itemEntities.size();
    }

    /**
     * Drops the given stack directly in front of the player (rather than at their feet),
     * used when their inventory is full.
     */
    private static void dropInFrontOfPlayer(Player player, ItemStack stack) {
        // Compute a point ~1.5 blocks in front of the player based on their look direction
        double dropX = player.getX() + player.getLookAngle().x * 1.5D;
        double dropY = player.getY() + player.getEyeHeight() * 0.5D;
        double dropZ = player.getZ() + player.getLookAngle().z * 1.5D;

        ItemEntity dropped = new ItemEntity(player.level(), dropX, dropY, dropZ, stack.copy());
        dropped.setDeltaMovement(player.getLookAngle().x * 0.1D, 0.1D, player.getLookAngle().z * 0.1D);
        dropped.setPickUpDelay(20); // brief delay before it can be picked up again

        player.level().addFreshEntity(dropped);
    }



    private static double getDayFactor(ServerLevel level) {
        return level.getDayTime() / 24000L;
    }
}