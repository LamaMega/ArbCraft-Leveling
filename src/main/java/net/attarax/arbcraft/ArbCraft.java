package net.attarax.arbcraft;

import net.attarax.arbcraft.AttributeModifiers.*;
import net.attarax.arbcraft.MobScaling.MobScalingEventHandler;
import net.attarax.arbcraft.CreativeTab.ModCreativeTabs;
import net.attarax.arbcraft.TickAccelerator.BlockTickAccelerator;
import net.attarax.arbcraft.blocks.ExperienceConverterBlock;
import net.attarax.arbcraft.blocks.ModBlocks;
import net.attarax.arbcraft.commands.ModCommands;
import net.attarax.arbcraft.components.ModDataComponents;
import net.attarax.arbcraft.config.ExperienceConverterConfig;
import net.attarax.arbcraft.gui.ModMenuTypes;
import net.attarax.arbcraft.network.NetworkHandler;
import net.attarax.arbcraft.recipes.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
//import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.attarax.arbcraft.client.*;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.attarax.arbcraft.events.ConfigReloadHandler;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ArbCraft.MODID)
public class ArbCraft {
    public static final String MODID = "arbcraft";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

static boolean effectEnabled = true;

    public ArbCraft(IEventBus modEventBus, ModContainer modContainer) {
        // Register registries FIRST
        net.attarax.arbcraft.registry.ModRegistries.register(modEventBus);

        // Then initialize blocks (which will add entries to the registries)
        net.attarax.arbcraft.blocks.ModBlocks.init();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModRecipes.register(modEventBus);  // <-- ADD THIS LINE
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModTabs.REGISTRY.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(ModCommands.class);
        //AttributeModifiers
        NeoForge.EVENT_BUS.register(AttackSpeedLevelHandler.class);
        NeoForge.EVENT_BUS.register(HealthBonusLevelHandler.class);
        NeoForge.EVENT_BUS.register(DamageBonusLevelHandler.class);
        NeoForge.EVENT_BUS.register(SneakingSpeedLevelHandler.class);
        NeoForge.EVENT_BUS.register(StepHeightLevelHandler.class);
        NeoForge.EVENT_BUS.register(EfficiencyBonusLevelHandler.class);
        NeoForge.EVENT_BUS.register(SwimmingSpeedLevelHandler.class);
        NeoForge.EVENT_BUS.register(BlockTickAccelerator.class);
        //NeoForge.EVENT_BUS.register(MovementSpeedLevelHandler.class);

        ModCreativeTabs.register(modEventBus);

        ExperienceConverterConfig.load();
        modEventBus.addListener(NetworkHandler::registerPayloads);
        modEventBus.addListener(ExperienceConverterBlock::registerCapabilities);
        net.attarax.arbcraft.blocks.ModBlocks.init();
        ModDataComponents.register(modEventBus);
        ConfigReloadHandler.initialize();
        NeoForge.EVENT_BUS.register(new MobScalingEventHandler());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
        event.enqueueWork(() -> {


        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    //@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModBlocks.EXPERIENCE_CONVERTER_MENU.get(), ExperienceConverterScreen::new);
            event.register(ModBlocks.CRUSHER_MENU.get(), CrusherScreen::new);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        int xpLevel = player.experienceLevel;
        if(player.tickCount % 100 == 0) {
                manageEffectsForLevel(player, xpLevel);
        }

        if (player.tickCount % 3000 == 0){ //24000 default == 20 mins
            // Get the current day count from the world
            long dayCount = player.level().getDayTime() / 24000L;
            int dayExpPoints = (int)Math.min(dayCount, 10000);
//            if(player.level().getDayTime() > 0) //If the server Time in Ticks is greater then Max Int Value
//            {
//                player.giveExperiencePoints(dayExpPoints);
//            }else {
//                player.giveExperiencePoints(1000);
//            }
            if (player.experienceLevel >= 200)
            {
                player.giveExperiencePoints(dayExpPoints*2);
            }else {
                player.giveExperiencePoints(dayExpPoints);
            }
        }

        // Food restoration for high-level players
        if (player.tickCount % 600 == 0 && xpLevel >= 100) {
            player.getFoodData().setFoodLevel(Math.min(
                    player.getFoodData().getFoodLevel() + 1,
                    20  // Max Hunger 20
            ));
        }
        if (player.tickCount % 500 == 0 && xpLevel >= 2000) {
            player.getFoodData().setFoodLevel(Math.min(
                    player.getFoodData().getFoodLevel() + 1,
                    20  // Max Hunger 20
            ));
        }
        if (player.tickCount % 400 == 0 && xpLevel >= 10000) {
            player.getFoodData().setFoodLevel(Math.min(
                    player.getFoodData().getFoodLevel() + 1,
                    20  // Max Hunger 20
            ));
        }
        if (player.tickCount % 300 == 0 && xpLevel >= 20000) {
            player.getFoodData().setFoodLevel(Math.min(
                    player.getFoodData().getFoodLevel() + 1,
                    20  // Max Hunger 20
            ));
        }

    }

    private void manageEffectsForLevel(Player player, int level) {
        if(effectEnabled)
        {
            applyBestEffect(player, level, MobEffects.MOVEMENT_SPEED,
                    new int[]{1000, 3000, 5000},
                    new int[]{0, 1, 2}); // SPEED

            applyBestEffect(player, level, MobEffects.LUCK,
                    new int[]{100, 200, 400, 600, 1000, 1500, 2000, 2500, 3500, 5000,10000},
                    new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        }

    }

    private void applyBestEffect(Player player, int playerLevel, Holder <MobEffect> effect, int[] requiredLevels, int[] amplifiers) {
        int bestAmplifier = -1;

        for (int i = 0; i < requiredLevels.length; i++) {
            if (playerLevel >= requiredLevels[i]) {
                bestAmplifier = amplifiers[i];
            }
        }
        if (bestAmplifier >= 0) {
            MobEffectInstance currentEffect = player.getEffect(effect);

            if (bestAmplifier >= 0) {
                if (currentEffect == null ||
                        currentEffect.getAmplifier() < bestAmplifier ||
                        currentEffect.getDuration() < 200) {

                    player.addEffect(new MobEffectInstance(
                            effect,
                            72000,
                            bestAmplifier,
                            false,
                            false
                    ));
                }
            }
        }
    }

    private static boolean status = true;
    private static int normalBlockExp = 1;
    private static int oreBlockExp = 10;
    private static int bonusBlockExp = 3;

    public static void setNormalBlockExp(int value)
    {
        normalBlockExp = value;
    }
    public static int getNormalBlockExp()
    {
        return normalBlockExp;
    }
    public static void setOreBlockExp(int value)
    {
        oreBlockExp = value;
    }
    public static int getOreBlockExp()
    {
        return oreBlockExp;
    }
    public static void setBonusBlockExp(int value)
    {
        bonusBlockExp = value;
    }
    public static int getBonusBlockExp()
    {
        return bonusBlockExp;
    }
    public static void setBlockExpStatus(boolean commandStatus)
    {
        status = commandStatus;
    }
    public static boolean getBlockExpStatus()
    {
        return status;
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player != null && !player.level().isClientSide) {

            int level = player.experienceLevel;

            int normalExp = 0;
            int oreExp = 0;
            int bonusExp = 0;
            // Scale based on level
//            if (level >= 10000) {
//                normalExp = 30;
//                oreExp = 160;
//                bonusExp = 40;
//            }
//            else if (level >= 5000) {
//                normalExp = 15;
//                oreExp = 90;
//                bonusExp = 20;
//            }
//            else if (level >= 3000) {
//                normalExp = 12;
//                oreExp = 60;
//                bonusExp = 15;
//            }
//            else if (level >= 2000) {
//                normalExp = 10;
//                oreExp = 50;
//                bonusExp = 12;
//            }
//            else if (level >= 1500) {
//                normalExp = 9;
//                oreExp = 35;
//                bonusExp = 11;
//            }
//            else if (level >= 1000) {
//                normalExp = 8;
//                oreExp = 30;
//                bonusExp = 10;
//            }
//            else if (level >= 750) {
//                normalExp = 7;
//                oreExp = 20;
//                bonusExp = 9;
//            }
//            else if (level >= 500) {
//                normalExp = 6;
//                oreExp = 20;
//                bonusExp = 8;
//            }
//            else if (level >= 350) {
//                normalExp = 5;
//                oreExp = 18;
//                bonusExp = 7;
//            }
//            else if (level >= 250) {
//                normalExp = 4;
//                bonusExp = 6;
//            }
//            else if (level >= 200) {
//                normalExp = 3;
//                oreExp = 15;
//                bonusExp = 5;
//            }
//            else if (level >= 150) {
//                normalExp = 2;
//                oreExp = 12;
//                bonusExp = 4;
//            }
//            else if (level >= 100) {
//                normalExp = 2;
//                oreExp = 10;
//                bonusExp = 4;
//            } else if (level >= 50) {
//                normalExp = 1;
//                bonusExp = 2;
//            }
            /// v.1.3.6.9
            if(level >= 100)
            {
                normalExp += 1 + (player.experienceLevel / 100);
            }

            else if(player.experienceLevel >= 50)
            {

                oreExp = player.experienceLevel / 20;
                bonusExp = player.experienceLevel / 40;
            }


            if (getBlockExpStatus() == false)
            {
                return;
            }

            int expPoints = ArbCraft.getNormalBlockExp() + normalExp;
            if(ArbCraft.getNormalBlockExp() == 0)
            {
                expPoints -= normalExp;
            }

            if (isOreBlock(event.getState().getBlock())) { //Checks if the block is a Ore Block and increases the Exp Given to player by the bonusExp Amount
                expPoints += ArbCraft.getOreBlockExp();
                expPoints += oreExp;
            }
            if(ArbCraft.getOreBlockExp() == 0)
            {
                expPoints -= oreExp;
            }

            if (isBonusBlock(event.getState().getBlock())){ //Checks if the block is a Bonus Block and increases the Exp Given to player by the bonusExp Amount
                expPoints += ArbCraft.getBonusBlockExp();
                expPoints += bonusExp;
            }
            if(ArbCraft.getBonusBlockExp() == 0)
            {
                expPoints -= bonusExp;
            }
            /// v.1.3.6.9
            //Multiplies the Experience Reward per block by Players Current Level divided by 100, example: 100 / 100 = ExpR * 1, 200 / 100 = ExpR * 2 etc.
//            if (player.experienceLevel > 500){
//                expPoints += expPoints * (player.experienceLevel / 500);
//            }
            if(player.experienceLevel > 100){
                expPoints += (player.experienceLevel / 100);
            }

            player.giveExperiencePoints(expPoints);

            blockBreakers.put(event.getPos(), player);

            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
                blockBreakers.remove(event.getPos());
            });
        }
    }

    private boolean isOreBlock(Block block) {
        String blockName = block.getDescriptionId().toLowerCase();
        return blockName.contains("ore") || blockName.contains("ancient_debris") || blockName.contains("lucky");
    }
    private boolean isBonusBlock(Block block){
        String blockName = block.getDescriptionId().toLowerCase();
        return blockName.contains("amethyst_block") || blockName.contains("prismarine")
                || blockName.contains("obsidian") ||  blockName.contains("gilded_blackstone")
                || blockName.contains("glowstone") || blockName.contains("magma_block");
    }

    //AUTO PICK UP
    // Add this field to store the block breaker
    private static final Map<BlockPos, Player> blockBreakers = new HashMap<>();

    // Modified item drop handler
    @SubscribeEvent
    public void onItemDrop(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        // Find the player who broke the block at this location
        BlockPos dropPos = itemEntity.blockPosition();
        Player blockBreaker = findBlockBreaker(dropPos);

        if (blockBreaker == null) {
            return;
        }
        // Check if player has reached level 250
        if (blockBreaker.experienceLevel < 100) {
            return;
        }
        // Check if the block breaker is within reasonable distance (prevent teleporting items across the world)
        if (blockBreaker.distanceToSqr(itemEntity) > 155.0) { //25.0 = 5 block radius
            return;
        }
        // Try to add the item to the block breaker's inventory
        ItemStack itemStack = itemEntity.getItem();
        if (canAddToInventory(blockBreaker, itemStack)) {
            // Add to inventory
            blockBreaker.getInventory().add(itemStack);

            // Remove the item entity from the world
            itemEntity.discard();

            // Play pickup sound
            blockBreaker.level().playSound(null, blockBreaker.getX(), blockBreaker.getY(), blockBreaker.getZ(),
                    net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS,
                    0.2F, ((blockBreaker.getRandom().nextFloat() - blockBreaker.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
    // Helper method to find who broke the block
    private Player findBlockBreaker(BlockPos dropPos) {
        // Check the exact position and nearby positions (items can drop slightly offset)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = dropPos.offset(x, y, z);
                    Player breaker = blockBreakers.get(checkPos);
                    if (breaker != null) {
                        return breaker;
                    }
                }
            }
        }
        return null;
    }
    private boolean canAddToInventory(Player player, ItemStack itemStack) {
        // Check if the player's inventory has space for this item
        return player.getInventory().getFreeSlot() != -1 ||
                player.getInventory().getSlotWithRemainingSpace(itemStack) != -1;
    }

    // BLOCK REACH
    @EventBusSubscriber(modid = "arbcraft")
    public static class ExtendedBlockReachHandler {

        private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("arbcraft", "level_based_reach");
        private static final String REACH_MODIFIER_NAME = "Level Based Block Reach";

        // Configuration constants
        private static final int LEVELS_PER_REACH = 1000;  // Levels needed per +1 reach
        private static final int MAX_BONUS_REACH = 10;     // Maximum bonus reach blocks
        private static final int MAX_EFFECTIVE_LEVEL = MAX_BONUS_REACH * LEVELS_PER_REACH; // 10,000 levels

        public static double calculateBonusReach(int level) {
            // Cap the level at the maximum effective level
            int effectiveLevel = Math.min(level, MAX_EFFECTIVE_LEVEL);
            return Math.floor(effectiveLevel / (double) LEVELS_PER_REACH);
        }

        /**
         * Updates the player's block reach attribute based on their current level
         * @param player The player to update
         */
        public static void updatePlayerReach(Player player) {
            if (player == null || player.level().isClientSide) {
                return;
            }

            int playerLevel = player.experienceLevel;
            double bonusReach = calculateBonusReach(playerLevel);

            // Get the reach attribute
            var reachAttribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            if (reachAttribute == null) {
                return;
            }

            // Always remove existing modifier first to prevent duplicates
            boolean hadModifier = reachAttribute.getModifier(REACH_MODIFIER_ID) != null;
            if (hadModifier) {
                reachAttribute.removeModifier(REACH_MODIFIER_ID);
            }

            // Add new modifier if bonus reach > 0
            if (bonusReach > 0) {
                AttributeModifier modifier = new AttributeModifier(
                        REACH_MODIFIER_ID,
                        bonusReach, // value
                        AttributeModifier.Operation.ADD_VALUE
                );
                reachAttribute.addTransientModifier(modifier); // Use transient instead of permanent
            }
        }

        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            updatePlayerReach(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerLevelChange(PlayerEvent.PlayerChangedDimensionEvent event) {
            updatePlayerReach(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();

            // Only check every 20 ticks (1 second) and only on server side
            if (player.level().isClientSide || player.tickCount % 60 != 0) {
                return;
            }

            // Store the last known level to avoid unnecessary updates
            int currentLevel = player.experienceLevel;
            int lastStoredLevel = player.getPersistentData().getInt("LastReachLevel");

            if (currentLevel != lastStoredLevel) {
                // Level changed, update reach
                updatePlayerReach(player);
                player.getPersistentData().putInt("LastReachLevel", currentLevel);
            }
        }


        public static int getMaxBonusReach() {
            return MAX_BONUS_REACH;
        }

        public static int getLevelForMaxReach() {
            return MAX_EFFECTIVE_LEVEL;
        }

        public static boolean hasMaxBonusReach(Player player) {
            return player.experienceLevel >= MAX_EFFECTIVE_LEVEL;
        }

        public static double getTotalBlockReach(Player player) {
            var attribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            return attribute != null ? attribute.getValue() : 4.5; // Default reach is 4.5 blocks
        }
    }
    public static void setHealthModifier(double value)
    {
        HealthBonusLevelHandler.healthModifier = value;
    }
    public static double getHealthModifier()
    {
        return HealthBonusLevelHandler.healthModifier;
    }

    public static void setDamageModifier(double value)
    {
        DamageBonusLevelHandler.damageModifier = value;
    }
    public static double getDamageModifier()
    {
        return DamageBonusLevelHandler.damageModifier;
    }

    public static void setEfficiencyModifier(double value)
    {
        EfficiencyBonusLevelHandler.efficiencyModifier = value;
    }
    public static double getEfficiencyModifier()
    {
        return EfficiencyBonusLevelHandler.efficiencyModifier;
    }

    public static void setAttackSpeedModifier(double value)
    {
        AttackSpeedLevelHandler.aSpeedModifier = value;
    }
    public static double getAttackSpeedModifier()
    {
        return AttackSpeedLevelHandler.aSpeedModifier;
    }

    public static void setBonusTickMultiplier(int value)
    {
        BlockTickAccelerator.bonusTickMultiplier = value;
    }

    public static int getBonusTickMultiplier()
    {
        return BlockTickAccelerator.bonusTickMultiplier;
    }

    public static void setIncreasedBonusTickMultiplier(int value)
    {
        BlockTickAccelerator.increasedBonusTickMultiplier = value;
    }

    public static int getIncreasedBonusTickMultiplier()
    {
        return BlockTickAccelerator.increasedBonusTickMultiplier;
    }

    public static void setEffectsEnabled(boolean state)
    {
        effectEnabled = state;
    }

    public static boolean getEffectsEnabled()
    {
        return effectEnabled;
    }

    public static void setMaxHpMultiplier(double n)
    {
        MobScalingEventHandler.MAX_HP_MULTIPLIER = n;
    }

    public static double getMaxHpMultiplier()
    {
        return MobScalingEventHandler.MAX_HP_MULTIPLIER;
    }

}


