package com.industrialcivilization.core;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.*;

public final class IndustrialPolicyStateTest {
    @Test public void reserveHysteresisAndCooldownPreventThrashing() {
        IndustrialPolicyState state = new IndustrialPolicyState();
        state.enabled = true;
        state.configureReserve("minecraft:iron_ingot", 8, 16, 5);
        assertTrue(state.belowMinimum(7));
        state.acted(100, "freight requested", "");
        assertFalse(state.canAct(199));
        assertTrue(state.canAct(200));
        assertFalse(state.recovered(15));
        assertTrue(state.recovered(16));
    }

    @Test public void emergencyReserveUsesStricterAbsoluteOrPercentLimit() {
        IndustrialPolicyState state = new IndustrialPolicyState();
        state.configureEmergency(900, 25);
        assertEquals(1000, state.emergencyThreshold(4000));
        assertEquals(900, state.emergencyThreshold(2000));
    }

    @Test public void priorityOnlyOutranksMatchingEnabledResourcePolicy() {
        IndustrialPolicyState critical = new IndustrialPolicyState();
        IndustrialPolicyState normal = new IndustrialPolicyState();
        critical.enabled = true; normal.enabled = true;
        critical.configureReserve("minecraft:iron_ingot", 8, 16, 8);
        normal.configureReserve("minecraft:iron_ingot", 8, 16, 2);
        assertTrue(critical.outranks(normal));
        assertFalse(normal.outranks(critical));
        normal.configureReserve("minecraft:redstone", 8, 16, 2);
        assertFalse(critical.outranks(normal));
        critical.manualOverride = true;
        assertFalse(critical.outranks(normal));
    }

    @Test public void manifestAndServiceStatePersist() {
        IndustrialPolicyState original = new IndustrialPolicyState();
        original.facilityName = "Mars Spares";
        original.configureReserve("industrialcivilizationcore:precision_frame", 4, 12, 8);
        original.requestManifest("Mars Spares", original.reserveItem, 8, 1000);
        original.recordDelivery(1040);
        original.serviceProgram = "mars_spares";
        original.servicePhase = "commissioning";
        original.commissioningTicks = 77;
        IndustrialPolicyState restored = new IndustrialPolicyState();
        restored.read(original.write());
        assertEquals("Mars Spares", restored.facilityName);
        assertEquals(1, restored.manifestDelivered);
        assertEquals("in_transit", restored.manifestStatus);
        assertEquals(77, restored.commissioningTicks);
    }
}
