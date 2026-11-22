package net.attarax.arbcraft.menu;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * A slot that uses the handler's slot limit instead of the item's own max stack size
 * (64) or vanilla's container default (99), so players can place large stacks in it.
 */
public class LargeStackSlot extends SlotItemHandler {

    private final IItemHandler handler;
    private final int slotIndex;

    public LargeStackSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.handler = handler;
        this.slotIndex = index;
    }

    /** Slot-wide limit: read straight from the handler so no vanilla default (99) can sneak in. */
    @Override
    public int getMaxStackSize() {
        return handler.getSlotLimit(slotIndex);
    }

    /** Per-item limit: unstackable items (tools, armor) stay at 1. */
    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.getMaxStackSize() <= 1 ? 1 : handler.getSlotLimit(slotIndex);
    }
}