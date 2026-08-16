package com.industrialcivilization.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class NativeIc2PowerModelTest {
    @Test
    public void preservesBaselineEnergyAndDuration() {
        long work = NativeIc2PowerModel.totalWorkEU(512, 320);
        assertEquals(163840L, work);
        assertEquals(320, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 1));
    }

    @Test
    public void scalesAtTwoAndFourTimesBaseline() {
        long work = NativeIc2PowerModel.totalWorkEU(512, 320);
        assertEquals(160, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 2));
        assertEquals(80, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 4));
    }

    @Test
    public void supportsParallelMfsuBanksWithoutAggregatePacketReinterpretation() {
        long work = NativeIc2PowerModel.totalWorkEU(512, 320);
        assertEquals(320, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 1));
        assertEquals(80, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 4));
        assertEquals(32, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 10));
        assertEquals(7, NativeIc2PowerModel.simulateDuration(work, 512, 0, 512, 512, 50));
    }

    @Test
    public void distinguishesManyLegalPacketsFromOneIllegalPacket() {
        assertTrue(NativeIc2PowerModel.isLegalPacket(512, 512));
        assertTrue(NativeIc2PowerModel.isLegalPacket(512, 512));
        assertFalse(NativeIc2PowerModel.isLegalPacket(513, 512));
        assertEquals(-1, NativeIc2PowerModel.simulateDuration(163840, 512, 0, 512, 1024, 1));
    }

    @Test
    public void identifiesOnlyMfsuClassVoltage() {
        assertTrue(NativeIc2PowerModel.isMfsuClassVoltage(512D));
        assertFalse(NativeIc2PowerModel.isMfsuClassVoltage(128D));
        assertFalse(NativeIc2PowerModel.isMfsuClassVoltage(2048D));
    }

    @Test
    public void countsAcceptedEnergyInsteadOfSplitEnergyNetCallbacks() {
        assertEquals(0, NativeIc2PowerModel.mfsuPacketEquivalents(255.999D));
        assertEquals(1, NativeIc2PowerModel.mfsuPacketEquivalents(511.999D));
        assertEquals(1, NativeIc2PowerModel.mfsuPacketEquivalents(512D));
        assertEquals(10, NativeIc2PowerModel.mfsuPacketEquivalents(5120D));
        assertEquals(50, NativeIc2PowerModel.mfsuPacketEquivalents(25600D));
        // Small legal glass-fibre losses still represent fifty emitted MFSU packets.
        assertEquals(50, NativeIc2PowerModel.mfsuPacketEquivalents(25500D));
        // Forty-nine full packets remain below the fifty-bank threshold.
        assertEquals(49, NativeIc2PowerModel.mfsuPacketEquivalents(49D * 512D));
        // Fifty-six callbacks that aggregate to ten real source packets still
        // represent ten MFSUs, not a fifty-MFSU bank.
        assertEquals(10, NativeIc2PowerModel.mfsuPacketEquivalents(56D * (5120D / 56D)));
    }

    @Test
    public void blinkChallengeRequiresFiftyPacketsAndEnergyLimitedWork() {
        assertTrue(NativeIc2PowerModel.qualifiesBlinkManufacturing(50, 7, 0));
        assertFalse(NativeIc2PowerModel.qualifiesBlinkManufacturing(49, 7, 0));
        assertFalse(NativeIc2PowerModel.qualifiesBlinkManufacturing(50, 9, 0));
        assertFalse(NativeIc2PowerModel.qualifiesBlinkManufacturing(50, 7, 600));
    }

    @Test
    public void directOperationCapacityAvoidsLegacyBufferThroughputCap() {
        assertEquals(25600D, NativeIc2PowerModel.usableWorkEU(
            0D, 25600D, 25600D, 512, 163840D, true, true), 0D);
        assertEquals(512D, NativeIc2PowerModel.usableWorkEU(
            400000D, 0D, 25600D, 512, 163840D, true, false), 0D);
    }

    @Test
    public void preservesScientificMinimumElapsedTime() {
        long work = NativeIc2PowerModel.totalWorkEU(32, 600);
        assertEquals(600, NativeIc2PowerModel.simulateDuration(work, 32, 600, 32, 32, 50));
    }

    @Test
    public void migratesLegacyProgressProportionally() {
        assertEquals(81920D, NativeIc2PowerModel.migrateLegacyProgress(
            160, 320, 163840), 0D);
    }

    @Test
    public void estimatesEnergyAndMinimumTimeTogether() {
        assertEquals(40, NativeIc2PowerModel.estimateTicksRemaining(
            81920D, 163840, 0, 0, 512, 2048D, true, true));
        assertEquals(500, NativeIc2PowerModel.estimateTicksRemaining(
            19200D, 19200, 100, 600, 32, 1600D, true, true));
    }
}
