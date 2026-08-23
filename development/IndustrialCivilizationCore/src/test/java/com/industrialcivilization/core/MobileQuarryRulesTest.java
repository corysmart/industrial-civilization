package com.industrialcivilization.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class MobileQuarryRulesTest {
    @Test
    public void assignsDistinctHarnessOutputsRelativeToTravel() {
        assertEquals(EnumFacing.NORTH, MobileQuarryRules.outputSide(
            EnumFacing.EAST, MobileQuarryRules.Output.BREAK));
        assertEquals(EnumFacing.WEST, MobileQuarryRules.outputSide(
            EnumFacing.EAST, MobileQuarryRules.Output.MOVE));
        assertEquals(EnumFacing.DOWN, MobileQuarryRules.outputSide(
            EnumFacing.EAST, MobileQuarryRules.Output.DEPLOY));
        // Minecraft queries each source with the receiver-to-source face.
        assertEquals(EnumFacing.SOUTH, MobileQuarryRules.powerQuerySide(
            EnumFacing.EAST, MobileQuarryRules.Output.BREAK));
        assertEquals(EnumFacing.EAST, MobileQuarryRules.powerQuerySide(
            EnumFacing.EAST, MobileQuarryRules.Output.MOVE));
        assertEquals(EnumFacing.UP, MobileQuarryRules.powerQuerySide(
            EnumFacing.EAST, MobileQuarryRules.Output.DEPLOY));
    }

    @Test
    public void acceptsOnlyOnePhysicalStepInTravelDirection() {
        BlockPos origin = new BlockPos(10, 64, -5);
        assertTrue(MobileQuarryRules.isOneStepForward(origin,
            new BlockPos(11, 64, -5), EnumFacing.EAST));
        assertFalse(MobileQuarryRules.isOneStepForward(origin,
            new BlockPos(26, 64, -5), EnumFacing.EAST));
        assertFalse(MobileQuarryRules.isOneStepForward(origin,
            new BlockPos(10, 64, -6), EnumFacing.EAST));
        assertFalse(MobileQuarryRules.isOneStepForward(origin,
            new BlockPos(11, 65, -5), EnumFacing.EAST));
    }

    @Test
    public void freshQuarryIsNeverComplete() {
        assertFalse(MobileQuarryRules.isBuildCraftQuarryComplete(new NBTTagCompound()));
    }

    @Test
    public void initializedActiveIteratorIsNotComplete() {
        NBTTagCompound state = exhaustedState();
        state.getCompoundTag("boxIterator").setIntArray("current", new int[] {1, 2, 3});
        assertFalse(MobileQuarryRules.isBuildCraftQuarryComplete(state));
    }

    @Test
    public void inFlightBreakTaskIsNotComplete() {
        NBTTagCompound state = exhaustedState();
        state.setByte("currentTaskId", (byte) 2);
        assertFalse(MobileQuarryRules.isBuildCraftQuarryComplete(state));
    }

    @Test
    public void exhaustedPersistedIteratorIsComplete() {
        assertTrue(MobileQuarryRules.isBuildCraftQuarryComplete(exhaustedState()));
    }

    private static NBTTagCompound exhaustedState() {
        NBTTagCompound state = new NBTTagCompound();
        state.setBoolean("firstChecked", true);
        state.setTag("boxIterator", new NBTTagCompound());
        return state;
    }
}
