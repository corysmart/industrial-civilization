package com.industrialcivilization.core;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class WorldgenShowcaseRulesTest {
    @Test
    public void coversEveryFirstPartyWorldgenVariantWithThreeViews() {
        HashSet<String> targets = new HashSet<>(Arrays.asList(CommandIndustrialShowcase.STRUCTURES));
        assertEquals(CommandIndustrialShowcase.STRUCTURES.length, targets.size());
        assertEquals(12, targets.size());
        assertEquals(3, CommandIndustrialShowcase.ANGLES_PER_STRUCTURE);
        assertTrue(targets.contains("primitive_settlement"));
        assertTrue(targets.contains("militia_outpost"));
        assertTrue(targets.contains("industrial_city"));
        assertTrue(targets.contains("industrial_city_variant_b"));
        assertTrue(targets.contains("abandoned_factory"));
        assertTrue(targets.contains("regional_road"));
        assertTrue(targets.contains("apollo_11_memorial"));
        for (String specialty : Arrays.asList("steel", "electronics", "fuel", "armaments", "research"))
            assertTrue(targets.contains("factory_" + specialty));
    }

    @Test
    public void locatorCoversEveryNaturalOverworldStructureType() {
        HashSet<String> targets = new HashSet<>(Arrays.asList(CommandIndustrialLocateAll.TARGETS));
        assertEquals(CommandIndustrialLocateAll.TARGETS.length, targets.size());
        assertEquals(10, targets.size());
        assertTrue(targets.contains("primitive_settlement"));
        assertTrue(targets.contains("militia_outpost"));
        assertTrue(targets.contains("industrial_city"));
        assertTrue(targets.contains("abandoned_factory"));
        assertTrue(targets.contains("regional_road"));
        for (String specialty : Arrays.asList("steel", "electronics", "fuel", "armaments", "research"))
            assertTrue(targets.contains("factory_" + specialty));
    }

    @Test
    public void naturalStructuresWaitForLateQueuedTerrainGenerators() {
        assertTrue(CivilizationStructureData.DEFERRED_REPAIR_TICKS >= 20);
        assertTrue(CivilizationStructureData.DEFERRED_REPAIR_PASSES >= 3);
    }

    @Test
    public void regionalRoadIntersectionsContainBothDirections() {
        assertTrue(GameplayRules.regionalRoadChunk(0, 0));
        assertTrue(GameplayRules.regionalRoadChunk(8, 3));
        assertTrue(GameplayRules.regionalRoadChunk(3, 8));
    }

    @Test
    public void primitiveSettlementGetsDirtApproachEndingOnRegionalGrid() {
        int[] road = GameplayRules.closestRegionalRoadApproach(241, 497, 31, 15, 3);
        assertEquals(3, road[4]);
        assertEquals(8, Math.floorMod(road[2], 128));
        assertTrue(Math.abs(road[1] - road[2]) <= 128);
    }

    @Test
    public void industrialCityGetsAsphaltApproachEndingOnRegionalGrid() {
        int[] road = GameplayRules.closestRegionalRoadApproach(3017, -2879, 79, 38, 2);
        assertEquals(2, road[4]);
        assertEquals(8, Math.floorMod(road[2], 128));
        assertTrue(Math.abs(road[1] - road[2]) <= 128);
    }

}
