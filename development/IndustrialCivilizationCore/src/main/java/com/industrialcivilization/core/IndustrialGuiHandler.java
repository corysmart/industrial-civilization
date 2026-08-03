package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class IndustrialGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == IndustrialCivilizationCore.GUI_INDUSTRIAL_MACHINE && tile instanceof TileIndustrialMachine)
            return new ContainerIndustrialMachine(player.inventory, (TileIndustrialMachine) tile);
        if (id == IndustrialCivilizationCore.GUI_VEHICLE_CRAFTING)
            return new ContainerMobileWorkbench(player.inventory, world, new BlockPos(x, y, z));
        if (id == IndustrialCivilizationCore.GUI_VEHICLE_STORAGE) {
            Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, new BlockPos(x, y, z));
            if (vehicle != null) {
                IItemHandlerModifiable handler = (IItemHandlerModifiable) vehicle.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                InventoryVehicleHandler inventory = new InventoryVehicleHandler(vehicle, handler);
                return new ContainerChest(player.inventory, inventory, player);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (id == IndustrialCivilizationCore.GUI_INDUSTRIAL_MACHINE && tile instanceof TileIndustrialMachine)
            return new GuiIndustrialMachine(player.inventory, (TileIndustrialMachine) tile);
        if (id == IndustrialCivilizationCore.GUI_VEHICLE_CRAFTING)
            return new GuiCrafting(player.inventory, world, new BlockPos(x, y, z));
        if (id == IndustrialCivilizationCore.GUI_VEHICLE_STORAGE) {
            Entity vehicle = VehicleIntegrationSystem.findServiceVehicle(world, new BlockPos(x, y, z));
            if (vehicle != null) {
                IItemHandlerModifiable handler = (IItemHandlerModifiable) vehicle.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                return new GuiChest(player.inventory, new InventoryVehicleHandler(vehicle, handler));
            }
        }
        return null;
    }
}
