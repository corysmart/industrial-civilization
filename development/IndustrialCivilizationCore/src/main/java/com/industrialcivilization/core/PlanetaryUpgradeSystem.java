package com.industrialcivilization.core;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Upgrades already-generated, loaded Mars chunks after the AI Age. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class PlanetaryUpgradeSystem extends WorldSavedData {
    private static final String NAME = "industrial_civilization_mars_ai_upgrade_v1";
    private final Set<Long> processed = new HashSet<>();
    public PlanetaryUpgradeSystem() { super(NAME); }
    public PlanetaryUpgradeSystem(String name) { super(name); }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % 200 != 0 || !isMars(player.world)
                || !ProgressionState.has(player, "ai_age")) return;
        PlanetaryUpgradeSystem data = get(player.world);
        int centerX = player.chunkCoordX, centerZ = player.chunkCoordZ;
        int budget = 8;
        for (int radius = 0; radius <= 6 && budget > 0; radius++) {
            for (int dx = -radius; dx <= radius && budget > 0; dx++) {
                for (int dz = -radius; dz <= radius && budget > 0; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int x = centerX + dx, z = centerZ + dz;
                    if (!player.world.getChunkProvider().isChunkGeneratedAt(x, z)) continue;
                    long key = ChunkPos.asLong(x, z);
                    if (!data.processed.add(key)) continue;
                    CivilizationWorldGenerator.generatePostAiMarsChunk(player.world, x, z);
                    budget--;
                }
            }
        }
        if (budget < 8) data.markDirty();
    }

    private static boolean isMars(World world) {
        return world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT).contains("mars");
    }

    private static PlanetaryUpgradeSystem get(World world) {
        PlanetaryUpgradeSystem data = (PlanetaryUpgradeSystem) world.getPerWorldStorage()
            .getOrLoadData(PlanetaryUpgradeSystem.class, NAME);
        if (data == null) { data = new PlanetaryUpgradeSystem(); world.getPerWorldStorage().setData(NAME, data); }
        return data;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        processed.clear();
        NBTTagList list = tag.getTagList("Chunks", 10);
        for (int i=0;i<list.tagCount();i++) processed.add(list.getCompoundTagAt(i).getLong("Key"));
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Long key : processed) { NBTTagCompound n=new NBTTagCompound(); n.setLong("Key",key); list.appendTag(n); }
        tag.setTag("Chunks",list); return tag;
    }
}
