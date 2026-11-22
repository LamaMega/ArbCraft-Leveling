package net.attarax.arbcraft.blockentity;

import net.attarax.arbcraft.blocks.ModBlocks;
import net.attarax.arbcraft.config.ExperienceConverterConfig;
import net.attarax.arbcraft.menu.ExperienceConverterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class ExperienceConverterBlockEntity extends BlockEntity implements MenuProvider {

    /** Max items the input slot can hold (vanilla would be 64). Change to whatever you want. */
    public static final int INPUT_SLOT_LIMIT = 2147483646;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ExperienceConverterConfig.getExperienceValue(stack.getItem()) > 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return INPUT_SLOT_LIMIT;
        }

        // Without this, insertItem (hoppers, pipes, shift-click) still caps at the item's own max stack size.
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return stack.getMaxStackSize() <= 1 ? 1 : getSlotLimit(slot);
        }
    };

    private int storedExperience  = 0;
    private int conversionProgress = 0;
    private int upgradeLevel       = 0;

    // ── ContainerData indices ─────────────────────────────────────────────────
    //  0  storedExperience   (NOTE: synced as short; values > 32 767 will wrap on client display)
    //  1  conversionProgress
    //  2  current conversion time  (dynamic – changes when upgraded)
    //  3  upgradeLevel
    //  4  next upgrade cost        (copper blocks / configured material)
    // ─────────────────────────────────────────────────────────────────────────
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ExperienceConverterBlockEntity.this.storedExperience;
                case 1 -> ExperienceConverterBlockEntity.this.conversionProgress;
                case 2 -> ExperienceConverterBlockEntity.this.getCurrentConversionTime();
                case 3 -> ExperienceConverterBlockEntity.this.upgradeLevel;
                case 4 -> ExperienceConverterBlockEntity.this.getUpgradeCost();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ExperienceConverterBlockEntity.this.storedExperience   = value;
                case 1 -> ExperienceConverterBlockEntity.this.conversionProgress = value;
                case 3 -> ExperienceConverterBlockEntity.this.upgradeLevel       = value;
            }
        }

        @Override
        public int getCount() { return 5; }
    };

    // ─────────────────────────────────────────────────────────────────────────

    public ExperienceConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXPERIENCE_CONVERTER_BLOCK_ENTITY.get(), pos, state);
    }

    // ── Upgrade maths ─────────────────────────────────────────────────────────

    /**
     * Ticks required to convert one item at the current upgrade level.
     * Reduces by {@link ExperienceConverterConfig#getUpgradeTimeReduction()} per level,
     * clamped to {@link ExperienceConverterConfig#getMinConversionTime()}.
     */
    public int getCurrentConversionTime() {
        int reduction = upgradeLevel * ExperienceConverterConfig.getUpgradeTimeReduction();
        return Math.max(
                ExperienceConverterConfig.getMinConversionTime(),
                ExperienceConverterConfig.getBaseConversionTime() - reduction
        );
    }

    /**
     * XP multiplier at the current upgrade level.
     * Level 0 → 1.00, Level 1 → 1.25, Level 2 → 1.50, etc. (with default 25 % per level).
     */
    public float getExpMultiplier() {
        return 1.0f + (upgradeLevel * ExperienceConverterConfig.getUpgradeExpBonusPct() / 100.0f);
    }

    /**
     * How many of the upgrade material are needed for the NEXT upgrade.
     * Formula: baseCost + (currentLevel × costIncrease)
     * e.g. level 0→1 costs 100, level 1→2 costs 150, level 2→3 costs 200 …
     */
    public int getUpgradeCost() {
        return ExperienceConverterConfig.getUpgradeBaseCost()
                + (upgradeLevel * ExperienceConverterConfig.getUpgradeCostIncrease());
    }

    /** Returns false once the configured max level has been reached (0 = no cap). */
    public boolean canUpgrade() {
        int max = ExperienceConverterConfig.getMaxUpgradeLevel();
        return max <= 0 || upgradeLevel < max;
    }

    /**
     * Increments the upgrade level and resets in-progress conversion
     * so the faster timer applies immediately.
     * Call only from the server thread (via {@link ExperienceConverterMenu#clickMenuButton}).
     */
    public void upgrade() {
        if (canUpgrade()) {
            upgradeLevel++;
            conversionProgress = 0; // restart timer with the new (faster) conversion time
            setChanged();
        }
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExperienceConverterBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack stack = be.inventory.getStackInSlot(0);
        if (!stack.isEmpty()) {
            be.conversionProgress++;

            if (be.conversionProgress >= be.getCurrentConversionTime()) {
                int baseXp     = ExperienceConverterConfig.getExperienceValue(stack.getItem());
                int actualXp   = Math.round(baseXp * be.getExpMultiplier());
                if (actualXp > 0) {
                    be.storedExperience += actualXp;
                    stack.shrink(1);
                    be.conversionProgress = 0;
                    be.setChanged();
                }
            }
        } else {
            be.conversionProgress = 0;
        }
    }

    // ── XP transfer helpers ───────────────────────────────────────────────────

    public void addStoredExperience(int amount) {
        storedExperience += amount;
        setChanged();
    }

    public boolean withdrawExperience(Player player, int amount) {
        if (storedExperience >= amount) {
            storedExperience -= amount;
            player.giveExperiencePoints(amount);
            setChanged();
            return true;
        }
        return false;
    }

    public boolean withdrawAllExperience(Player player) {
        if (storedExperience > 0) {
            player.giveExperiencePoints(storedExperience);
            storedExperience = 0;
            setChanged();
            return true;
        }
        return false;
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("StoredExperience",   storedExperience);
        tag.putInt("ConversionProgress", conversionProgress);
        tag.putInt("UpgradeLevel",       upgradeLevel);   // ← persisted
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        storedExperience   = tag.getInt("StoredExperience");
        conversionProgress = tag.getInt("ConversionProgress");
        upgradeLevel       = tag.getInt("UpgradeLevel");        // ← restored
    }

    // ── MenuProvider / capability ─────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.arbcraft.experience_converter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ExperienceConverterMenu(containerId, playerInventory, this, this.data);
    }

    public ItemStackHandler getInventory() { return inventory; }

    public net.neoforged.neoforge.items.IItemHandler getItemHandler(@Nullable Direction side) {
        return inventory;
    }

    // ── Simple getters (used by screen / menu) ────────────────────────────────

    public int getStoredExperience()   { return storedExperience; }
    public int getConversionProgress() { return conversionProgress; }
    public int getMaxProgress()        { return getCurrentConversionTime(); }
    public int getUpgradeLevel()       { return upgradeLevel; }
    public ContainerData getData()     { return data; }
}