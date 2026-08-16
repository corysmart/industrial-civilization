package com.industrialcivilization.core;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public final class IndustrialMachinePeripheralContractTest {
    @Test
    public void preservesLegacyMethodIndices() {
        String[] legacy = {
            "getStatus", "getEnergy", "getCapacity", "getProgress", "getEnvironment",
            "listRecipes", "selectRecipe", "queue", "getCompleted", "setCargoChannel",
            "getCargoChannel", "transferCargo"
        };
        assertArrayEquals(legacy, Arrays.copyOf(
            TileIndustrialMachine.PERIPHERAL_METHODS, legacy.length));
    }

    @Test
    public void appendsNativePowerTelemetry() {
        assertTrue(Arrays.asList(TileIndustrialMachine.PERIPHERAL_METHODS).containsAll(
            Arrays.asList("getEnergyStored", "getInputTier", "getAcceptedEUThisTick",
                "getBaselineEUPerTick", "getEffectiveSpeedMultiplier", "getWorkCompleted",
                "getWorkRequired", "getEstimatedTicksRemaining", "getMfsuPacketsThisTick",
                "getOperationPeakMfsuPackets")));
    }
}
