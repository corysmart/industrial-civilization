package com.industrialcivilization.core;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Drives named scenarios through a real client and integrated server. */
public final class ClientE2ERunner {
    private static final int TITLE_SETTLE_TICKS = 100;
    private static final int WORLD_SETTLE_TICKS = 600;
    private int state;
    private int ticks;

    ClientE2ERunner() {}

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        String scenario = IndustrialCivilizationCore.E2E_AUTO_SCENARIO;
        if (scenario.isEmpty()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (state == 0) {
            // A true headless LWJGL shim can reach a stable title loop without
            // exposing a concrete GuiScreen, so world absence is the reliable gate.
            if (minecraft.world != null || ++ticks < TITLE_SETTLE_TICKS) return;
            state = 1;
            ticks = 0;
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|WORLD_LAUNCH|scenario={}", scenario);
            WorldSettings settings = new WorldSettings(8675309L, GameType.CREATIVE, true, false, WorldType.FLAT);
            settings.setGeneratorOptions("3;minecraft:bedrock,2*minecraft:dirt,minecraft:grass;1;");
            minecraft.launchIntegratedServer("ic-e2e-" + safeName(scenario), "Industrial Civilization E2E", settings);
            return;
        }
        if (state == 1) {
            if (minecraft.player == null || minecraft.world == null || minecraft.getIntegratedServer() == null) return;
            if (++ticks < WORLD_SETTLE_TICKS) return;
            state = 2;
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCENARIO_START|{}", scenario);
            final String playerName = minecraft.player.getName();
            final MinecraftServer server = minecraft.getIntegratedServer();
            server.addScheduledTask(new Runnable() {
                @Override public void run() {
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                    if (player == null) {
                        IndustrialCivilizationCore.LOGGER.error("IC_E2E|SCENARIO_EXCEPTION|{}|player_missing", scenario);
                        return;
                    }
                    CommandIndustrialTest command = new CommandIndustrialTest();
                    try {
                        command.execute(server, player, new String[] {"scenario", scenario});
                        command.execute(server, player, new String[] {"snapshot", "64"});
                        IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCENARIO_COMMANDS_COMPLETE|{}", scenario);
                    } catch (CommandException exception) {
                        IndustrialCivilizationCore.LOGGER.error("IC_E2E|SCENARIO_EXCEPTION|" + scenario, exception);
                    }
                }
            });
            state = 3;
        }
    }

    private static String safeName(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }
}
