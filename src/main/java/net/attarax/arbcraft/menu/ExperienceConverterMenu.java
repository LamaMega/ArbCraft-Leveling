package net.attarax.arbcraft.menu;

import net.attarax.arbcraft.blocks.ModBlocks;
import net.attarax.arbcraft.blockentity.ExperienceConverterBlockEntity;
import net.attarax.arbcraft.config.ExperienceConverterConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExperienceConverterMenu extends AbstractContainerMenu {

    private final ExperienceConverterBlockEntity blockEntity;
    private final ContainerData data;

    // ── Server constructor ────────────────────────────────────────────────────

    public ExperienceConverterMenu(int containerId, Inventory playerInventory,
                                   ExperienceConverterBlockEntity blockEntity, ContainerData data) {
        super(ModBlocks.EXPERIENCE_CONVERTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // Block entity input slot (can hold more than 64, see ExperienceConverterBlockEntity.INPUT_SLOT_LIMIT)
        this.addSlot(new LargeStackSlot(blockEntity.getInventory(), 0, 80, 35));

        // Player inventory (moved down 14 px from vanilla to give room for upgrade UI)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 98 + i * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 156));
        }

        // 5 data slots:
        //  0 storedExperience  |  1 conversionProgress  |  2 current conversion time
        //  3 upgradeLevel      |  4 next upgrade cost
        addDataSlots(data);
    }

    // ── Client constructor (called from packet) ────────────────────────────────

    public static ExperienceConverterMenu create(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        ExperienceConverterBlockEntity blockEntity =
                (ExperienceConverterBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos());
        // 5 data slots to match server ContainerData
        return new ExperienceConverterMenu(containerId, playerInventory, blockEntity, new SimpleContainerData(5));
    }

    // ── Upgrade button (button id 0) ──────────────────────────────────────────
    //
    //  Called on the SERVER when the client sends ServerboundContainerButtonClickPacket.
    //  The screen triggers this via:
    //      Minecraft.getInstance().gameMode.handleInventoryButtonClick(containerId, 0);

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && !player.level().isClientSide()) {
            if (!blockEntity.canUpgrade()) return false;

            int cost         = blockEntity.getUpgradeCost();
            Item material    = ExperienceConverterConfig.getUpgradeMaterial();

            // ── Count how many of the upgrade material the player is carrying ──
            int found = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(material)) found += stack.getCount();
            }

            if (found < cost) return false; // not enough material – silently reject

            // ── Consume material from inventory ───────────────────────────────
            int remaining = cost;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(material) && remaining > 0) {
                    int take  = Math.min(stack.getCount(), remaining);
                    stack.shrink(take);
                    remaining -= take;
                }
            }

            blockEntity.upgrade();
            return true;
        }
        return false;
    }

    // ── Data getters (read from synced ContainerData on client) ───────────────

    public int getStoredExperience()   { return data.get(0); }
    public int getConversionProgress() { return data.get(1); }
    public int getMaxProgress()        { return data.get(2); }
    public int getUpgradeLevel()       { return data.get(3); }
    public int getUpgradeCost()        { return data.get(4); }

    public ExperienceConverterBlockEntity getBlockEntity() { return blockEntity; }

    // ── Quick-move (shift-click) ──────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                // Block entity slot → player inventory
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory → block entity slot.
                // Insert through the handler directly: moveItemStackTo caps at the item's max stack size (64).
                ItemStack remainder = blockEntity.getInventory().insertItem(0, stackInSlot.copy(), false);
                if (remainder.getCount() == stackInSlot.getCount()) {
                    return ItemStack.EMPTY; // nothing fit (invalid item or slot full)
                }
                stackInSlot.setCount(remainder.getCount());
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) == blockEntity &&
                player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5,
                        blockEntity.getBlockPos().getY() + 0.5,
                        blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}