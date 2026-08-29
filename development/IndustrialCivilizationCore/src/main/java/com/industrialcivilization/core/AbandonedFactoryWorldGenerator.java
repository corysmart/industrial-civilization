package com.industrialcivilization.core;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/** Rare, multi-building derelict works. Recovery requires repair materials. */
public final class AbandonedFactoryWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0 || random.nextInt(72) != 0) return;
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        int y = world.getHeight(new BlockPos(x, 0, z)).getY();
        if (y < 55 || y > 110) return;
        BlockPos origin = new BlockPos(x - 4, y, z - 4);
        buildShell(world, origin);
    }

    public static void buildShell(World world, BlockPos origin) {
        long seed = world.getSeed() ^ (long) origin.getX() * 341873128712L
            ^ (long) origin.getZ() * 132897987541L;
        for (int x = 0; x < 31; x++) for (int z = 0; z < 31; z++) {
            set(world, origin.add(x, 0, z), Blocks.STONE.getDefaultState());
            set(world, origin.add(x, 1, z), ((x + z) % 7 == 0)
                ? Blocks.GRAVEL.getDefaultState() : Blocks.STONEBRICK.getDefaultState());
            for (int y = 2; y <= 12; y++) set(world, origin.add(x, y, z), Blocks.AIR.getDefaultState());
        }
        ruinedBuilding(world, origin.add(2, 1, 3), 17, 14, 7, Blocks.STONEBRICK, seed);
        ruinedBuilding(world, origin.add(20, 1, 3), 9, 12, 5, Blocks.IRON_BLOCK, seed ^ 0x57415245L);
        ruinedBuilding(world, origin.add(4, 1, 20), 12, 8, 4, Blocks.BRICK_BLOCK, seed ^ 0x4F46464943L);
        // Loading yard, broken gantry, chimney and scattered salvage make the
        // site read as a former complex instead of a single damaged room.
        for (int z = 17; z < 30; z++) {
            set(world, origin.add(20, 2, z), Blocks.IRON_BARS.getDefaultState());
            set(world, origin.add(26, 2, z), Blocks.IRON_BARS.getDefaultState());
        }
        for (int x = 20; x <= 26; x++)
            set(world, origin.add(x, 6, 19), (x == 23) ? Blocks.AIR.getDefaultState()
                : Blocks.IRON_BLOCK.getDefaultState());
        for (int y = 2; y <= 12; y++) for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++)
            if ((y + x + z) % 9 != 0) set(world, origin.add(17 + x, y, 24 + z),
                Blocks.BRICK_BLOCK.getDefaultState());
        set(world, origin.add(10, 2, 9), IndustrialCivilizationCore.FACTORY_CONTROL_TERMINAL.getDefaultState());
        set(world, origin.add(7, 2, 9), Blocks.ANVIL.getDefaultState());
        set(world, origin.add(13, 2, 9), Blocks.REDSTONE_BLOCK.getDefaultState());
        set(world, origin.add(23, 2, 8), Blocks.CHEST.getDefaultState());
        set(world, origin.add(8, 2, 23), Blocks.CRAFTING_TABLE.getDefaultState());
    }

    private static void ruinedBuilding(World world, BlockPos origin, int width, int depth,
            int height, Block wall, long seed) {
        for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) for (int y = 0; y <= height; y++) {
            boolean shell = y == 0 || y == height || x == 0 || z == 0
                || x == width - 1 || z == depth - 1;
            if (!shell) {
                set(world, origin.add(x, y, z), Blocks.AIR.getDefaultState());
                continue;
            }
            long hash = seed ^ x * 73428767L ^ y * 912931L ^ z * 438289L;
            if (y > 0 && Math.floorMod(hash, 11L) < (y == height ? 3 : 1))
                set(world, origin.add(x, y, z), Blocks.AIR.getDefaultState());
            else if (Math.floorMod(hash, 17L) == 0)
                set(world, origin.add(x, y, z), Blocks.IRON_BARS.getDefaultState());
            else
                set(world, origin.add(x, y, z), wall.getDefaultState());
        }
        set(world, origin.add(width / 2, 1, 0), Blocks.AIR.getDefaultState());
        set(world, origin.add(width / 2, 2, 0), Blocks.AIR.getDefaultState());
    }

    private static void set(World world, BlockPos pos, net.minecraft.block.state.IBlockState state) {
        if (CivilizationWorldGenerator.insidePlacement(pos)) world.setBlockState(pos, state, 2);
    }
}
