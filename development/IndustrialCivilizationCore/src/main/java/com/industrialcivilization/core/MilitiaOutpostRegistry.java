package com.industrialcivilization.core;

import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

/** Persistent generated-outpost coordinates used for witness-independent takedown tracking. */
public final class MilitiaOutpostRegistry extends WorldSavedData {
    private static final String NAME = IndustrialCivilizationCore.MODID + "_militia_outposts";
    private NBTTagList outposts = new NBTTagList();

    public MilitiaOutpostRegistry() { super(NAME); }
    public MilitiaOutpostRegistry(String name) { super(name); }

    public static void record(World world, BlockPos origin) {
        MilitiaOutpostRegistry registry = get(world);
        String id = id(origin);
        for (int index = 0; index < registry.outposts.tagCount(); index++) {
            if (id.equals(registry.outposts.getCompoundTagAt(index).getString("Id"))) return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Id", id);
        tag.setInteger("X", origin.getX());
        tag.setInteger("Y", origin.getY());
        tag.setInteger("Z", origin.getZ());
        registry.outposts.appendTag(tag);
        registry.markDirty();
    }

    @Nullable
    public static String nearby(World world, BlockPos pos, int radius) {
        MilitiaOutpostRegistry registry = get(world);
        double limit = radius * radius;
        for (int index = 0; index < registry.outposts.tagCount(); index++) {
            NBTTagCompound tag = registry.outposts.getCompoundTagAt(index);
            BlockPos origin = new BlockPos(tag.getInteger("X"), tag.getInteger("Y"), tag.getInteger("Z"));
            if (origin.distanceSq(pos) <= limit) return tag.getString("Id");
        }
        return null;
    }

    private static MilitiaOutpostRegistry get(World world) {
        MilitiaOutpostRegistry data = (MilitiaOutpostRegistry) world.getPerWorldStorage()
            .getOrLoadData(MilitiaOutpostRegistry.class, NAME);
        if (data == null) {
            data = new MilitiaOutpostRegistry();
            world.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }

    private static String id(BlockPos origin) { return origin.getX() + "_" + origin.getZ(); }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        outposts = nbt.getTagList("Outposts", 10);
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Outposts", outposts);
        return compound;
    }
}
