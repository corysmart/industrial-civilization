package com.industrialcivilization.core;

import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** New worlds use three bounded primitive settlements instead of endless villages. */
public final class VillageSuppressionHandler {
    @SubscribeEvent
    public void replaceVanillaVillageGenerator(InitMapGenEvent event) {
        if (event.getType() == InitMapGenEvent.EventType.VILLAGE) {
            event.setNewGen(new MapGenBase() {});
        }
    }
}
