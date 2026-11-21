
package net.attarax.arbcraft.TickAccelerator;

import net.attarax.arbcraft.ArbCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Accelerates block ticks AND block entities (furnaces, brewing stands, modded machines, etc.)
 * within a radius around high-level players.
 * - Level 1000: 15% faster ticks
 * - Level 2000: 30% faster ticks
 * - And so on...
 */
@EventBusSubscriber(modid = ArbCraft.MODID)
public class BlockTickAccelerator {

    // Configuration
    private static final int MIN_LEVEL_REQUIRED = 100;
    private static final int LEVELS_PER_10_PERCENT = 100; // Every 1000 levels = +15% tick speed
    private static int BLOCK_RADIUS = 4; // 5 blocks in each direction (11x11x11 cube centered on player)
    private static final int CHECK_INTERVAL = 1; // Check every tick
    private static final int MAX_BONUS_PERCENT = 200000; // Cap at 200% (3x speed) at level 20,000

    public static int bonusTickMultiplier = 5;
    public static int increasedBonusTickMultiplier = 10;

    private static final Random RANDOM = new Random();

    // Cache for block entity tick methods (performance optimization)
    private static final Map<Class<?>, Method> TICK_METHOD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Boolean> TICKABLE_CACHE = new HashMap<>();
    /**
     * Calculates the tick speed bonus percentage based on player level
     * @param level Player's experience level
     * @return Bonus percentage (e.g., 10 for 10%, 20 for 20%)
     */
    public static int calculateTickSpeedBonus(int level) {
        if (level < MIN_LEVEL_REQUIRED) {
            return 0;
        }
        int bonus;
        // Calculate bonus: level 1000 = 15%, 2000 = 30%, etc.
        if(level >= 100000)
        {
             bonus = (level / LEVELS_PER_10_PERCENT * 15);
        }
        else if(level >= 50000)
        {
            bonus = (level / LEVELS_PER_10_PERCENT * increasedBonusTickMultiplier);
        }
        else {
             bonus = (level / LEVELS_PER_10_PERCENT) * bonusTickMultiplier;
        }


        // Cap at maximum
        return Math.min(bonus, MAX_BONUS_PERCENT);
    }

    /**
     * Main event handler - accelerates block ticks and block entities around the player
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side
        if (player.level().isClientSide) {
            return;
        }

        // Only check periodically to reduce performance impact
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        // Check if player meets minimum level requirement
        int playerLevel = player.experienceLevel;
        if (playerLevel < MIN_LEVEL_REQUIRED) {
            return;
        }

        if (playerLevel >= 50000)
        {
            BLOCK_RADIUS = 9;
        }
        // Calculate how much faster blocks should tick
        int bonusPercent = calculateTickSpeedBonus(playerLevel);
        if (bonusPercent <= 0) {
            return;
        }

        // Accelerate both regular blocks and block entities
        accelerateNearbyBlocks(player, bonusPercent);
        accelerateNearbyBlockEntities(player, bonusPercent);
    }

    /**
     * Accelerates random block ticks for blocks near the player
     * @param player The player
     * @param bonusPercent The speed bonus percentage (10 = 10% faster)
     */
    private static void accelerateNearbyBlocks(Player player, int bonusPercent) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition();

        // Get all blocks in radius
        List<BlockPos> tickableBlocks = getTickableBlocksInRadius(level, playerPos);

        if (tickableBlocks.isEmpty()) {
            return;
        }

        // Calculate how many extra ticks to apply
        int extraTicksToApply = calculateExtraTicks(bonusPercent);

        // Apply extra random ticks to random blocks
        for (int i = 0; i < extraTicksToApply; i++) {
            if (tickableBlocks.isEmpty()) break;

            // Pick a random block
            BlockPos randomPos = tickableBlocks.get(RANDOM.nextInt(tickableBlocks.size()));
            BlockState state = level.getBlockState(randomPos);

            // Trigger a random tick on this block
            if (state.isRandomlyTicking()) {
                state.randomTick(level, randomPos, level.random);
            }
        }
    }

    /**
     * Accelerates block entities (furnaces, brewing stands, modded machines, etc.) near the player
     * Uses reflection to support any mod's block entities
     * @param player The player
     * @param bonusPercent The speed bonus percentage (10 = 10% faster)
     */
    private static void accelerateNearbyBlockEntities(Player player, int bonusPercent) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition();

        // Get all block entities in radius
        List<BlockEntity> blockEntities = getAllBlockEntitiesInRadius(level, playerPos);

        if (blockEntities.isEmpty()) {
            return;
        }

        // Calculate how many extra ticks to apply per block entity
        int extraTicksPerEntity = calculateExtraBlockEntityTicks(bonusPercent);

        // Apply extra ticks to each block entity
        for (BlockEntity blockEntity : blockEntities) {
            accelerateBlockEntity(blockEntity, extraTicksPerEntity, level);
        }
    }

    /**
     * Accelerates any block entity using reflection to find and call its tick method
     * This works with both vanilla and modded block entities
     * @param blockEntity The block entity to accelerate
     * @param extraTicks Number of extra ticks to apply
     * @param level The server level
     */
    private static void accelerateBlockEntity(BlockEntity blockEntity, int extraTicks, ServerLevel level) {
        if (blockEntity == null) return;

        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Class<?> blockEntityClass = blockEntity.getClass();

        // Check cache first
        Boolean isTickable = TICKABLE_CACHE.get(blockEntityClass);
        if (isTickable != null && !isTickable) {
            return; // Known non-tickable block entity
        }

        try {
            // Try to get the ticker from the block using the block entity type
            if (state.getBlock() instanceof net.minecraft.world.level.block.EntityBlock entityBlock) {
                BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>)
                        entityBlock.getTicker(level, state, blockEntity.getType());

                if (ticker != null) {
                    // Use the ticker to apply extra ticks
                    for (int i = 0; i < extraTicks; i++) {
                        if (level.getBlockEntity(pos) == blockEntity) {
                            ticker.tick(level, pos, state, blockEntity);
                        }
                    }
                    TICKABLE_CACHE.put(blockEntityClass, true);
                    return;
                }
            }
        } catch (Exception e) {
            // Ticker approach failed, try reflection
        }

        // Fallback: Try to find a tick method via reflection
        Method tickMethod = TICK_METHOD_CACHE.get(blockEntityClass);

        if (tickMethod == null && !TICKABLE_CACHE.containsKey(blockEntityClass)) {
            // Search for common tick method signatures
            tickMethod = findTickMethod(blockEntityClass);

            if (tickMethod != null) {
                TICK_METHOD_CACHE.put(blockEntityClass, tickMethod);
                TICKABLE_CACHE.put(blockEntityClass, true);
            } else {
                TICKABLE_CACHE.put(blockEntityClass, false);
                return;
            }
        }

        // Apply extra ticks using the found method
        if (tickMethod != null) {
            try {
                tickMethod.setAccessible(true);
                for (int i = 0; i < extraTicks; i++) {
                    if (level.getBlockEntity(pos) == blockEntity) {
                        // Try different method signatures
                        int paramCount = tickMethod.getParameterCount();
                        if (paramCount == 0) {
                            tickMethod.invoke(blockEntity);
                        } else if (paramCount == 4) {
                            tickMethod.invoke(null, level, pos, state, blockEntity);
                        } else if (paramCount == 3) {
                            tickMethod.invoke(blockEntity, level, pos, state);
                        }
                    }
                }
            } catch (Exception e) {
                // Failed to invoke tick method, remove from cache
                TICK_METHOD_CACHE.remove(blockEntityClass);
                TICKABLE_CACHE.put(blockEntityClass, false);
            }
        }
    }

    /**
     * Searches for a tick method in a block entity class using reflection
     * @param blockEntityClass The class to search
     * @return The tick method if found, null otherwise
     */
    private static Method findTickMethod(Class<?> blockEntityClass) {
        // Common tick method names used by various mods
        String[] methodNames = {
                "tick", "serverTick", "tickServer", "update",
                "updateEntity", "onUpdate", "doTick", "tickBlock"
        };

        // Try to find any of these methods
        for (String methodName : methodNames) {
            try {
                // Try no-parameter method (instance method)
                Method method = blockEntityClass.getMethod(methodName);
                return method;
            } catch (NoSuchMethodException e) {
                // Try with common parameters
                try {
                    Method method = blockEntityClass.getMethod(methodName,
                            ServerLevel.class, BlockPos.class, BlockState.class);
                    return method;
                } catch (NoSuchMethodException e2) {
                    // Try static method signature
                    try {
                        Method method = blockEntityClass.getMethod(methodName,
                                ServerLevel.class, BlockPos.class, BlockState.class, blockEntityClass);
                        return method;
                    } catch (NoSuchMethodException e3) {
                        // Continue to next method name
                    }
                }
            }
        }

        // Also check parent classes and interfaces
        Class<?> superClass = blockEntityClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return findTickMethod(superClass);
        }

        return null;
    }

    /**
     * Calculates how many extra ticks to apply based on bonus percentage
     * @param bonusPercent The speed bonus (10 = 10%)
     * @return Number of extra ticks to apply
     */
    private static int calculateExtraTicks(int bonusPercent) {
        // Base number of blocks that would normally tick in our radius
        int volumeSize = (BLOCK_RADIUS * 2 + 1) * (BLOCK_RADIUS * 2 + 1) * (BLOCK_RADIUS * 2 + 1);

        // Random tick speed default is 3 per chunk section per tick
        double baseTicksPerInterval = (volumeSize / 4096.0) * 3.0 * CHECK_INTERVAL;

        // Apply bonus percentage
        double extraTicks = baseTicksPerInterval * (bonusPercent / 100.0);

        return Math.max(1, (int) Math.round(extraTicks));
    }

    /**
     * Calculates how many extra ticks to apply to each block entity
     * @param bonusPercent The speed bonus (10 = 10%)
     * @return Number of extra ticks per block entity
     */
    private static int calculateExtraBlockEntityTicks(int bonusPercent) {
        // Block entities tick every game tick normally
        // We check every CHECK_INTERVAL ticks
        // So we need to apply (CHECK_INTERVAL * bonusPercent / 100) extra ticks
        double extraTicks = CHECK_INTERVAL * (bonusPercent / 100.0);

        return Math.max(0, (int) Math.round(extraTicks));
    }

    /**
     * Gets all tickable blocks within radius of a position
     * @param level The server level
     * @param center Center position
     * @return List of positions with tickable blocks
     */
    private static List<BlockPos> getTickableBlocksInRadius(ServerLevel level, BlockPos center) {
        List<BlockPos> tickableBlocks = new ArrayList<>();

        // Iterate through all blocks in radius
        for (int x = -BLOCK_RADIUS; x <= BLOCK_RADIUS; x++) {
            for (int y = -BLOCK_RADIUS; y <= BLOCK_RADIUS; y++) {
                for (int z = -BLOCK_RADIUS; z <= BLOCK_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    // Only include blocks that can receive random ticks
                    if (state.isRandomlyTicking()) {
                        tickableBlocks.add(pos);
                    }
                }
            }
        }

        return tickableBlocks;
    }

    /**
     * Gets ALL block entities within radius of a position (not just vanilla ones)
     * @param level The server level
     * @param center Center position
     * @return List of block entities
     */
    private static List<BlockEntity> getAllBlockEntitiesInRadius(ServerLevel level, BlockPos center) {
        List<BlockEntity> blockEntities = new ArrayList<>();

        // Iterate through all positions in radius
        for (int x = -BLOCK_RADIUS; x <= BLOCK_RADIUS; x++) {
            for (int y = -BLOCK_RADIUS; y <= BLOCK_RADIUS; y++) {
                for (int z = -BLOCK_RADIUS; z <= BLOCK_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    // Include ALL block entities (vanilla and modded)
                    if (blockEntity != null) {
                        blockEntities.add(blockEntity);
                    }
                }
            }
        }

        return blockEntities;
    }

    /**
     * Gets the current tick speed multiplier for a player
     * @param player The player
     * @return Speed multiplier (e.g., 1.1 for 10% faster, 1.5 for 50% faster)
     */
    public static double getTickSpeedMultiplier(Player player) {
        int bonus = calculateTickSpeedBonus(player.experienceLevel);
        return 1.0 + (bonus / 100.0);
    }

    /**
     * Gets the level required for a specific bonus percentage
     * @param bonusPercent Desired bonus (e.g., 50 for 50%)
     * @return Required level
     */
    public static int getLevelForBonus(int bonusPercent) {
        return (bonusPercent / 15) * LEVELS_PER_10_PERCENT;
    }

    /**
     * Checks if a player has max tick speed bonus
     * @param player The player
     * @return True if at max bonus
     */
    public static boolean hasMaxTickSpeed(Player player) {
        return calculateTickSpeedBonus(player.experienceLevel) >= MAX_BONUS_PERCENT;
    }

    // Getters for configuration values (useful for commands/display)
    public static int getMinLevelRequired() {
        return MIN_LEVEL_REQUIRED;
    }

    public static int getBlockRadius() {
        return BLOCK_RADIUS;
    }

    public static int getMaxBonusPercent() {
        return MAX_BONUS_PERCENT;
    }

    public static int getMaxBonusLevel() {
        return getLevelForBonus(MAX_BONUS_PERCENT);
    }

    /**
     * Clears the reflection caches (useful if mods are reloaded)
     */
    public static void clearCaches() {
        TICK_METHOD_CACHE.clear();
        TICKABLE_CACHE.clear();
    }
}