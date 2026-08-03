package com.industrialcivilization.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

/** IInventory view used by vanilla's stable six-row chest container. */
public final class InventoryVehicleHandler implements IInventory {
    private final Entity vehicle;
    private final IItemHandlerModifiable handler;
    public InventoryVehicleHandler(Entity vehicle, IItemHandlerModifiable handler) {
        this.vehicle = vehicle; this.handler = handler;
    }
    @Override public int getSizeInventory() { return handler.getSlots(); }
    @Override public boolean isEmpty() { for (int i=0;i<handler.getSlots();i++) if(!handler.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override public ItemStack getStackInSlot(int i) { return handler.getStackInSlot(i); }
    @Override public ItemStack decrStackSize(int i, int count) { return handler.extractItem(i,count,false); }
    @Override public ItemStack removeStackFromSlot(int i) { return handler.extractItem(i,64,false); }
    @Override public void setInventorySlotContents(int i, ItemStack stack) { handler.setStackInSlot(i,stack); }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public void markDirty() {}
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return vehicle.isEntityAlive() && player.getDistanceSq(vehicle)<64; }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int i, ItemStack stack) { return true; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { for(int i=0;i<handler.getSlots();i++) handler.setStackInSlot(i,ItemStack.EMPTY); }
    @Override public String getName() { return "container.industrialcivilization.service_vehicle"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentTranslation(getName()); }
}
