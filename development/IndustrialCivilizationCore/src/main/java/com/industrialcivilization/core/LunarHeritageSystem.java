package com.industrialcivilization.core;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Backfills historically mapped Apollo markers into already-existing Moon chunks. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class LunarHeritageSystem {
    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (event.getWorld().isRemote) return;
        String dimension = event.getWorld().provider.getDimensionType().getName()
            .toLowerCase(java.util.Locale.ROOT);
        if (!dimension.contains("moon")) return;
        CivilizationWorldGenerator.generateLunarHeritageFlags(
            event.getChunk().x, event.getChunk().z, event.getWorld());
    }
    private LunarHeritageSystem() {}
}
