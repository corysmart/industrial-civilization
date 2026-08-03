package com.industrialcivilization.core;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/** Delegates item/fluid capabilities to a parked Industrial Service Carrier. */
public final class TileVehicleServiceDock extends TileEntity {
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, pos);
        return vehicle != null && vehicle.hasCapability(capability, facing)
            || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, pos);
        if (vehicle != null && vehicle.hasCapability(capability, facing))
            return vehicle.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }
}
