package com.industrialcivilization.core;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Deploys large workshop equipment footprints from a single controller block. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class WorkshopSystem {
    @SubscribeEvent
    public static void placed(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || !(event.getPlacedBlock().getBlock() instanceof BlockIndustrialMachine)) return;
        BlockIndustrialMachine controller = (BlockIndustrialMachine) event.getPlacedBlock().getBlock();
        if (controller.getKind() != IndustrialMachineKind.CAR_WORKSHOP
                && controller.getKind() != IndustrialMachineKind.GUN_FACTORY) return;
        deploy((World) event.getWorld(), event.getPos(), controller.getKind());
        if (event.getPlayer() != null) RuntimeAdvancements.grant(event.getPlayer(),
            controller.getKind() == IndustrialMachineKind.CAR_WORKSHOP
                ? "car_workshop_deployed" : "advanced_armament_factory");
    }

    private static void deploy(World world, BlockPos origin, IndustrialMachineKind kind) {
        Block casing = kind == IndustrialMachineKind.CAR_WORKSHOP ? Blocks.IRON_BLOCK : Blocks.STONEBRICK;
        // 9x7 industrial set piece: safety floor, gantry, side benches, lamps,
        // and an open vehicle/assembly bay. The player supplies the building roof.
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++) {
            BlockPos floor = origin.add(x, -1, z);
            if (world.isAirBlock(floor) || !world.getBlockState(floor).getMaterial().isSolid())
                world.setBlockState(floor, Blocks.IRON_BLOCK.getDefaultState(), 2);
        }
        for (int x : new int[] {-4, 4}) for (int z = -3; z <= 3; z++) for (int y = 0; y <= 3; y++) {
            BlockPos frame = origin.add(x, y, z);
            if (world.isAirBlock(frame)) world.setBlockState(frame, casing.getDefaultState(), 2);
        }
        for (int x = -4; x <= 4; x++) for (int z : new int[] {-3, 3}) {
            BlockPos rail = origin.add(x, 3, z);
            if (world.isAirBlock(rail)) world.setBlockState(rail, casing.getDefaultState(), 2);
        }
        for (int x = -3; x <= 3; x += 2) {
            BlockPos lamp = origin.add(x, 3, 0);
            if (world.isAirBlock(lamp)) world.setBlockState(lamp, Blocks.REDSTONE_LAMP.getDefaultState(), 2);
        }
        for (int z = -2; z <= 2; z += 2) {
            BlockPos bench = origin.add(-3, 0, z);
            if (world.isAirBlock(bench)) world.setBlockState(bench,
                kind == IndustrialMachineKind.CAR_WORKSHOP ? Blocks.ANVIL.getDefaultState()
                    : Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), 2);
        }
    }

    private WorkshopSystem() {}
}
