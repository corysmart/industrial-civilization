package com.industrialcivilization.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/** Deterministic sequencing and BuildCraft completion rules shared with tests. */
public final class MobileQuarryRules {
    public static final int LANE_STEP = 16;

    public enum Output { BREAK, MOVE, DEPLOY }

    public static EnumFacing outputSide(EnumFacing facing, Output output) {
        EnumFacing horizontal = facing != null && facing.getAxis().isHorizontal()
            ? facing : EnumFacing.NORTH;
        if (output == Output.BREAK) return horizontal.rotateYCCW();
        if (output == Output.DEPLOY) return EnumFacing.DOWN;
        return horizontal.getOpposite();
    }

    public static EnumFacing powerQuerySide(EnumFacing facing, Output output) {
        return outputSide(facing, output).getOpposite();
    }

    public static boolean isOneStepForward(BlockPos origin, BlockPos current,
            EnumFacing facing) {
        if (origin == null || current == null) return false;
        EnumFacing horizontal = facing != null && facing.getAxis().isHorizontal()
            ? facing : EnumFacing.NORTH;
        return current.equals(origin.offset(horizontal));
    }

    public static BlockPos laneDestination(BlockPos current, EnumFacing facing) {
        EnumFacing horizontal = facing != null && facing.getAxis().isHorizontal()
            ? facing : EnumFacing.NORTH;
        return current.offset(horizontal, LANE_STEP);
    }

    /**
     * BuildCraft 7.99.24.8 persists an exhausted BoxIterator without a current
     * position. Requiring firstChecked prevents a fresh, uninitialized quarry
     * from being mistaken for a completed one.
     */
    public static boolean isBuildCraftQuarryComplete(NBTTagCompound state) {
        if (state == null || !state.getBoolean("firstChecked")
                || !state.hasKey("boxIterator", 10)
                || state.hasKey("currentTaskId")) return false;
        return !state.getCompoundTag("boxIterator").hasKey("current");
    }

    private MobileQuarryRules() {}
}
