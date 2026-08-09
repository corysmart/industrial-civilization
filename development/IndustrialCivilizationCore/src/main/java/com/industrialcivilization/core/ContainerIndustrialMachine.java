package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class ContainerIndustrialMachine extends Container {
    private final TileIndustrialMachine tile;
    private final int[] lastFields = {-1, -1, -1, -1};

    public ContainerIndustrialMachine(InventoryPlayer playerInventory, TileIndustrialMachine tile) {
        this.tile = tile;
        addSlotToContainer(new Slot(tile, 0, 56, 39));
        addSlotToContainer(new Slot(tile, 1, 82, 39));
        addSlotToContainer(new Slot(tile, 2, 108, 39));
        addSlotToContainer(new SlotOutput(tile, 3, 156, 39));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                    24 + col * 18, 100 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 24 + col * 18, 167));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            for (int field = 0; field < tile.getFieldCount(); field++) {
                int value = tile.getField(field);
                if (value != lastFields[field]) listener.sendWindowProperty(this, field, value);
            }
        }
        for (int field = 0; field < tile.getFieldCount(); field++) lastFields[field] = tile.getField(field);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return result;
        ItemStack stack = slot.getStack();
        result = stack.copy();
        if (index < 4) {
            if (!mergeItemStack(stack, 4, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(stack, 0, 3, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        return result;
    }

    private static final class SlotOutput extends Slot {
        SlotOutput(TileIndustrialMachine inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
