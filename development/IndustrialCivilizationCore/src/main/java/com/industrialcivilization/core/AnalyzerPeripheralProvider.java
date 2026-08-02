package com.industrialcivilization.core;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class AnalyzerPeripheralProvider implements IPeripheralProvider {
    private static boolean registered;

    public static void register() {
        if (!registered) {
            ComputerCraftAPI.registerPeripheralProvider(new AnalyzerPeripheralProvider());
            registered = true;
        }
    }

    @Override
    public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileMolecularAnalyzer ? (TileMolecularAnalyzer) tile : null;
    }
}
