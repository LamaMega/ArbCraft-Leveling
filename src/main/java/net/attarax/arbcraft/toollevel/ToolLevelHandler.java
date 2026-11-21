package net.attarax.arbcraft.toollevel;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool leveling: every USES_PER_LEVEL uses a tool gains a level.
 * Each level gives:
 *   +1 max durability, +0.1 attack damage, +0.05 mining efficiency (diggers only).
 *
 * Works for pickaxe, axe, shovel, hoe (DiggerItem) and sword (SwordItem),
 * including modded tools that extend those classes.
 */
@EventBusSubscriber(modid = ArbCraft.MODID)
public class ToolLevelHandler {

    // ---- Tuning ----
    public static final int USES_PER_LEVEL = 250;
    public static int DURABILITY_PER_LEVEL = 5;
    public static final double DAMAGE_PER_LEVEL = 0.1;
    public static final double EFFICIENCY_PER_LEVEL = 0.05;
    /** Each level needs this fraction more uses than the previous one (0.05 = +5%). */
    public static final double REQUIREMENT_GROWTH = 0.035;

    private static final ResourceLocation DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(ArbCraft.MODID, "tool_level_damage");
    private static final ResourceLocation EFFICIENCY_ID =
            ResourceLocation.fromNamespaceAndPath(ArbCraft.MODID, "tool_level_efficiency");

    // ---- Helpers ----

    public static boolean isLevelable(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        return stack.getItem() instanceof DiggerItem || stack.getItem() instanceof SwordItem;
    }

    public static int getUses(ItemStack stack) {
        return stack.getOrDefault(ToolLevelComponents.TOOL_USES.get(), 0);
    }

    /** Uses needed to go from `level` to `level + 1`: 250, 263, 276, ... (+5% each level). */
    public static long usesToNextLevel(int level) {
        return Math.max(1L, Math.round(USES_PER_LEVEL * Math.pow(1.0 + REQUIREMENT_GROWTH, level)));
    }

    /** Level, progress into the current level, and uses required for the next level. */
    public record LevelInfo(int level, long progress, long required) {}

    public static LevelInfo getLevelInfo(int totalUses) {
        long remaining = totalUses;
        int level = 0;
        long required = usesToNextLevel(level);
        while (remaining >= required) {
            remaining -= required;
            level++;
            required = usesToNextLevel(level);
        }
        return new LevelInfo(level, remaining, required);
    }

    public static int getLevel(ItemStack stack) {
        return getLevelInfo(getUses(stack)).level();
    }

    /** Counts one use of the stack and levels it up when the threshold is reached. */
    public static void addUse(ServerPlayer player, ItemStack stack) {
        if (!isLevelable(stack)) return;

        int uses = getUses(stack) + 1;
        stack.set(ToolLevelComponents.TOOL_USES.get(), uses);

        LevelInfo info = getLevelInfo(uses);
        if (info.progress() == 0) { // progress resets to 0 exactly when a new level is reached
            int level = info.level();
            applyLevelBonuses(stack, level);

            player.displayClientMessage(
                    Component.literal("Tool level up! Level " + level).withStyle(ChatFormatting.GOLD), true);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
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
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    // ---- Events ----

    /** Mining: every block broken with the held tool counts as one use. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (player instanceof FakePlayer || player.isCreative()) return;
        if (event.getState().getDestroySpeed(event.getLevel(), event.getPos()) <= 0) return;

        addUse(player, player.getMainHandItem());
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

    /** Right-click actions: stripping logs (axe), tilling (hoe), flattening paths (shovel). */
    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()) return;
        if (event.getFinalState() == null) return;
        if (!(event.getContext().getPlayer() instanceof ServerPlayer player)) return;
        if (player instanceof FakePlayer || player.isCreative()) return;

        addUse(player, event.getContext().getItemInHand());
    }

    /** Shows the level and progress in the item tooltip. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isLevelable(stack)) return;

        LevelInfo info = getLevelInfo(getUses(stack));
        event.getToolTip().add(Component.literal("Level " + info.level())
                .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.literal("Progress: " + info.progress() + "/" + info.required())
                .withStyle(ChatFormatting.GRAY));
    }
}
