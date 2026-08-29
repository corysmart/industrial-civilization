package com.industrialcivilization.core;

import java.util.Random;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.tile.TilePipeHolder;
import ic2.core.block.wiring.BlockCable;
import ic2.core.block.wiring.tile.TileEntityCable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

/**
 * Spawn-relative civilization geography. The first ring contains exactly three
 * primitive settlements; increasingly organized structures appear only as the
 * player explores farther from world spawn.
 */
public final class CivilizationWorldGenerator implements IWorldGenerator {
    private static final int[] PRIMITIVE_RADII = {240, 520, 800};
    private static final String[] FACTORY_SPECIALTIES = {
        "steel", "electronics", "fuel", "armaments", "research"
    };
    private static final ThreadLocal<PlacementBounds> PLACEMENT = new ThreadLocal<>();
    private static final Map<World, int[][]> PRIMITIVE_CHUNKS = Collections.synchronizedMap(
        new WeakHashMap<World, int[][]>());

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        try {
            if (world.provider.getDimension() != 0) {
                if (isMoon(world)) generateLunarHeritageFlags(chunkX, chunkZ, world);
                if (isMoon(world) || isMars(world)) generateSpaceCivilization(
                    deterministicRandom(world, chunkX, chunkZ), chunkX, chunkZ, world);
                return;
            }
            BlockPos spawn = world.getSpawnPoint();
            if (isPrimitiveSettlementChunk(world, chunkX, chunkZ, spawn)) {
                // Candidate selection already rejects water, mountains and steep
                // biomes. Raw height variation here is often just tree canopy,
                // which the settlement platform safely clears.
                BlockPos origin = surfaceOrigin(world, chunkX, chunkZ);
                if (origin != null) schedule(world, origin, "primitive_settlement", chunkX, chunkZ);
                return;
            }
            long x = (long) chunkX * 16L + 8L - spawn.getX();
            long z = (long) chunkZ * 16L + 8L - spawn.getZ();
            long distanceSquared = x * x + z * z;
            boolean roadChunk = isRegionalRoadChunk(chunkX, chunkZ);

        // Decide whether this chunk contains anything before asking the world
        // for height data. Most generated chunks now leave without touching
        // terrain, which removes the largest first-party exploration cost.
            int structure = selectOverworldStructure(random, distanceSquared);
            if (!roadChunk && structure == 0) return;

            double distance = Math.sqrt((double) distanceSquared);
            if (roadChunk) buildRegionalRoad(world, chunkX, chunkZ, distance);
            if (structure == 0) return;

            String structureId = overworldStructureId(structure, chunkX, chunkZ);
            BlockPos origin = suitableOverworldOrigin(world, chunkX, chunkZ, structureId);
            if (origin == null) return;

        // One structure at most per chunk. The order keeps the rarer, more
        // distant landmarks from being replaced by common factory shells.
            schedule(world, origin, structureId, chunkX, chunkZ);
        } finally {
            CivilizationStructureData.applyGeneratedChunk(world, chunkX, chunkZ);
        }
    }

    private static void schedule(World world, BlockPos origin, String type, int chunkX, int chunkZ) {
        CivilizationStructureData.schedule(world, origin, type, chunkX, chunkZ);
    }

    private static int selectOverworldStructure(Random random, long distanceSquared) {
        if (distanceSquared >= 3000L * 3000L && random.nextInt(256) == 0) return 4;
        if (distanceSquared >= 2200L * 2200L && random.nextInt(160) == 0) return 3;
        if (distanceSquared >= 1400L * 1400L && random.nextInt(128) == 0) return 2;
        if (distanceSquared >= 900L * 900L && random.nextInt(96) == 0) return 1;
        return 0;
    }

    private static String overworldStructureId(int structure, int chunkX, int chunkZ) {
        if (structure == 4) return "industrial_city";
        if (structure == 3) return "factory_" + FACTORY_SPECIALTIES[
            Math.floorMod(chunkX * 31 + chunkZ * 17, FACTORY_SPECIALTIES.length)];
        if (structure == 2) return "militia_outpost";
        if (structure == 1) return "abandoned_factory";
        return null;
    }

    /** Predicts the structure selected by Forge without loading the candidate chunk. */
    public static String predictedOverworldStructure(World world, int chunkX, int chunkZ) {
        if (world.provider.getDimension() != 0) return null;
        BlockPos spawn = world.getSpawnPoint();
        if (isPrimitiveSettlementChunk(world, chunkX, chunkZ, spawn))
            return "primitive_settlement";
        long x = (long) chunkX * 16L + 8L - spawn.getX();
        long z = (long) chunkZ * 16L + 8L - spawn.getZ();
        long distanceSquared = x * x + z * z;
        Random forgeRandom = new Random(world.getSeed());
        long xSeed = forgeRandom.nextLong() >> 3;
        long zSeed = forgeRandom.nextLong() >> 3;
        forgeRandom.setSeed((xSeed * chunkX + zSeed * chunkZ) ^ world.getSeed());
        int structure = selectOverworldStructure(forgeRandom, distanceSquared);
        return overworldStructureId(structure, chunkX, chunkZ);
    }

    public static boolean predictedOverworldRoad(World world, int chunkX, int chunkZ) {
        if (world.provider.getDimension() != 0) return false;
        return isRegionalRoadChunk(chunkX, chunkZ);
    }

    static boolean isRegionalRoadChunk(int chunkX, int chunkZ) {
        return GameplayRules.regionalRoadChunk(chunkX, chunkZ);
    }

    private static boolean isMars(World world) {
        String name = world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT);
        return name.contains("mars");
    }

    private static boolean isMoon(World world) {
        return world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT).contains("moon");
    }

    /**
     * Equirectangular 24-block/degree interpretation of NASA landing
     * coordinates. These are respectful scale markers, not claims that
     * Galacticraft terrain reproduces the real lunar surface.
     */
    public static void generateLunarHeritageFlags(int chunkX, int chunkZ, World world) {
        Object[][] sites = {
            {"Apollo 11", "1969-07-20", 0.67408D, 23.47297D},
            {"Apollo 12", "1969-11-19", -3.0128D, -23.4219D},
            {"Apollo 14", "1971-02-05", -3.6453D, -17.4714D},
            {"Apollo 15", "1971-07-30", 26.1322D, 3.6339D},
            {"Apollo 16", "1972-04-21", -8.9734D, 15.5011D},
            {"Apollo 17", "1972-12-11", 20.1908D, 30.7717D}
        };
        for (Object[] site : sites) {
            int siteX = (int) Math.round((Double) site[3] * 24.0D);
            int siteZ = (int) Math.round(-(Double) site[2] * 24.0D);
            if (Math.floorDiv(siteX, 16) != chunkX || Math.floorDiv(siteZ, 16) != chunkZ) continue;
            BlockPos ground = world.getTopSolidOrLiquidBlock(new BlockPos(siteX, 0, siteZ)).down();
            if (ground.getY() < 10) return;
            // Non-loot basalt/iron memorial pad makes the site legible without
            // pretending an intact lander is naturally present.
            buildLunarHeritageMemorial(world, ground, (String) site[0], (String) site[1],
                (Double) site[2], (Double) site[3]);
            return;
        }
    }

    private static boolean aiAgeUnlocked(World world) {
        for (net.minecraft.entity.player.EntityPlayer player : world.playerEntities) {
            if (ProgressionState.has(player, "ai_age") || MarketEconomy.playerStage(player) >= 7) return true;
        }
        return false;
    }

    public static boolean hasAiAgePlayer(World world) { return aiAgeUnlocked(world); }

    /** Same seed and chunk always produce the same post-AI Mars decision. */
    public static void generatePostAiMarsChunk(World world, int chunkX, int chunkZ) {
        if (isMars(world) && aiAgeUnlocked(world)) generateSpaceCivilization(
            deterministicRandom(world, chunkX, chunkZ), chunkX, chunkZ, world);
    }

    private static Random deterministicRandom(World world, int chunkX, int chunkZ) {
        long seed = world.getSeed() ^ 0x4D415253434956L;
        seed ^= (long) chunkX * 341873128712L;
        seed ^= (long) chunkZ * 132897987541L;
        return new Random(seed);
    }

    /** Human settlements replace fantasy monster ecology on both program worlds. */
    private static void generateSpaceCivilization(Random random, int chunkX, int chunkZ, World world) {
        BlockPos origin = surfaceOrigin(world, chunkX, chunkZ, 25, 190);
        if (origin == null) return;
        double distance = Math.sqrt((double) chunkX * chunkX + (double) chunkZ * chunkZ) * 16.0D;
        if (random.nextInt(12) == 0) buildRegionalRoad(world, chunkX, chunkZ, distance);
        if (random.nextInt(320) == 0) schedule(world, origin, "industrial_city", chunkX, chunkZ);
        else if (random.nextInt(220) == 0) schedule(world, origin, "factory_"
            + FACTORY_SPECIALTIES[Math.floorMod(chunkX * 31 + chunkZ * 17,
                FACTORY_SPECIALTIES.length)], chunkX, chunkZ);
        else if (random.nextInt(180) == 0) schedule(world, origin, "militia_outpost", chunkX, chunkZ);
        else if (random.nextInt(140) == 0) schedule(world, origin, "primitive_settlement", chunkX, chunkZ);
        else if (random.nextInt(110) == 0) schedule(world, origin, "abandoned_factory", chunkX, chunkZ);
    }

    private static boolean isPrimitiveSettlementChunk(World world, int chunkX, int chunkZ,
            BlockPos spawn) {
        for (int[] candidate : primitiveSettlementChunks(world, spawn))
            if (candidate[0] == chunkX && candidate[1] == chunkZ) return true;
        return false;
    }

    private static int[][] primitiveSettlementChunks(World world, BlockPos spawn) {
        int[][] cached = PRIMITIVE_CHUNKS.get(world);
        if (cached != null) return cached;
        int[][] selected = new int[PRIMITIVE_RADII.length][2];
        Random anchors = new Random(world.getSeed() ^ 0x49C1A1F7L);
        for (int index = 0; index < PRIMITIVE_RADII.length; index++) {
            double angle = anchors.nextDouble() * Math.PI * 2.0 + index * 2.094;
            int targetX = spawn.getX() + (int) Math.round(Math.cos(angle) * PRIMITIVE_RADII[index]);
            int targetZ = spawn.getZ() + (int) Math.round(Math.sin(angle) * PRIMITIVE_RADII[index]);
            int targetChunkX = Math.floorDiv(targetX, 16);
            int targetChunkZ = Math.floorDiv(targetZ, 16);
            selected[index][0] = targetChunkX;
            selected[index][1] = targetChunkZ;
            boolean found = false;
            for (int radius = 0; radius <= 12 && !found; radius++) {
                for (int dx = -radius; dx <= radius && !found; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                        int candidateX = targetChunkX + dx;
                        int candidateZ = targetChunkZ + dz;
                        if (!predictedOverworldBiomeSuitable(world, candidateX, candidateZ,
                                "primitive_settlement")) continue;
                        selected[index][0] = candidateX;
                        selected[index][1] = candidateZ;
                        found = true;
                        break;
                    }
                }
            }
        }
        PRIMITIVE_CHUNKS.put(world, selected);
        return selected;
    }

    private static BlockPos surfaceOrigin(World world, int chunkX, int chunkZ) {
        return surfaceOrigin(world, chunkX, chunkZ, 55, 115);
    }

    /** Avoids flat platforms over oceans or embedded in steep terrain. */
    private static BlockPos suitableOverworldOrigin(World world, int chunkX, int chunkZ,
            String structureId) {
        if (!predictedOverworldBiomeSuitable(world, chunkX, chunkZ, structureId)) return null;
        int x = chunkX * 16 + 1;
        int z = chunkZ * 16 + 1;
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        // Population already has the anchor's east/south 2x2 chunk square in
        // memory, so inspect that full safe area without recursively loading
        // the farther chunks occupied by a city.
        for (int offsetX : new int[] {0, 7, 14, 21, 30})
            for (int offsetZ : new int[] {0, 7, 14, 21, 30}) {
            int height = world.getHeight(new BlockPos(x + offsetX, 0, z + offsetZ)).getY();
            minimum = Math.min(minimum, height);
            maximum = Math.max(maximum, height);
        }
        if (maximum - minimum > 6) return null;
        return surfaceOrigin(world, chunkX, chunkZ);
    }

    /** Cheap locator-side filter that never requests terrain chunks. */
    public static boolean predictedOverworldBiomeSuitable(World world, int chunkX, int chunkZ,
            String structureId) {
        int x = chunkX * 16 + 1;
        int z = chunkZ * 16 + 1;
        int width = structureWidth(structureId);
        int[] offsets = {0, width / 2, width - 1};
        for (int offsetX : offsets) for (int offsetZ : offsets) {
            Biome biome = world.getBiome(new BlockPos(x + offsetX, 64, z + offsetZ));
            if ("regional_road".equals(structureId)
                    && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)
                        || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)
                        || BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE)))
                return false;
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.WATER)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.HILLS)
                    || biome.getBaseHeight() > 0.35F || biome.getHeightVariation() > 0.30F)
                return false;
        }
        return true;
    }

    private static BlockPos surfaceOrigin(World world, int chunkX, int chunkZ, int minY, int maxY) {
        int x = chunkX * 16 + 1;
        int z = chunkZ * 16 + 1;
        int y = world.getHeight(new BlockPos(x + 7, 0, z + 7)).getY();
        return y < minY || y > maxY ? null : new BlockPos(x, y, z);
    }

    private static void buildPrimitiveSettlement(World world, BlockPos origin) {
        Random layout = layoutRandom(world, origin, 0x5052494D49544956L);
        if (allowSideEffects()) SettlementEconomySystem.register(world, origin, "primitive", "food", 0);
        platform(world, origin, 31, Blocks.COBBLESTONE.getDefaultState(), 9);
        // Six homes, a communal longhouse, two crop plots and a village green.
        int[][] homes = {{2,2},{12,2},{22,2},{2,23},{12,23},{22,23}};
        for (int index = 0; index < homes.length; index++) {
            IBlockState wall = layout.nextBoolean() ? Blocks.PLANKS.getDefaultState()
                : Blocks.LOG.getStateFromMeta(layout.nextBoolean() ? 0 : 1);
            cottage(world, origin.add(homes[index][0], 1, homes[index][1]), wall,
                3 + layout.nextInt(2), index >= 3);
        }
        building(world, origin.add(11, 1, 11), 9, 9, Blocks.PLANKS.getDefaultState(), 5);
        set(world, origin.add(15, 2, 11), Blocks.AIR.getDefaultState());
        set(world, origin.add(15, 3, 11), Blocks.AIR.getDefaultState());
        set(world, origin.add(13, 2, 15), Blocks.CRAFTING_TABLE.getDefaultState());
        set(world, origin.add(17, 2, 15), Blocks.CHEST.getDefaultState());
        for (int index = 0; index < 31; index++) for (int lane = -1; lane <= 1; lane++) {
            set(world, origin.add(15 + lane, 1, index), Blocks.GRAVEL.getDefaultState());
            set(world, origin.add(index, 1, 15 + lane), Blocks.GRAVEL.getDefaultState());
        }
        cropPlot(world, origin, 2, 10, layout.nextBoolean());
        cropPlot(world, origin, 22, 10, !layout.nextBoolean());
        for (int y = 1; y <= 2; y++) set(world, origin.add(15, y, 15),
            y == 1 ? Blocks.COBBLESTONE.getDefaultState() : Blocks.OAK_FENCE.getDefaultState());
        set(world, origin.add(15, 3, 15), Blocks.TORCH.getDefaultState());
        if (allowSideEffects()) {
            FactionSystem.spawnCitizen(world, origin.getX() + 8.5, origin.getY() + 2,
                origin.getZ() + 14.5, "frontier_cooperative", "villager", "food", "Frontier Grower", 2);
            FactionSystem.spawnCitizen(world, origin.getX() + 14.5, origin.getY() + 2,
                origin.getZ() + 10.5, "frontier_cooperative", "trader", "general", "Cooperative Trader", 2);
            FactionSystem.spawnCitizen(world, origin.getX() + 10.5, origin.getY() + 2,
                origin.getZ() + 14.5, "frontier_cooperative", "guard", "general", "Village Watch");
        }
    }

    private static void buildMilitiaOutpost(World world, BlockPos origin) {
        Random layout = layoutRandom(world, origin, 0x4F5554504F53544CL);
        if (allowSideEffects()) {
            SettlementEconomySystem.register(world, origin, "militia_outpost", "armaments", 2);
            MilitiaOutpostRegistry.record(world, origin);
        }
        platform(world, origin, 31, Blocks.STONEBRICK.getDefaultState(), 11);
        for (int i = 0; i < 31; i++) {
            for (int y = 1; y <= 3; y++) {
                set(world, origin.add(i, y, 0), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(i, y, 30), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(0, y, i), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(30, y, i), Blocks.IRON_BARS.getDefaultState());
            }
        }
        tower(world, origin.add(1, 1, 1), 7 + layout.nextInt(2));
        tower(world, origin.add(25, 1, 1), 7 + layout.nextInt(2));
        tower(world, origin.add(1, 1, 25), 7 + layout.nextInt(2));
        tower(world, origin.add(25, 1, 25), 7 + layout.nextInt(2));
        building(world, origin.add(3, 1, 11), 11, 8, Blocks.STONEBRICK.getDefaultState(), 5);
        building(world, origin.add(18, 1, 10), 9, 10, Blocks.IRON_BLOCK.getDefaultState(), 6);
        building(world, origin.add(10, 1, 23), 11, 6, Blocks.STONEBRICK.getDefaultState(), 4);
        set(world, origin.add(8, 2, 11), Blocks.AIR.getDefaultState());
        set(world, origin.add(22, 2, 10), Blocks.IRON_DOOR.getDefaultState());
        set(world, origin.add(15, 2, 23), Blocks.AIR.getDefaultState());
        set(world, origin.add(13, 2, 26), Blocks.CHEST.getDefaultState());
        set(world, origin.add(17, 2, 26), Blocks.ANVIL.getDefaultState());
        for (int x = 14; x <= 16; x++) for (int y = 1; y <= 3; y++)
            set(world, origin.add(x, y, 0), Blocks.AIR.getDefaultState());
        for (int z = 1; z <= 9; z++) set(world, origin.add(15, 1, z), Blocks.GRAVEL.getDefaultState());
        if (layout.nextBoolean()) installUtilitySpine(world, origin.add(8, 0, 16), false, 7, 0, 2);
        if (allowSideEffects()) {
            markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 12.5, origin.getY() + 2,
                origin.getZ() + 9.5, "territorial_militia", "militia", "armaments", "Militia Quartermaster", 5), origin);
            if (world.provider.getDimension() == 0) {
                markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 8.5, origin.getY() + 2,
                    origin.getZ() + 8.5, "territorial_militia", "militia", "armaments", "Outpost Enforcer"), origin);
                markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 13.5, origin.getY() + 2,
                    origin.getZ() + 6.5, "territorial_militia", "militia", "armaments", "Outpost Enforcer"), origin);
            } else {
                PlanetaryEcologySystem.spawnSpaceMilitia(world, origin.getX() + 8.5,
                    origin.getY() + 2, origin.getZ() + 8.5);
                PlanetaryEcologySystem.spawnSpaceMilitia(world, origin.getX() + 13.5,
                    origin.getY() + 2, origin.getZ() + 6.5);
            }
        }
    }

    private static void markOutpost(net.minecraft.entity.passive.EntityVillager citizen, BlockPos origin) {
        citizen.getEntityData().setBoolean("IndustrialMilitiaOutpost", true);
        citizen.getEntityData().setString("IndustrialOutpostId", origin.getX() + "_" + origin.getZ());
    }

    private static void buildOperationalFactory(World world, BlockPos origin, String specialty) {
        Random layout = layoutRandom(world, origin, specialty.hashCode() * 0x5DEECE66DL);
        if (allowSideEffects()) SettlementEconomySystem.register(world, origin, "operational_factory", specialty, 2);
        platform(world, origin, 31, Blocks.STONEBRICK.getDefaultState(), 15);
        buildSpecialtyFactory(world, origin, specialty, layout);
        Block machine = "research".equals(specialty) ? IndustrialCivilizationCore.RESEARCH_STATION
            : "electronics".equals(specialty) ? IndustrialCivilizationCore.PROGRAMMABLE_ASSEMBLER
            : "armaments".equals(specialty) ? IndustrialCivilizationCore.ROBOTIC_MANUFACTURING_CELL
            : IndustrialCivilizationCore.ELECTRIC_FABRICATOR;
        set(world, origin.add(15, 2, 13), machine.getDefaultState());
        set(world, origin.add(11, 2, 13), Blocks.CHEST.getDefaultState());
        set(world, origin.add(19, 2, 13), Blocks.CHEST.getDefaultState());
        boolean criminal = "armaments".equals(specialty);
        String faction = criminal ? "territorial_militia"
            : "research".equals(specialty) ? "survey_detachment_7" : "riverside_works";
        String title = Character.toUpperCase(specialty.charAt(0)) + specialty.substring(1);
        if (allowSideEffects()) {
            FactionSystem.spawnCitizen(world, origin.getX() + 8.5, origin.getY() + 2,
                origin.getZ() + 8.5, faction, "trader", specialty, title + " Works Factor", 5);
            FactionSystem.spawnCitizen(world, origin.getX() + 13.5, origin.getY() + 2,
                origin.getZ() + 8.5, faction, "engineer", specialty, title + " Works Engineer");
            FactionSystem.spawnCitizen(world, origin.getX() + 9.5, origin.getY() + 2,
                origin.getZ() + 14.5, criminal ? "territorial_militia" : "civil_defense",
                "guard", specialty, criminal ? "Militia Factory Enforcer" : "Civil Defense Inspector");
        }
        installLargeFactoryUtilities(world, origin);
    }

    private static void buildIndustrialCity(World world, BlockPos origin) {
        Random layout = layoutRandom(world, origin, 0x494E445553545259L);
        if (allowSideEffects()) SettlementEconomySystem.register(world, origin, "industrial_city", "general", 3);
        platform(world, origin, 79, Blocks.STONEBRICK.getDefaultState(), 22);
        int variant = layout.nextInt(8);
        IBlockState[] palettes = {Blocks.BRICK_BLOCK.getDefaultState(),
            Blocks.STONEBRICK.getDefaultState(), Blocks.QUARTZ_BLOCK.getDefaultState(),
            Blocks.HARDENED_CLAY.getStateFromMeta(8), Blocks.IRON_BLOCK.getDefaultState()};
        int buildingCount = 0;
        for (int lotX = 0; lotX < 6; lotX++) for (int lotZ = 0; lotZ < 6; lotZ++) {
            // Four central lots form the civic district; the north-west lot holds city hall.
            if ((lotX == 2 || lotX == 3) && (lotZ == 2 || lotZ == 3)) continue;
            int x = 1 + lotX * 13;
            int z = 1 + lotZ * 13;
            int width = 10 + layout.nextInt(2);
            int depth = 10 + layout.nextInt(2);
            boolean residential = lotX <= 1 && lotZ <= 3;
            boolean industrial = lotX >= 4 || lotZ >= 4;
            int height = residential ? 5 + layout.nextInt(4)
                : industrial ? 7 + layout.nextInt(6) : 9 + layout.nextInt(6);
            int paletteOffset = residential ? 0 : industrial ? 3 : 1;
            IBlockState wall = palettes[Math.floorMod(
                layout.nextInt() + lotX + lotZ + paletteOffset, palettes.length)];
            cityBuilding(world, origin.add(x, 1, z), width, depth, wall, height,
                (variant + lotX * 3 + lotZ) & 3);
            buildingCount++;
        }
        // City hall makes the thirty-third building and anchors three open civic plazas.
        cityBuilding(world, origin.add(27, 1, 27), 11, 11,
            Blocks.QUARTZ_BLOCK.getDefaultState(), 13 + (variant & 2), 3);
        buildingCount++;
        for (int road : new int[] {12, 25, 38, 51, 64}) for (int index = 0; index < 79; index++) {
            for (int lane = 0; lane <= 1; lane++) {
                set(world, origin.add(road + lane, 1, index), Blocks.DOUBLE_STONE_SLAB.getDefaultState());
                set(world, origin.add(index, 1, road + lane), Blocks.DOUBLE_STONE_SLAB.getDefaultState());
            }
        }
        for (int x = 27; x <= 50; x++) for (int z = 27; z <= 50; z++)
            if (!isCityRoad(x, z) && !(x <= 37 && z <= 37))
                set(world, origin.add(x, 1, z),
                ((x + z + variant) & 3) == 0 ? Blocks.GRASS.getDefaultState()
                    : Blocks.STONE_SLAB.getDefaultState());
        // A fountain and civic monument make the three open central lots legible from above.
        for (int x = 42; x <= 47; x++) for (int z = 29; z <= 34; z++)
            if (x == 42 || x == 47 || z == 29 || z == 34)
                set(world, origin.add(x, 2, z), Blocks.STONEBRICK.getDefaultState());
            else set(world, origin.add(x, 2, z), Blocks.WATER.getDefaultState());
        for (int y = 2; y <= 7; y++)
            set(world, origin.add(32, y, 45), Blocks.IRON_BLOCK.getDefaultState());
        set(world, origin.add(32, 8, 45), Blocks.GLOWSTONE.getDefaultState());
        for (int x : new int[] {11,24,37,50,63,76})
            for (int z : new int[] {11,24,37,50,63,76}) {
            set(world, origin.add(x, 2, z), Blocks.IRON_BARS.getDefaultState());
            set(world, origin.add(x, 3, z), Blocks.GLOWSTONE.getDefaultState());
        }
        if (allowSideEffects()) {
            FactionSystem.spawnCitizen(world, origin.getX() + 10.5, origin.getY() + 2,
                origin.getZ() + 12.5, "riverside_works", "trader", "electronics", "City Exchange Broker", 7);
            FactionSystem.spawnCitizen(world, origin.getX() + 12.5, origin.getY() + 2,
                origin.getZ() + 10.5, "riverside_works", "trader", "steel", "Foundry Representative", 7);
            FactionSystem.spawnCitizen(world, origin.getX() + 9.5, origin.getY() + 2,
                origin.getZ() + 10.5, "civil_defense", "guard", "armaments", "Civil Defense Officer");
            FactionSystem.spawnCitizen(world, origin.getX() + 13.5, origin.getY() + 2,
                origin.getZ() + 13.5, "survey_detachment_7", "scientist", "research", "Urban Surveyor");
        }
        installLargeCityUtilities(world, origin);
        BlockPos exchange = origin.add(32, 2, 32);
        set(world, exchange, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState());
        net.minecraft.tileentity.TileEntity tile = insidePlacement(exchange) ? world.getTileEntity(exchange) : null;
        if (tile instanceof TileIndustrialMachine) {
            String[] products = {"minecraft:iron_ingot", "minecraft:redstone", "minecraft:coal",
                "minecraft:paper", "minecraft:bread"};
            int index = Math.floorMod(origin.getX() * 31 + origin.getZ(), products.length);
            ((TileIndustrialMachine) tile).seedNationExchange("earth_nation_exchange", products[index]);
        }
        if (allowSideEffects()) IndustrialCivilizationCore.LOGGER.info(
            "IC_WORLDGEN|CITY_LAYOUT|origin={},{},{}|variant={}|buildings={}",
            origin.getX(), origin.getY(), origin.getZ(), variant, buildingCount);
    }

    /** Width and depth of a deterministic structure footprint. */
    public static int structureWidth(String id) {
        if ("industrial_city".equals(id) || "industrial_city_variant_b".equals(id)) return 79;
        if ("primitive_settlement".equals(id) || "militia_outpost".equals(id)
                || "abandoned_factory".equals(id) || id.startsWith("factory_")) return 31;
        return 16;
    }

    static int descriptorMinX(BlockPos origin, String id) {
        AccessRoadPlan road = accessRoadPlan(origin, id);
        return road == null ? origin.getX() : Math.min(origin.getX(), road.minX());
    }

    static int descriptorMaxX(BlockPos origin, String id) {
        AccessRoadPlan road = accessRoadPlan(origin, id);
        int footprint = origin.getX() + structureWidth(id) - 1;
        return road == null ? footprint : Math.max(footprint, road.maxX());
    }

    static int descriptorMinZ(BlockPos origin, String id) {
        AccessRoadPlan road = accessRoadPlan(origin, id);
        return road == null ? origin.getZ() : Math.min(origin.getZ(), road.minZ());
    }

    static int descriptorMaxZ(BlockPos origin, String id) {
        AccessRoadPlan road = accessRoadPlan(origin, id);
        int footprint = origin.getZ() + structureWidth(id) - 1;
        return road == null ? footprint : Math.max(footprint, road.maxZ());
    }

    /** Places one chunk slice without requesting any neighboring chunks. */
    public static void buildNaturalStructureChunk(World world, BlockPos origin, String id,
            int chunkX, int chunkZ, boolean initialize) {
        PlacementBounds previous = PLACEMENT.get();
        PLACEMENT.set(new PlacementBounds(chunkX * 16, chunkZ * 16,
            chunkX * 16 + 15, chunkZ * 16 + 15, initialize));
        try {
            buildStructure(world, origin, id);
            buildAccessRoad(world, origin, id);
        } finally {
            if (previous == null) PLACEMENT.remove(); else PLACEMENT.set(previous);
        }
    }

    /** Dispatches the same builders used by natural generation into a creative review gallery. */
    public static boolean buildShowcaseStructure(World world, BlockPos origin, String id) {
        if ("industrial_city_variant_b".equals(id)) {
            int minChunkX = Math.floorDiv(origin.getX(), 16);
            int minChunkZ = Math.floorDiv(origin.getZ(), 16);
            int maxChunkX = Math.floorDiv(origin.getX() + structureWidth(id) - 1, 16);
            int maxChunkZ = Math.floorDiv(origin.getZ() + structureWidth(id) - 1, 16);
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
                    buildNaturalStructureChunk(world, origin, "industrial_city", chunkX, chunkZ,
                        chunkX == minChunkX && chunkZ == minChunkZ);
            return true;
        }
        return buildStructure(world, origin, id);
    }

    private static boolean buildStructure(World world, BlockPos origin, String id) {
        if ("primitive_settlement".equals(id)) {
            buildPrimitiveSettlement(world, origin);
        } else if ("militia_outpost".equals(id)) {
            buildMilitiaOutpost(world, origin);
        } else if ("industrial_city".equals(id) || "industrial_city_variant_b".equals(id)) {
            buildIndustrialCity(world, origin);
        } else if ("abandoned_factory".equals(id)) {
            AbandonedFactoryWorldGenerator.buildShell(world, origin);
            if (allowSideEffects()) {
                if (world.provider.getDimension() == 0) {
                    FactionSystem.spawnCitizen(world, origin.getX() + 4.5, origin.getY() + 2,
                        origin.getZ() + 17.5, "ashline_raiders", "raider", "armaments", "Ashline Lookout", 3);
                    FactionSystem.spawnCitizen(world, origin.getX() + 12.5, origin.getY() + 2,
                        origin.getZ() + 7.5, "ashline_raiders", "raider", "armaments", "Ashline Salvager", 3);
                    FactionSystem.spawnCitizen(world, origin.getX() + 10.5, origin.getY() + 2,
                        origin.getZ() + 13.5, "territorial_militia", "trader", "armaments", "Militia Fence", 4);
                } else {
                    PlanetaryEcologySystem.spawnSpacePirate(world, origin.getX() + 4.5,
                        origin.getY() + 2, origin.getZ() + 17.5);
                    PlanetaryEcologySystem.spawnSpacePirate(world, origin.getX() + 12.5,
                        origin.getY() + 2, origin.getZ() + 7.5);
                }
            }
        } else if (id.startsWith("factory_")) {
            String specialty = id.substring("factory_".length());
            if (!java.util.Arrays.asList(FACTORY_SPECIALTIES).contains(specialty)) return false;
            buildOperationalFactory(world, origin, specialty);
        } else if ("regional_road".equals(id)) {
            buildRoadShowcase(world, origin);
        } else if ("apollo_11_memorial".equals(id)) {
            buildLunarHeritageMemorial(world, origin.add(4, 0, 7),
                "Apollo 11", "1969-07-20", 0.67408D, 23.47297D);
        } else {
            return false;
        }
        return true;
    }

    public static void placeShowcaseLabel(World world, BlockPos pos, String label) {
        set(world, pos.down(), Blocks.STONEBRICK.getDefaultState());
        set(world, pos, Blocks.STANDING_SIGN.getDefaultState());
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof net.minecraft.tileentity.TileEntitySign)) return;
        net.minecraft.tileentity.TileEntitySign sign = (net.minecraft.tileentity.TileEntitySign) tile;
        String[] words = label.split(" ");
        StringBuilder line = new StringBuilder();
        int row = 0;
        for (String word : words) {
            if (line.length() > 0 && line.length() + word.length() + 1 > 15) {
                sign.signText[row++] = new net.minecraft.util.text.TextComponentString(line.toString());
                line.setLength(0);
                if (row == 4) break;
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (row < 4 && line.length() > 0)
            sign.signText[row] = new net.minecraft.util.text.TextComponentString(line.toString());
        sign.markDirty();
    }

    private static void buildRoadShowcase(World world, BlockPos origin) {
        layRegionalRoad(world, origin.getX(), origin.getZ(), true,
            Blocks.DOUBLE_STONE_SLAB.getDefaultState(), origin.getY());
    }

    private static void buildLunarHeritageMemorial(World world, BlockPos ground, String siteName,
            String date, double latitude, double longitude) {
        for (int dx = -2; dx <= 5; dx++) for (int dz = -2; dz <= 2; dz++)
            set(world, ground.add(dx, 0, dz), Blocks.STONE.getDefaultState());
        for (int y = 1; y <= 5; y++) set(world, ground.add(0, y, 0), Blocks.IRON_BARS.getDefaultState());
        for (int dx = 1; dx <= 4; dx++) for (int dy = 0; dy <= 2; dy++) {
            int color = dx <= 2 && dy >= 1 ? 11 : ((dy & 1) == 0 ? 14 : 0);
            set(world, ground.add(dx, 5 - dy, 0), Blocks.WOOL.getStateFromMeta(color));
        }
        BlockPos plaque = ground.add(-1, 1, 0);
        set(world, plaque, Blocks.STANDING_SIGN.getDefaultState());
        TileEntity plaqueTile = world.getTileEntity(plaque);
        if (plaqueTile instanceof net.minecraft.tileentity.TileEntitySign) {
            net.minecraft.tileentity.TileEntitySign sign = (net.minecraft.tileentity.TileEntitySign) plaqueTile;
            sign.signText[0] = new net.minecraft.util.text.TextComponentString(siteName);
            sign.signText[1] = new net.minecraft.util.text.TextComponentString(date);
            sign.signText[2] = new net.minecraft.util.text.TextComponentString(
                String.format(java.util.Locale.ROOT, "%.3f %.3f", latitude, longitude));
            sign.signText[3] = new net.minecraft.util.text.TextComponentString("Heritage Site");
            sign.markDirty();
        }
    }

    private static void platform(World world, BlockPos origin, int width,
            IBlockState floor, int clearance) {
        for (int x = 0; x < width; x++) for (int z = 0; z < width; z++) {
            set(world, origin.add(x, 0, z), Blocks.STONE.getDefaultState());
            set(world, origin.add(x, 1, z), floor);
            for (int y = 2; y <= clearance; y++) set(world, origin.add(x, y, z), Blocks.AIR.getDefaultState());
        }
    }

    private static Random layoutRandom(World world, BlockPos origin, long salt) {
        long seed = world.getSeed() ^ salt;
        seed ^= (long) origin.getX() * 341873128712L;
        seed ^= (long) origin.getZ() * 132897987541L;
        return new Random(seed);
    }

    public static long structureVariationSignature(World world, BlockPos origin, String id) {
        long salt = "industrial_city".equals(id) ? 0x494E445553545259L : id.hashCode();
        return layoutRandom(world, origin, salt).nextLong();
    }

    private static void cropPlot(World world, BlockPos origin, int startX, int startZ,
            boolean potatoes) {
        for (int x = startX; x < startX + 7; x++) for (int z = startZ; z < startZ + 10; z++) {
            boolean irrigation = x == startX + 3;
            set(world, origin.add(x, 1, z), irrigation ? Blocks.WATER.getDefaultState()
                : Blocks.FARMLAND.getDefaultState());
            if (!irrigation) set(world, origin.add(x, 2, z), potatoes
                ? Blocks.POTATOES.getDefaultState() : Blocks.WHEAT.getDefaultState());
        }
    }

    private static void cottage(World world, BlockPos origin, IBlockState wall,
            int height, boolean doorNorth) {
        building(world, origin, 7, 6, wall, height);
        int doorZ = doorNorth ? 5 : 0;
        set(world, origin.add(3, 1, doorZ), Blocks.AIR.getDefaultState());
        set(world, origin.add(3, 2, doorZ), Blocks.AIR.getDefaultState());
        set(world, origin.add(1, 2, doorNorth ? 0 : 5), Blocks.GLASS_PANE.getDefaultState());
        set(world, origin.add(5, 2, doorNorth ? 0 : 5), Blocks.GLASS_PANE.getDefaultState());
        set(world, origin.add(2, 1, 2), Blocks.BED.getDefaultState());
        set(world, origin.add(5, 2, 2), Blocks.TORCH.getDefaultState());
    }

    private static boolean isCityRoad(int x, int z) {
        for (int road : new int[] {12, 25, 38, 51, 64})
            if (x == road || x == road + 1 || z == road || z == road + 1) return true;
        return false;
    }

    private static void cityBuilding(World world, BlockPos origin, int width, int depth,
            IBlockState wall, int height, int facade) {
        building(world, origin, width, depth, wall, height);
        int door = Math.max(2, width / 2);
        set(world, origin.add(door, 1, 0), Blocks.AIR.getDefaultState());
        set(world, origin.add(door, 2, 0), Blocks.AIR.getDefaultState());
        for (int y = 3; y < height; y += 3) {
            for (int x = 2; x < width - 1; x += facade % 2 == 0 ? 3 : 2) {
                set(world, origin.add(x, y, 0), Blocks.GLASS_PANE.getDefaultState());
                set(world, origin.add(x, y, depth - 1), Blocks.GLASS_PANE.getDefaultState());
            }
        }
        if ((facade & 2) != 0) {
            for (int x = 1; x < width - 1; x += 2)
                set(world, origin.add(x, height + 1, depth / 2), Blocks.IRON_BARS.getDefaultState());
        }
        // Roof furniture and occasional inset penthouses break up the former
        // field of plain cuboids while remaining deterministic per lot.
        if (height >= 9 && (facade & 1) != 0) {
            building(world, origin.add(2, height + 1, 2), Math.max(4, width - 4),
                Math.max(4, depth - 4), wall, 2);
        } else {
            set(world, origin.add(2, height + 1, 2), Blocks.IRON_BLOCK.getDefaultState());
            set(world, origin.add(3, height + 1, 2), Blocks.IRON_BLOCK.getDefaultState());
            set(world, origin.add(2, height + 2, 2), Blocks.IRON_BARS.getDefaultState());
        }
        for (int x = 0; x < width; x += 2) {
            set(world, origin.add(x, height + 1, 0), Blocks.STONE_SLAB.getDefaultState());
            set(world, origin.add(x, height + 1, depth - 1), Blocks.STONE_SLAB.getDefaultState());
        }
    }

    private static void buildSpecialtyFactory(World world, BlockPos origin, String specialty,
            Random layout) {
        if ("steel".equals(specialty)) {
            building(world, origin.add(2, 1, 4), 21, 19, Blocks.IRON_BLOCK.getDefaultState(), 9);
            building(world, origin.add(22, 1, 8), 7, 13, Blocks.STONEBRICK.getDefaultState(), 6);
            factoryWindowsAndDoor(world, origin, 2, 4, 21, 19);
            chimney(world, origin.add(5, 10, 19), 5 + layout.nextInt(3));
            chimney(world, origin.add(18, 10, 19), 4 + layout.nextInt(3));
            // Alternating raised roof monitors give the foundry a sawtooth silhouette.
            for (int x = 4; x <= 20; x += 4) for (int z = 6; z <= 20; z++) {
                set(world, origin.add(x, 10, z), Blocks.IRON_BLOCK.getDefaultState());
                set(world, origin.add(x + 1, 10, z), Blocks.GLASS.getDefaultState());
                if ((x & 4) == 0) set(world, origin.add(x, 11, z), Blocks.IRON_BLOCK.getDefaultState());
            }
        } else if ("electronics".equals(specialty)) {
            building(world, origin.add(2, 1, 4), 18, 18, Blocks.QUARTZ_BLOCK.getDefaultState(), 8);
            building(world, origin.add(19, 1, 8), 10, 14, Blocks.IRON_BLOCK.getDefaultState(), 6);
            building(world, origin.add(5, 1, 22), 11, 7, Blocks.QUARTZ_BLOCK.getDefaultState(), 4);
            factoryWindowsAndDoor(world, origin, 2, 4, 18, 18);
            antenna(world, origin.add(26, 8, 15), 6 + layout.nextInt(3));
            for (int x = 5; x <= 17; x += 3) for (int z : new int[] {5,20})
                set(world, origin.add(x, 4, z), Blocks.GLASS_PANE.getDefaultState());
            rooftopUnits(world, origin, 5, 8, layout.nextInt(3));
        } else if ("fuel".equals(specialty)) {
            building(world, origin.add(2, 1, 4), 20, 18, Blocks.BRICK_BLOCK.getDefaultState(), 8);
            building(world, origin.add(3, 1, 23), 12, 6, Blocks.STONEBRICK.getDefaultState(), 4);
            factoryWindowsAndDoor(world, origin, 2, 4, 20, 18);
            for (int z : new int[] {5,13,21}) tank(world, origin.add(24, 1, z), 5 + layout.nextInt(2));
            chimney(world, origin.add(5, 9, 18), 6);
            for (int x = 16; x <= 20; x++) for (int z = 7; z <= 18; z++)
                if (x == 16 || x == 20) set(world, origin.add(x, 10, z), Blocks.IRON_BARS.getDefaultState());
        } else if ("armaments".equals(specialty)) {
            building(world, origin.add(6, 1, 5), 19, 18, Blocks.STONEBRICK.getDefaultState(), 7);
            building(world, origin.add(4, 1, 23), 11, 6, Blocks.IRON_BLOCK.getDefaultState(), 4);
            factoryWindowsAndDoor(world, origin, 6, 5, 19, 18);
            tower(world, origin.add(2, 1, 3), 7);
            tower(world, origin.add(24, 1, 3), 7);
            tower(world, origin.add(2, 1, 24), 7);
            tower(world, origin.add(24, 1, 24), 7);
            for (int x = 1; x < 30; x++) {
                set(world, origin.add(x, 2, 1), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(x, 2, 29), Blocks.IRON_BARS.getDefaultState());
            }
        } else {
            building(world, origin.add(3, 1, 5), 19, 18, Blocks.QUARTZ_BLOCK.getDefaultState(), 8);
            building(world, origin.add(21, 1, 9), 8, 14, Blocks.GLASS.getDefaultState(), 6);
            building(world, origin.add(5, 1, 23), 12, 6, Blocks.QUARTZ_BLOCK.getDefaultState(), 4);
            factoryWindowsAndDoor(world, origin, 3, 5, 19, 18);
            antenna(world, origin.add(25, 8, 16), 7 + layout.nextInt(2));
            for (int x = 5; x < 20; x++) for (int z = 7; z < 21; z++)
                if ((x + z) % 4 == 0) set(world, origin.add(x, 9, z), Blocks.GLASS.getDefaultState());
            rooftopUnits(world, origin, 7, 9, layout.nextInt(3));
        }
    }

    private static void factoryWindowsAndDoor(World world, BlockPos origin,
            int startX, int startZ, int width, int depth) {
        int doorX = startX + width / 2;
        set(world, origin.add(doorX, 2, startZ), Blocks.AIR.getDefaultState());
        set(world, origin.add(doorX, 3, startZ), Blocks.AIR.getDefaultState());
        for (int x = startX + 2; x < startX + width - 1; x += 4) {
            set(world, origin.add(x, 4, startZ), Blocks.GLASS_PANE.getDefaultState());
            set(world, origin.add(x, 4, startZ + depth - 1), Blocks.GLASS_PANE.getDefaultState());
        }
    }

    private static void chimney(World world, BlockPos origin, int height) {
        for (int y = 0; y < height; y++) for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++)
            set(world, origin.add(x, y, z), Blocks.BRICK_BLOCK.getDefaultState());
        set(world, origin.add(0, height, 0), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(1, height, 1), Blocks.IRON_BARS.getDefaultState());
    }

    private static void antenna(World world, BlockPos origin, int height) {
        for (int y = 0; y < height; y++) set(world, origin.add(0, y, 0), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(1, height - 2, 0), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(-1, height - 2, 0), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(0, height - 2, 1), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(0, height - 2, -1), Blocks.IRON_BARS.getDefaultState());
        set(world, origin.add(0, height, 0), Blocks.REDSTONE_TORCH.getDefaultState());
    }

    private static void tank(World world, BlockPos origin, int height) {
        for (int y = 0; y <= height; y++) for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++) {
            boolean shell = y == 0 || y == height || x == 0 || x == 4 || z == 0 || z == 4;
            if (shell && !((x == 0 || x == 4) && (z == 0 || z == 4)))
                set(world, origin.add(x, y, z), Blocks.IRON_BLOCK.getDefaultState());
        }
    }

    private static void rooftopUnits(World world, BlockPos origin, int startX, int y, int variant) {
        for (int index = 0; index < 3 + variant; index++) {
            int x = startX + index * 3;
            for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++)
                set(world, origin.add(x + dx, y + 1, 10 + dz), Blocks.IRON_BLOCK.getDefaultState());
            set(world, origin.add(x, y + 2, 10), Blocks.IRON_BARS.getDefaultState());
        }
    }

    private static void buildRegionalRoad(World world, int chunkX, int chunkZ, double distance) {
        boolean northSouth = Math.floorMod(chunkX, 8) == 0;
        boolean eastWest = Math.floorMod(chunkZ, 8) == 0;
        if (!northSouth && !eastWest) return;
        IBlockState surface = distance < 1700 ? Blocks.DIRT.getDefaultState()
            : Blocks.DOUBLE_STONE_SLAB.getDefaultState();
        if (northSouth)
            layRegionalRoad(world, chunkX * 16, chunkZ * 16, true, surface, -1);
        if (eastWest)
            layRegionalRoad(world, chunkX * 16, chunkZ * 16, false, surface, -1);
        CivilizationStructureData.scheduleRoadRepair(world, chunkX, chunkZ);
    }

    /** Restores a road once delayed terrain generators such as AE2 meteorites have finished. */
    static void repairRegionalRoad(World world, int chunkX, int chunkZ) {
        BlockPos spawn = world.getSpawnPoint();
        long x = (long) chunkX * 16L + 8L - spawn.getX();
        long z = (long) chunkZ * 16L + 8L - spawn.getZ();
        double distance = Math.sqrt((double) (x * x + z * z));
        boolean northSouth = Math.floorMod(chunkX, 8) == 0;
        boolean eastWest = Math.floorMod(chunkZ, 8) == 0;
        if (!northSouth && !eastWest) return;
        IBlockState surface = distance < 1700 ? Blocks.DIRT.getDefaultState()
            : Blocks.DOUBLE_STONE_SLAB.getDefaultState();
        if (northSouth)
            layRegionalRoad(world, chunkX * 16, chunkZ * 16, true, surface, -1);
        if (eastWest)
            layRegionalRoad(world, chunkX * 16, chunkZ * 16, false, surface, -1);
    }

    /** A short, persisted approach joins a structure entrance to the closest arterial. */
    static AccessRoadPlan accessRoadPlan(BlockPos origin, String id) {
        boolean settlement = "primitive_settlement".equals(id);
        boolean city = "industrial_city".equals(id) || "industrial_city_variant_b".equals(id);
        if (!settlement && !city) return null;
        int width = structureWidth(id);
        int lanes = settlement ? 3 : 2;
        // Primitive streets are centered on local 15 (lanes 14..16); city
        // streets occupy local 38..39.
        int street = settlement ? 14 : 38;
        IBlockState surface = settlement ? Blocks.DIRT.getDefaultState()
            : Blocks.DOUBLE_STONE_SLAB.getDefaultState();
        int[] route = GameplayRules.closestRegionalRoadApproach(origin.getX(), origin.getZ(),
            width, street, lanes);
        return new AccessRoadPlan(route[0] == 1, route[1], route[2], route[3], route[4], surface);
    }

    private static void buildAccessRoad(World world, BlockPos origin, String id) {
        AccessRoadPlan plan = accessRoadPlan(origin, id);
        if (plan == null) return;
        int low = Math.min(plan.start, plan.end);
        int high = Math.max(plan.start, plan.end);
        for (int step = low; step <= high; step++) for (int lane = 0; lane < plan.lanes; lane++) {
            int x = plan.horizontal ? step : plan.fixed + lane;
            int z = plan.horizontal ? plan.fixed + lane : step;
            layTerrainRoadPoint(world, x, z, plan.surface);
        }
    }

    /** Loads every approach chunk so the locator can validate the complete natural connection. */
    static void prepareAccessRoad(World world, BlockPos origin, String id) {
        AccessRoadPlan plan = accessRoadPlan(origin, id);
        if (plan == null) return;
        int minChunkX = Math.floorDiv(plan.minX(), 16);
        int maxChunkX = Math.floorDiv(plan.maxX(), 16);
        int minChunkZ = Math.floorDiv(plan.minZ(), 16);
        int maxChunkZ = Math.floorDiv(plan.maxZ(), 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
                world.getChunkFromChunkCoords(chunkX, chunkZ);
    }

    /** Returns null only when the structure street, approach and arterial are continuous. */
    static String validateAccessRoad(World world, BlockPos origin, String id) {
        AccessRoadPlan plan = accessRoadPlan(origin, id);
        if (plan == null) return "unsupported_structure_" + id;
        Block expectedApproach = plan.surface.getBlock();
        Block expectedStreet = "primitive_settlement".equals(id)
            ? Blocks.GRAVEL : Blocks.DOUBLE_STONE_SLAB;
        int low = Math.min(plan.start, plan.end);
        int high = Math.max(plan.start, plan.end);
        for (int step = low; step <= high; step++) for (int lane = 0; lane < plan.lanes; lane++) {
            int x = plan.horizontal ? step : plan.fixed + lane;
            int z = plan.horizontal ? plan.fixed + lane : step;
            if (terrainSurfaceBlock(world, x, z) != expectedApproach)
                return "missing_approach_at_" + x + "_" + z;
        }
        boolean towardPositive = plan.end > plan.start;
        for (int lane = 0; lane < plan.lanes; lane++) {
            int x = plan.horizontal ? plan.start + (towardPositive ? -1 : 1) : plan.fixed + lane;
            int z = plan.horizontal ? plan.fixed + lane : plan.start + (towardPositive ? -1 : 1);
            if (terrainSurfaceBlock(world, x, z) != expectedStreet)
                return "missing_structure_street_at_" + x + "_" + z;
        }
        int arterialOffset = plan.lanes + 2;
        int arterialX = plan.horizontal ? plan.end
            : plan.fixed + Math.min(1, plan.lanes - 1);
        int arterialZ = plan.horizontal ? plan.fixed + Math.min(1, plan.lanes - 1)
            : plan.end;
        if (plan.horizontal) arterialZ += arterialOffset;
        else arterialX += arterialOffset;
        if (terrainSurfaceBlock(world, arterialX, arterialZ) != expectedApproach)
            return "missing_arterial_at_" + arterialX + "_" + arterialZ;
        return null;
    }

    private static Block terrainSurfaceBlock(World world, int x, int z) {
        BlockPos road = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).down();
        while (road.getY() > 2 && isRoadFoliage(world.getBlockState(road).getMaterial()))
            road = road.down();
        return world.getBlockState(road).getBlock();
    }

    private static void layTerrainRoadPoint(World world, int x, int z, IBlockState surface) {
        BlockPos column = new BlockPos(x, 64, z);
        if (!insidePlacement(column)) return;
        BlockPos road = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).down();
        while (road.getY() > 2 && isRoadFoliage(world.getBlockState(road).getMaterial()))
            road = road.down();
        if (road.getY() <= 52 || road.getY() >= 120) return;
        world.setBlockState(road, surface, 2);
        for (int clearance = 1; clearance <= 8; clearance++)
            world.setBlockToAir(road.up(clearance));
    }

    static final class AccessRoadPlan {
        final boolean horizontal;
        final int start, end, fixed, lanes;
        final IBlockState surface;

        AccessRoadPlan(boolean horizontal, int start, int end, int fixed,
                int lanes, IBlockState surface) {
            this.horizontal = horizontal;
            this.start = start;
            this.end = end;
            this.fixed = fixed;
            this.lanes = lanes;
            this.surface = surface;
        }

        int minX() { return horizontal ? Math.min(start, end) : fixed; }
        int maxX() { return horizontal ? Math.max(start, end) : fixed + lanes - 1; }
        int minZ() { return horizontal ? fixed : Math.min(start, end); }
        int maxZ() { return horizontal ? fixed + lanes - 1 : Math.max(start, end); }
        int networkCoordinate() { return end; }
    }

    private static void layRegionalRoad(World world, int startX, int startZ,
            boolean northSouth, IBlockState surface, int fixedSurfaceY) {
        for (int step = 0; step < 16; step++) for (int lane = -1; lane <= 1; lane++) {
            int x = startX + (northSouth ? 8 + lane : step);
            int z = startZ + (northSouth ? step : 8 + lane);
            if (fixedSurfaceY >= 0) {
                BlockPos road = new BlockPos(x, fixedSurfaceY, z);
                world.setBlockState(road, surface, 2);
                world.setBlockToAir(road.up());
                world.setBlockToAir(road.up(2));
            } else {
                BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
                BlockPos road = top.down();
                while (road.getY() > 2 && isRoadFoliage(world.getBlockState(road).getMaterial()))
                    road = road.down();
                if (road.getY() > 52 && road.getY() < 120) {
                    world.setBlockState(road, surface, 2);
                    for (int clearance = 1; clearance <= 8; clearance++)
                        world.setBlockToAir(road.up(clearance));
                }
            }
        }
    }

    private static boolean isRoadFoliage(Material material) {
        return material == Material.LEAVES || material == Material.WOOD
            || material == Material.PLANTS || material == Material.VINE
            || material == Material.SNOW || material == Material.CACTUS
            || material == Material.WEB;
    }

    private static void installUtilitySpine(World world, BlockPos origin, boolean pipes,
            int cableZ, int pipeX, int solarY) {
        Block cableBlock = optionalBlock("ic2:blockcable");
        set(world, origin.add(2, solarY, cableZ),
            IndustrialCivilizationCore.ENVIRONMENTAL_SOLAR_ARRAY.getDefaultState());
        if (cableBlock instanceof BlockCable) {
            BlockCable cable = (BlockCable) cableBlock;
            // Roof-mounted factory arrays need a vertical riser to the interior spine.
            for (int y = 2; y < solarY; y++)
                placeCable(world, origin.add(2, y, cableZ), cable);
            for (int x = 3; x <= 12; x++) placeCable(world, origin.add(x, 2, cableZ), cable);
            // Wall-height service outlets, with adjacent air reserved for player machines.
            for (int x : new int[] {4, 7, 10, 12}) {
                // An elevated pipe may use this exact crossing cell. The
                // horizontal cable below remains continuous either way.
                if (!(pipes && x == pipeX && cableZ >= 3 && cableZ <= 11))
                    placeCable(world, origin.add(x, 3, cableZ), cable);
                set(world, origin.add(x, 3, cableZ - 1), Blocks.AIR.getDefaultState());
            }
        }
        if (pipes && BCTransportItems.pipeItemCobble != null) {
            // BuildCraft holders are only containers. onPlacedBy with a real
            // pipe item creates the Pipe payload required for rendering and transport.
            for (int z = 3; z <= 11; z++)
                placeItemPipe(world, origin.add(pipeX, 3, z));
        }
    }

    private static void placeCable(World world, BlockPos pos, BlockCable cable) {
        if (!insidePlacement(pos)) return;
        // IC2 Classic deliberately returns null from getStateFromStack; its
        // default state is the supported state that instantiates TileEntityCable.
        world.setBlockState(pos, cable.getDefaultState(), 3);
    }

    private static void placeItemPipe(World world, BlockPos pos) {
        if (!insidePlacement(pos)) return;
        Block holder = optionalBlock("buildcrafttransport:pipe_holder");
        if (holder == Blocks.AIR) return;
        IBlockState state = holder.getDefaultState();
        world.setBlockState(pos, state, 3);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TilePipeHolder) {
            if (!(world instanceof WorldServer)) return;
            ((TilePipeHolder) tile).onPlacedBy(FakePlayerFactory.getMinecraft((WorldServer) world),
                new ItemStack(BCTransportItems.pipeItemCobble));
            tile.markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private static void installLargeFactoryUtilities(World world, BlockPos origin) {
        Block cableBlock = optionalBlock("ic2:blockcable");
        set(world, origin.add(4, 11, 8),
            IndustrialCivilizationCore.ENVIRONMENTAL_SOLAR_ARRAY.getDefaultState());
        if (cableBlock instanceof BlockCable) {
            BlockCable cable = (BlockCable) cableBlock;
            for (int y = 2; y < 11; y++) placeCable(world, origin.add(4, y, 8), cable);
            for (int x = 5; x <= 26; x++) placeCable(world, origin.add(x, 2, 8), cable);
            for (int z = 9; z <= 12; z++) placeCable(world, origin.add(15, 2, z), cable);
        }
        if (BCTransportItems.pipeItemCobble != null) {
            for (int z = 7; z <= 22; z++) placeItemPipe(world, origin.add(7, 3, z));
            for (int x = 8; x <= 22; x++) placeItemPipe(world, origin.add(x, 3, 13));
            placeItemPipe(world, origin.add(10, 2, 13));
            placeItemPipe(world, origin.add(20, 2, 13));
        }
    }

    private static void installLargeCityUtilities(World world, BlockPos origin) {
        Block cableBlock = optionalBlock("ic2:blockcable");
        set(world, origin.add(3, 12, 39),
            IndustrialCivilizationCore.ENVIRONMENTAL_SOLAR_ARRAY.getDefaultState());
        if (cableBlock instanceof BlockCable) {
            BlockCable cable = (BlockCable) cableBlock;
            for (int y = 2; y < 12; y++) placeCable(world, origin.add(3, y, 39), cable);
            for (int x = 4; x <= 76; x++) placeCable(world, origin.add(x, 2, 39), cable);
            for (int z = 33; z <= 38; z++) placeCable(world, origin.add(32, 2, z), cable);
        }
        if (BCTransportItems.pipeItemCobble != null)
            for (int z = 2; z <= 76; z++) placeItemPipe(world, origin.add(39, 3, z));
    }

    private static void connectFactoryUtilities(World world, BlockPos origin) {
        Block cableBlock = optionalBlock("ic2:blockcable");
        if (cableBlock instanceof BlockCable) {
            // Branch from the z=3 service spine to the face of the machine at z=7.
            for (int z = 4; z <= 6; z++)
                placeCable(world, origin.add(7, 2, z), (BlockCable) cableBlock);
        }
        if (BCTransportItems.pipeItemCobble != null) {
            // Elevated cross-aisle ties the main pipe to drops beside both chests.
            for (int x = 4; x <= 10; x++) placeItemPipe(world, origin.add(x, 3, 7));
            placeItemPipe(world, origin.add(4, 2, 7));
            placeItemPipe(world, origin.add(10, 2, 7));
        }
    }

    private static void connectCityPower(World world, BlockPos origin) {
        Block cableBlock = optionalBlock("ic2:blockcable");
        if (!(cableBlock instanceof BlockCable)) return;
        // Branch from the street spine to the east face of the cargo controller.
        placeCable(world, origin.add(8, 2, 6), (BlockCable) cableBlock);
        placeCable(world, origin.add(8, 2, 5), (BlockCable) cableBlock);
    }

    /** Returns a concrete problem for malformed showcase utilities, otherwise null. */
    public static String validateShowcaseUtilitySpine(World world, BlockPos origin, String id) {
        final boolean factory = id.startsWith("factory_");
        if (!factory && !"industrial_city".equals(id) && !"industrial_city_variant_b".equals(id)) return null;
        int cableZ = factory ? 8 : 39;
        int pipeX = factory ? 7 : 39;
        int cableStart = factory ? 5 : 4;
        int cableEnd = factory ? 26 : 76;
        int pipeStart = factory ? 7 : 2;
        int pipeEnd = factory ? 22 : 76;
        for (int x = cableStart; x <= cableEnd; x++) {
            TileEntity tile = world.getTileEntity(origin.add(x, 2, cableZ));
            if (!(tile instanceof TileEntityCable)) return "missing_ic2_cable_tile_at_x_" + x;
        }
        for (int z = pipeStart; z <= pipeEnd; z++) {
            TileEntity tile = world.getTileEntity(origin.add(pipeX, 3, z));
            if (!(tile instanceof TilePipeHolder)) return "missing_buildcraft_pipe_tile_at_z_" + z;
            if (((TilePipeHolder) tile).getPipe() == null)
                return "empty_buildcraft_pipe_holder_at_z_" + z;
        }
        if (factory) {
            if (!(world.getTileEntity(origin.add(4, 11, 8))
                    instanceof TileEnvironmentalSolarArray))
                return "factory_rooftop_solar_missing";
            for (int y = 2; y <= 10; y++)
                if (!(world.getTileEntity(origin.add(4, y, 8)) instanceof TileEntityCable))
                    return "factory_solar_riser_missing_at_y_" + y;
            if (!(world.getTileEntity(origin.add(15, 2, 13)) instanceof TileIndustrialMachine))
                return "factory_machine_missing_after_utility_generation";
            if (world.getBlockState(origin.add(11, 2, 13)).getBlock() != Blocks.CHEST
                    || world.getBlockState(origin.add(19, 2, 13)).getBlock() != Blocks.CHEST)
                return "factory_storage_replaced_by_utility";
            for (int z = 9; z <= 12; z++)
                if (!(world.getTileEntity(origin.add(15, 2, z)) instanceof TileEntityCable))
                    return "factory_power_branch_missing_at_z_" + z;
            for (int x = 8; x <= 22; x++) {
                TileEntity tile = world.getTileEntity(origin.add(x, 3, 13));
                if (!(tile instanceof TilePipeHolder) || ((TilePipeHolder) tile).getPipe() == null)
                    return "factory_pipe_cross_aisle_missing_at_x_" + x;
            }
            for (int x : new int[] {10, 20}) {
                TileEntity tile = world.getTileEntity(origin.add(x, 2, 13));
                if (!(tile instanceof TilePipeHolder) || ((TilePipeHolder) tile).getPipe() == null)
                    return "factory_chest_pipe_drop_missing_at_x_" + x;
            }
        } else {
            if (!(world.getTileEntity(origin.add(3, 12, 39)) instanceof TileEnvironmentalSolarArray))
                return "city_solar_missing";
            for (int z = 33; z <= 38; z++)
                if (!(world.getTileEntity(origin.add(32, 2, z)) instanceof TileEntityCable))
                    return "city_controller_power_branch_missing_at_z_" + z;
            if (!(world.getTileEntity(origin.add(32, 2, 32)) instanceof TileIndustrialMachine))
                return "city_controller_missing";
        }
        return null;
    }

    public static boolean showcaseFactoryHasPower(World world, BlockPos origin) {
        TileEntity tile = world.getTileEntity(origin.add(15, 2, 13));
        return tile instanceof TileIndustrialMachine
            && ((TileIndustrialMachine) tile).getEnergyStored() > 0;
    }

    private static Block optionalBlock(String id) {
        Block block = net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValue(
            new net.minecraft.util.ResourceLocation(id));
        return block == null ? Blocks.AIR : block;
    }

    private static void hut(World world, BlockPos origin, IBlockState wall) {
        building(world, origin, 5, 5, wall, 3);
        set(world, origin.add(2, 1, 0), Blocks.AIR.getDefaultState());
        set(world, origin.add(2, 2, 0), Blocks.AIR.getDefaultState());
        set(world, origin.add(1, 2, 1), Blocks.TORCH.getDefaultState());
    }

    private static void tower(World world, BlockPos origin) { tower(world, origin, 6); }

    private static void tower(World world, BlockPos origin, int height) {
        building(world, origin, 5, 5, Blocks.STONEBRICK.getDefaultState(), height);
        for (int y = 1; y < height; y++) set(world, origin.add(1, y, 1), Blocks.LADDER.getDefaultState());
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++)
            if (x == 0 || x == 4 || z == 0 || z == 4)
                if (((x + z) & 1) == 0) set(world, origin.add(x, height + 1, z), Blocks.STONEBRICK.getDefaultState());
    }

    private static void industrialShell(World world, BlockPos origin, String specialty) {
        IBlockState wall = "fuel".equals(specialty) ? Blocks.BRICK_BLOCK.getDefaultState()
            : Blocks.IRON_BLOCK.getDefaultState();
        building(world, origin, 13, 13, wall, 5);
        for (int x = 2; x < 11; x += 3) {
            set(world, origin.add(x, 3, 0), Blocks.GLASS_PANE.getDefaultState());
            set(world, origin.add(x, 3, 12), Blocks.GLASS_PANE.getDefaultState());
        }
        set(world, origin.add(6, 1, 0), Blocks.AIR.getDefaultState());
        set(world, origin.add(6, 2, 0), Blocks.AIR.getDefaultState());
    }

    private static void building(World world, BlockPos origin, int width, int depth,
            IBlockState wall, int height) {
        for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) {
            for (int y = 0; y <= height; y++) {
                boolean shell = y == 0 || y == height || x == 0 || z == 0
                    || x == width - 1 || z == depth - 1;
                set(world, origin.add(x, y, z), shell ? wall : Blocks.AIR.getDefaultState());
            }
        }
    }

    /** Applies exactly one paid construction stage; no random upgrade roll. */
    public static void applySettlementUpgrade(World world, BlockPos origin, int tier) {
        if (tier == 1) {
            // Timber market hall and material receiving yard.
            building(world, origin.add(32, 1, 2), 10, 9, Blocks.PLANKS.getDefaultState(), 5);
            set(world, origin.add(37, 2, 2), Blocks.AIR.getDefaultState());
            set(world, origin.add(35, 2, 6), Blocks.CHEST.getDefaultState());
            set(world, origin.add(39, 2, 6), Blocks.CRAFTING_TABLE.getDefaultState());
        } else if (tier == 2) {
            // Masonry works hall and first electrified service spine.
            building(world, origin.add(32, 1, 14), 13, 10, Blocks.STONEBRICK.getDefaultState(), 6);
            set(world, origin.add(38, 2, 14), Blocks.AIR.getDefaultState());
            set(world, origin.add(36, 2, 19), Blocks.FURNACE.getDefaultState());
            set(world, origin.add(40, 2, 19), IndustrialCivilizationCore.ELECTRIC_FABRICATOR.getDefaultState());
            // Run outside the front wall instead of through the works hall.
            installUtilitySpine(world, origin.add(31, 0, 24), false, 0, 0, 2);
        } else if (tier == 3) {
            // Civic exchange: the final primitive-settlement upgrade consumes
            // steel/electronics/fuel/credits and joins the nation cargo grid.
            building(world, origin.add(17, 1, 32), 13, 11, Blocks.BRICK_BLOCK.getDefaultState(), 8);
            set(world, origin.add(23, 2, 32), Blocks.AIR.getDefaultState());
            BlockPos exchange = origin.add(23, 2, 37);
            set(world, exchange, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState());
            TileEntity tile = world.getTileEntity(exchange);
            if (tile instanceof TileIndustrialMachine)
                ((TileIndustrialMachine) tile).seedNationExchange("earth_nation_exchange", "minecraft:bread");
            // Run around the civic exchange rather than replacing its interior.
            installUtilitySpine(world, origin.add(16, 0, 43), true, 0, 12, 2);
        }
    }

    /** Physical post-tier-three machine-service annex commissioned by a powered interface. */
    public static void applySettlementServiceAnnex(World world, BlockPos origin) {
        BlockPos annex = origin.add(32, 1, 32);
        building(world, annex, 11, 9, IndustrialCivilizationCore.STEEL_CASING.getDefaultState(), 6);
        set(world, annex.add(5, 1, 0), Blocks.AIR.getDefaultState());
        set(world, annex.add(3, 1, 4), IndustrialCivilizationCore.REPAIR_BENCH.getDefaultState());
        set(world, annex.add(7, 1, 4), IndustrialCivilizationCore.ELECTRIC_FABRICATOR.getDefaultState());
        set(world, annex.add(5, 1, 7), Blocks.CHEST.getDefaultState());
        for (int x = 0; x <= 10; x++)
            set(world, annex.add(x, 0, -1), IndustrialCivilizationCore.INDUSTRIAL_FLOOR.getDefaultState());
    }

    private static void set(World world, BlockPos pos, IBlockState state) {
        if (!insidePlacement(pos)) return;
        world.setBlockState(pos, state, 2);
    }

    static boolean insidePlacement(BlockPos pos) {
        PlacementBounds bounds = PLACEMENT.get();
        return bounds == null || bounds.contains(pos);
    }

    private static boolean allowSideEffects() {
        PlacementBounds bounds = PLACEMENT.get();
        return bounds == null || bounds.initialize;
    }

    private static final class PlacementBounds {
        final int minX, minZ, maxX, maxZ;
        final boolean initialize;
        PlacementBounds(int minX, int minZ, int maxX, int maxZ, boolean initialize) {
            this.minX = minX; this.minZ = minZ; this.maxX = maxX; this.maxZ = maxZ;
            this.initialize = initialize;
        }
        boolean contains(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }
    }
}
