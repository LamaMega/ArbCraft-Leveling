package net.attarax.arbcraft.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.attarax.arbcraft.blocks.ModBlocks;

public class CrusherMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    // Factory method for IMenuTypeExtension (server-side from block entity)
    public static CrusherMenu create(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        return new CrusherMenu(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(2));
    }

    // Client-side constructor
    public CrusherMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(2));
    }

    // Server-side constructor
    public CrusherMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModBlocks.CRUSHER_MENU.get(), containerId);

        checkContainerSize(container, 2);
        checkContainerDataCount(data, 2);

        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);

        // Crusher slots
        this.addSlot(new Slot(container, INPUT_SLOT, 56, 35));  // Input slot
        this.addSlot(new OutputSlot(container, OUTPUT_SLOT, 116, 35));  // Output slot

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                // Moving from output slot
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index != INPUT_SLOT) {
                // Moving from player inventory to input
                if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    if (index < 29) {
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index < 38 && !this.moveItemStackTo(slotStack, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 2, 38, false)) {
                // Moving from input slot
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    // Custom slot that only allows output (no input)
    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}