package com.industrialcivilization.core;

import java.util.Collections;
import java.util.UUID;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketAdvancementInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;

/** Awards runtime criteria shared by the visible advancement tree and Better Questing. */
public final class RuntimeAdvancements {
    public static boolean grant(EntityPlayer player, String milestone) {
        return grant(player, milestone, "runtime_event");
    }

    public static boolean grant(EntityPlayer player, String milestone, String evidenceSource) {
        return grant(player, IndustrialCivilizationCore.MODID, milestone, evidenceSource);
    }

    public static boolean grant(EntityPlayer player, String namespace, String path,
            String evidenceSource) {
        if (!(player instanceof EntityPlayerMP)) return false;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        if (serverPlayer instanceof FakePlayer) return false;
        Advancement advancement = serverPlayer.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(namespace, path));
        if (advancement == null) {
            IndustrialCivilizationCore.LOGGER.warn(
                "Runtime advancement is not loaded: {}:{}", namespace, path);
            return false;
        }
        // Use the advancement definition as the source of truth. In 1.12,
        // AdvancementProgress#getRemaningCriteria() can be empty for a newly
        // loaded minecraft:impossible criterion even though the definition
        // contains a grantable criterion. Granting each defined criterion is
        // idempotent and also works for partially completed advancements.
        AdvancementProgress progress = serverPlayer.getAdvancements().getProgress(advancement);
        String progressBefore = progress.toString();
        boolean completedBefore = progress.isDone();
        boolean grantedAny = false;
        boolean directGrant = false;
        for (String criterion : advancement.getCriteria().keySet()) {
            boolean granted = serverPlayer.getAdvancements().grantCriterion(advancement, criterion);
            grantedAny |= granted;
            CriterionProgress criterionProgress = progress.getCriterionProgress(criterion);
            if (!granted && criterionProgress != null && !criterionProgress.isObtained()) {
                // Sledgehammer's 1.12 PlayerAdvancements redirect can reject
                // minecraft:impossible criteria even for a real connected
                // Galacticraft player. Obtain that criterion directly, then
                // reproduce the vanilla completion side effects below.
                criterionProgress.obtain();
                directGrant = true;
            }
        }
        boolean completed = progress.isDone();
        if (directGrant) {
            if (!completedBefore && completed) {
                advancement.getRewards().apply(serverPlayer);
                ForgeHooks.onAdvancement(serverPlayer, advancement);
            }
            serverPlayer.connection.sendPacket(new SPacketAdvancementInfo(false,
                Collections.singleton(advancement), Collections.<ResourceLocation>emptySet(),
                Collections.singletonMap(advancement.getId(), progress)));
            serverPlayer.getAdvancements().save();
        }
        if (IndustrialCivilizationCore.TEST_BRIDGE_ENABLED
                && IndustrialCivilizationCore.MODID.equals(namespace)
                && "mobile_quarry_relocation".equals(path)) {
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|ADVANCEMENT_GRANT|id={}:{}|criteria={}|before={}|granted_any={}"
                + "|direct_grant={}|after={}|completed={}|player_class={}", namespace, path,
                advancement.getCriteria().keySet(), progressBefore, grantedAny,
                directGrant, progress, completed,
                serverPlayer.getClass().getName());
        }
        if (completed) {
            UnifiedAdvancementSystem.synchronizePort(serverPlayer, namespace + ":" + path);
            ProgressionState.record(player,
                IndustrialCivilizationCore.MODID.equals(namespace) ? path : namespace + ":" + path,
                evidenceSource);
        }
        return completed;
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

    public static boolean loaded(EntityPlayer player, String milestone) {
        if (!(player instanceof EntityPlayerMP)) return false;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        return serverPlayer.getServer().getAdvancementManager().getAdvancement(
            new ResourceLocation(IndustrialCivilizationCore.MODID, milestone)) != null;
    }

    private RuntimeAdvancements() {}
}
