package net.attarax.arbcraft.toollevel;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool leveling: tools gain levels from use, and each level gives
 *   +durability, +attack damage, +mining efficiency (diggers only).
 * Every TIER_LEVEL_INTERVAL levels, digger tools also gain +1 mining tier
 * (e.g. a stone pickaxe at level 50 can mine diamond ore).
 * Breaking an ore block gives ORE_USES progress instead of 1.
 *
 * Non-sword tools (DiggerItem) gain progress per block they break, so a tool that
 * breaks several blocks at once (area / vein miners) gets progress for every block.
 * Swords only gain progress from melee hits.
 *
 * Works for pickaxe, axe, shovel, hoe (DiggerItem) and sword (SwordItem),
 * including modded tools that extend those classes.
 */
@EventBusSubscriber(modid = ArbCraft.MODID)
public class ToolLevelHandler {

    // ---- Tuning ----
    public static final int USES_PER_LEVEL = 250;
    public static final int DURABILITY_PER_LEVEL = 5;
    public static final double DAMAGE_PER_LEVEL = 0.1;
    public static final double EFFICIENCY_PER_LEVEL = 0.05;
    /** Each level needs this fraction more uses than the previous one (0.07 = +7%). */
    public static final double REQUIREMENT_GROWTH = 0.07;
    /** The requirement for a single level never grows past this many uses. */
    public static final long MAX_USES_PER_LEVEL = 10000;
    /** Every this many levels, digger tools gain +1 mining tier. */
    public static final int TIER_LEVEL_INTERVAL = 50;
    /** Progress a tool gains from breaking an ore block (any other block gives 1). */
    public static final int ORE_USES = 10;
    /** Progress a tool gains from killing a mob (on top of the 1 for the killing hit). */
    public static final int KILL_USES = 10;

    private static final ResourceLocation DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(ArbCraft.MODID, "tool_level_damage");
    private static final ResourceLocation EFFICIENCY_ID =
            ResourceLocation.fromNamespaceAndPath(ArbCraft.MODID, "tool_level_efficiency");

    /**
     * Tiers from lowest to highest, as the "incorrect for drops" tag of each tier.
     * Gold counts as the same tier as wood.
     */
    private static final List<TagKey<Block>> TIER_TAGS = List.of(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

    // ---- Helpers ----

    public static boolean isLevelable(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        return stack.getItem() instanceof DiggerItem || stack.getItem() instanceof SwordItem;
    }

    public static int getUses(ItemStack stack) {
        return stack.getOrDefault(ToolLevelComponents.TOOL_USES.get(), 0);
    }

    /** Uses needed to go from `level` to `level + 1`, growing each level and capped at MAX_USES_PER_LEVEL. */
    public static long usesToNextLevel(int level) {
        long required = Math.round(USES_PER_LEVEL * Math.pow(1.0 + REQUIREMENT_GROWTH, level));
        return Math.min(MAX_USES_PER_LEVEL, Math.max(1L, required));
    }

    /** Level, progress into the current level, and uses required for the next level. */
    public record LevelInfo(int level, long progress, long required) {}

    public static LevelInfo getLevelInfo(int totalUses) {
        long remaining = totalUses;
        int level = 0;
        long required = usesToNextLevel(level);
        while (remaining >= required && required < MAX_USES_PER_LEVEL) {
            remaining -= required;
            level++;
            required = usesToNextLevel(level);
        }
        if (required >= MAX_USES_PER_LEVEL) {
            // Every further level costs exactly the cap, so skip the loop
            level += (int) (remaining / MAX_USES_PER_LEVEL);
            remaining %= MAX_USES_PER_LEVEL;
            required = MAX_USES_PER_LEVEL;
        }
        return new LevelInfo(level, remaining, required);
    }

    public static int getLevel(ItemStack stack) {
        return getLevelInfo(getUses(stack)).level();
    }

    /** How many mining tiers a tool of this level has gained. */
    public static int getBonusTiers(int level) {
        return level / TIER_LEVEL_INTERVAL;
    }

    /** Counts one use of the stack. */
    public static void addUse(ServerPlayer player, ItemStack stack) {
        addUses(player, stack, 1);
    }

    /**
     * Adds `amount` progress to the stack and levels it up if a threshold was crossed.
     * Compares the level before and after, so a big jump (e.g. 10 from an ore)
     * that skips over a threshold still triggers the level-up.
     */
    public static void addUses(ServerPlayer player, ItemStack stack, int amount) {
        if (!isLevelable(stack) || amount <= 0) return;

        int oldUses = getUses(stack);
        int newUses = (int) Math.min(Integer.MAX_VALUE, (long) oldUses + amount);
        stack.set(ToolLevelComponents.TOOL_USES.get(), newUses);

        int oldLevel = getLevelInfo(oldUses).level();
        int newLevel = getLevelInfo(newUses).level();
        if (newLevel <= oldLevel) return;

        applyLevelBonuses(stack, newLevel);

        boolean tierUp = stack.getItem() instanceof DiggerItem
                && getBonusTiers(newLevel) > getBonusTiers(oldLevel);
        Component message = tierUp
                ? Component.literal("Tool level " + newLevel + "! Mining tier increased!")
                .withStyle(ChatFormatting.AQUA)
                : Component.literal("Tool level up! Level " + newLevel)
                .withStyle(ChatFormatting.GOLD);

        player.displayClientMessage(message, true);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, tierUp ? 1.0F : 1.4F);
    }

    /**
     * Sets the stack's stats for the given level. Always computed from the item's
     * base values, so calling it again never stacks bonuses twice.
     */
    public static void applyLevelBonuses(ItemStack stack, int level) {
        // Durability: base max damage + level * DURABILITY_PER_LEVEL
        int baseMaxDamage = stack.getItem().components().getOrDefault(DataComponents.MAX_DAMAGE, 0);
        if (baseMaxDamage > 0) {
            stack.set(DataComponents.MAX_DAMAGE, baseMaxDamage + level * DURABILITY_PER_LEVEL);
        }

        // Attributes: copy the existing ones, drop our old modifiers, add the new ones
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            ResourceLocation id = entry.modifier().id();
            if (id.equals(DAMAGE_ID) || id.equals(EFFICIENCY_ID)) continue;
            entries.add(entry);
        }

        if (level > 0) {
            entries.add(new ItemAttributeModifiers.Entry(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(DAMAGE_ID, round(level * DAMAGE_PER_LEVEL),
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND));

            if (stack.getItem() instanceof DiggerItem) {
                entries.add(new ItemAttributeModifiers.Entry(
                        Attributes.MINING_EFFICIENCY,
                        new AttributeModifier(EFFICIENCY_ID, round(level * EFFICIENCY_PER_LEVEL),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND));
            }
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
                new ItemAttributeModifiers(List.copyOf(entries), existing.showInTooltip()));

        applyTierBonus(stack, level);
    }

    /**
     * Raises the tool's mining tier by getBonusTiers(level).
     * A tool's tier is the "incorrect for drops" rule inside its Tool component, e.g. a stone
     * pickaxe denies drops for blocks in #minecraft:incorrect_for_stone_tool. Swapping that tag for
     * the next tier's tag lets the tool harvest the next tier of blocks. Rebuilt from the item's
     * base Tool component every time, so it never stacks twice.
     * Tools with custom tiers (rules we don't recognise) are left unchanged.
     */
    private static void applyTierBonus(ItemStack stack, int level) {
        if (!(stack.getItem() instanceof DiggerItem)) return;

        Tool base = stack.getItem().components().get(DataComponents.TOOL);
        if (base == null) return;

        int bonus = getBonusTiers(level);
        if (bonus <= 0) {
            stack.set(DataComponents.TOOL, base);
            return;
        }

        List<Tool.Rule> rules = new ArrayList<>();
        for (Tool.Rule rule : base.rules()) {
            Tool.Rule newRule = rule;

            boolean isDenyRule = rule.correctForDrops().isPresent() && !rule.correctForDrops().get();
            if (isDenyRule && rule.blocks() instanceof HolderSet.Named<Block> named) {
                int index = tierIndex(named.key());
                if (index >= 0) {
                    int target = Math.min(index + bonus, TIER_TAGS.size() - 1);
                    newRule = new Tool.Rule(
                            BuiltInRegistries.BLOCK.getOrCreateTag(TIER_TAGS.get(target)),
                            rule.speed(),
                            rule.correctForDrops());
                }
            }
            rules.add(newRule);
        }

        stack.set(DataComponents.TOOL,
                new Tool(List.copyOf(rules), base.defaultMiningSpeed(), base.damagePerBlock()));
    }

    private static int tierIndex(TagKey<Block> tag) {
        if (tag.equals(BlockTags.INCORRECT_FOR_GOLD_TOOL)) return 0; // gold == wood tier
        return TIER_TAGS.indexOf(tag);
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // ---- Events ----

    /**
     * Mining: non-sword tools get progress for every block they break (ore blocks count as
     * ORE_USES). Multi-block tools (hammers, vein miners...) break their extra blocks through
     * the normal break path, so this event fires once per block and each one adds progress.
     * Swords don't gain progress from breaking blocks, only from hitting.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player instanceof FakePlayer || player.isCreative()) return;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof SwordItem) return;
        if (event.getState().getDestroySpeed(event.getLevel(), event.getPos()) <= 0) return;

        int amount = event.getState().is(Tags.Blocks.ORES) ? ORE_USES : 1;
        addUses(player, held, amount);
    }

    /** Combat: every melee hit that deals damage counts as one use. */
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (source.getDirectEntity() != player) return; // melee only, not arrows etc.
        if (player instanceof FakePlayer || player.isCreative()) return;
        if (event.getNewDamage() <= 0) return;

        addUse(player, player.getMainHandItem());
    }

    /** Kills: a melee kill of a mob gives KILL_USES progress to the held tool. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (source.getDirectEntity() != player) return; // melee only, not arrows etc.
        if (player instanceof FakePlayer || player.isCreative()) return;

        addUses(player, player.getMainHandItem(), KILL_USES);
    }

    /** Right-click actions: stripping logs (axe), tilling (hoe), flattening paths (shovel). */
    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()) return;
        if (event.getFinalState() == null) return;
        if (!(event.getContext().getPlayer() instanceof ServerPlayer player)) return;
        if (player instanceof FakePlayer || player.isCreative()) return;

        addUse(player, event.getContext().getItemInHand());
    }

    /** Shows the level, progress and bonus mining tier in the item tooltip. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isLevelable(stack)) return;

        LevelInfo info = getLevelInfo(getUses(stack));
        event.getToolTip().add(Component.literal("Level " + info.level())
                .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.literal("Progress: " + info.progress() + "/" + info.required())
                .withStyle(ChatFormatting.GRAY));

        int bonusTiers = getBonusTiers(info.level());
        if (bonusTiers > 0 && stack.getItem() instanceof DiggerItem) {
            event.getToolTip().add(Component.literal("Mining tier +" + bonusTiers)
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}