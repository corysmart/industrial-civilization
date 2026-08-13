package com.industrialcivilization.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/** Mirrors hidden native advancement progress into the one visible IC tab. */
public final class UnifiedAdvancementSystem {
    private static final Map<String, String> PORTS = load();

    public static void synchronize(EntityPlayerMP player) {
        // Multiple passes resolve an entire already-completed native chain on
        // login without relying on JSON/map iteration order.
        boolean changed;
        int passes = 0;
        do {
            changed = false;
            for (Map.Entry<String, String> entry : PORTS.entrySet()) {
                changed |= grantIfEligible(player, entry.getKey(), entry.getValue());
            }
        } while (changed && ++passes < PORTS.size());
    }

    public static void synchronizePort(EntityPlayerMP player, String sourceId) {
        String port = PORTS.get(sourceId);
        if (port == null) return;
        grantIfEligible(player, sourceId, port);
    }

    private static boolean grantIfEligible(EntityPlayerMP player, String sourceId, String portId) {
        Advancement source = player.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(sourceId));
        Advancement target = player.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(portId));
        if (source == null || target == null
                || !player.getAdvancements().getProgress(source).isDone()) return false;
        Advancement parent = target.getParent();
        if (parent != null && !player.getAdvancements().getProgress(parent).isDone()) return false;
        AdvancementProgress progress = player.getAdvancements().getProgress(target);
        boolean changed = false;
        for (String criterion : progress.getRemaningCriteria()) {
            changed |= player.getAdvancements().grantCriterion(target, criterion);
        }
        return changed;
    }

    private static Map<String, String> load() {
        InputStream input = UnifiedAdvancementSystem.class.getResourceAsStream(
            "/assets/industrialcivilizationcore/ported_advancements.json");
        if (input == null) return Collections.emptyMap();
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), type);
    }

    private UnifiedAdvancementSystem() {}
}
