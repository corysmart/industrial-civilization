package com.industrialcivilization.core;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
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

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            if (isMoon(world)) generateLunarHeritageFlags(chunkX, chunkZ, world);
            if (isMars(world) && aiAgeUnlocked(world)) generateMartianCivilization(
                random, chunkX, chunkZ, world);
            return;
        }
        BlockPos spawn = world.getSpawnPoint();
        if (isPrimitiveSettlementChunk(world, chunkX, chunkZ, spawn)) {
            BlockPos origin = surfaceOrigin(world, chunkX, chunkZ);
            if (origin != null) buildPrimitiveSettlement(world, origin);
            return;
        }
        double x = chunkX * 16 + 8 - spawn.getX();
        double z = chunkZ * 16 + 8 - spawn.getZ();
        double distance = Math.sqrt(x * x + z * z);
        BlockPos origin = surfaceOrigin(world, chunkX, chunkZ);
        if (origin == null) return;
        buildRegionalRoad(world, chunkX, chunkZ, distance);

        // One structure at most per chunk. The order keeps the rarer, more
        // distant landmarks from being replaced by common factory shells.
        if (distance >= 3000 && random.nextInt(256) == 0) {
            buildIndustrialCity(world, origin);
        } else if (distance >= 2200 && random.nextInt(160) == 0) {
            buildOperationalFactory(world, origin, FACTORY_SPECIALTIES[
                Math.floorMod(chunkX * 31 + chunkZ * 17, FACTORY_SPECIALTIES.length)]);
        } else if (distance >= 1400 && random.nextInt(128) == 0) {
            buildMilitiaOutpost(world, origin);
        } else if (distance >= 900 && random.nextInt(96) == 0) {
            AbandonedFactoryWorldGenerator.buildShell(world, origin);
            FactionSystem.spawnCitizen(world, origin.getX() + 2.5, origin.getY() + 1,
                origin.getZ() + 11.5, "ashline_raiders", "raider", "armaments", "Ashline Lookout", 3);
            FactionSystem.spawnCitizen(world, origin.getX() + 11.5, origin.getY() + 1,
                origin.getZ() + 2.5, "ashline_raiders", "raider", "armaments", "Ashline Salvager", 3);
        }
    }

    private static boolean isMars(World world) {
        String name = world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT);
        return name.contains("mars");
    }

    private static boolean isMoon(World world) {
        return world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT).contains("moon");
    }

    /** Six compact heritage markers represent the six Apollo surface flag sites. */
    private static void generateLunarHeritageFlags(int chunkX, int chunkZ, World world) {
        int[][] sites = {{96, 0}, {192, 80}, {-160, 144}, {320, -96}, {-288, -160}, {64, 352}};
        for (int[] site : sites) {
            if (Math.floorDiv(site[0], 16) != chunkX || Math.floorDiv(site[1], 16) != chunkZ) continue;
            BlockPos ground = world.getTopSolidOrLiquidBlock(new BlockPos(site[0], 0, site[1])).down();
            if (ground.getY() < 10) return;
            for (int y = 1; y <= 5; y++) set(world, ground.add(0, y, 0), Blocks.IRON_BARS.getDefaultState());
            // Small block-art United States flag; these are markers, not loot structures.
            for (int dx = 1; dx <= 4; dx++) for (int dy = 0; dy <= 2; dy++) {
                int color = dx <= 2 && dy >= 1 ? 11 : ((dy & 1) == 0 ? 14 : 0);
                set(world, ground.add(dx, 5 - dy, 0), Blocks.WOOL.getStateFromMeta(color));
            }
            return;
        }
    }

    private static boolean aiAgeUnlocked(World world) {
        for (net.minecraft.entity.player.EntityPlayer player : world.playerEntities) {
            if (ProgressionState.has(player, "ai_age") || MarketEconomy.playerStage(player) >= 7) return true;
        }
        return false;
    }

    /** Mars remains untouched until an AI-age player generates new terrain. */
    private static void generateMartianCivilization(Random random, int chunkX, int chunkZ, World world) {
        BlockPos origin = surfaceOrigin(world, chunkX, chunkZ, 25, 190);
        if (origin == null) return;
        double distance = Math.sqrt((double) chunkX * chunkX + (double) chunkZ * chunkZ) * 16.0D;
        buildRegionalRoad(world, chunkX, chunkZ, distance);
        if (random.nextInt(320) == 0) buildIndustrialCity(world, origin);
        else if (random.nextInt(220) == 0) buildMilitiaOutpost(world, origin);
        else if (random.nextInt(160) == 0) buildPrimitiveSettlement(world, origin);
    }

    private static boolean isPrimitiveSettlementChunk(World world, int chunkX, int chunkZ,
            BlockPos spawn) {
        Random anchors = new Random(world.getSeed() ^ 0x49C1A1F7L);
        for (int index = 0; index < PRIMITIVE_RADII.length; index++) {
            double angle = anchors.nextDouble() * Math.PI * 2.0 + index * 2.094;
            int targetX = spawn.getX() + (int) Math.round(Math.cos(angle) * PRIMITIVE_RADII[index]);
            int targetZ = spawn.getZ() + (int) Math.round(Math.sin(angle) * PRIMITIVE_RADII[index]);
            if (Math.floorDiv(targetX, 16) == chunkX && Math.floorDiv(targetZ, 16) == chunkZ) return true;
        }
        return false;
    }

    private static BlockPos surfaceOrigin(World world, int chunkX, int chunkZ) {
        return surfaceOrigin(world, chunkX, chunkZ, 55, 115);
    }

    private static BlockPos surfaceOrigin(World world, int chunkX, int chunkZ, int minY, int maxY) {
        int x = chunkX * 16 + 1;
        int z = chunkZ * 16 + 1;
        int y = world.getHeight(new BlockPos(x + 7, 0, z + 7)).getY();
        return y < minY || y > maxY ? null : new BlockPos(x, y, z);
    }

    private static void buildPrimitiveSettlement(World world, BlockPos origin) {
        platform(world, origin, Blocks.COBBLESTONE.getDefaultState());
        // A deliberately primitive cluster: two timber shelters, a well, a
        // small crop plot and a crossroads. It does not contain free machines.
        hut(world, origin.add(1, 1, 1), Blocks.PLANKS.getDefaultState());
        hut(world, origin.add(9, 1, 9), Blocks.PLANKS.getDefaultState());
        for (int index = 0; index < 15; index++) {
            set(world, origin.add(7, 1, index), Blocks.DIRT.getDefaultState());
            set(world, origin.add(index, 1, 7), Blocks.DIRT.getDefaultState());
        }
        for (int x = 9; x <= 13; x++) for (int z = 1; z <= 5; z++) {
            set(world, origin.add(x, 1, z), (x == 11) ? Blocks.WATER.getDefaultState()
                : Blocks.FARMLAND.getDefaultState());
            if (x != 11) set(world, origin.add(x, 2, z), Blocks.WHEAT.getDefaultState());
        }
        set(world, origin.add(7, 1, 7), Blocks.COBBLESTONE.getDefaultState());
        set(world, origin.add(7, 2, 7), Blocks.TORCH.getDefaultState());
        FactionSystem.spawnCitizen(world, origin.getX() + 6.5, origin.getY() + 2,
            origin.getZ() + 6.5, "frontier_cooperative", "villager", "food", "Frontier Grower", 2);
        FactionSystem.spawnCitizen(world, origin.getX() + 8.5, origin.getY() + 2,
            origin.getZ() + 6.5, "frontier_cooperative", "trader", "general", "Cooperative Trader", 2);
        FactionSystem.spawnCitizen(world, origin.getX() + 6.5, origin.getY() + 2,
            origin.getZ() + 8.5, "frontier_cooperative", "guard", "general", "Village Watch");
    }

    private static void buildMilitiaOutpost(World world, BlockPos origin) {
        MilitiaOutpostRegistry.record(world, origin);
        platform(world, origin, Blocks.STONEBRICK.getDefaultState());
        for (int i = 0; i < 15; i++) {
            for (int y = 1; y <= 3; y++) {
                set(world, origin.add(i, y, 0), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(i, y, 14), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(0, y, i), Blocks.IRON_BARS.getDefaultState());
                set(world, origin.add(14, y, i), Blocks.IRON_BARS.getDefaultState());
            }
        }
        tower(world, origin.add(1, 1, 1));
        tower(world, origin.add(10, 1, 10));
        set(world, origin.add(7, 1, 0), Blocks.IRON_DOOR.getDefaultState());
        markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 7.5, origin.getY() + 2,
            origin.getZ() + 7.5, "civil_defense_militia", "militia", "armaments", "Militia Quartermaster", 5), origin);
        markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 4.5, origin.getY() + 2,
            origin.getZ() + 8.5, "civil_defense_militia", "guard", "armaments", "Outpost Guard"), origin);
        markOutpost(FactionSystem.spawnCitizen(world, origin.getX() + 10.5, origin.getY() + 2,
            origin.getZ() + 6.5, "civil_defense_militia", "guard", "armaments", "Outpost Guard"), origin);
        if ((origin.getX() ^ origin.getZ()) % 2 == 0) installUtilitySpine(world, origin, false);
    }

    private static void markOutpost(net.minecraft.entity.passive.EntityVillager citizen, BlockPos origin) {
        citizen.getEntityData().setBoolean("IndustrialMilitiaOutpost", true);
        citizen.getEntityData().setString("IndustrialOutpostId", origin.getX() + "_" + origin.getZ());
    }

    private static void buildOperationalFactory(World world, BlockPos origin, String specialty) {
        platform(world, origin, Blocks.STONEBRICK.getDefaultState());
        industrialShell(world, origin.add(1, 1, 1), specialty);
        Block machine = "research".equals(specialty) ? IndustrialCivilizationCore.RESEARCH_STATION
            : "electronics".equals(specialty) ? IndustrialCivilizationCore.PROGRAMMABLE_ASSEMBLER
            : "armaments".equals(specialty) ? IndustrialCivilizationCore.ROBOTIC_MANUFACTURING_CELL
            : IndustrialCivilizationCore.ELECTRIC_FABRICATOR;
        set(world, origin.add(7, 2, 7), machine.getDefaultState());
        set(world, origin.add(5, 2, 7), Blocks.CHEST.getDefaultState());
        set(world, origin.add(9, 2, 7), Blocks.CHEST.getDefaultState());
        String faction = "research".equals(specialty) ? "survey_detachment_7" : "riverside_works";
        String title = Character.toUpperCase(specialty.charAt(0)) + specialty.substring(1);
        FactionSystem.spawnCitizen(world, origin.getX() + 5.5, origin.getY() + 2,
            origin.getZ() + 4.5, faction, "trader", specialty, title + " Works Factor", 5);
        FactionSystem.spawnCitizen(world, origin.getX() + 9.5, origin.getY() + 2,
            origin.getZ() + 4.5, faction, "engineer", specialty, title + " Works Engineer");
        FactionSystem.spawnCitizen(world, origin.getX() + 7.5, origin.getY() + 2,
            origin.getZ() + 12.5, faction, "guard", specialty, "Factory Security");
        installUtilitySpine(world, origin, true);
    }

    private static void buildIndustrialCity(World world, BlockPos origin) {
        platform(world, origin, Blocks.STONEBRICK.getDefaultState());
        building(world, origin.add(1, 1, 1), 5, 5, Blocks.BRICK_BLOCK.getDefaultState(), 6);
        building(world, origin.add(9, 1, 1), 5, 5, Blocks.STONEBRICK.getDefaultState(), 8);
        building(world, origin.add(1, 1, 9), 5, 5, Blocks.QUARTZ_BLOCK.getDefaultState(), 5);
        building(world, origin.add(9, 1, 9), 5, 5, Blocks.BRICK_BLOCK.getDefaultState(), 7);
        for (int i = 0; i < 15; i++) {
            set(world, origin.add(7, 1, i), Blocks.DOUBLE_STONE_SLAB.getDefaultState());
            set(world, origin.add(i, 1, 7), Blocks.DOUBLE_STONE_SLAB.getDefaultState());
        }
        for (BlockPos lamp : new BlockPos[] {origin.add(6, 2, 6), origin.add(8, 2, 6),
                origin.add(6, 2, 8), origin.add(8, 2, 8)}) {
            set(world, lamp, Blocks.IRON_BARS.getDefaultState());
            set(world, lamp.up(), Blocks.GLOWSTONE.getDefaultState());
        }
        FactionSystem.spawnCitizen(world, origin.getX() + 6.5, origin.getY() + 2,
            origin.getZ() + 7.5, "riverside_works", "trader", "electronics", "City Exchange Broker", 7);
        FactionSystem.spawnCitizen(world, origin.getX() + 8.5, origin.getY() + 2,
            origin.getZ() + 7.5, "riverside_works", "trader", "steel", "Foundry Representative", 7);
        FactionSystem.spawnCitizen(world, origin.getX() + 7.5, origin.getY() + 2,
            origin.getZ() + 6.5, "civil_defense_militia", "guard", "armaments", "City Militia");
        FactionSystem.spawnCitizen(world, origin.getX() + 7.5, origin.getY() + 2,
            origin.getZ() + 8.5, "survey_detachment_7", "scientist", "research", "Urban Surveyor");
        installUtilitySpine(world, origin, true);
        BlockPos exchange = origin.add(7, 2, 5);
        set(world, exchange, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState());
        net.minecraft.tileentity.TileEntity tile = world.getTileEntity(exchange);
        if (tile instanceof TileIndustrialMachine) {
            String[] products = {"minecraft:iron_ingot", "minecraft:redstone", "minecraft:coal",
                "minecraft:paper", "minecraft:bread"};
            int index = Math.floorMod(origin.getX() * 31 + origin.getZ(), products.length);
            ((TileIndustrialMachine) tile).seedNationExchange("earth_nation_exchange", products[index]);
        }
    }

    private static void platform(World world, BlockPos origin, IBlockState floor) {
        for (int x = 0; x < 15; x++) for (int z = 0; z < 15; z++) {
            set(world, origin.add(x, 0, z), Blocks.STONE.getDefaultState());
            set(world, origin.add(x, 1, z), floor);
            for (int y = 2; y <= 10; y++) set(world, origin.add(x, y, z), Blocks.AIR.getDefaultState());
        }
    }

    private static void buildRegionalRoad(World world, int chunkX, int chunkZ, double distance) {
        if (distance < 850) return;
        boolean northSouth = Math.floorMod(chunkX, 8) == 0;
        boolean eastWest = Math.floorMod(chunkZ, 8) == 0;
        if (!northSouth && !eastWest) return;
        IBlockState surface = distance < 1700 ? Blocks.DIRT.getDefaultState()
            : Blocks.DOUBLE_STONE_SLAB.getDefaultState();
        for (int step = 0; step < 16; step++) for (int lane = -1; lane <= 1; lane++) {
            int x = chunkX * 16 + (northSouth ? 8 + lane : step);
            int z = chunkZ * 16 + (northSouth ? step : 8 + lane);
            BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
            if (top.getY() > 52 && top.getY() < 120) {
                world.setBlockState(top.down(), surface, 2);
                world.setBlockToAir(top);
                world.setBlockToAir(top.up());
            }
        }
    }

    private static void installUtilitySpine(World world, BlockPos origin, boolean pipes) {
        Block cable = optionalBlock("ic2:blockcable");
        Block pipe = optionalBlock("buildcrafttransport:pipe_holder");
        set(world, origin.add(2, 2, 7), IndustrialCivilizationCore.ENVIRONMENTAL_SOLAR_ARRAY.getDefaultState());
        if (cable != Blocks.AIR) {
            for (int x = 3; x <= 12; x++) set(world, origin.add(x, 2, 7), cable.getDefaultState());
            // Wall-height service outlets, with adjacent air reserved for player machines.
            for (int x : new int[] {4, 7, 10, 12}) {
                set(world, origin.add(x, 3, 7), cable.getDefaultState());
                set(world, origin.add(x, 3, 6), Blocks.AIR.getDefaultState());
            }
        }
        if (pipes && pipe != Blocks.AIR) {
            for (int z = 3; z <= 11; z++) set(world, origin.add(3, 2, z), pipe.getDefaultState());
        }
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

    private static void tower(World world, BlockPos origin) {
        building(world, origin, 4, 4, Blocks.STONEBRICK.getDefaultState(), 6);
        for (int y = 1; y <= 5; y++) set(world, origin.add(1, y, 1), Blocks.LADDER.getDefaultState());
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

    private static void set(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 2);
    }
}
