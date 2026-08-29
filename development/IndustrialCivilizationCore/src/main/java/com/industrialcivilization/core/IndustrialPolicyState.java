package com.industrialcivilization.core;

import net.minecraft.nbt.NBTTagCompound;

/** Persistent, deliberately small policy vocabulary for one observable facility. */
final class IndustrialPolicyState {
    static final int ACTION_COOLDOWN_TICKS = 100;
    static final int RESERVE_HYSTERESIS = 2;

    String facilityName = "unnamed facility";
    String facilityRole = "general industry";
    String reserveItem = "";
    int minimumReserve;
    int preferredReserve;
    int priority = 1;
    int observedReserve;
    int routePeers;
    int emergencyEU;
    int emergencyPercent;
    boolean enabled;
    boolean manualOverride;
    boolean noncriticalLoadRegistered;
    boolean noncriticalLoadShed;
    String loadSide = "north";
    String action = "idle";
    String blocker = "policy disabled";
    long nextActionTick;
    boolean shortageObserved;
    boolean productionRequested;
    boolean freightRequested;
    boolean loadShedObserved;

    String manifestDestination = "";
    String manifestItem = "";
    int manifestRequested;
    int manifestDelivered;
    String manifestStatus = "none";
    String manifestFailure = "";
    long manifestCreatedTick;
    long manifestLastTransferTick;

    String serviceProgram = "";
    String servicePhase = "unconfigured";
    int commissioningTicks;
    boolean serviceCommissioned;
    String serviceBlocker = "program not configured";

    void configureReserve(String item, int minimum, int preferred, int requestedPriority) {
        reserveItem = item == null ? "" : item.trim();
        minimumReserve = Math.max(0, minimum);
        preferredReserve = Math.max(minimumReserve > 0 ? minimumReserve + RESERVE_HYSTERESIS : 0,
            preferred);
        priority = Math.max(0, Math.min(9, requestedPriority));
        blocker = reserveItem.isEmpty() ? "no reserve resource configured" : "awaiting observation";
    }

    void configureEmergency(int eu, int percent) {
        emergencyEU = Math.max(0, eu);
        emergencyPercent = Math.max(0, Math.min(100, percent));
    }

    int emergencyThreshold(int capacity) {
        return Math.max(emergencyEU, (int) Math.ceil(capacity * emergencyPercent / 100D));
    }

    boolean belowMinimum(int observed) {
        return !reserveItem.isEmpty() && observed < minimumReserve;
    }

    boolean recovered(int observed) {
        return reserveItem.isEmpty() || observed >= preferredReserve;
    }

    boolean outranks(IndustrialPolicyState other) {
        return other != null && enabled && !manualOverride && priority > other.priority
            && !reserveItem.isEmpty() && reserveItem.equals(other.reserveItem);
    }

    boolean canAct(long tick) {
        return enabled && !manualOverride && tick >= nextActionTick;
    }

    void acted(long tick, String newAction, String newBlocker) {
        action = newAction;
        blocker = newBlocker == null ? "" : newBlocker;
        nextActionTick = tick + ACTION_COOLDOWN_TICKS;
    }

    void requestManifest(String destination, String item, int count, long tick) {
        manifestDestination = destination == null ? "" : destination.trim();
        manifestItem = item == null ? "" : item.trim();
        manifestRequested = Math.max(1, count);
        manifestDelivered = 0;
        manifestStatus = "pending";
        manifestFailure = "";
        manifestCreatedTick = tick;
        manifestLastTransferTick = 0L;
    }

    void recordDelivery(long tick) {
        manifestDelivered = Math.min(manifestRequested, manifestDelivered + 1);
        manifestLastTransferTick = tick;
        manifestStatus = manifestDelivered >= manifestRequested ? "delivered" : "in_transit";
        manifestFailure = "";
    }

    void failManifest(String reason) {
        if (!"delivered".equals(manifestStatus)) manifestStatus = "blocked";
        manifestFailure = reason == null ? "unknown route failure" : reason;
    }

    NBTTagCompound write() {
        NBTTagCompound n = new NBTTagCompound();
        n.setString("FacilityName", facilityName); n.setString("FacilityRole", facilityRole);
        n.setString("ReserveItem", reserveItem); n.setInteger("MinimumReserve", minimumReserve);
        n.setInteger("PreferredReserve", preferredReserve); n.setInteger("PolicyPriority", priority);
        n.setInteger("ObservedReserve", observedReserve); n.setInteger("RoutePeers", routePeers);
        n.setInteger("EmergencyEU", emergencyEU); n.setInteger("EmergencyPercent", emergencyPercent);
        n.setBoolean("PolicyEnabled", enabled); n.setBoolean("ManualOverride", manualOverride);
        n.setBoolean("LoadRegistered", noncriticalLoadRegistered);
        n.setBoolean("LoadShed", noncriticalLoadShed); n.setString("LoadSide", loadSide);
        n.setString("PolicyAction", action); n.setString("PolicyBlocker", blocker);
        n.setLong("NextActionTick", nextActionTick);
        n.setBoolean("ShortageObserved", shortageObserved);
        n.setBoolean("ProductionRequested", productionRequested);
        n.setBoolean("FreightRequested", freightRequested);
        n.setBoolean("LoadShedObserved", loadShedObserved);
        n.setString("ManifestDestination", manifestDestination); n.setString("ManifestItem", manifestItem);
        n.setInteger("ManifestRequested", manifestRequested); n.setInteger("ManifestDelivered", manifestDelivered);
        n.setString("ManifestStatus", manifestStatus); n.setString("ManifestFailure", manifestFailure);
        n.setLong("ManifestCreatedTick", manifestCreatedTick);
        n.setLong("ManifestLastTransferTick", manifestLastTransferTick);
        n.setString("ServiceProgram", serviceProgram); n.setString("ServicePhase", servicePhase);
        n.setInteger("CommissioningTicks", commissioningTicks);
        n.setBoolean("ServiceCommissioned", serviceCommissioned);
        n.setString("ServiceBlocker", serviceBlocker);
        return n;
    }

    void read(NBTTagCompound n) {
        facilityName = value(n.getString("FacilityName"), "unnamed facility");
        facilityRole = value(n.getString("FacilityRole"), "general industry");
        reserveItem = n.getString("ReserveItem"); minimumReserve = Math.max(0, n.getInteger("MinimumReserve"));
        preferredReserve = Math.max(minimumReserve > 0 ? minimumReserve + RESERVE_HYSTERESIS : 0,
            n.getInteger("PreferredReserve"));
        priority = Math.max(0, Math.min(9, n.getInteger("PolicyPriority")));
        observedReserve = Math.max(0, n.getInteger("ObservedReserve"));
        routePeers = Math.max(0, n.getInteger("RoutePeers"));
        emergencyEU = Math.max(0, n.getInteger("EmergencyEU"));
        emergencyPercent = Math.max(0, Math.min(100, n.getInteger("EmergencyPercent")));
        enabled = n.getBoolean("PolicyEnabled"); manualOverride = n.getBoolean("ManualOverride");
        noncriticalLoadRegistered = n.getBoolean("LoadRegistered"); noncriticalLoadShed = n.getBoolean("LoadShed");
        loadSide = value(n.getString("LoadSide"), "north"); action = value(n.getString("PolicyAction"), "idle");
        blocker = value(n.getString("PolicyBlocker"), "policy disabled"); nextActionTick = n.getLong("NextActionTick");
        shortageObserved = n.getBoolean("ShortageObserved");
        productionRequested = n.getBoolean("ProductionRequested");
        freightRequested = n.getBoolean("FreightRequested");
        loadShedObserved = n.getBoolean("LoadShedObserved");
        manifestDestination = n.getString("ManifestDestination"); manifestItem = n.getString("ManifestItem");
        manifestRequested = Math.max(0, n.getInteger("ManifestRequested"));
        manifestDelivered = Math.max(0, n.getInteger("ManifestDelivered"));
        manifestStatus = value(n.getString("ManifestStatus"), "none"); manifestFailure = n.getString("ManifestFailure");
        manifestCreatedTick = n.getLong("ManifestCreatedTick"); manifestLastTransferTick = n.getLong("ManifestLastTransferTick");
        serviceProgram = n.getString("ServiceProgram"); servicePhase = value(n.getString("ServicePhase"), "unconfigured");
        commissioningTicks = Math.max(0, n.getInteger("CommissioningTicks"));
        serviceCommissioned = n.getBoolean("ServiceCommissioned");
        serviceBlocker = value(n.getString("ServiceBlocker"), "program not configured");
    }

    private static String value(String actual, String fallback) {
        return actual == null || actual.isEmpty() ? fallback : actual;
    }
}
