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
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        Advancement advancement = serverPlayer.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(IndustrialCivilizationCore.MODID, milestone));
        if (advancement == null) return;
        AdvancementProgress progress = serverPlayer.getAdvancements().getProgress(advancement);
        for (String criterion : progress.getRemaningCriteria()) {
            serverPlayer.getAdvancements().grantCriterion(advancement, criterion);
        }
        ProgressionState.record(player, milestone);
    }

    public static EntityPlayerMP playerFor(TileIndustrialMachine tile, UUID id) {
        if (id == null || tile.getWorld() == null || tile.getWorld().getMinecraftServer() == null) return null;
        return tile.getWorld().getMinecraftServer().getPlayerList().getPlayerByUUID(id);
    }

    private RuntimeAdvancements() {}
}
