package com.industrialcivilization.core;

import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import net.minecraft.world.World;

/** Neutral astronaut militia using the same provocation rules as Earth patrols. */
public final class EntitySpaceMilitia extends EntityMilitiaPatrol implements IEntityBreathable {
    public EntitySpaceMilitia(World world) { super(world); }
    @Override public boolean canBreath() { return true; }
}
