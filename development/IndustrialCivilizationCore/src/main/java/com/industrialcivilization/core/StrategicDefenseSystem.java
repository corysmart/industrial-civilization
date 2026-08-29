package com.industrialcivilization.core;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Awards strategic-defense progress from installed, powered and configured ICBM systems. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class StrategicDefenseSystem {
    private static final String OWNER = "IndustrialDefenseOwner";
    private static final double SITE_RANGE_SQ = 16D * 16D;

    @SubscribeEvent
    public static void placed(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || event.getPlayer() == null) return;
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (isDefenseTile(tile)) markOwner(tile, event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || event.world.getTotalWorldTime() % 20L != 0L) return;
        List<TileLauncherBase> bases = new ArrayList<>();
        List<TileLauncherScreen> screens = new ArrayList<>();
        List<TileRadarStation> radars = new ArrayList<>();
        for (TileEntity tile : event.world.loadedTileEntityList) {
            if (tile instanceof TileLauncherBase) bases.add((TileLauncherBase) tile);
            else if (tile instanceof TileLauncherScreen) screens.add((TileLauncherScreen) tile);
            else if (tile instanceof TileRadarStation) radars.add((TileRadarStation) tile);
        }
        for (TileLauncherBase base : bases) evaluate(base, screens, radars);
    }

    private static void evaluate(TileLauncherBase base, List<TileLauncherScreen> screens,
            List<TileRadarStation> radars) {
        UUID owner = owner(base);
        if (owner == null || base.getWorld().getMinecraftServer() == null) return;
        EntityPlayerMP player = base.getWorld().getMinecraftServer().getPlayerList()
            .getPlayerByUUID(owner);
        if (player == null) return;
        TileLauncherScreen operationalScreen = null;
        for (TileLauncherScreen screen : screens) {
            if (!owner.equals(owner(screen)) || screen.getPos().distanceSq(base.getPos()) > SITE_RANGE_SQ)
                continue;
            boolean connected = screen.getNetworkNode().getNetwork()
                == base.getNetworkNode().getNetwork();
            if (connected && base.energyStorage.getEnergyStored() > 0
                    && screen.energyStorage.getEnergyStored() > 0 && screen.getTarget() != null) {
                operationalScreen = screen;
                RuntimeAdvancements.grant(player, "icbm_launch_control");
                break;
            }
        }
        if (operationalScreen == null) return;
        for (TileRadarStation radar : radars) {
            if (owner.equals(owner(radar))
                    && radar.getPos().distanceSq(operationalScreen.getPos()) <= SITE_RANGE_SQ
                    && radar.energyStorage.getEnergyStored() > 0
                    && radar.getDetectionRange() > 0 && radar.getTriggerRange() > 0) {
                RuntimeAdvancements.grant(player, "icbm_radar_defense");
                break;
            }
        }
        ItemStack missile = base.getMissileStack();
        ResourceLocation id = missile.isEmpty() ? null : missile.getItem().getRegistryName();
        if (id != null && "icbmclassic:explosive_missile".equals(id.toString()))
            RuntimeAdvancements.grant(player, "icbm_conventional_missile");
    }

    static void markForTest(TileEntity tile, EntityPlayerMP player) {
        if (isDefenseTile(tile)) markOwner(tile, player.getUniqueID());
    }

    static void evaluateForTest(TileLauncherBase base) {
        List<TileLauncherScreen> screens = new ArrayList<>();
        List<TileRadarStation> radars = new ArrayList<>();
        for (TileEntity tile : base.getWorld().loadedTileEntityList) {
            if (tile instanceof TileLauncherScreen) screens.add((TileLauncherScreen) tile);
            else if (tile instanceof TileRadarStation) radars.add((TileRadarStation) tile);
        }
        evaluate(base, screens, radars);
    }

    private static boolean isDefenseTile(TileEntity tile) {
        return tile instanceof TileLauncherBase || tile instanceof TileLauncherScreen
            || tile instanceof TileRadarStation;
    }

    private static void markOwner(TileEntity tile, UUID owner) {
        tile.getTileData().setUniqueId(OWNER, owner);
        tile.markDirty();
    }

    private static UUID owner(TileEntity tile) {
        return tile.getTileData().hasUniqueId(OWNER) ? tile.getTileData().getUniqueId(OWNER) : null;
    }

    private StrategicDefenseSystem() {}
}
