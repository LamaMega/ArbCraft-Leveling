//
//package net.attarax.arbcraft.config;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonObject;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.Items;
//import net.neoforged.fml.loading.FMLPaths;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ExperienceConverterConfig {
//
//    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
//    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "arbcraft_experience_converter.json");
//    public static File getConfigFile() { return CONFIG_FILE; }
//
//    private static final Map<Item, Integer> EXPERIENCE_VALUES = new HashMap<>();
//
//    // ── Upgrade config (loaded from JSON, sensible defaults here as fallback) ──
//    private static Item upgradeMaterial         = Items.COPPER_BLOCK;
//    private static int  upgradeBaseCost         = 100;   // cost for level 0 → 1
//    private static int  upgradeCostIncrease     = 50;    // +50 per subsequent level
//    private static int  upgradeTimeReduction    = 10;    // ticks saved per level
//    private static int  upgradeExpBonusPct      = 25;    // % XP bonus added per level
//    private static int  baseConversionTime      = 80;    // ticks at level 0
//    private static int  minConversionTime       = 10;    // hard floor on conversion time
//    private static int  maxUpgradeLevel         = 20;    // cap; 0 = unlimited
//
//    // ─────────────────────────────────────────────────────────────────────────
//
//    public static void load() {
//        if (!CONFIG_FILE.exists()) {
//            createDefaultConfig();
//            return;
//        }
//
//        try (FileReader reader = new FileReader(CONFIG_FILE)) {
//            JsonObject json = GSON.fromJson(reader, JsonObject.class);
//
//            // ── Conversions ──────────────────────────────────────────────────
//            EXPERIENCE_VALUES.clear();
//            if (json.has("conversions")) {
//                json.getAsJsonObject("conversions").entrySet().forEach(entry -> {
//                    ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
//                    if (id != null) {
//                        Item item = BuiltInRegistries.ITEM.get(id);
//                        if (item != null) {
//                            EXPERIENCE_VALUES.put(item, entry.getValue().getAsInt());
//                        }
//                    }
//                });
//            }
//
//            // ── Upgrade settings ─────────────────────────────────────────────
//            if (json.has("upgrades")) {
//                JsonObject u = json.getAsJsonObject("upgrades");
//
//                if (u.has("upgrade_material")) {
//                    ResourceLocation matId = ResourceLocation.tryParse(u.get("upgrade_material").getAsString());
//                    if (matId != null) {
//                        Item mat = BuiltInRegistries.ITEM.get(matId);
//                        if (mat != Items.AIR) upgradeMaterial = mat;
//                    }
//                }
//                if (u.has("base_cost"))                  upgradeBaseCost      = u.get("base_cost").getAsInt();
//                if (u.has("cost_increase_per_level"))    upgradeCostIncrease  = u.get("cost_increase_per_level").getAsInt();
//                if (u.has("time_reduction_per_level"))   upgradeTimeReduction = u.get("time_reduction_per_level").getAsInt();
//                if (u.has("exp_bonus_percent_per_level"))upgradeExpBonusPct   = u.get("exp_bonus_percent_per_level").getAsInt();
//                if (u.has("base_conversion_time"))       baseConversionTime   = u.get("base_conversion_time").getAsInt();
//                if (u.has("min_conversion_time"))        minConversionTime    = u.get("min_conversion_time").getAsInt();
//                if (u.has("max_upgrade_level"))          maxUpgradeLevel      = u.get("max_upgrade_level").getAsInt();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            createDefaultConfig();
//        }
//    }
//
//    private static void createDefaultConfig() {
//        JsonObject config = new JsonObject();
//
//        // ── Conversions ──────────────────────────────────────────────────────
//        JsonObject conversions = new JsonObject();
//         //Default values
//
//        //MISC
//        conversions.addProperty("minecraft:enchanted_book", 100);
//        conversions.addProperty("minecraft:potion", 60);
//        conversions.addProperty("minecraft:splash_potion", 60);
//        conversions.addProperty("minecraft:arrow", 60);
//        conversions.addProperty("minecraft:spectral_arrow", 60);
//        conversions.addProperty("minecraft:tipped_arrow", 60);
//        conversions.addProperty("minecraft:tnt", 60);
//        conversions.addProperty("minecraft:end_crystal", 600);
//        conversions.addProperty("minecraft:wind_charge", 60);
//        conversions.addProperty("minecraft:firework_rocket", 60);
//        conversions.addProperty("minecraft:experience_bottle", 600);
//        conversions.addProperty("minecraft:nether_star", 3000);
//        conversions.addProperty("minecraft:leather", 30);
//        conversions.addProperty("minecraft:heart_of_the_sea", 1000);
//        conversions.addProperty("minecraft:ender_eye", 300);
//        conversions.addProperty("minecraft:echo_shard", 120);
//
//
//        //ORES
//        conversions.addProperty("minecraft:coal_block", 180);
//        conversions.addProperty("minecraft:coal", 20);
//        conversions.addProperty("minecraft:copper_block", 270);
//        conversions.addProperty("minecraft:raw_copper", 30);
//        conversions.addProperty("minecraft:copper_ingot", 30);
//        conversions.addProperty("minecraft:iron_block", 360);
//        conversions.addProperty("minecraft:iron_ingot", 40);
//        conversions.addProperty("minecraft:gold_block", 500);
//        conversions.addProperty("minecraft:gold_ingot", 55);
//        conversions.addProperty("minecraft:diamond_block", 900);
//        conversions.addProperty("minecraft:diamond", 100);
//        conversions.addProperty("minecraft:netherite_block", 1200);
//        conversions.addProperty("minecraft:netherite_ingot", 133);
//        conversions.addProperty("minecraft:quartz_block", 160);
//        conversions.addProperty("minecraft:quartz", 18);
//        conversions.addProperty("minecraft:redstone_block", 360);
//        conversions.addProperty("minecraft:redstone", 40);
//        conversions.addProperty("minecraft:lapis_block", 200);
//        conversions.addProperty("minecraft:lapis", 22);
//        conversions.addProperty("minecraft:emerald", 111);
//        conversions.addProperty("minecraft:emerald_block", 1000);
//
//        //Decoration Blocks
//        conversions.addProperty("minecraft:amethyst_block", 300);
//        conversions.addProperty("minecraft:amethyst_shard", 35);
//        conversions.addProperty("minecraft:prismarine", 125);
//        conversions.addProperty("minecraft:obsidian", 100);
//        conversions.addProperty("minecraft:cobblestone", 3);
//        conversions.addProperty("minecraft:stone", 5);
//        conversions.addProperty("minecraft:smooth_stone", 7);
//        conversions.addProperty("minecraft:dirt", 3);
//        conversions.addProperty("minecraft:wheat", 2);
//        conversions.addProperty("minecraft:egg", 2);
//        conversions.addProperty("minecraft:apple", 25);
//        conversions.addProperty("minecraft:gunpowder", 50);
//        conversions.addProperty("minecraft:glowstone", 360);
//        conversions.addProperty("minecraft:glowstone_dust", 40);
//
//        //Wood
//        conversions.addProperty("minecraft:oak_log", 12);
//        conversions.addProperty("minecraft:dark_oak_log", 12);
//        conversions.addProperty("minecraft:spruce_log", 12);
//        conversions.addProperty("minecraft:birch_log", 12);
//        conversions.addProperty("minecraft:jungle_log", 12);
//        conversions.addProperty("minecraft:acacia_log", 12);
//        conversions.addProperty("minecraft:mangrove_log", 12);
//        conversions.addProperty("minecraft:cherry_log", 12);
//
//
//
//        config.add("conversions", conversions);
//
//        // ── Upgrade settings ─────────────────────────────────────────────────
//        JsonObject upgrades = new JsonObject();
//        upgrades.addProperty("upgrade_material",          "minecraft:copper_block");
//        upgrades.addProperty("base_cost",                  100);  // copper blocks for first upgrade
//        upgrades.addProperty("cost_increase_per_level",     50);  // each level costs 50 more
//        upgrades.addProperty("time_reduction_per_level",    10);  // ticks shaved off per level
//        upgrades.addProperty("exp_bonus_percent_per_level", 25);  // +25% XP per level
//        upgrades.addProperty("base_conversion_time",        400);  // ticks at level 0
//        upgrades.addProperty("min_conversion_time",         10);  // never faster than this
//        upgrades.addProperty("max_upgrade_level",           32);  // 0 = no cap
//        config.add("upgrades", upgrades);
//
//        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
//            GSON.toJson(config, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        load();
//    }
//
//    // ── Conversion helpers ────────────────────────────────────────────────────
//
//    public static int getExperienceValue(Item item) {
//        return EXPERIENCE_VALUES.getOrDefault(item, 3);
//    }
//
//    public static boolean canConvert(Item item) {
//        return EXPERIENCE_VALUES.containsKey(item);
//    }
//
//    // ── Upgrade getters ───────────────────────────────────────────────────────
//
//    public static Item getUpgradeMaterial()          { return upgradeMaterial; }
//    public static int  getUpgradeBaseCost()          { return upgradeBaseCost; }
//    public static int  getUpgradeCostIncrease()      { return upgradeCostIncrease; }
//    public static int  getUpgradeTimeReduction()     { return upgradeTimeReduction; }
//    public static int  getUpgradeExpBonusPct()       { return upgradeExpBonusPct; }
//    public static int  getBaseConversionTime()       { return baseConversionTime; }
//    public static int  getMinConversionTime()        { return minConversionTime; }
//    public static int  getMaxUpgradeLevel()          { return maxUpgradeLevel; }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////package net.attarax.arbcraft.config;
////
////import com.google.gson.Gson;
////import com.google.gson.GsonBuilder;
////import com.google.gson.JsonObject;
////import net.minecraft.resources.ResourceLocation;
////import net.minecraft.world.item.Item;
////import net.neoforged.fml.loading.FMLPaths;
////import net.neoforged.neoforge.registries.RegistryManager;
////import net.minecraft.core.registries.BuiltInRegistries;
////
////import java.io.File;
////import java.io.FileReader;
////import java.io.FileWriter;
////import java.io.IOException;
////import java.util.HashMap;
////import java.util.Map;
////
////public class ExperienceConverterConfig {
////
////    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
////    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "arbcraft_experience_converter.json");
////    public static File getConfigFile() {return CONFIG_FILE;}
////    private static final Map<Item, Integer> EXPERIENCE_VALUES = new HashMap<>();
////
////    public static void load() {
////        if (!CONFIG_FILE.exists()) {
////            createDefaultConfig();
////        }
////
////        try (FileReader reader = new FileReader(CONFIG_FILE)) {
////            JsonObject json = GSON.fromJson(reader, JsonObject.class);
////            EXPERIENCE_VALUES.clear();
////
////            JsonObject conversions = json.getAsJsonObject("conversions");
////            conversions.entrySet().forEach(entry -> {
////                ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
////                if (itemId != null) {
////                    Item item = BuiltInRegistries.ITEM.get(itemId);
//////                    Item item = RegistryManager.ACTIVE.getRegistry(net.minecraft.core.registries.Registries.ITEM)
////                    if (item != null) {
////                        EXPERIENCE_VALUES.put(item, entry.getValue().getAsInt());
////                    }
////                }
////            });
////
////        } catch (IOException e) {
////            e.printStackTrace();
////            createDefaultConfig();
////        }
////    }
////
////    private static void createDefaultConfig() {
////        JsonObject config = new JsonObject();
////        JsonObject conversions = new JsonObject();
////
////        // Default values
////        conversions.addProperty("minecraft:enchanted_book", 100);
////        conversions.addProperty("minecraft:coal_block", 180);
////        conversions.addProperty("minecraft:coal", 20);
////        conversions.addProperty("minecraft:copper_block", 270);
////        conversions.addProperty("minecraft:raw_copper", 30);
////        conversions.addProperty("minecraft:copper_ingot", 30);
////        conversions.addProperty("minecraft:iron_block", 150);
////        conversions.addProperty("minecraft:gold_block", 500);
////        conversions.addProperty("minecraft:gold_ingot", 55);
////        conversions.addProperty("minecraft:diamond_block", 900);
////        conversions.addProperty("minecraft:diamond", 100);
////        conversions.addProperty("minecraft:netherite_block", 1200);
////        conversions.addProperty("minecraft:quartz_block", 160);
////        conversions.addProperty("minecraft:quartz", 18);
////        conversions.addProperty("minecraft:amethyst_block", 300);
////        conversions.addProperty("minecraft:amethyst_shard", 35);
////        conversions.addProperty("minecraft:prismarine", 125);
////        conversions.addProperty("minecraft:obsidian", 100);
////        conversions.addProperty("minecraft:cobblestone", 3);
////        conversions.addProperty("minecraft:dirt", 3);
////        conversions.addProperty("minecraft:wheat", 2);
////        conversions.addProperty("minecraft:egg", 2);
////        conversions.addProperty("minecraft:apple", 25);
////        conversions.addProperty("minecraft:gunpowder", 50);
////        conversions.addProperty("minecraft:redstone_block", 360);
////        conversions.addProperty("minecraft:redstone", 40);
////        conversions.addProperty("minecraft:lapis_block", 200);
////        conversions.addProperty("minecraft:lapis", 22);
////
////        //conversions.addProperty("avaritia:infinity_block", 1000000);
////
////        config.add("conversions", conversions);
////
////        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
////            GSON.toJson(config, writer);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////
////        load();
////    }
////
////
////
////    public static int getExperienceValue(Item item) {
////        return EXPERIENCE_VALUES.getOrDefault(item, 5);
////    }
////
////    public static boolean canConvert(Item item) {
////        return EXPERIENCE_VALUES.containsKey(item);
////    }
////}

package net.attarax.arbcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExperienceConverterConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "arbcraft_experience_converter.json");
    public static File getConfigFile() { return CONFIG_FILE; }

    private static final Map<Item, Integer> EXPERIENCE_VALUES = new HashMap<>();

    private static Item upgradeMaterial      = Items.COPPER_BLOCK;
    private static int  upgradeBaseCost      = 100;
    private static int  upgradeCostIncrease  = 50;
    private static int  upgradeTimeReduction = 10;
    private static int  upgradeExpBonusPct   = 25;
    private static int  baseConversionTime   = 400;
    private static int  minConversionTime    = 10;
    private static int  maxUpgradeLevel      = 32;

    // ── Default builders ──────────────────────────────────────────────────────
    // Add new items here in future mod versions; they will be merged in automatically.

    private static JsonObject buildDefaultConversions() {
        JsonObject c = new JsonObject();

        // MISC
        c.addProperty("minecraft:enchanted_book",    100);
        c.addProperty("minecraft:potion",             60);
        c.addProperty("minecraft:splash_potion",      60);
        c.addProperty("minecraft:arrow",              60);
        c.addProperty("minecraft:spectral_arrow",     60);
        c.addProperty("minecraft:tipped_arrow",       60);
        c.addProperty("minecraft:tnt",                60);
        c.addProperty("minecraft:end_crystal",       600);
        c.addProperty("minecraft:wind_charge",        60);
        c.addProperty("minecraft:firework_rocket",    60);
        c.addProperty("minecraft:experience_bottle", 600);
        c.addProperty("minecraft:nether_star",      3000);
        c.addProperty("minecraft:leather",            30);
        c.addProperty("minecraft:heart_of_the_sea", 1000);
        c.addProperty("minecraft:ender_eye",         300);
        c.addProperty("minecraft:echo_shard",        120);

        // ORES
        c.addProperty("minecraft:coal_block",        180);
        c.addProperty("minecraft:coal",               20);
        c.addProperty("minecraft:copper_block",      270);
        c.addProperty("minecraft:raw_copper",         30);
        c.addProperty("minecraft:copper_ingot",       30);
        c.addProperty("minecraft:iron_block",        360);
        c.addProperty("minecraft:iron_ingot",         40);
        c.addProperty("minecraft:gold_block",        500);
        c.addProperty("minecraft:gold_ingot",         55);
        c.addProperty("minecraft:diamond_block",     900);
        c.addProperty("minecraft:diamond",           100);
        c.addProperty("minecraft:netherite_block",  1200);
        c.addProperty("minecraft:netherite_ingot",   133);
        c.addProperty("minecraft:quartz_block",      160);
        c.addProperty("minecraft:quartz",             18);
        c.addProperty("minecraft:redstone_block",    360);
        c.addProperty("minecraft:redstone",           40);
        c.addProperty("minecraft:lapis_block",       200);
        c.addProperty("minecraft:lapis",              22);
        c.addProperty("minecraft:emerald",           111);
        c.addProperty("minecraft:emerald_block",    1000);

        // DECORATION / MISC BLOCKS
        c.addProperty("minecraft:amethyst_block",    300);
        c.addProperty("minecraft:amethyst_shard",     35);
        c.addProperty("minecraft:prismarine",        125);
        c.addProperty("minecraft:obsidian",          100);
        c.addProperty("minecraft:cobblestone",         3);
        c.addProperty("minecraft:stone",               5);
        c.addProperty("minecraft:smooth_stone",        7);
        c.addProperty("minecraft:dirt",                3);
        c.addProperty("minecraft:wheat",               2);
        c.addProperty("minecraft:egg",                 2);
        c.addProperty("minecraft:apple",              25);
        c.addProperty("minecraft:gunpowder",          50);
        c.addProperty("minecraft:glowstone",         360);
        c.addProperty("minecraft:glowstone_dust",     40);

        // WOOD
        c.addProperty("minecraft:oak_log",            12);
        c.addProperty("minecraft:dark_oak_log",       12);
        c.addProperty("minecraft:spruce_log",         12);
        c.addProperty("minecraft:birch_log",          12);
        c.addProperty("minecraft:jungle_log",         12);
        c.addProperty("minecraft:acacia_log",         12);
        c.addProperty("minecraft:mangrove_log",       12);
        c.addProperty("minecraft:cherry_log",         12);

        return c;
    }

    private static JsonObject buildDefaultUpgrades() {
        JsonObject u = new JsonObject();
        u.addProperty("upgrade_material",           "minecraft:copper_block");
        u.addProperty("base_cost",                   100);
        u.addProperty("cost_increase_per_level",      50);
        u.addProperty("time_reduction_per_level",     20);
        u.addProperty("exp_bonus_percent_per_level",  30);
        u.addProperty("base_conversion_time",        400);
        u.addProperty("min_conversion_time",          10);
        u.addProperty("max_upgrade_level",            16);
        return u;
    }

    // ── Merge helper ──────────────────────────────────────────────────────────
    // Returns true if anything was added (so the caller knows to save).

    private static boolean mergeDefaults(JsonObject target, JsonObject defaults) {
        boolean changed = false;
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            if (!target.has(entry.getKey())) {
                target.add(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            createDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            boolean dirty = false;

            // ── Conversions: merge new defaults, keep existing values ─────────
            if (!json.has("conversions")) {
                json.add("conversions", new JsonObject());
            }
            JsonObject conversions = json.getAsJsonObject("conversions");
            dirty |= mergeDefaults(conversions, buildDefaultConversions());

            // Populate the in-memory map from the (now merged) conversions
            EXPERIENCE_VALUES.clear();
            conversions.entrySet().forEach(entry -> {
                ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                if (id != null) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != null) {
                        EXPERIENCE_VALUES.put(item, entry.getValue().getAsInt());
                    }
                }
            });

            // ── Upgrades: merge new keys, keep existing values ────────────────
            if (!json.has("upgrades")) {
                json.add("upgrades", new JsonObject());
            }
            JsonObject u = json.getAsJsonObject("upgrades");
            dirty |= mergeDefaults(u, buildDefaultUpgrades());

            if (u.has("upgrade_material")) {
                ResourceLocation matId = ResourceLocation.tryParse(u.get("upgrade_material").getAsString());
                if (matId != null) {
                    Item mat = BuiltInRegistries.ITEM.get(matId);
                    if (mat != Items.AIR) upgradeMaterial = mat;
                }
            }
            if (u.has("base_cost"))                   upgradeBaseCost      = u.get("base_cost").getAsInt();
            if (u.has("cost_increase_per_level"))     upgradeCostIncrease  = u.get("cost_increase_per_level").getAsInt();
            if (u.has("time_reduction_per_level"))    upgradeTimeReduction = u.get("time_reduction_per_level").getAsInt();
            if (u.has("exp_bonus_percent_per_level")) upgradeExpBonusPct   = u.get("exp_bonus_percent_per_level").getAsInt();
            if (u.has("base_conversion_time"))        baseConversionTime   = u.get("base_conversion_time").getAsInt();
            if (u.has("min_conversion_time"))         minConversionTime    = u.get("min_conversion_time").getAsInt();
            if (u.has("max_upgrade_level"))           maxUpgradeLevel      = u.get("max_upgrade_level").getAsInt();

            // ── Write back only if something was actually added ───────────────
            if (dirty) {
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    GSON.toJson(json, writer);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            createDefaultConfig();
        }
    }

    // ── First-run config creation ─────────────────────────────────────────────

    private static void createDefaultConfig() {
        JsonObject config = new JsonObject();
        config.add("conversions", buildDefaultConversions());
        config.add("upgrades",    buildDefaultUpgrades());

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        load();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static int     getExperienceValue(Item item)  { return EXPERIENCE_VALUES.getOrDefault(item, 5); }
    public static boolean canConvert(Item item)          { return EXPERIENCE_VALUES.containsKey(item); }

    public static Item getUpgradeMaterial()              { return upgradeMaterial; }
    public static int  getUpgradeBaseCost()              { return upgradeBaseCost; }
    public static int  getUpgradeCostIncrease()          { return upgradeCostIncrease; }
    public static int  getUpgradeTimeReduction()         { return upgradeTimeReduction; }
    public static int  getUpgradeExpBonusPct()           { return upgradeExpBonusPct; }
    public static int  getBaseConversionTime()           { return baseConversionTime; }
    public static int  getMinConversionTime()            { return minConversionTime; }
    public static int  getMaxUpgradeLevel()              { return maxUpgradeLevel; }
}