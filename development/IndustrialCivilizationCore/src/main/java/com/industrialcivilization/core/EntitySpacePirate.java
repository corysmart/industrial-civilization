package com.industrialcivilization.core;

import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import net.minecraft.world.World;

/** Human lunar/Martian robber replacement; never an undead astronaut. */
public final class EntitySpacePirate extends EntityRobber implements IEntityBreathable {
    public EntitySpacePirate(World world) { super(world); }
    @Override public boolean canBreath() { return true; }
}
