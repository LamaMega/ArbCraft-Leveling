package net.attarax.arbcraft;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Config for everything in the main ArbCraft class.
 * File: config/arbcraft-gameplay.toml (created on first launch, editable while the game is closed
 * or live while it is running: changes are picked up automatically).
 *
 * Values that also have an in-game command (block exp, modifiers, tick multipliers, effects toggle,
 * max HP multiplier) are copied into the runtime fields whenever the file is loaded or reloaded.
 * That means a file reload overwrites whatever a command set before it.
 */
public class ArbCraftConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // ---- Block experience ----
    public static final ModConfigSpec.BooleanValue BLOCK_XP_ENABLED;
    public static final ModConfigSpec.IntValue NORMAL_BLOCK_EXP;
    public static final ModConfigSpec.IntValue ORE_BLOCK_EXP;
    public static final ModConfigSpec.IntValue BONUS_BLOCK_EXP;
    public static final ModConfigSpec.IntValue HIGH_LEVEL_THRESHOLD;
    public static final ModConfigSpec.IntValue NORMAL_LEVEL_DIVISOR;
    public static final ModConfigSpec.IntValue LOW_LEVEL_THRESHOLD;
    public static final ModConfigSpec.IntValue ORE_LEVEL_DIVISOR;
    public static final ModConfigSpec.IntValue BONUS_LEVEL_DIVISOR;
    public static final ModConfigSpec.IntValue EXTRA_BONUS_MIN_LEVEL;
    public static final ModConfigSpec.IntValue EXTRA_BONUS_DIVISOR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ORE_NAME_FRAGMENTS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BONUS_NAME_FRAGMENTS;

    // ---- Passive experience ----
    public static final ModConfigSpec.BooleanValue PASSIVE_XP_ENABLED;
    public static final ModConfigSpec.IntValue PASSIVE_XP_INTERVAL;
    public static final ModConfigSpec.IntValue PASSIVE_XP_MAX_DAY_EXP;
    public static final ModConfigSpec.IntValue PASSIVE_XP_DOUBLE_LEVEL;
    public static final ModConfigSpec.IntValue PASSIVE_XP_MULTIPLIER;

    // ---- Level effects ----
    public static final ModConfigSpec.BooleanValue EFFECTS_ENABLED;
    public static final ModConfigSpec.IntValue EFFECT_CHECK_INTERVAL;
    public static final ModConfigSpec.IntValue EFFECT_DURATION;
    public static final ModConfigSpec.IntValue EFFECT_REFRESH_BELOW;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> SPEED_LEVELS;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> LUCK_LEVELS;

    // ---- Food regeneration ----
    public static final ModConfigSpec.BooleanValue FOOD_REGEN_ENABLED;
    public static final ModConfigSpec.IntValue FOOD_REGEN_AMOUNT;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> FOOD_TIER_LEVELS;
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> FOOD_TIER_INTERVALS;

    // ---- Auto pickup ----
    public static final ModConfigSpec.BooleanValue AUTO_PICKUP_ENABLED;
    public static final ModConfigSpec.IntValue AUTO_PICKUP_REQUIRED_LEVEL;
    public static final ModConfigSpec.DoubleValue AUTO_PICKUP_MAX_DISTANCE_SQR;
    public static final ModConfigSpec.IntValue AUTO_PICKUP_BREAKER_MEMORY_SECONDS;

    // ---- Block reach ----
    public static final ModConfigSpec.IntValue REACH_LEVELS_PER_BONUS;
    public static final ModConfigSpec.IntValue REACH_MAX_BONUS;
    public static final ModConfigSpec.IntValue REACH_CHECK_INTERVAL;

    // ---- Tool leveling ----
    public static final ModConfigSpec.IntValue TOOL_USES_PER_LEVEL;
    public static final ModConfigSpec.IntValue TOOL_DURABILITY_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TOOL_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TOOL_EFFICIENCY_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TOOL_REQUIREMENT_GROWTH;
    public static final ModConfigSpec.LongValue TOOL_MAX_USES_PER_LEVEL;
    public static final ModConfigSpec.IntValue TOOL_TIER_LEVEL_INTERVAL;
    public static final ModConfigSpec.IntValue TOOL_ORE_USES;
    public static final ModConfigSpec.IntValue TOOL_KILL_USES;
    public static final ModConfigSpec.IntValue TOOL_PLAYER_LEVELS_PER_BONUS;
    public static final ModConfigSpec.BooleanValue TOOL_PROGRESS_MESSAGES;

    // ---- Modifiers (also settable by command) ----
    public static final ModConfigSpec.DoubleValue HEALTH_MODIFIER;
    public static final ModConfigSpec.DoubleValue DAMAGE_MODIFIER;
    public static final ModConfigSpec.DoubleValue EFFICIENCY_MODIFIER;
    public static final ModConfigSpec.DoubleValue ATTACK_SPEED_MODIFIER;
    public static final ModConfigSpec.IntValue BONUS_TICK_MULTIPLIER;
    public static final ModConfigSpec.IntValue INCREASED_BONUS_TICK_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_HP_MULTIPLIER;

    static {
        // ------------------------------------------------------------------
        BUILDER.comment("Experience given to the player for every block they break").push("blockExperience");

        BLOCK_XP_ENABLED = BUILDER
                .comment("Master switch. If false, breaking blocks gives no experience.",
                        "NOTE: auto pickup is also skipped while this is false.")
                .define("enabled", true);
        NORMAL_BLOCK_EXP = BUILDER
                .comment("Base experience for any block")
                .defineInRange("normalBlockExp", 1, 0, 1_000_000);
        ORE_BLOCK_EXP = BUILDER
                .comment("Extra base experience when the block counts as an ore (see oreNameFragments)")
                .defineInRange("oreBlockExp", 10, 0, 1_000_000);
        BONUS_BLOCK_EXP = BUILDER
                .comment("Extra base experience when the block counts as a bonus block (see bonusNameFragments)")
                .defineInRange("bonusBlockExp", 3, 0, 1_000_000);

        HIGH_LEVEL_THRESHOLD = BUILDER
                .comment("From this player level, every block gives 1 + (level / normalLevelDivisor) extra experience.",
                        "Below it, the ore/bonus level bonuses below can apply instead.")
                .defineInRange("highLevelThreshold", 100, 0, Integer.MAX_VALUE);
        NORMAL_LEVEL_DIVISOR = BUILDER
                .comment("Divisor for the high level bonus above")
                .defineInRange("normalLevelDivisor", 75, 1, Integer.MAX_VALUE);
        LOW_LEVEL_THRESHOLD = BUILDER
                .comment("From this level (and below highLevelThreshold), ore and bonus blocks give level based extra experience")
                .defineInRange("lowLevelThreshold", 20, 0, Integer.MAX_VALUE);
        ORE_LEVEL_DIVISOR = BUILDER
                .comment("Ore blocks give (level / this) extra experience in the low level range")
                .defineInRange("oreLevelDivisor", 10, 1, Integer.MAX_VALUE);
        BONUS_LEVEL_DIVISOR = BUILDER
                .comment("Bonus blocks give (level / this) extra experience in the low level range")
                .defineInRange("bonusLevelDivisor", 30, 1, Integer.MAX_VALUE);

        EXTRA_BONUS_MIN_LEVEL = BUILDER
                .comment("Players ABOVE this level get (level / extraBonusDivisor) extra experience on every block")
                .defineInRange("extraBonusMinLevel", 100, 0, Integer.MAX_VALUE);
        EXTRA_BONUS_DIVISOR = BUILDER
                .comment("Divisor for the extra bonus above")
                .defineInRange("extraBonusDivisor", 100, 1, Integer.MAX_VALUE);

        ORE_NAME_FRAGMENTS = BUILDER
                .comment("A block is an ore if its description id contains any of these (lowercase)")
                .defineListAllowEmpty("oreNameFragments",
                        List.of("ore", "ancient_debris", "lucky"),
                        () -> "", o -> o instanceof String);
        BONUS_NAME_FRAGMENTS = BUILDER
                .comment("A block is a bonus block if its description id contains any of these (lowercase)")
                .defineListAllowEmpty("bonusNameFragments",
                        List.of("amethyst_block", "prismarine", "obsidian", "gilded_blackstone",
                                "glowstone", "magma_block", "crying_obsidian", "calcite"),
                        () -> "", o -> o instanceof String);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Experience given to players over time").push("passiveExperience");

        PASSIVE_XP_ENABLED = BUILDER
                .comment("Turn the periodic experience reward on or off")
                .define("enabled", true);
        PASSIVE_XP_INTERVAL = BUILDER
                .comment("Ticks between rewards (20 ticks = 1 second)")
                .defineInRange("intervalTicks", 3000, 1, Integer.MAX_VALUE);
        PASSIVE_XP_MAX_DAY_EXP = BUILDER
                .comment("Each reward is the number of world days that have passed, capped at this value")
                .defineInRange("maxDayExp", 10000, 0, Integer.MAX_VALUE);
        PASSIVE_XP_DOUBLE_LEVEL = BUILDER
                .comment("Players at or above this level get the reward multiplied")
                .defineInRange("multiplierLevel", 200, 0, Integer.MAX_VALUE);
        PASSIVE_XP_MULTIPLIER = BUILDER
                .comment("Multiplier applied to the reward for players at or above multiplierLevel")
                .defineInRange("multiplier", 2, 1, 1000);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Permanent potion effects given at certain player levels").push("effects");

        EFFECTS_ENABLED = BUILDER
                .comment("Master switch for the level based effects")
                .define("enabled", true);
        EFFECT_CHECK_INTERVAL = BUILDER
                .comment("Ticks between effect checks")
                .defineInRange("checkIntervalTicks", 100, 1, Integer.MAX_VALUE);
        EFFECT_DURATION = BUILDER
                .comment("Duration in ticks of the effect when it is (re)applied")
                .defineInRange("durationTicks", 72000, 1, Integer.MAX_VALUE);
        EFFECT_REFRESH_BELOW = BUILDER
                .comment("The effect is re-applied when its remaining duration drops below this many ticks")
                .defineInRange("refreshBelowTicks", 200, 0, Integer.MAX_VALUE);
        SPEED_LEVELS = BUILDER
                .comment("Speed: player levels, in ascending order. The first entry gives Speed I,",
                        "the second Speed II, and so on (amplifier = position in the list).")
                .defineListAllowEmpty("speedLevels",
                        List.of(1000, 3000, 5000),
                        () -> 0, o -> o instanceof Integer i && i >= 0);
        LUCK_LEVELS = BUILDER
                .comment("Luck: player levels, in ascending order. Amplifier = position in the list.")
                .defineListAllowEmpty("luckLevels",
                        List.of(100, 200, 400, 600, 1000, 1500, 2000, 2500, 3500, 5000, 10000),
                        () -> 0, o -> o instanceof Integer i && i >= 0);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Hunger restored over time for high level players").push("foodRegeneration");

        FOOD_REGEN_ENABLED = BUILDER
                .comment("Turn food regeneration on or off")
                .define("enabled", true);
        FOOD_REGEN_AMOUNT = BUILDER
                .comment("Food points restored each time a tier triggers (food level never goes above 20)")
                .defineInRange("amount", 1, 1, 20);
        FOOD_TIER_LEVELS = BUILDER
                .comment("Tier N applies from this player level. Must line up with tierIntervalsTicks.",
                        "Tiers stack: a player who qualifies for several gets all of them.")
                .defineListAllowEmpty("tierLevels",
                        List.of(100, 2000, 10000, 20000),
                        () -> 0, o -> o instanceof Integer i && i >= 0);
        FOOD_TIER_INTERVALS = BUILDER
                .comment("Ticks between food restores for tier N (same order as tierLevels)")
                .defineListAllowEmpty("tierIntervalsTicks",
                        List.of(600, 500, 400, 300),
                        () -> 600, o -> o instanceof Integer i && i >= 1);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Block drops go straight into the inventory of the player who broke the block").push("autoPickup");

        AUTO_PICKUP_ENABLED = BUILDER
                .comment("Turn auto pickup on or off")
                .define("enabled", true);
        AUTO_PICKUP_REQUIRED_LEVEL = BUILDER
                .comment("Minimum player level for auto pickup")
                .defineInRange("requiredLevel", 100, 0, Integer.MAX_VALUE);
        AUTO_PICKUP_MAX_DISTANCE_SQR = BUILDER
                .comment("Max SQUARED distance between player and drop (25 = 5 blocks, 155 = about 12.4 blocks)")
                .defineInRange("maxDistanceSquared", 155.0, 0.0, 1_000_000.0);
        AUTO_PICKUP_BREAKER_MEMORY_SECONDS = BUILDER
                .comment("How long a broken block position remembers who broke it")
                .defineInRange("breakerMemorySeconds", 5, 1, 3600);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Extra block interaction range based on player level").push("blockReach");

        REACH_LEVELS_PER_BONUS = BUILDER
                .comment("Player levels needed for each +1 block of reach")
                .defineInRange("levelsPerBonus", 1000, 1, Integer.MAX_VALUE);
        REACH_MAX_BONUS = BUILDER
                .comment("Maximum bonus reach in blocks")
                .defineInRange("maxBonus", 10, 0, 1000);
        REACH_CHECK_INTERVAL = BUILDER
                .comment("Ticks between checks for a level change")
                .defineInRange("checkIntervalTicks", 60, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        // ------------------------------------------------------------------
        BUILDER.comment("Tool leveling: tools gain levels from use (see ToolLevelHandler)").push("toolLeveling");

        TOOL_USES_PER_LEVEL = BUILDER
                .comment("Progress needed for the first tool level (level 0 -> 1)")
                .defineInRange("usesPerLevel", 255, 1, Integer.MAX_VALUE);
        TOOL_DURABILITY_PER_LEVEL = BUILDER
                .comment("Extra durability a tool gains per tool level")
                .defineInRange("durabilityPerLevel", 6, 0, 1_000_000);
        TOOL_DAMAGE_PER_LEVEL = BUILDER
                .comment("Extra attack damage a tool gains per tool level")
                .defineInRange("damagePerLevel", 0.1, 0.0, 1_000_000.0);
        TOOL_EFFICIENCY_PER_LEVEL = BUILDER
                .comment("Extra mining efficiency a digger tool (pickaxe, axe, shovel, hoe) gains per tool level")
                .defineInRange("efficiencyPerLevel", 0.05, 0.0, 1_000_000.0);
        TOOL_REQUIREMENT_GROWTH = BUILDER
                .comment("Each level needs this fraction more progress than the previous one (0.07 = +7%)")
                .defineInRange("requirementGrowth", 0.07, 0.0, 10.0);
        TOOL_MAX_USES_PER_LEVEL = BUILDER
                .comment("The progress needed for a single level never grows past this value")
                .defineInRange("maxUsesPerLevel", 7500L, 1L, 1_000_000_000L);
        TOOL_TIER_LEVEL_INTERVAL = BUILDER
                .comment("Every this many tool levels, digger tools gain +1 mining tier",
                        "(e.g. a stone pickaxe at 50 can mine diamond ore)")
                .defineInRange("tierLevelInterval", 50, 1, Integer.MAX_VALUE);
        TOOL_ORE_USES = BUILDER
                .comment("Base progress a tool gains from breaking an ore block (any other block gives 1)")
                .defineInRange("oreUses", 10, 0, 1_000_000);
        TOOL_KILL_USES = BUILDER
                .comment("Base progress a tool gains from a melee mob kill (on top of 1 for the killing hit)")
                .defineInRange("killUses", 15, 0, 1_000_000);
        TOOL_PLAYER_LEVELS_PER_BONUS = BUILDER
                .comment("Every this many player (XP) levels, ore breaks and kills give +1 extra progress")
                .defineInRange("playerLevelsPerBonus", 200, 1, Integer.MAX_VALUE);
        TOOL_PROGRESS_MESSAGES = BUILDER
                .comment("Print 'Amount: N' in chat every time a block break adds tool progress (debug)")
                .define("progressMessages", true);
        BUILDER.pop();

        // ------------------------------------------------------------------
        // Defaults here are the values the game uses right now, so nothing changes
        // until you edit them.
        BUILDER.comment("Multipliers that are also settable with in-game commands").push("modifiers");

        HEALTH_MODIFIER = BUILDER
                .comment("Health bonus modifier")
                .defineInRange("healthModifier", ArbCraft.getHealthModifier(), -1_000_000.0, 1_000_000.0);
        DAMAGE_MODIFIER = BUILDER
                .comment("Damage bonus modifier")
                .defineInRange("damageModifier", ArbCraft.getDamageModifier(), -1_000_000.0, 1_000_000.0);
        EFFICIENCY_MODIFIER = BUILDER
                .comment("Mining efficiency bonus modifier")
                .defineInRange("efficiencyModifier", ArbCraft.getEfficiencyModifier(), -1_000_000.0, 1_000_000.0);
        ATTACK_SPEED_MODIFIER = BUILDER
                .comment("Attack speed bonus modifier")
                .defineInRange("attackSpeedModifier", ArbCraft.getAttackSpeedModifier(), -1_000_000.0, 1_000_000.0);
        BONUS_TICK_MULTIPLIER = BUILDER
                .comment("Block tick accelerator: bonus tick multiplier")
                .defineInRange("bonusTickMultiplier", ArbCraft.getBonusTickMultiplier(), 0, Integer.MAX_VALUE);
        INCREASED_BONUS_TICK_MULTIPLIER = BUILDER
                .comment("Block tick accelerator: increased bonus tick multiplier")
                .defineInRange("increasedBonusTickMultiplier", ArbCraft.getIncreasedBonusTickMultiplier(), 0, Integer.MAX_VALUE);
        MAX_HP_MULTIPLIER = BUILDER
                .comment("Mob scaling: max HP multiplier")
                .defineInRange("maxHpMultiplier", ArbCraft.getMaxHpMultiplier(), -1_000_000.0, 1_000_000.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** Copies the config values that have command-controlled runtime fields into those fields. */
    public static void applyToRuntime() {
        ArbCraft.setBlockExpStatus(BLOCK_XP_ENABLED.get());
        ArbCraft.setNormalBlockExp(NORMAL_BLOCK_EXP.get());
        ArbCraft.setOreBlockExp(ORE_BLOCK_EXP.get());
        ArbCraft.setBonusBlockExp(BONUS_BLOCK_EXP.get());
        ArbCraft.setEffectsEnabled(EFFECTS_ENABLED.get());
        ArbCraft.setHealthModifier(HEALTH_MODIFIER.get());
        ArbCraft.setDamageModifier(DAMAGE_MODIFIER.get());
        ArbCraft.setEfficiencyModifier(EFFICIENCY_MODIFIER.get());
        ArbCraft.setAttackSpeedModifier(ATTACK_SPEED_MODIFIER.get());
        ArbCraft.setBonusTickMultiplier(BONUS_TICK_MULTIPLIER.get());
        ArbCraft.setIncreasedBonusTickMultiplier(INCREASED_BONUS_TICK_MULTIPLIER.get());
        ArbCraft.setMaxHpMultiplier(MAX_HP_MULTIPLIER.get());
    }
}


































//package net.attarax.arbcraft;
//
//import net.neoforged.neoforge.common.ModConfigSpec;
//
//import java.util.List;
//
///**
// * Config for everything in the main ArbCraft class.
// * File: config/arbcraft-gameplay.toml (created on first launch, editable while the game is closed
// * or live while it is running: changes are picked up automatically).
// *
// * Values that also have an in-game command (block exp, modifiers, tick multipliers, effects toggle,
// * max HP multiplier) are copied into the runtime fields whenever the file is loaded or reloaded.
// * That means a file reload overwrites whatever a command set before it.
// */
//public class ArbCraftConfig {
//
//    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
//    public static final ModConfigSpec SPEC;
//
//    // ---- Block experience ----
//    public static final ModConfigSpec.BooleanValue BLOCK_XP_ENABLED;
//    public static final ModConfigSpec.IntValue NORMAL_BLOCK_EXP;
//    public static final ModConfigSpec.IntValue ORE_BLOCK_EXP;
//    public static final ModConfigSpec.IntValue BONUS_BLOCK_EXP;
//    public static final ModConfigSpec.IntValue HIGH_LEVEL_THRESHOLD;
//    public static final ModConfigSpec.IntValue NORMAL_LEVEL_DIVISOR;
//    public static final ModConfigSpec.IntValue LOW_LEVEL_THRESHOLD;
//    public static final ModConfigSpec.IntValue ORE_LEVEL_DIVISOR;
//    public static final ModConfigSpec.IntValue BONUS_LEVEL_DIVISOR;
//    public static final ModConfigSpec.IntValue EXTRA_BONUS_MIN_LEVEL;
//    public static final ModConfigSpec.IntValue EXTRA_BONUS_DIVISOR;
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ORE_NAME_FRAGMENTS;
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> BONUS_NAME_FRAGMENTS;
//
//    // ---- Passive experience ----
//    public static final ModConfigSpec.BooleanValue PASSIVE_XP_ENABLED;
//    public static final ModConfigSpec.IntValue PASSIVE_XP_INTERVAL;
//    public static final ModConfigSpec.IntValue PASSIVE_XP_MAX_DAY_EXP;
//    public static final ModConfigSpec.IntValue PASSIVE_XP_DOUBLE_LEVEL;
//    public static final ModConfigSpec.IntValue PASSIVE_XP_MULTIPLIER;
//
//    // ---- Level effects ----
//    public static final ModConfigSpec.BooleanValue EFFECTS_ENABLED;
//    public static final ModConfigSpec.IntValue EFFECT_CHECK_INTERVAL;
//    public static final ModConfigSpec.IntValue EFFECT_DURATION;
//    public static final ModConfigSpec.IntValue EFFECT_REFRESH_BELOW;
//    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> SPEED_LEVELS;
//    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> LUCK_LEVELS;
//
//    // ---- Food regeneration ----
//    public static final ModConfigSpec.BooleanValue FOOD_REGEN_ENABLED;
//    public static final ModConfigSpec.IntValue FOOD_REGEN_AMOUNT;
//    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> FOOD_TIER_LEVELS;
//    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> FOOD_TIER_INTERVALS;
//
//    // ---- Auto pickup ----
//    public static final ModConfigSpec.BooleanValue AUTO_PICKUP_ENABLED;
//    public static final ModConfigSpec.IntValue AUTO_PICKUP_REQUIRED_LEVEL;
//    public static final ModConfigSpec.DoubleValue AUTO_PICKUP_MAX_DISTANCE_SQR;
//    public static final ModConfigSpec.IntValue AUTO_PICKUP_BREAKER_MEMORY_SECONDS;
//
//    // ---- Block reach ----
//    public static final ModConfigSpec.IntValue REACH_LEVELS_PER_BONUS;
//    public static final ModConfigSpec.IntValue REACH_MAX_BONUS;
//    public static final ModConfigSpec.IntValue REACH_CHECK_INTERVAL;
//
//    // ---- Modifiers (also settable by command) ----
//    public static final ModConfigSpec.DoubleValue HEALTH_MODIFIER;
//    public static final ModConfigSpec.DoubleValue DAMAGE_MODIFIER;
//    public static final ModConfigSpec.DoubleValue EFFICIENCY_MODIFIER;
//    public static final ModConfigSpec.DoubleValue ATTACK_SPEED_MODIFIER;
//    public static final ModConfigSpec.IntValue BONUS_TICK_MULTIPLIER;
//    public static final ModConfigSpec.IntValue INCREASED_BONUS_TICK_MULTIPLIER;
//    public static final ModConfigSpec.DoubleValue MAX_HP_MULTIPLIER;
//
//    static {
//        // ------------------------------------------------------------------
//        BUILDER.comment("Experience given to the player for every block they break").push("blockExperience");
//
//        BLOCK_XP_ENABLED = BUILDER
//                .comment("Master switch. If false, breaking blocks gives no experience.",
//                        "NOTE: auto pickup is also skipped while this is false.")
//                .define("enabled", true);
//        NORMAL_BLOCK_EXP = BUILDER
//                .comment("Base experience for any block")
//                .defineInRange("normalBlockExp", 1, 0, 1_000_000);
//        ORE_BLOCK_EXP = BUILDER
//                .comment("Extra base experience when the block counts as an ore (see oreNameFragments)")
//                .defineInRange("oreBlockExp", 10, 0, 1_000_000);
//        BONUS_BLOCK_EXP = BUILDER
//                .comment("Extra base experience when the block counts as a bonus block (see bonusNameFragments)")
//                .defineInRange("bonusBlockExp", 3, 0, 1_000_000);
//
//        HIGH_LEVEL_THRESHOLD = BUILDER
//                .comment("From this player level, every block gives 1 + (level / normalLevelDivisor) extra experience.",
//                        "Below it, the ore/bonus level bonuses below can apply instead.")
//                .defineInRange("highLevelThreshold", 100, 0, Integer.MAX_VALUE);
//        NORMAL_LEVEL_DIVISOR = BUILDER
//                .comment("Divisor for the high level bonus above")
//                .defineInRange("normalLevelDivisor", 75, 1, Integer.MAX_VALUE);
//        LOW_LEVEL_THRESHOLD = BUILDER
//                .comment("From this level (and below highLevelThreshold), ore and bonus blocks give level based extra experience")
//                .defineInRange("lowLevelThreshold", 20, 0, Integer.MAX_VALUE);
//        ORE_LEVEL_DIVISOR = BUILDER
//                .comment("Ore blocks give (level / this) extra experience in the low level range")
//                .defineInRange("oreLevelDivisor", 10, 1, Integer.MAX_VALUE);
//        BONUS_LEVEL_DIVISOR = BUILDER
//                .comment("Bonus blocks give (level / this) extra experience in the low level range")
//                .defineInRange("bonusLevelDivisor", 30, 1, Integer.MAX_VALUE);
//
//        EXTRA_BONUS_MIN_LEVEL = BUILDER
//                .comment("Players ABOVE this level get (level / extraBonusDivisor) extra experience on every block")
//                .defineInRange("extraBonusMinLevel", 100, 0, Integer.MAX_VALUE);
//        EXTRA_BONUS_DIVISOR = BUILDER
//                .comment("Divisor for the extra bonus above")
//                .defineInRange("extraBonusDivisor", 100, 1, Integer.MAX_VALUE);
//
//        ORE_NAME_FRAGMENTS = BUILDER
//                .comment("A block is an ore if its description id contains any of these (lowercase)")
//                .defineListAllowEmpty("oreNameFragments",
//                        List.of("ore", "ancient_debris", "lucky"),
//                        () -> "", o -> o instanceof String);
//        BONUS_NAME_FRAGMENTS = BUILDER
//                .comment("A block is a bonus block if its description id contains any of these (lowercase)")
//                .defineListAllowEmpty("bonusNameFragments",
//                        List.of("amethyst_block", "prismarine", "obsidian", "gilded_blackstone",
//                                "glowstone", "magma_block", "crying_obsidian", "calcite"),
//                        () -> "", o -> o instanceof String);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        BUILDER.comment("Experience given to players over time").push("passiveExperience");
//
//        PASSIVE_XP_ENABLED = BUILDER
//                .comment("Turn the periodic experience reward on or off")
//                .define("enabled", true);
//        PASSIVE_XP_INTERVAL = BUILDER
//                .comment("Ticks between rewards (20 ticks = 1 second)")
//                .defineInRange("intervalTicks", 3000, 1, Integer.MAX_VALUE);
//        PASSIVE_XP_MAX_DAY_EXP = BUILDER
//                .comment("Each reward is the number of world days that have passed, capped at this value")
//                .defineInRange("maxDayExp", 10000, 0, Integer.MAX_VALUE);
//        PASSIVE_XP_DOUBLE_LEVEL = BUILDER
//                .comment("Players at or above this level get the reward multiplied")
//                .defineInRange("multiplierLevel", 200, 0, Integer.MAX_VALUE);
//        PASSIVE_XP_MULTIPLIER = BUILDER
//                .comment("Multiplier applied to the reward for players at or above multiplierLevel")
//                .defineInRange("multiplier", 2, 1, 1000);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        BUILDER.comment("Permanent potion effects given at certain player levels").push("effects");
//
//        EFFECTS_ENABLED = BUILDER
//                .comment("Master switch for the level based effects")
//                .define("enabled", true);
//        EFFECT_CHECK_INTERVAL = BUILDER
//                .comment("Ticks between effect checks")
//                .defineInRange("checkIntervalTicks", 100, 1, Integer.MAX_VALUE);
//        EFFECT_DURATION = BUILDER
//                .comment("Duration in ticks of the effect when it is (re)applied")
//                .defineInRange("durationTicks", 72000, 1, Integer.MAX_VALUE);
//        EFFECT_REFRESH_BELOW = BUILDER
//                .comment("The effect is re-applied when its remaining duration drops below this many ticks")
//                .defineInRange("refreshBelowTicks", 200, 0, Integer.MAX_VALUE);
//        SPEED_LEVELS = BUILDER
//                .comment("Speed: player levels, in ascending order. The first entry gives Speed I,",
//                        "the second Speed II, and so on (amplifier = position in the list).")
//                .defineListAllowEmpty("speedLevels",
//                        List.of(1000, 3000, 5000),
//                        () -> 0, o -> o instanceof Integer i && i >= 0);
//        LUCK_LEVELS = BUILDER
//                .comment("Luck: player levels, in ascending order. Amplifier = position in the list.")
//                .defineListAllowEmpty("luckLevels",
//                        List.of(100, 200, 400, 600, 1000, 1500, 2000, 2500, 3500, 5000, 10000),
//                        () -> 0, o -> o instanceof Integer i && i >= 0);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        BUILDER.comment("Hunger restored over time for high level players").push("foodRegeneration");
//
//        FOOD_REGEN_ENABLED = BUILDER
//                .comment("Turn food regeneration on or off")
//                .define("enabled", true);
//        FOOD_REGEN_AMOUNT = BUILDER
//                .comment("Food points restored each time a tier triggers (food level never goes above 20)")
//                .defineInRange("amount", 1, 1, 20);
//        FOOD_TIER_LEVELS = BUILDER
//                .comment("Tier N applies from this player level. Must line up with tierIntervalsTicks.",
//                        "Tiers stack: a player who qualifies for several gets all of them.")
//                .defineListAllowEmpty("tierLevels",
//                        List.of(100, 2000, 10000, 20000),
//                        () -> 0, o -> o instanceof Integer i && i >= 0);
//        FOOD_TIER_INTERVALS = BUILDER
//                .comment("Ticks between food restores for tier N (same order as tierLevels)")
//                .defineListAllowEmpty("tierIntervalsTicks",
//                        List.of(600, 500, 400, 300),
//                        () -> 600, o -> o instanceof Integer i && i >= 1);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        BUILDER.comment("Block drops go straight into the inventory of the player who broke the block").push("autoPickup");
//
//        AUTO_PICKUP_ENABLED = BUILDER
//                .comment("Turn auto pickup on or off")
//                .define("enabled", true);
//        AUTO_PICKUP_REQUIRED_LEVEL = BUILDER
//                .comment("Minimum player level for auto pickup")
//                .defineInRange("requiredLevel", 100, 0, Integer.MAX_VALUE);
//        AUTO_PICKUP_MAX_DISTANCE_SQR = BUILDER
//                .comment("Max SQUARED distance between player and drop (25 = 5 blocks, 155 = about 12.4 blocks)")
//                .defineInRange("maxDistanceSquared", 155.0, 0.0, 1_000_000.0);
//        AUTO_PICKUP_BREAKER_MEMORY_SECONDS = BUILDER
//                .comment("How long a broken block position remembers who broke it")
//                .defineInRange("breakerMemorySeconds", 5, 1, 3600);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        BUILDER.comment("Extra block interaction range based on player level").push("blockReach");
//
//        REACH_LEVELS_PER_BONUS = BUILDER
//                .comment("Player levels needed for each +1 block of reach")
//                .defineInRange("levelsPerBonus", 1000, 1, Integer.MAX_VALUE);
//        REACH_MAX_BONUS = BUILDER
//                .comment("Maximum bonus reach in blocks")
//                .defineInRange("maxBonus", 10, 0, 1000);
//        REACH_CHECK_INTERVAL = BUILDER
//                .comment("Ticks between checks for a level change")
//                .defineInRange("checkIntervalTicks", 60, 1, Integer.MAX_VALUE);
//        BUILDER.pop();
//
//        // ------------------------------------------------------------------
//        // Defaults here are the values the game uses right now, so nothing changes
//        // until you edit them.
//        BUILDER.comment("Multipliers that are also settable with in-game commands").push("modifiers");
//
//        HEALTH_MODIFIER = BUILDER
//                .comment("Health bonus modifier")
//                .defineInRange("healthModifier", ArbCraft.getHealthModifier(), -1_000_000.0, 1_000_000.0);
//        DAMAGE_MODIFIER = BUILDER
//                .comment("Damage bonus modifier")
//                .defineInRange("damageModifier", ArbCraft.getDamageModifier(), -1_000_000.0, 1_000_000.0);
//        EFFICIENCY_MODIFIER = BUILDER
//                .comment("Mining efficiency bonus modifier")
//                .defineInRange("efficiencyModifier", ArbCraft.getEfficiencyModifier(), -1_000_000.0, 1_000_000.0);
//        ATTACK_SPEED_MODIFIER = BUILDER
//                .comment("Attack speed bonus modifier")
//                .defineInRange("attackSpeedModifier", ArbCraft.getAttackSpeedModifier(), -1_000_000.0, 1_000_000.0);
//        BONUS_TICK_MULTIPLIER = BUILDER
//                .comment("Block tick accelerator: bonus tick multiplier")
//                .defineInRange("bonusTickMultiplier", ArbCraft.getBonusTickMultiplier(), 0, Integer.MAX_VALUE);
//        INCREASED_BONUS_TICK_MULTIPLIER = BUILDER
//                .comment("Block tick accelerator: increased bonus tick multiplier")
//                .defineInRange("increasedBonusTickMultiplier", ArbCraft.getIncreasedBonusTickMultiplier(), 0, Integer.MAX_VALUE);
//        MAX_HP_MULTIPLIER = BUILDER
//                .comment("Mob scaling: max HP multiplier")
//                .defineInRange("maxHpMultiplier", ArbCraft.getMaxHpMultiplier(), -1_000_000.0, 1_000_000.0);
//        BUILDER.pop();
//
//        SPEC = BUILDER.build();
//    }
//
//    /** Copies the config values that have command-controlled runtime fields into those fields. */
//    public static void applyToRuntime() {
//        ArbCraft.setBlockExpStatus(BLOCK_XP_ENABLED.get());
//        ArbCraft.setNormalBlockExp(NORMAL_BLOCK_EXP.get());
//        ArbCraft.setOreBlockExp(ORE_BLOCK_EXP.get());
//        ArbCraft.setBonusBlockExp(BONUS_BLOCK_EXP.get());
//        ArbCraft.setEffectsEnabled(EFFECTS_ENABLED.get());
//        ArbCraft.setHealthModifier(HEALTH_MODIFIER.get());
//        ArbCraft.setDamageModifier(DAMAGE_MODIFIER.get());
//        ArbCraft.setEfficiencyModifier(EFFICIENCY_MODIFIER.get());
//        ArbCraft.setAttackSpeedModifier(ATTACK_SPEED_MODIFIER.get());
//        ArbCraft.setBonusTickMultiplier(BONUS_TICK_MULTIPLIER.get());
//        ArbCraft.setIncreasedBonusTickMultiplier(INCREASED_BONUS_TICK_MULTIPLIER.get());
//        ArbCraft.setMaxHpMultiplier(MAX_HP_MULTIPLIER.get());
//    }
//}