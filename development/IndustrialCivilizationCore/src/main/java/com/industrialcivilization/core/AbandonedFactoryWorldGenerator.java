package com.industrialcivilization.core;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

/** Rare, resource-light factory shell. Recovery requires repair materials. */
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
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                world.setBlockState(origin.add(x, 0, z), Blocks.STONEBRICK.getDefaultState(), 2);
                for (int y = 1; y <= 4; y++) {
                    boolean wall = x == 0 || x == 8 || z == 0 || z == 8;
                    world.setBlockState(origin.add(x, y, z),
                        wall ? (randomizedWall(x, y, z)) : Blocks.AIR.getDefaultState(), 2);
                }
                world.setBlockState(origin.add(x, 5, z),
                    ((x + z) % 5 == 0) ? Blocks.AIR.getDefaultState() : Blocks.IRON_BLOCK.getDefaultState(), 2);
            }
        }
        world.setBlockState(origin.add(4, 1, 0), Blocks.IRON_DOOR.getDefaultState(), 2);
        world.setBlockState(origin.add(4, 1, 4), IndustrialCivilizationCore.FACTORY_CONTROL_TERMINAL.getDefaultState(), 2);
        world.setBlockState(origin.add(2, 1, 4), Blocks.ANVIL.getDefaultState(), 2);
        world.setBlockState(origin.add(6, 1, 4), Blocks.REDSTONE_BLOCK.getDefaultState(), 2);
        world.setBlockState(origin.add(2, 1, 2), Blocks.IRON_BARS.getDefaultState(), 2);
        world.setBlockState(origin.add(6, 1, 6), Blocks.IRON_BARS.getDefaultState(), 2);
    }

    private static net.minecraft.block.state.IBlockState randomizedWall(int x, int y, int z) {
        if ((x * 17 + y * 7 + z * 13) % 11 == 0) return Blocks.IRON_BARS.getDefaultState();
        if ((x * 5 + y * 3 + z) % 13 == 0) return Blocks.AIR.getDefaultState();
        return Blocks.STONEBRICK.getDefaultState();
    }
}
