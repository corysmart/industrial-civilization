package com.industrialcivilization.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Persistent descriptors for multi-chunk civilization structures.
 *
 * <p>The old generator wrote an entire landmark while one chunk was being
 * populated. That was harmless for its 15x15 templates, but enlarging those
 * templates would recursively request neighboring chunks. Descriptors let
 * each occupied chunk place only its own slice, including when that chunk was
 * generated before the structure's anchor or is loaded in a later session.</p>
 */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class CivilizationStructureData extends WorldSavedData {
    private static final String NAME = IndustrialCivilizationCore.MODID + "_structure_descriptors_v1";
    static final int DEFERRED_REPAIR_TICKS = 40;
    static final int DEFERRED_REPAIR_PASSES = 3;
    private static final List<PendingRepair> PENDING_REPAIRS = new ArrayList<>();
    private static final List<PendingRoadRepair> PENDING_ROAD_REPAIRS = new ArrayList<>();
    private final List<Descriptor> descriptors = new ArrayList<>();

    public CivilizationStructureData() { super(NAME); }
    public CivilizationStructureData(String name) { super(name); }

    public static void schedule(World world, BlockPos origin, String type,
            int anchorChunkX, int anchorChunkZ) {
        if (world.isRemote) return;
        CivilizationStructureData data = get(world);
        Descriptor descriptor = data.find(origin, type);
        if (descriptor == null) {
            descriptor = new Descriptor(origin, type);
            data.descriptors.add(descriptor);
            data.markDirty();
        }
        // The anchor is in its population pass now, so it is safe to apply its
        // slice even though Chunk#isTerrainPopulated may not be set until later.
        data.apply(world, descriptor, anchorChunkX, anchorChunkZ);
        data.applyOtherLoadedChunks(world, descriptor, anchorChunkX, anchorChunkZ);
    }

    /** Applies descriptors discovered by earlier anchor chunks. */
    public static void applyGeneratedChunk(World world, int chunkX, int chunkZ) {
        if (world.isRemote) return;
        CivilizationStructureData data = (CivilizationStructureData) world.getPerWorldStorage()
            .getOrLoadData(CivilizationStructureData.class, NAME);
        if (data == null) return;
        for (Descriptor descriptor : data.descriptors)
            data.apply(world, descriptor, chunkX, chunkZ);
    }

    /** Backfills a slice when its terrain existed before the anchor descriptor. */
    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (event.getWorld().isRemote || !event.getChunk().isTerrainPopulated()) return;
        applyGeneratedChunk(event.getWorld(), event.getChunk().x, event.getChunk().z);
    }

    private void applyOtherLoadedChunks(World world, Descriptor descriptor,
            int anchorChunkX, int anchorChunkZ) {
        int minChunkX = Math.floorDiv(CivilizationWorldGenerator.descriptorMinX(
            descriptor.origin, descriptor.type), 16);
        int minChunkZ = Math.floorDiv(CivilizationWorldGenerator.descriptorMinZ(
            descriptor.origin, descriptor.type), 16);
        int maxChunkX = Math.floorDiv(CivilizationWorldGenerator.descriptorMaxX(
            descriptor.origin, descriptor.type), 16);
        int maxChunkZ = Math.floorDiv(CivilizationWorldGenerator.descriptorMaxZ(
            descriptor.origin, descriptor.type), 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (chunkX == anchorChunkX && chunkZ == anchorChunkZ) continue;
                BlockPos center = new BlockPos(chunkX * 16 + 8, descriptor.origin.getY(), chunkZ * 16 + 8);
                if (!world.isBlockLoaded(center, false)) continue;
                Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                if (chunk.isTerrainPopulated()) apply(world, descriptor, chunkX, chunkZ);
            }
        }
    }

    private void apply(World world, Descriptor descriptor, int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        if (!descriptor.overlaps(chunkX, chunkZ)) return;
        if (!descriptor.applied.contains(key)) {
            boolean initialize = chunkX == Math.floorDiv(descriptor.origin.getX(), 16)
                && chunkZ == Math.floorDiv(descriptor.origin.getZ(), 16);
            CivilizationWorldGenerator.buildNaturalStructureChunk(world, descriptor.origin,
                descriptor.type, chunkX, chunkZ, initialize);
            descriptor.applied.add(key);
            markDirty();
        }
        // AE2 meteorites and a few other pack generators finish from a server-tick
        // queue after Forge's normal world-generator pass. Reapply each slice once
        // after that queue has drained so late terrain cannot erase buildings or
        // their utility tile entities. The repaired bit is persisted, preventing
        // player changes from being overwritten on later chunk loads.
        if (!descriptor.repaired.contains(key) && descriptor.repairQueued.add(key))
            PENDING_REPAIRS.add(new PendingRepair(world, this, descriptor, chunkX, chunkZ,
                world.getTotalWorldTime() + DEFERRED_REPAIR_TICKS));
    }

    static void scheduleRoadRepair(World world, int chunkX, int chunkZ) {
        if (world.isRemote) return;
        for (PendingRoadRepair pending : PENDING_ROAD_REPAIRS)
            if (pending.world == world && pending.chunkX == chunkX && pending.chunkZ == chunkZ)
                return;
        PENDING_ROAD_REPAIRS.add(new PendingRoadRepair(world, chunkX, chunkZ,
            world.getTotalWorldTime() + DEFERRED_REPAIR_TICKS));
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
        long now = event.world.getTotalWorldTime();
        // Structure reconstruction can load/populate adjacent chunks, which in turn
        // schedules more deferred repairs. Iterate a stable snapshot so those new
        // entries are handled on a later tick instead of invalidating this pass.
        List<PendingRepair> structures = new ArrayList<>(PENDING_REPAIRS);
        for (PendingRepair pending : structures) {
            if (pending.world != event.world || pending.dueTick > now) continue;
            BlockPos center = new BlockPos(pending.chunkX * 16 + 8,
                pending.descriptor.origin.getY(), pending.chunkZ * 16 + 8);
            if (!event.world.isBlockLoaded(center, false)) continue;
            Chunk chunk = event.world.getChunkFromChunkCoords(pending.chunkX, pending.chunkZ);
            if (!chunk.isTerrainPopulated()) continue;
            long key = chunkKey(pending.chunkX, pending.chunkZ);
            CivilizationWorldGenerator.buildNaturalStructureChunk(event.world,
                pending.descriptor.origin, pending.descriptor.type,
                pending.chunkX, pending.chunkZ, false);
            pending.repairPass++;
            if (pending.repairPass >= DEFERRED_REPAIR_PASSES) {
                PENDING_REPAIRS.remove(pending);
                pending.descriptor.repairQueued.remove(key);
                pending.descriptor.repaired.add(key);
                pending.data.markDirty();
            } else {
                // AE2 may extend a meteor's fallout when adjacent chunks finish.
                // The later passes cover that cascading work without repeatedly
                // resetting a structure on future world loads.
                pending.dueTick = now + (pending.repairPass == 1 ? 120 : 240);
            }
        }
        List<PendingRoadRepair> roads = new ArrayList<>(PENDING_ROAD_REPAIRS);
        for (PendingRoadRepair pending : roads) {
            if (pending.world != event.world || pending.dueTick > now) continue;
            BlockPos center = new BlockPos(pending.chunkX * 16 + 8, 64,
                pending.chunkZ * 16 + 8);
            if (!event.world.isBlockLoaded(center, false)) continue;
            Chunk chunk = event.world.getChunkFromChunkCoords(pending.chunkX, pending.chunkZ);
            if (!chunk.isTerrainPopulated()) continue;
            if (!generatedStructureOverlaps(event.world, pending.chunkX, pending.chunkZ))
                CivilizationWorldGenerator.repairRegionalRoad(event.world,
                    pending.chunkX, pending.chunkZ);
            pending.repairPass++;
            if (pending.repairPass >= DEFERRED_REPAIR_PASSES)
                PENDING_ROAD_REPAIRS.remove(pending);
            else pending.dueTick = now + (pending.repairPass == 1 ? 120 : 240);
        }
    }

    private static boolean generatedStructureOverlaps(World world, int chunkX, int chunkZ) {
        CivilizationStructureData data = (CivilizationStructureData) world.getPerWorldStorage()
            .getOrLoadData(CivilizationStructureData.class, NAME);
        if (data == null) return false;
        for (Descriptor descriptor : data.descriptors)
            if (descriptor.footprintOverlaps(chunkX, chunkZ)) return true;
        return false;
    }

    @SubscribeEvent
    public static void worldUnloaded(WorldEvent.Unload event) {
        PENDING_REPAIRS.removeIf(pending -> pending.world == event.getWorld());
        PENDING_ROAD_REPAIRS.removeIf(pending -> pending.world == event.getWorld());
    }

    private Descriptor find(BlockPos origin, String type) {
        for (Descriptor descriptor : descriptors)
            if (descriptor.origin.equals(origin) && descriptor.type.equals(type)) return descriptor;
        return null;
    }

    /** Returns a confirmed generated anchor in one candidate chunk. */
    public static BlockPos findGeneratedAnchor(World world, String type, int chunkX, int chunkZ) {
        CivilizationStructureData data = (CivilizationStructureData) world.getPerWorldStorage()
            .getOrLoadData(CivilizationStructureData.class, NAME);
        if (data == null) return null;
        for (Descriptor descriptor : data.descriptors) {
            if (descriptor.type.equals(type)
                    && Math.floorDiv(descriptor.origin.getX(), 16) == chunkX
                    && Math.floorDiv(descriptor.origin.getZ(), 16) == chunkZ)
                return descriptor.origin;
        }
        return null;
    }

    private static CivilizationStructureData get(World world) {
        CivilizationStructureData data = (CivilizationStructureData) world.getPerWorldStorage()
            .getOrLoadData(CivilizationStructureData.class, NAME);
        if (data == null) {
            data = new CivilizationStructureData();
            world.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x & 0xffffffffL) << 32 | ((long) z & 0xffffffffL);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        descriptors.clear();
        NBTTagList list = nbt.getTagList("Structures", 10);
        for (int index = 0; index < list.tagCount(); index++)
            descriptors.add(new Descriptor(list.getCompoundTagAt(index)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Descriptor descriptor : descriptors) list.appendTag(descriptor.write());
        compound.setTag("Structures", list);
        return compound;
    }

    private static final class Descriptor {
        final BlockPos origin;
        final String type;
        final Set<Long> applied = new HashSet<>();
        final Set<Long> repaired = new HashSet<>();
        final Set<Long> repairQueued = new HashSet<>();

        Descriptor(BlockPos origin, String type) {
            this.origin = origin;
            this.type = type;
        }

        Descriptor(NBTTagCompound tag) {
            this(new BlockPos(tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z")),
                tag.getString("Type"));
            NBTTagList chunks = tag.getTagList("Applied", 10);
            for (int index = 0; index < chunks.tagCount(); index++)
                applied.add(chunks.getCompoundTagAt(index).getLong("Chunk"));
            NBTTagList repairs = tag.getTagList("Repaired", 10);
            for (int index = 0; index < repairs.tagCount(); index++)
                repaired.add(repairs.getCompoundTagAt(index).getLong("Chunk"));
        }

        boolean overlaps(int chunkX, int chunkZ) {
            int minX = chunkX * 16;
            int minZ = chunkZ * 16;
            int maxX = minX + 15;
            int maxZ = minZ + 15;
            return CivilizationWorldGenerator.descriptorMaxX(origin, type) >= minX
                && CivilizationWorldGenerator.descriptorMinX(origin, type) <= maxX
                && CivilizationWorldGenerator.descriptorMaxZ(origin, type) >= minZ
                && CivilizationWorldGenerator.descriptorMinZ(origin, type) <= maxZ;
        }

        boolean footprintOverlaps(int chunkX, int chunkZ) {
            int width = CivilizationWorldGenerator.structureWidth(type);
            int minX = chunkX * 16;
            int minZ = chunkZ * 16;
            int maxX = minX + 15;
            int maxZ = minZ + 15;
            return descriptorMax(origin.getX(), width) >= minX && origin.getX() <= maxX
                && descriptorMax(origin.getZ(), width) >= minZ && origin.getZ() <= maxZ;
        }

        NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("X", origin.getX());
            tag.setInteger("Y", origin.getY());
            tag.setInteger("Z", origin.getZ());
            tag.setString("Type", type);
            NBTTagList chunks = new NBTTagList();
            for (long key : applied) {
                NBTTagCompound chunk = new NBTTagCompound();
                chunk.setLong("Chunk", key);
                chunks.appendTag(chunk);
            }
            tag.setTag("Applied", chunks);
            NBTTagList repairs = new NBTTagList();
            for (long key : repaired) {
                NBTTagCompound chunk = new NBTTagCompound();
                chunk.setLong("Chunk", key);
                repairs.appendTag(chunk);
            }
            tag.setTag("Repaired", repairs);
            return tag;
        }

        private static int descriptorMax(int start, int width) { return start + width - 1; }
    }

    private static final class PendingRepair {
        final World world;
        final CivilizationStructureData data;
        final Descriptor descriptor;
        final int chunkX, chunkZ;
        long dueTick;
        int repairPass;

        PendingRepair(World world, CivilizationStructureData data, Descriptor descriptor,
                int chunkX, int chunkZ, long dueTick) {
            this.world = world;
            this.data = data;
            this.descriptor = descriptor;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.dueTick = dueTick;
        }
    }

    private static final class PendingRoadRepair {
        final World world;
        final int chunkX, chunkZ;
        long dueTick;
        int repairPass;

        PendingRoadRepair(World world, int chunkX, int chunkZ, long dueTick) {
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.dueTick = dueTick;
        }
    }
}
