package com.industrialcivilization.core;

import java.util.UUID;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/** Awards runtime criteria shared by the visible advancement tree and Better Questing. */
public final class RuntimeAdvancements {
    public static void grant(EntityPlayer player, String milestone) {
        grant(player, milestone, "runtime_event");
    }

    public static void grant(EntityPlayer player, String milestone, String evidenceSource) {
        grant(player, IndustrialCivilizationCore.MODID, milestone, evidenceSource);
    }

    public static void grant(EntityPlayer player, String namespace, String path,
            String evidenceSource) {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        Advancement advancement = serverPlayer.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(namespace, path));
        if (advancement == null) return;
        AdvancementProgress progress = serverPlayer.getAdvancements().getProgress(advancement);
        for (String criterion : progress.getRemaningCriteria()) {
            serverPlayer.getAdvancements().grantCriterion(advancement, criterion);
        }
        UnifiedAdvancementSystem.synchronizePort(serverPlayer, namespace + ":" + path);
        ProgressionState.record(player,
            IndustrialCivilizationCore.MODID.equals(namespace) ? path : namespace + ":" + path,
            evidenceSource);
    }

    public static EntityPlayerMP playerFor(TileIndustrialMachine tile, UUID id) {
        if (id == null || tile.getWorld() == null || tile.getWorld().getMinecraftServer() == null) return null;
        return tile.getWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(id);
    }

    public static boolean completed(EntityPlayer player, String milestone) {
        if (!(player instanceof EntityPlayerMP)) return false;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        Advancement advancement = serverPlayer.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(IndustrialCivilizationCore.MODID, milestone));
        return advancement != null && serverPlayer.getAdvancements().getProgress(advancement).isDone();
    }

    private RuntimeAdvancements() {}
}
