package com.industrialcivilization.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Deploys rotatable workshop footprints and joins touching workshops. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class WorkshopSystem {
    private static final int SEARCH_RADIUS = 14;

    @SubscribeEvent
    public static void placed(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || !(event.getPlacedBlock().getBlock() instanceof BlockIndustrialMachine)) return;
        BlockIndustrialMachine controller = (BlockIndustrialMachine) event.getPlacedBlock().getBlock();
        if (!isWorkshop(controller.getKind())) return;
        World world = (World) event.getWorld();
        BlockPos origin = event.getPos();
        EnumFacing facing = facing(world, origin);
        String problem = placementProblem(world, origin, facing);
        if (problem != null) {
            event.setCanceled(true);
            if (event.getPlayer() != null) event.getPlayer().sendStatusMessage(
                new TextComponentTranslation("message.industrialcivilization.workshop.blocked", problem), false);
            return;
        }
        deploy(world, origin, controller.getKind(), facing);
        int joins = connectAdjacent(world, origin);
        if (event.getPlayer() != null) {
            RuntimeAdvancements.grant(event.getPlayer(),
                controller.getKind() == IndustrialMachineKind.CAR_WORKSHOP
                    ? "car_workshop_deployed" : "advanced_armament_factory");
            if (joins > 0) event.getPlayer().sendStatusMessage(
                new TextComponentTranslation("message.industrialcivilization.workshop.joined", joins), false);
        }
    }

    private static boolean isWorkshop(IndustrialMachineKind kind) {
        return kind == IndustrialMachineKind.CAR_WORKSHOP || kind == IndustrialMachineKind.GUN_FACTORY;
    }

    private static EnumFacing facing(World world, BlockPos origin) {
        TileEntity tile = world.getTileEntity(origin);
        return tile instanceof TileIndustrialMachine
            ? ((TileIndustrialMachine) tile).getWorkshopFacing() : EnumFacing.NORTH;
    }

    private static WorkshopLayout.Bounds bounds(BlockPos pos, EnumFacing facing) {
        return WorkshopLayout.bounds(pos.getX(), pos.getZ(), facing.getAxis() == EnumFacing.Axis.X);
    }

    private static String placementProblem(World world, BlockPos origin, EnumFacing facing) {
        WorkshopLayout.Bounds proposed = bounds(origin, facing);
        for (BlockPos other : controllers(world, origin, SEARCH_RADIUS)) {
            if (other.equals(origin)) continue;
            if (proposed.overlaps(bounds(other, facing(world, other)))) return "workshop footprints overlap";
        }
        // Keep the central operating bay safe while allowing player-built walls and roofs.
        for (int x = -2; x <= 2; x++) for (int z = -1; z <= 1; z++) for (int y = 0; y <= 2; y++) {
            if (x == 0 && z == 0 && y == 0) continue;
            BlockPos check = local(origin, facing, x, y, z);
            if (!world.isAirBlock(check)) return "central assembly bay is obstructed at "
                + check.getX() + "," + check.getY() + "," + check.getZ();
        }
        return null;
    }

    static void deploy(World world, BlockPos origin, IndustrialMachineKind kind, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(origin);
        if (tile instanceof TileIndustrialMachine) ((TileIndustrialMachine) tile).setWorkshopFacing(facing);
        Block casing = kind == IndustrialMachineKind.CAR_WORKSHOP
            ? IndustrialCivilizationCore.STEEL_FRAME : IndustrialCivilizationCore.STEEL_CASING;
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++) {
            BlockPos floor = local(origin, facing, x, -1, z);
            world.setBlockState(floor, IndustrialCivilizationCore.INDUSTRIAL_FLOOR.getDefaultState(), 2);
        }
        for (int x : new int[] {-4, 4}) for (int z = -3; z <= 3; z++) for (int y = 0; y <= 3; y++) {
            BlockPos frame = local(origin, facing, x, y, z);
            if (world.isAirBlock(frame)) world.setBlockState(frame, casing.getDefaultState(), 2);
        }
        for (int x = -4; x <= 4; x++) for (int z : new int[] {-3, 3}) {
            BlockPos rail = local(origin, facing, x, 3, z);
            if (world.isAirBlock(rail)) world.setBlockState(rail, casing.getDefaultState(), 2);
        }
        for (int x = -3; x <= 3; x += 2) {
            BlockPos lamp = local(origin, facing, x, 3, 0);
            if (world.isAirBlock(lamp)) world.setBlockState(lamp, Blocks.REDSTONE_LAMP.getDefaultState(), 2);
        }
        for (int x = -2; x <= 2; x++) {
            world.setBlockState(local(origin, facing, x, -1, -2),
                IndustrialCivilizationCore.HAZARD_STRIPE.getDefaultState(), 2);
        }
        for (int z = -2; z <= 2; z += 2) {
            BlockPos storage = local(origin, facing, 3, 0, z);
            if (world.isAirBlock(storage)) world.setBlockState(storage,
                (kind == IndustrialMachineKind.CAR_WORKSHOP
                    ? IndustrialCivilizationCore.DRAWER_CABINET
                    : IndustrialCivilizationCore.TOOL_WALL).getDefaultState(), 2);
        }
        for (int x = -2; x <= 2; x++) {
            BlockPos cable = local(origin, facing, x, 2, -3);
            if (world.isAirBlock(cable)) world.setBlockState(cable,
                IndustrialCivilizationCore.CABLE_COVER.getDefaultState(), 2);
        }
        for (int z = -2; z <= 2; z += 2) {
            BlockPos bench = local(origin, facing, -3, 0, z);
            if (world.isAirBlock(bench)) world.setBlockState(bench,
                kind == IndustrialMachineKind.CAR_WORKSHOP ? Blocks.ANVIL.getDefaultState()
                    : Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState(), 2);
        }
    }

    private static BlockPos local(BlockPos origin, EnumFacing facing, int x, int y, int z) {
        switch (facing) {
            case SOUTH: return origin.add(-x, y, -z);
            case EAST: return origin.add(-z, y, x);
            case WEST: return origin.add(z, y, -x);
            default: return origin.add(x, y, z);
        }
    }

    static int connectAdjacent(World world, BlockPos origin) {
        int joined = 0;
        for (BlockPos other : controllers(world, origin, SEARCH_RADIUS)) {
            if (other.equals(origin)) continue;
            WorkshopLayout.Bounds a = bounds(origin, facing(world, origin));
            WorkshopLayout.Bounds b = bounds(other, facing(world, other));
            WorkshopLayout.Join join = origin.getY() == other.getY()
                ? WorkshopLayout.join(a, b) : WorkshopLayout.Join.NONE;
            if (join != WorkshopLayout.Join.NONE) {
                connect(world, a, b, join, origin.getY());
                joined++;
            }
        }
        return joined;
    }

    private static void connect(World world, WorkshopLayout.Bounds a, WorkshopLayout.Bounds b,
            WorkshopLayout.Join join, int baseY) {
        Block cable = optionalBlock("ic2:blockcable", IndustrialCivilizationCore.CABLE_BLOCK);
        Block pipe = optionalBlock("buildcrafttransport:pipe_holder", IndustrialCivilizationCore.CABLE_COVER);
        if (join == WorkshopLayout.Join.EAST_WEST) {
            WorkshopLayout.Bounds left = a.minX < b.minX ? a : b;
            WorkshopLayout.Bounds right = left == a ? b : a;
            int z = WorkshopLayout.centerOfOverlap(a.minZ, a.maxZ, b.minZ, b.maxZ);
            for (int x : new int[] {left.maxX, right.minX}) {
                for (int dz = -1; dz <= 1; dz++) for (int y = 0; y <= 2; y++)
                    world.setBlockToAir(new BlockPos(x, baseY + y, z + dz));
                world.setBlockState(new BlockPos(x, baseY - 1, z),
                    IndustrialCivilizationCore.INDUSTRIAL_FLOOR.getDefaultState(), 2);
                world.setBlockState(new BlockPos(x, baseY + 3, z - 1), cable.getDefaultState(), 2);
                world.setBlockState(new BlockPos(x, baseY + 3, z + 1), pipe.getDefaultState(), 2);
            }
        } else {
            WorkshopLayout.Bounds north = a.minZ < b.minZ ? a : b;
            WorkshopLayout.Bounds south = north == a ? b : a;
            int x = WorkshopLayout.centerOfOverlap(a.minX, a.maxX, b.minX, b.maxX);
            for (int z : new int[] {north.maxZ, south.minZ}) {
                for (int dx = -1; dx <= 1; dx++) for (int y = 0; y <= 2; y++)
                    world.setBlockToAir(new BlockPos(x + dx, baseY + y, z));
                world.setBlockState(new BlockPos(x, baseY - 1, z),
                    IndustrialCivilizationCore.INDUSTRIAL_FLOOR.getDefaultState(), 2);
                world.setBlockState(new BlockPos(x - 1, baseY + 3, z), cable.getDefaultState(), 2);
                world.setBlockState(new BlockPos(x + 1, baseY + 3, z), pipe.getDefaultState(), 2);
            }
        }
    }

    private static Block optionalBlock(String id, Block fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        return block == null || block == Blocks.AIR ? fallback : block;
    }

    static List<BlockPos> controllers(World world, BlockPos center, int radius) {
        List<BlockPos> result = new ArrayList<>();
        for (TileEntity tile : new ArrayList<TileEntity>(world.loadedTileEntityList)) {
            if (!(tile instanceof TileIndustrialMachine)) continue;
            TileIndustrialMachine machine = (TileIndustrialMachine) tile;
            if (isWorkshop(machine.getKind()) && tile.getPos().distanceSq(center) <= radius * radius)
                result.add(tile.getPos());
        }
        return result;
    }

    /** Returns controller, touching-pair, and physically-open-connection counts. */
    static int[] inspect(World world, BlockPos center, int radius) {
        List<BlockPos> positions = controllers(world, center, radius);
        int pairs = 0;
        int connected = 0;
        for (int i = 0; i < positions.size(); i++) for (int j = i + 1; j < positions.size(); j++) {
            BlockPos first = positions.get(i);
            BlockPos second = positions.get(j);
            WorkshopLayout.Bounds a = bounds(first, facing(world, first));
            WorkshopLayout.Bounds b = bounds(second, facing(world, second));
            WorkshopLayout.Join join = first.getY() == second.getY()
                ? WorkshopLayout.join(a, b) : WorkshopLayout.Join.NONE;
            if (join == WorkshopLayout.Join.NONE) continue;
            pairs++;
            if (passageOpen(world, first.getY(), a, b, join)) connected++;
        }
        return new int[] {positions.size(), pairs, connected};
    }

    private static boolean passageOpen(World world, int y, WorkshopLayout.Bounds a,
            WorkshopLayout.Bounds b, WorkshopLayout.Join join) {
        if (join == WorkshopLayout.Join.EAST_WEST) {
            WorkshopLayout.Bounds left = a.minX < b.minX ? a : b;
            WorkshopLayout.Bounds right = left == a ? b : a;
            int z = WorkshopLayout.centerOfOverlap(a.minZ, a.maxZ, b.minZ, b.maxZ);
            return world.isAirBlock(new BlockPos(left.maxX, y + 1, z))
                && world.isAirBlock(new BlockPos(right.minX, y + 1, z));
        }
        WorkshopLayout.Bounds north = a.minZ < b.minZ ? a : b;
        WorkshopLayout.Bounds south = north == a ? b : a;
        int x = WorkshopLayout.centerOfOverlap(a.minX, a.maxX, b.minX, b.maxX);
        return world.isAirBlock(new BlockPos(x, y + 1, north.maxZ))
            && world.isAirBlock(new BlockPos(x, y + 1, south.minZ));
    }

    private WorkshopSystem() {}
}
