package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class IndustrialGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        return id == IndustrialCivilizationCore.GUI_INDUSTRIAL_MACHINE
            && tile instanceof TileIndustrialMachine
            ? new ContainerIndustrialMachine(player.inventory, (TileIndustrialMachine) tile) : null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        return id == IndustrialCivilizationCore.GUI_INDUSTRIAL_MACHINE
            && tile instanceof TileIndustrialMachine
            ? new GuiIndustrialMachine(player.inventory, (TileIndustrialMachine) tile) : null;
    }
}
