package com.industrialcivilization.core;

/** Pure deterministic rules shared with the headless mechanics test suite. */
public final class GameplayRules {
    public static float machineGuiScale(int screenWidth, int screenHeight) {
        float widthScale = Math.max(0, screenWidth) / 427.0F;
        float heightScale = Math.max(0, screenHeight) / 240.0F;
        return Math.max(1.0F, Math.min(widthScale, heightScale));
    }

    public static int questHomeTitleWidth(int backdropWidth, int backdropHeight) {
        int proportional = (int) (Math.max(0, backdropWidth) * 0.65F);
        int widthLimit = Math.max(256, backdropWidth - 16);
        int heightLimit = Math.max(256, Math.max(128, backdropHeight - 16) * 2);
        return Math.max(256, Math.min(proportional, Math.min(widthLimit, heightLimit)));
    }

    public static float questMinimumZoom(int viewportWidth, int viewportHeight,
            int backgroundSize) {
        if (backgroundSize <= 0) return 1.0F;
        float coverWidth = Math.max(0, viewportWidth) / (float) backgroundSize;
        float coverHeight = Math.max(0, viewportHeight) / (float) backgroundSize;
        return Math.min(2.0F, Math.max(coverWidth, coverHeight));
    }

    public static int questBoundedScroll(int requestedScroll, int viewportPixels,
            int backgroundSize, float zoom) {
        if (backgroundSize <= 0 || zoom <= 0.0F) return 0;
        int visibleBackgroundPixels = (int) Math.ceil(
            Math.max(0, viewportPixels) / (double) zoom);
        int maximumScroll = Math.max(0, backgroundSize - visibleBackgroundPixels);
        return Math.max(0, Math.min(maximumScroll, requestedScroll));
    }

    public static boolean robberSpawnAllowed(int roll, int percent, int nearby,
            int localCap, boolean forced) {
        if (forced) return true;
        int chance = Math.max(0, Math.min(100, percent));
        return roll >= 0 && roll < chance && nearby < Math.max(1, localCap);
    }

    public static boolean robberTargetsPlayer(boolean carriesTechnicalOrValuableLoot,
            boolean retaliating) {
        return carriesTechnicalOrValuableLoot || retaliating;
    }

    public static boolean militiaPatrolSpawnAllowed(boolean nearRegisteredOutpost,
            int nearby, int localCap, boolean forced) {
        return forced || (nearRegisteredOutpost && nearby < Math.max(1, localCap));
    }

    public static boolean aiAgeReady(boolean hasAiCore, boolean liteMatterComplete,
            boolean hasMartianAutonomyArchive) {
        return hasAiCore && liteMatterComplete && hasMartianAutonomyArchive;
    }

    public static boolean suppressVanillaEarthHostile(String domain, String path) {
        if (!"minecraft".equals(domain) || path == null) return false;
        // Zombies and skeletons are replaced before this policy is evaluated.
        // Every other vanilla EntityMob is outside the grounded Earth ecology.
        return !"zombie".equals(path) && !"zombie_villager".equals(path)
            && !"husk".equals(path) && !"skeleton".equals(path)
            && !"stray".equals(path);
    }

    public static int planeTargets(int radius) {
        int width = radius * 2 + 1;
        return Math.max(0, width * width - 1);
    }

    public static int harvestTicks(int targets, int perTick) {
        return targets <= 0 ? 0 : (targets + Math.max(1, perTick) - 1) / Math.max(1, perTick);
    }

    public static int payableTargets(int availableEnergy, int energyPerBlock, int requested) {
        if (requested <= 0) return 0;
        if (energyPerBlock <= 0) return requested;
        return Math.max(0, Math.min(requested, availableEnergy / energyPerBlock));
    }

    public static boolean completeArmor(boolean[] equippedAndUsable) {
        if (equippedAndUsable == null || equippedAndUsable.length != 4) return false;
        for (boolean usable : equippedAndUsable) if (!usable) return false;
        return true;
    }

    public static boolean activeOxygenDetector(String registryName, int metadata) {
        return "galacticraftcore:oxygen_detector".equals(registryName) && metadata == 1;
    }

    public static String habitatMilestone(String environment) {
        return "orbit".equals(environment) ? "orbital_habitat" : environment + "_habitat";
    }

    public static long nextFunctionalStableSamples(long current, boolean completeInfrastructure) {
        if (!completeInfrastructure) return 0L;
        return Math.min(24L, Math.max(0L, current) + 1L);
    }

    public static int marketStage(int playerStage, int settlementCapacity) {
        return Math.max(0, Math.min(settlementCapacity, playerStage - 1));
    }

    public static int usedValue(int newPrice, int condition) {
        double health = Math.max(0.05D, Math.min(1.0D,
            condition / (double) MarketEconomy.NEW_CONDITION));
        return Math.max(1, (int) Math.floor(newPrice * 0.32D * health));
    }

    public static boolean scanCoordinateAllowed(int dx, int dy, int dz, boolean loaded) {
        return loaded && Math.abs(dx) <= 10 && Math.abs(dy) <= 6 && Math.abs(dz) <= 10;
    }

    private GameplayRules() {}
}
