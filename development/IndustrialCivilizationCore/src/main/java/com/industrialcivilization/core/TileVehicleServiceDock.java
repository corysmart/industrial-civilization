package com.industrialcivilization.core;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import com.mrcrayfish.vehicle.entity.vehicle.EntityMiniBus;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/** Delegates item/fluid capabilities to a parked Industrial Service Carrier. */
public final class TileVehicleServiceDock extends TileEntity {
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, pos);
        return vehicle != null && vehicle.hasCapability(capability, facing)
            || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, pos);
        if (vehicle instanceof EntityMiniBus && vehicle.hasCapability(capability, facing)) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                IItemHandler target = vehicle.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
                return (T) new TrackedItems((EntityMiniBus) vehicle, target);
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                IFluidHandler target = vehicle.getCapability(
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                return (T) new TrackedFluids((EntityMiniBus) vehicle, target);
            }
            return vehicle.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    private static final class TrackedItems implements IItemHandler {
        private final EntityMiniBus vehicle;
        private final IItemHandler target;

        TrackedItems(EntityMiniBus vehicle, IItemHandler target) {
            this.vehicle = vehicle;
            this.target = target;
        }

        @Override public int getSlots() { return target.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return target.getStackInSlot(slot); }
        @Override public int getSlotLimit(int slot) { return target.getSlotLimit(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack remainder = target.insertItem(slot, stack, simulate);
            if (!simulate && remainder.getCount() < stack.getCount())
                VehicleIntegrationSystem.recordDockTransfer(vehicle, false);
            return remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = target.extractItem(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty())
                VehicleIntegrationSystem.recordDockTransfer(vehicle, false);
            return extracted;
        }
    }

    private static final class TrackedFluids implements IFluidHandler {
        private final EntityMiniBus vehicle;
        private final IFluidHandler target;

        TrackedFluids(EntityMiniBus vehicle, IFluidHandler target) {
            this.vehicle = vehicle;
            this.target = target;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            return target.getTankProperties();
        }
        @Override public int fill(FluidStack resource, boolean doFill) {
            int filled = target.fill(resource, doFill);
            if (doFill && filled > 0) VehicleIntegrationSystem.recordDockTransfer(vehicle, true);
            return filled;
        }
        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack drained = target.drain(resource, doDrain);
            if (doDrain && drained != null && drained.amount > 0)
                VehicleIntegrationSystem.recordDockTransfer(vehicle, true);
            return drained;
        }
        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drained = target.drain(maxDrain, doDrain);
            if (doDrain && drained != null && drained.amount > 0)
                VehicleIntegrationSystem.recordDockTransfer(vehicle, true);
            return drained;
        }
    }
}
