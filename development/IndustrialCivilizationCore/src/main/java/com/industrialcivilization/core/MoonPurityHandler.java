package com.industrialcivilization.core;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Removes Galacticraft's generated Moon dungeons while preserving natural geology. */
public final class MoonPurityHandler {
    @SubscribeEvent
    public void populated(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (!world.provider.getDimensionType().getName().toLowerCase(Locale.ROOT).contains("moon")) return;
        int baseX = event.getChunkX() * 16;
        int baseZ = event.getChunkZ() * 16;
        boolean dungeon = false;
        for (int x = 0; x < 16 && !dungeon; x++) for (int z = 0; z < 16 && !dungeon; z++) {
            for (int y = 1; y < 128; y++) {
                IBlockState state = world.getBlockState(new BlockPos(baseX + x, y, baseZ + z));
                if (isDungeonBrick(state)) { dungeon = true; break; }
            }
        }
        if (!dungeon) return;
        Block moon = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("galacticraftcore:basic_block_moon"));
        IBlockState stone = moon == null ? Blocks.STONE.getDefaultState() : moon.getStateFromMeta(4);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) for (int y = 1; y < 128; y++) {
            BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            ResourceLocation id = block.getRegistryName();
            String path = id == null ? "" : id.getResourcePath();
            if (isDungeonBrick(state) || path.contains("moon_stairs_brick")
                    || path.contains("dungeon_spawner")) world.setBlockState(pos, stone, 2);
            else if (block == Blocks.CHEST || block == Blocks.MOB_SPAWNER || block == Blocks.TORCH
                    || block == Blocks.WEB) world.setBlockToAir(pos);
        }
    }

    private static boolean isDungeonBrick(IBlockState state) {
        ResourceLocation id = state.getBlock().getRegistryName();
        return id != null && "galacticraftcore".equals(id.getResourceDomain())
            && "basic_block_moon".equals(id.getResourcePath()) && state.getBlock().getMetaFromState(state) == 14;
    }
}
