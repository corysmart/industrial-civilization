package com.industrialcivilization.core;

import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;

/** Breathable human astronaut citizen; faction/trade data is shared with Earth. */
public final class EntitySpaceCitizen extends EntityVillager implements IEntityBreathable {
    public EntitySpaceCitizen(World world) { super(world); }
    @Override public boolean canBreath() { return true; }
}
