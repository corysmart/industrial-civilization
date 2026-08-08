package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/** Small persistent state layer used by runtime gates and machine outputs. */
public final class ProgressionState {
    private static final String ROOT = "IndustrialCivilization";
    private static final String COMPLETION_TIMES = "MilestoneCompletionTimes";
    private static final String COMPLETION_SOURCES = "MilestoneCompletionSources";

    public static void record(EntityPlayer player, String milestone) {
        record(player, milestone, "runtime");
    }

    /** Record the first authoritative completion time and evidence source. */
    public static void record(EntityPlayer player, String milestone, String source) {
        NBTTagCompound root = data(player);
        if (!root.getBoolean(milestone)) {
            NBTTagCompound times = compound(root, COMPLETION_TIMES);
            NBTTagCompound sources = compound(root, COMPLETION_SOURCES);
            times.setLong(milestone, Math.max(0L, root.getLong("active_ticks") / 20L));
            sources.setString(milestone, source == null ? "runtime" : source);
        }
        root.setBoolean(milestone, true);
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

    public static long completionSeconds(EntityPlayer player, String milestone) {
        return compound(data(player), COMPLETION_TIMES).getLong(milestone);
    }

    public static String completionSource(EntityPlayer player, String milestone) {
        return compound(data(player), COMPLETION_SOURCES).getString(milestone);
    }

    private static NBTTagCompound compound(NBTTagCompound parent, String key) {
        if (!parent.hasKey(key, 10)) parent.setTag(key, new NBTTagCompound());
        return parent.getCompoundTag(key);
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
