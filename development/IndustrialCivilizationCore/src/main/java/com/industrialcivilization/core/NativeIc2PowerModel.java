package com.industrialcivilization.core;

/** Pure work/packet math shared by the machine tile and automated tests. */
public final class NativeIc2PowerModel {
    private NativeIc2PowerModel() {}

    public static long totalWorkEU(int baselineEUPerTick, int baselineTicks) {
        return (long) baselineEUPerTick * baselineTicks;
    }

    public static double migrateLegacyProgress(int legacyTicks, int legacyDuration,
            long totalWorkEU) {
        if (legacyTicks <= 0 || legacyDuration <= 0 || totalWorkEU <= 0) return 0;
        double fraction = Math.min(1D, legacyTicks / (double) legacyDuration);
        return totalWorkEU * fraction;
    }

    public static double usableWorkEU(double storedEU, double directEU,
            double acceptedSinceLastTickEU, int baselineEUPerTick,
            double workRemainingEU, boolean scalingEnabled, boolean multiPacketEnabled) {
        double available = Math.max(0D, storedEU) + Math.max(0D, directEU);
        double throughput = baselineEUPerTick;
        if (scalingEnabled) {
            throughput = Math.max(throughput, Math.max(0D, acceptedSinceLastTickEU));
            if (!multiPacketEnabled) throughput = Math.min(throughput, baselineEUPerTick);
        }
        return Math.max(0D, Math.min(Math.min(available, throughput), workRemainingEU));
    }

    public static int estimateTicksRemaining(double workCompletedEU, long totalWorkEU,
            int elapsedTicks, int minimumTicks, int baselineEUPerTick,
            double acceptedEUPerTick, boolean scalingEnabled, boolean multiPacketEnabled) {
        double rate = baselineEUPerTick;
        if (scalingEnabled) {
            rate = Math.max(rate, Math.max(0D, acceptedEUPerTick));
            if (!multiPacketEnabled) rate = Math.min(rate, baselineEUPerTick);
        }
        double remaining = Math.max(0D, totalWorkEU - workCompletedEU);
        int energyTicks = rate <= 0D ? Integer.MAX_VALUE
            : (int) Math.ceil(remaining / rate);
        int elapsedTicksNeeded = Math.max(0, minimumTicks - elapsedTicks);
        return Math.max(energyTicks, elapsedTicksNeeded);
    }

    /** Mirrors IC2 Classic's per-delivery sink-tier safety comparison. */
    public static boolean isLegalPacket(double deliveredPacketEU, int maximumPacketEU) {
        return deliveredPacketEU <= maximumPacketEU;
    }

    public static int simulateDuration(long totalWorkEU, int baselineEUPerTick,
            int minimumTicks, int maximumPacketEU, int packetEU, int packetsPerTick) {
        if (!isLegalPacket(packetEU, maximumPacketEU)) return -1;
        long work = 0;
        int ticks = 0;
        long aggregate = (long) packetEU * packetsPerTick;
        while (work < totalWorkEU || ticks < minimumTicks) {
            ticks++;
            work = Math.min(totalWorkEU, work + aggregate);
        }
        return ticks;
    }
}
