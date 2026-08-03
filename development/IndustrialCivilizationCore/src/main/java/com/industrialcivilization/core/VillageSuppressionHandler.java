package com.industrialcivilization.core;

import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** New worlds use three bounded primitive settlements instead of endless villages. */
public final class VillageSuppressionHandler {
    @SubscribeEvent
    public void replaceVanillaVillageGenerator(InitMapGenEvent event) {
        if (event.getType() == InitMapGenEvent.EventType.VILLAGE) {
            // ChunkGeneratorOverworld casts this generator to MapGenVillage.
            // Preserve that contract while rejecting every village candidate.
            event.setNewGen(new MapGenVillage() {
                @Override
                protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
                    return false;
                }
            });
        }
    }
}
