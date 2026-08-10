package com.industrialcivilization.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameplayRulesTest {
    @Test public void machineGuiUsesNativeScaleAtMinimumWindow() {
        assertEquals(1.0F, GameplayRules.machineGuiScale(427, 240), 0.0001F);
    }
    @Test public void machineGuiKeepsItsScreenShareOnLargeWindow() {
        assertEquals(1.386F, GameplayRules.machineGuiScale(592, 333), 0.001F);
    }
    @Test public void machineGuiScaleUsesConstrainingScreenDimension() {
        assertEquals(1.0F, GameplayRules.machineGuiScale(900, 240), 0.0001F);
        assertEquals(1.0F, GameplayRules.machineGuiScale(427, 600), 0.0001F);
    }

    @Test public void questHomeTitleKeepsSmallWindowMinimum() {
        assertEquals(256, GameplayRules.questHomeTitleWidth(395, 176));
    }
    @Test public void questHomeTitleGrowsOnLargeWindows() {
        assertEquals(364, GameplayRules.questHomeTitleWidth(560, 269));
        assertEquals(603, GameplayRules.questHomeTitleWidth(928, 476));
    }

    @Test public void questZoomStopsWhenBackgroundReachesViewportEdges() {
        assertEquals(0.75F, GameplayRules.questMinimumZoom(384, 300, 512), 0.0001F);
        assertEquals(1.25F, GameplayRules.questMinimumZoom(640, 400, 512), 0.0001F);
    }
    @Test public void questZoomNeverExceedsBetterQuestingMaximum() {
        assertEquals(2.0F, GameplayRules.questMinimumZoom(1600, 900, 512), 0.0001F);
    }
    @Test public void questPanningCannotExposeLeftOrTopBackgroundEdges() {
        assertEquals(0, GameplayRules.questBoundedScroll(-40, 384, 512, 1.0F));
    }
    @Test public void questPanningCannotExposeRightOrBottomBackgroundEdges() {
        assertEquals(128, GameplayRules.questBoundedScroll(300, 384, 512, 1.0F));
    }
    @Test public void questPanningUsesZoomedViewportSize() {
        assertEquals(256, GameplayRules.questBoundedScroll(400, 512, 512, 2.0F));
    }

    @Test public void ordinaryRobberSpawnsUseConfiguredTwentyFivePercentChance() {
        assertTrue(GameplayRules.robberSpawnAllowed(24, 25, 0, 4, false));
        assertFalse(GameplayRules.robberSpawnAllowed(25, 25, 0, 4, false));
    }
    @Test public void localRobberCapPreventsPopulationAccumulation() {
        assertFalse(GameplayRules.robberSpawnAllowed(0, 100, 4, 4, false));
    }
    @Test public void deterministicTestReplacementBypassesPopulationLimits() {
        assertTrue(GameplayRules.robberSpawnAllowed(99, 0, 99, 1, true));
    }
    @Test public void robberIgnoresPlayerWithoutTechnicalOrValuableLoot() {
        assertFalse(GameplayRules.robberTargetsPlayer(false, false));
    }
    @Test public void robberTargetsPlayerCarryingTechnicalOrValuableLoot() {
        assertTrue(GameplayRules.robberTargetsPlayer(true, false));
    }
    @Test public void robberDefendsItselfWhenAttackedByUnprofitablePlayer() {
        assertTrue(GameplayRules.robberTargetsPlayer(false, true));
    }
    @Test public void militiaPatrolRequiresRegisteredOutpostProximity() {
        assertFalse(GameplayRules.militiaPatrolSpawnAllowed(false, 0, 6, false));
        assertTrue(GameplayRules.militiaPatrolSpawnAllowed(true, 0, 6, false));
    }
    @Test public void militiaPatrolLocalCapPreventsAccumulation() {
        assertFalse(GameplayRules.militiaPatrolSpawnAllowed(true, 6, 6, false));
    }
    @Test public void deterministicPatrolTestBypassesOutpostRequirement() {
        assertTrue(GameplayRules.militiaPatrolSpawnAllowed(false, 99, 1, true));
    }
    @Test public void aiAgeRequiresCanonicalMartianAutonomyArchive() {
        assertTrue(GameplayRules.aiAgeReady(true, true, true));
        assertFalse(GameplayRules.aiAgeReady(true, true, false));
        assertFalse(GameplayRules.aiAgeReady(true, false, true));
        assertFalse(GameplayRules.aiAgeReady(false, true, true));
    }
    @Test public void creepersAreSuppressedOnIndustrialEarth() {
        assertTrue(GameplayRules.suppressVanillaEarthHostile("minecraft", "creeper"));
    }
    @Test public void spidersAreSuppressedOnIndustrialEarth() {
        assertTrue(GameplayRules.suppressVanillaEarthHostile("minecraft", "spider"));
        assertTrue(GameplayRules.suppressVanillaEarthHostile("minecraft", "cave_spider"));
    }
    @Test public void endermenAreSuppressedOnIndustrialEarth() {
        assertTrue(GameplayRules.suppressVanillaEarthHostile("minecraft", "enderman"));
    }
    @Test public void replacementSourcesAreNotSuppressedBeforeConversion() {
        assertFalse(GameplayRules.suppressVanillaEarthHostile("minecraft", "zombie"));
        assertFalse(GameplayRules.suppressVanillaEarthHostile("minecraft", "skeleton"));
    }
    @Test public void moddedHostilesRemainAvailableForExplicitIntegrations() {
        assertFalse(GameplayRules.suppressVanillaEarthHostile("techguns", "zombie_soldier"));
    }
    @Test public void drillThreeByThreeHasEightSecondaryTargets() {
        assertEquals(8, GameplayRules.planeTargets(1));
    }
    @Test public void diamondDrillNineByNineHasEightySecondaryTargets() {
        assertEquals(80, GameplayRules.planeTargets(4));
    }
    @Test public void horizontalAndVerticalPlanesHaveIdenticalBudgets() {
        assertEquals(GameplayRules.planeTargets(4), GameplayRules.planeTargets(4));
    }
    @Test public void partialEnergyStopsAtAffordableBlock() {
        assertEquals(3, GameplayRules.payableTargets(399, 100, 80));
    }
    @Test public void zeroEnergyMinesNoExtraBlocks() {
        assertEquals(0, GameplayRules.payableTargets(0, 100, 8));
    }
    @Test public void hugeTreeIsSpreadAcrossTicks() {
        assertEquals(43, GameplayRules.harvestTicks(512, 12));
    }
    @Test public void nineByNineDoesNotRunInOneTick() {
        assertEquals(7, GameplayRules.harvestTicks(80, 12));
    }
    @Test public void fullUsableQuantumSuitProtects() {
        assertTrue(GameplayRules.completeArmor(new boolean[]{true,true,true,true}));
    }
    @Test public void missingArmorPieceDoesNotProtect() {
        assertFalse(GameplayRules.completeArmor(new boolean[]{true,true,false,true}));
    }
    @Test public void brokenArmorPieceDoesNotProtect() {
        assertFalse(GameplayRules.completeArmor(new boolean[]{true,false,true,true}));
    }
    @Test public void malformedArmorSetDoesNotProtect() {
        assertFalse(GameplayRules.completeArmor(new boolean[]{true,true,true}));
    }
    @Test public void activeGalacticraftOxygenDetectorProvesTheHabitat() {
        assertTrue(GameplayRules.activeOxygenDetector(
            "galacticraftcore:oxygen_detector", 1));
    }
    @Test public void inactiveOrUnrelatedBlocksCannotSatisfyTheHabitatGate() {
        assertFalse(GameplayRules.activeOxygenDetector(
            "galacticraftcore:oxygen_detector", 0));
        assertFalse(GameplayRules.activeOxygenDetector("minecraft:air", 1));
        assertFalse(GameplayRules.activeOxygenDetector(null, 1));
    }
    @Test public void orbitalEnvironmentUsesCanonicalHabitatMilestone() {
        assertEquals("orbital_habitat", GameplayRules.habitatMilestone("orbit"));
    }
    @Test public void lunarAndMartianHabitatMilestonesRemainStable() {
        assertEquals("lunar_habitat", GameplayRules.habitatMilestone("lunar"));
        assertEquals("martian_habitat", GameplayRules.habitatMilestone("martian"));
    }
    @Test public void roomScanRejectsUnloadedCoordinates() {
        assertFalse(GameplayRules.scanCoordinateAllowed(0,0,0,false));
    }
    @Test public void roomScanIncludesIrregularNearbyCoordinate() {
        assertTrue(GameplayRules.scanCoordinateAllowed(9,-5,7,true));
    }
    @Test public void roomScanIsBoundedForLargeBases() {
        assertFalse(GameplayRules.scanCoordinateAllowed(11,0,0,true));
    }
    @Test public void marketRemainsOneStageBehindPlayer() {
        assertEquals(3, GameplayRules.marketStage(4,7));
    }
    @Test public void marketCapacityStillCapsInventory() {
        assertEquals(2, GameplayRules.marketStage(7,2));
    }
    @Test public void pristineUsedEquipmentCannotArbitrageNewPrice() {
        assertEquals(32, GameplayRules.usedValue(100,10000));
    }
    @Test public void brokenEquipmentRetainsOnlyScrapValue() {
        assertEquals(1, GameplayRules.usedValue(32,0));
    }
    @Test public void northFacingWorkshopUsesNineBySevenFootprint() {
        WorkshopLayout.Bounds bounds = WorkshopLayout.bounds(0, 0, false);
        assertEquals(9, bounds.maxX - bounds.minX + 1);
        assertEquals(7, bounds.maxZ - bounds.minZ + 1);
    }
    @Test public void eastFacingWorkshopRotatesToSevenByNine() {
        WorkshopLayout.Bounds bounds = WorkshopLayout.bounds(0, 0, true);
        assertEquals(7, bounds.maxX - bounds.minX + 1);
        assertEquals(9, bounds.maxZ - bounds.minZ + 1);
    }
    @Test public void sideBySideWorkshopsCreateEastWestJoin() {
        WorkshopLayout.Bounds car = WorkshopLayout.bounds(0, 0, false);
        WorkshopLayout.Bounds gun = WorkshopLayout.bounds(9, 0, false);
        assertEquals(WorkshopLayout.Join.EAST_WEST, WorkshopLayout.join(car, gun));
        assertFalse(car.overlaps(gun));
    }
    @Test public void frontToBackWorkshopsCreateNorthSouthJoin() {
        WorkshopLayout.Bounds car = WorkshopLayout.bounds(0, 0, false);
        WorkshopLayout.Bounds gun = WorkshopLayout.bounds(0, 7, false);
        assertEquals(WorkshopLayout.Join.NORTH_SOUTH, WorkshopLayout.join(car, gun));
        assertFalse(car.overlaps(gun));
    }
    @Test public void overlappingWorkshopFootprintsAreRejectedByContract() {
        WorkshopLayout.Bounds car = WorkshopLayout.bounds(0, 0, false);
        WorkshopLayout.Bounds gun = WorkshopLayout.bounds(8, 0, false);
        assertTrue(car.overlaps(gun));
        assertEquals(WorkshopLayout.Join.NONE, WorkshopLayout.join(car, gun));
    }
    @Test public void distantWorkshopsStayIndependent() {
        WorkshopLayout.Bounds car = WorkshopLayout.bounds(0, 0, false);
        WorkshopLayout.Bounds gun = WorkshopLayout.bounds(10, 0, false);
        assertEquals(WorkshopLayout.Join.NONE, WorkshopLayout.join(car, gun));
    }
}
