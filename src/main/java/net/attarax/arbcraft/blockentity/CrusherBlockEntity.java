package net.attarax.arbcraft.blockentity;

import net.attarax.arbcraft.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.attarax.arbcraft.recipes.CrusherRecipe;
import net.attarax.arbcraft.menu.CrusherMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrusherBlockEntity extends BlockEntity implements Container, RecipeInput, MenuProvider {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int INVENTORY_SIZE = 2;

    private static final int MAX_PROGRESS = 200; // Ticks to complete crushing

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int progress = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrusherBlockEntity.this.progress;
                case 1 -> CrusherBlockEntity.this.MAX_PROGRESS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CrusherBlockEntity.this.progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRUSHER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.arbcraft.crusher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrusherMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("CrushProgress", this.progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.progress = tag.getInt("CrushProgress");
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        boolean dirty = false;
        CrusherRecipe recipe = getRecipe();

        if (recipe != null && canCraft(recipe)) {
            this.progress++;
            dirty = true;

            if (this.progress >= MAX_PROGRESS) {
                craftItem(recipe);
                this.progress = 0;
                dirty = true;
            }
        } else {
            if (this.progress > 0) {
                this.progress = Math.max(0, this.progress - 2);
                dirty = true;
            }
        }

        if (dirty) {
            setChanged();
        }
    }

    @Nullable
    private CrusherRecipe getRecipe() {
        if (this.level == null) {
            return null;
        }

        // Properly typed recipe lookup using RecipeInput
        Optional<RecipeHolder<CrusherRecipe>> recipeHolder = this.level.getRecipeManager()
                .getRecipeFor(CrusherRecipe.Type.INSTANCE, this, this.level);

        return recipeHolder.map(RecipeHolder::value).orElse(null);
    }

    private boolean canCraft(CrusherRecipe recipe) {
        ItemStack input = this.items.get(INPUT_SLOT);
        ItemStack output = this.items.get(OUTPUT_SLOT);
        ItemStack result = recipe.getResultItem(this.level.registryAccess());

        if (input.isEmpty() || result.isEmpty()) {
            return false;
        }

        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }

        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craftItem(CrusherRecipe recipe) {
        ItemStack input = this.items.get(INPUT_SLOT);
        ItemStack output = this.items.get(OUTPUT_SLOT);
        ItemStack result = recipe.getResultItem(this.level.registryAccess());

        input.shrink(1);

        if (output.isEmpty()) {
            this.items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this);
    }

    // Container implementation
    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // RecipeInput implementation (required for recipe matching in 1.21.1)
    @Override
    public int size() {
        return INVENTORY_SIZE;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }
}