package com.industrialcivilization.core;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class IndustrialMachinePersistenceTest {
    @Test
    public void loadsWorkAndElapsedTimeWithoutResettingOperation() {
        NBTTagCompound saved = new NBTTagCompound();
        saved.setDouble("Energy", 10000D);
        saved.setInteger("Progress", 80);
        saved.setDouble("WorkCompletedEU", 2560D);
        saved.setDouble("PendingOperationEU", 128D);
        saved.setInteger("ElapsedOperationTicks", 80);
        saved.setString("ActiveRecipe", "precision_frame");
        saved.setInteger("OperationPeakMfsuPackets", 10);
        saved.setInteger("Queued", 1);

        TileIndustrialMachine tile = new TileIndustrialMachine();
        tile.readFromNBT(saved);

        assertEquals(10000, tile.getEnergyStored());
        assertEquals(2560L, tile.getWorkCompletedEU());
        assertEquals(80, tile.getElapsedOperationTicks());
        assertEquals(80, tile.getProgress());
        assertEquals(10, tile.getOperationPeakMfsuPackets());
    }

    @Test
    public void defersCapacityClampUntilThePlacedMachineKindIsKnown() {
        NBTTagCompound saved = new NBTTagCompound();
        saved.setDouble("Energy", 30000000D);

        TileIndustrialMachine tile = new TileIndustrialMachine();
        tile.readFromNBT(saved);

        // A tile has no world/block state during this unit load. Clamping here to
        // the fallback Fabricator capacity would corrupt high-tier machine saves.
        assertEquals(30000000, tile.getEnergyStored());
    }
}
