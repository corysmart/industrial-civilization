package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/** Small persistent state layer used by runtime gates and machine outputs. */
public final class ProgressionState {
    private static final String ROOT = "IndustrialCivilization";

    public static void record(EntityPlayer player, String milestone) {
        data(player).setBoolean(milestone, true);
    }

    public static boolean has(EntityPlayer player, String milestone) {
        return data(player).getBoolean(milestone);
    }

    public static void increment(EntityPlayer player, String counter, long amount) {
        NBTTagCompound data = data(player);
        data.setLong(counter, data.getLong(counter) + amount);
    }

    public static long counter(EntityPlayer player, String counter) {
        return data(player).getLong(counter);
    }

    public static NBTTagCompound data(EntityPlayer player) {
        NBTTagCompound entity = player.getEntityData();
        NBTTagCompound persisted;
        if (entity.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) {
            persisted = entity.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        } else {
            persisted = new NBTTagCompound();
            entity.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        if (!persisted.hasKey(ROOT, 10)) {
            persisted.setTag(ROOT, new NBTTagCompound());
        }
        return persisted.getCompoundTag(ROOT);
    }

    private ProgressionState() {}
}
