package com.industrialcivilization.core;

import betterquesting.client.gui2.GuiHome;
import betterquesting.client.gui2.GuiQuestLines;
import java.io.File;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
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
    private int videoFrame;
    private int showcaseShot;
    private int showcaseFailures;
    private volatile Map<String, BlockPos> naturalReviewLocations;
    private int naturalReviewShot;
    private int naturalCapturedShots;
    private int naturalPositionRequestedShot = -1;
    private volatile int naturalPositionedShot = -1;
    private int naturalSettlingShot = -1;
    private int naturalRoadRefineTicks;
    private int naturalRoadRefineAttempts;
    private boolean naturalRoadRefineRequested;
    private volatile boolean naturalRoadRefined;
    private boolean naturalRoadValidationRequested;
    private volatile boolean naturalRoadValidationComplete;
    private int naturalAccessWarmupIndex;
    private int naturalAccessWarmupTicks;
    private boolean naturalAccessWarmupPositioned;
    private int questUiShot;
    private int questUiFailures;

    ClientE2ERunner() {}

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        String scenario = IndustrialCivilizationCore.E2E_AUTO_SCENARIO;
        if (scenario.isEmpty()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (state == 3 && Boolean.getBoolean("ic.e2e.captureFrames")
                && minecraft.world != null && ++ticks % 4 == 0) {
            File frames = new File(minecraft.mcDataDir, "screenshots/quarry-video");
            if (frames.isDirectory() || frames.mkdirs()) {
                ScreenShotHelper.saveScreenshot(minecraft.mcDataDir,
                    String.format(java.util.Locale.ROOT,
                        "quarry-video/frame-%05d.png", videoFrame++),
                    minecraft.displayWidth, minecraft.displayHeight, minecraft.getFramebuffer());
            }
        }
        if (state == 0) {
            // A true headless LWJGL shim can reach a stable title loop without
            // exposing a concrete GuiScreen, so world absence is the reliable gate.
            if (minecraft.world != null || ++ticks < TITLE_SETTLE_TICKS) return;
            state = 1;
            ticks = 0;
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|WORLD_LAUNCH|scenario={}", scenario);
            boolean naturalWorld = "worldgen_locator".equals(scenario)
                || "worldgen_natural_review".equals(scenario)
                || "worldgen_natural_road_review".equals(scenario)
                || "faction_side_path".equals(scenario)
                || "vehicle_logistics_path".equals(scenario);
            long worldSeed = Long.getLong("ic.e2e.worldSeed", 8675309L);
            WorldSettings settings = new WorldSettings(worldSeed, GameType.CREATIVE, true, false,
                naturalWorld ? WorldType.DEFAULT : WorldType.FLAT);
            if (!naturalWorld)
                settings.setGeneratorOptions("3;minecraft:bedrock,2*minecraft:dirt,minecraft:grass;1;");
            minecraft.launchIntegratedServer("ic-e2e-" + safeName(scenario), "Industrial Civilization E2E", settings);
            return;
        }
        if (state == 1) {
            if (minecraft.player == null || minecraft.world == null || minecraft.getIntegratedServer() == null) return;
            if (++ticks < WORLD_SETTLE_TICKS) return;
            state = 2;
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCENARIO_START|{}|seed={}",
                scenario, Long.getLong("ic.e2e.worldSeed", 8675309L));
            if ("advancement_ui".equals(scenario)) {
                minecraft.displayGuiScreen(new GuiScreenAdvancements(
                    minecraft.getConnection().getAdvancementManager()));
                ticks = 0;
                state = 4;
                return;
            }
            if ("quest_ui".equals(scenario)) {
                minecraft.displayGuiScreen(new GuiHome(null));
                ticks = 0;
                questUiShot = 0;
                questUiFailures = 0;
                File questUiDirectory = new File(minecraft.mcDataDir, "screenshots/quest-ui");
                if (!questUiDirectory.isDirectory() && !questUiDirectory.mkdirs())
                    questUiFailures++;
                state = 7;
                return;
            }
            if (Boolean.getBoolean("ic.e2e.captureFrames")) {
                // Preserve the action bar: narrated acceptance recordings are
                // much easier to audit when each physical phase is labelled.
                minecraft.gameSettings.hideGUI = false;
            }
            final String playerName = minecraft.player.getName();
            final MinecraftServer server = minecraft.getIntegratedServer();
            if ("worldgen_showcase".equals(scenario)) {
                minecraft.gameSettings.renderDistanceChunks = Math.max(
                    minecraft.gameSettings.renderDistanceChunks, 12);
                minecraft.gameSettings.clouds = 0;
                server.addScheduledTask(new Runnable() {
                    @Override public void run() {
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                        try {
                            if (player == null) throw new CommandException("player_missing");
                            CommandIndustrialShowcase.buildGallery(player);
                        } catch (CommandException exception) {
                            IndustrialCivilizationCore.LOGGER.error(
                                "IC_E2E|SCENARIO_EXCEPTION|worldgen_showcase", exception);
                        }
                    }
                });
                minecraft.gameSettings.hideGUI = true;
                File showcaseDirectory = new File(minecraft.mcDataDir,
                    "screenshots/worldgen-showcase");
                if (!showcaseDirectory.isDirectory() && !showcaseDirectory.mkdirs()) {
                    IndustrialCivilizationCore.LOGGER.error(
                        "IC_E2E|SCENARIO_EXCEPTION|worldgen_showcase|screenshot_directory");
                    showcaseFailures = 1;
                } else {
                    showcaseFailures = 0;
                }
                // IC2 needs time to settle two city grids plus five factory
                // grids before power is asserted; the 4-second window became
                // flaky after adding the second city-variation proof.
                ticks = -240;
                showcaseShot = 0;
                state = 5;
                return;
            }
            if ("worldgen_locator".equals(scenario)) {
                server.addScheduledTask(new Runnable() {
                    @Override public void run() {
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                        try {
                            if (player == null) throw new CommandException("player_missing");
                            int found = CommandIndustrialLocateAll.locateAll(player, 8192, true).size();
                            boolean pass = found == CommandIndustrialLocateAll.TARGETS.length;
                            IndustrialCivilizationCore.LOGGER.info(
                                "IC_TEST|{}|worldgen_locator|found={}|expected={}",
                                pass ? "PASS" : "FAIL", found,
                                CommandIndustrialLocateAll.TARGETS.length);
                            IndustrialCivilizationCore.LOGGER.info(
                                "IC_TEST|SNAPSHOT|{\"schema\":1,\"scenario\":\"worldgen_locator\",\"result\":\"{}\"}",
                                pass ? "pass" : "fail");
                        } catch (CommandException exception) {
                            IndustrialCivilizationCore.LOGGER.error(
                                "IC_E2E|SCENARIO_EXCEPTION|worldgen_locator", exception);
                            IndustrialCivilizationCore.LOGGER.info(
                                "IC_TEST|FAIL|worldgen_locator|reason=command_exception");
                        }
                    }
                });
                state = 3;
                return;
            }
            if ("worldgen_natural_review".equals(scenario)
                    || "worldgen_natural_road_review".equals(scenario)) {
                minecraft.gameSettings.renderDistanceChunks = 12;
                minecraft.gameSettings.clouds = 0;
                minecraft.gameSettings.hideGUI = true;
                File reviewDirectory = new File(minecraft.mcDataDir,
                    "screenshots/worldgen-natural-review");
                showcaseFailures = reviewDirectory.isDirectory() || reviewDirectory.mkdirs() ? 0 : 1;
                naturalReviewLocations = null;
                naturalReviewShot = "worldgen_natural_road_review".equals(scenario)
                    ? (CommandIndustrialLocateAll.TARGETS.length - 1)
                        * CommandIndustrialShowcase.ANGLES_PER_STRUCTURE
                    : 0;
                naturalCapturedShots = 0;
                naturalPositionRequestedShot = -1;
                naturalPositionedShot = -1;
                naturalSettlingShot = -1;
                naturalRoadRefineTicks = 0;
                naturalRoadRefineAttempts = 0;
                naturalRoadRefineRequested = false;
                naturalRoadRefined = false;
                naturalRoadValidationRequested = false;
                naturalRoadValidationComplete = false;
                naturalAccessWarmupIndex = 0;
                naturalAccessWarmupTicks = 0;
                naturalAccessWarmupPositioned = false;
                ticks = -120;
                server.addScheduledTask(new Runnable() {
                    @Override public void run() {
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                        try {
                            if (player == null) throw new CommandException("player_missing");
                            // Perspective cameras can sit five chunks from a city center;
                            // the integrated server otherwise clamps this pack to four.
                            server.getPlayerList().setViewDistance(12);
                            naturalReviewLocations = CommandIndustrialLocateAll.locateAll(
                                player, 8192, false);
                            if (naturalReviewLocations.size()
                                    != CommandIndustrialLocateAll.TARGETS.length) {
                                showcaseFailures++;
                                IndustrialCivilizationCore.LOGGER.info(
                                    "IC_TEST|FAIL|{}|reason=missing_locations|found={}|expected={}",
                                    scenario,
                                    naturalReviewLocations.size(),
                                    CommandIndustrialLocateAll.TARGETS.length);
                                state = 3;
                                return;
                            }
                        } catch (CommandException exception) {
                            showcaseFailures++;
                            IndustrialCivilizationCore.LOGGER.error(
                                "IC_E2E|SCENARIO_EXCEPTION|" + scenario, exception);
                            naturalReviewLocations = java.util.Collections.emptyMap();
                        }
                    }
                });
                state = 6;
                return;
            }
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
                        // The civilization PASS line already contains every causal subcheck.
                        // Its showcase recording should frame the built facility instead of
                        // covering it with the much larger generic JSON snapshot.
                        if (!"civilization_systems".equals(scenario))
                            command.execute(server, player, new String[] {"snapshot", "64"});
                        IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCENARIO_COMMANDS_COMPLETE|{}", scenario);
                    } catch (CommandException exception) {
                        IndustrialCivilizationCore.LOGGER.error("IC_E2E|SCENARIO_EXCEPTION|" + scenario, exception);
                    }
                }
            });
            state = 3;
        }
        if (state == 5) {
            if (++ticks <= 0) return;
            final int structure = showcaseShot / CommandIndustrialShowcase.ANGLES_PER_STRUCTURE;
            final int angle = showcaseShot % CommandIndustrialShowcase.ANGLES_PER_STRUCTURE;
            if (structure >= CommandIndustrialShowcase.STRUCTURES.length) {
                IndustrialCivilizationCore.LOGGER.info("IC_TEST|{}|worldgen_showcase|structures={}|angles={}|screenshots={}|capture_failures={}",
                    showcaseFailures == 0 ? "PASS" : "FAIL",
                    CommandIndustrialShowcase.STRUCTURES.length,
                    CommandIndustrialShowcase.ANGLES_PER_STRUCTURE, showcaseShot, showcaseFailures);
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|SNAPSHOT|{\"schema\":1,\"scenario\":\"worldgen_showcase\",\"result\":\"{}\"}",
                    showcaseFailures == 0 ? "pass" : "fail");
                state = 3;
                return;
            }
            if (ticks == 1 && naturalPositionedShot != naturalReviewShot) {
                final String playerName = minecraft.player.getName();
                final MinecraftServer server = minecraft.getIntegratedServer();
                server.addScheduledTask(new Runnable() {
                    @Override public void run() {
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                        try {
                            if (player == null) throw new CommandException("player_missing");
                            if (showcaseShot == 0)
                                CommandIndustrialShowcase.validatePoweredFactories(player);
                            CommandIndustrialShowcase.positionForView(player, structure, angle);
                        } catch (CommandException exception) {
                            showcaseFailures++;
                            IndustrialCivilizationCore.LOGGER.error(
                                "IC_E2E|SCENARIO_EXCEPTION|worldgen_showcase", exception);
                        }
                    }
                });
            }
            if (ticks >= 60) {
                String id = CommandIndustrialShowcase.STRUCTURES[structure];
                String name = String.format(java.util.Locale.ROOT,
                    "worldgen-showcase/%02d-%s-angle-%d.png", structure + 1, id, angle + 1);
                String result = ScreenShotHelper.saveScreenshot(minecraft.mcDataDir, name,
                    minecraft.displayWidth, minecraft.displayHeight,
                    minecraft.getFramebuffer()).getUnformattedText();
                if (result.startsWith("Couldn't")) showcaseFailures++;
                IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCREENSHOT|{}|{}",
                    id, result);
                showcaseShot++;
                ticks = 0;
            }
        }
        if (state == 6) {
            if (naturalReviewLocations == null) return;
            if (naturalAccessWarmupIndex < 2) {
                final String connected = naturalAccessWarmupIndex == 0
                    ? "primitive_settlement" : "industrial_city";
                final BlockPos center = naturalReviewLocations.get(connected);
                if (!naturalAccessWarmupPositioned) {
                    naturalAccessWarmupPositioned = true;
                    final String playerName = minecraft.player.getName();
                    final MinecraftServer server = minecraft.getIntegratedServer();
                    server.addScheduledTask(new Runnable() {
                        @Override public void run() {
                            EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                            if (player == null || center == null) {
                                showcaseFailures++;
                                naturalAccessWarmupTicks = 440;
                                return;
                            }
                            player.connection.setPlayerLocation(center.getX() + 0.5D,
                                center.getY() + 24.0D, center.getZ() + 0.5D, 0.0F, 90.0F);
                            int half = CivilizationWorldGenerator.structureWidth(connected) / 2;
                            CivilizationWorldGenerator.prepareAccessRoad(server.getWorld(0),
                                new BlockPos(center.getX() - half, center.getY(),
                                    center.getZ() - half), connected);
                            IndustrialCivilizationCore.LOGGER.info(
                                "IC_TEST|STATE|{}|phase=warming_{}_road_repairs",
                                scenario, connected);
                        }
                    });
                }
                if (++naturalAccessWarmupTicks < 440) return;
                naturalAccessWarmupIndex++;
                naturalAccessWarmupTicks = 0;
                naturalAccessWarmupPositioned = false;
                return;
            }
            final int structure = naturalReviewShot / CommandIndustrialShowcase.ANGLES_PER_STRUCTURE;
            final int angle = naturalReviewShot % CommandIndustrialShowcase.ANGLES_PER_STRUCTURE;
            if (structure >= CommandIndustrialLocateAll.TARGETS.length) {
                if (!naturalRoadValidationRequested) {
                    naturalRoadValidationRequested = true;
                    final MinecraftServer server = minecraft.getIntegratedServer();
                    server.addScheduledTask(new Runnable() {
                        @Override public void run() {
                            for (String connected : new String[] {
                                    "primitive_settlement", "industrial_city"}) {
                                BlockPos center = naturalReviewLocations.get(connected);
                                if (center == null) continue;
                                int half = CivilizationWorldGenerator.structureWidth(connected) / 2;
                                BlockPos origin = new BlockPos(center.getX() - half, center.getY(),
                                    center.getZ() - half);
                                String problem = CivilizationWorldGenerator.validateAccessRoad(
                                    server.getWorld(0), origin, connected);
                                if (problem != null) {
                                    showcaseFailures++;
                                    IndustrialCivilizationCore.LOGGER.info(
                                        "IC_TEST|FAIL|{}|reason={}_road_{}",
                                        scenario, connected, problem);
                                } else {
                                    IndustrialCivilizationCore.LOGGER.info(
                                        "IC_TEST|PASS|{}_road_connection|structure={}",
                                        scenario, connected);
                                }
                            }
                            naturalRoadValidationComplete = true;
                        }
                    });
                    return;
                }
                if (!naturalRoadValidationComplete) return;
                boolean pass = showcaseFailures == 0
                    && naturalReviewLocations.size() == CommandIndustrialLocateAll.TARGETS.length;
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|{}|{}|structures={}|angles={}|screenshots={}|capture_failures={}",
                    pass ? "PASS" : "FAIL", scenario,
                    naturalReviewLocations.size(),
                    CommandIndustrialShowcase.ANGLES_PER_STRUCTURE,
                    naturalCapturedShots, showcaseFailures);
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|SNAPSHOT|{\"schema\":1,\"scenario\":\"{}\",\"result\":\"{}\"}",
                    scenario, pass ? "pass" : "fail");
                state = 3;
                return;
            }
            final String id = CommandIndustrialLocateAll.TARGETS[structure];
            if ("regional_road".equals(id) && !naturalRoadRefined) {
                int delay = "worldgen_natural_road_review".equals(scenario) ? 500 : 0;
                if (++naturalRoadRefineTicks < delay) return;
                if (!naturalRoadRefineRequested) {
                    naturalRoadRefineRequested = true;
                    final String playerName = minecraft.player.getName();
                    final MinecraftServer server = minecraft.getIntegratedServer();
                    server.addScheduledTask(new Runnable() {
                        @Override public void run() {
                            naturalRoadRefineAttempts++;
                            EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                            BlockPos road = player == null ? null
                                : CommandIndustrialLocateAll.locateConfirmedGeneratedRoad(
                                    player, 8192, naturalReviewLocations);
                            if (road == null) {
                                if (naturalRoadRefineAttempts >= 5) {
                                    showcaseFailures++;
                                    IndustrialCivilizationCore.LOGGER.info(
                                        "IC_TEST|FAIL|{}|reason=no_intact_generated_road", scenario);
                                    naturalRoadRefined = true;
                                } else {
                                    // Loading a generated candidate lets the pending road
                                    // repair run at WorldTick END; retry after one second.
                                    naturalRoadRefineTicks = Math.max(0, delay - 20);
                                    naturalRoadRefineRequested = false;
                                }
                            } else {
                                Map<String, BlockPos> refined = new java.util.LinkedHashMap<>(
                                    naturalReviewLocations);
                                refined.put("regional_road", road);
                                naturalReviewLocations = refined;
                                naturalRoadRefined = true;
                            }
                        }
                    });
                }
                return;
            }
            final BlockPos center = naturalReviewLocations.get(id);
            if (naturalPositionRequestedShot != naturalReviewShot) {
                final int shotIndex = naturalReviewShot;
                naturalPositionRequestedShot = shotIndex;
                final String playerName = minecraft.player.getName();
                final MinecraftServer server = minecraft.getIntegratedServer();
                server.addScheduledTask(new Runnable() {
                    @Override public void run() {
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
                        try {
                            if (player == null) throw new CommandException("player_missing");
                            if (center == null) throw new CommandException("missing_location_" + id);
                            CommandIndustrialLocateAll.positionForReview(player, id, center, angle);
                            naturalPositionedShot = shotIndex;
                        } catch (CommandException exception) {
                            showcaseFailures++;
                            IndustrialCivilizationCore.LOGGER.error(
                                "IC_E2E|SCENARIO_EXCEPTION|" + scenario, exception);
                        }
                    }
                });
                return;
            }
            if (naturalPositionedShot != naturalReviewShot) return;
            if (naturalSettlingShot != naturalReviewShot) {
                naturalSettlingShot = naturalReviewShot;
                ticks = 0;
                return;
            }
            int settleTicks = "industrial_city".equals(id) ? 240 : 120;
            if (++ticks >= settleTicks) {
                String name = String.format(java.util.Locale.ROOT,
                    "worldgen-natural-review/%02d-%s-angle-%d.png",
                    structure + 1, id, angle + 1);
                String result = ScreenShotHelper.saveScreenshot(minecraft.mcDataDir, name,
                    minecraft.displayWidth, minecraft.displayHeight,
                    minecraft.getFramebuffer()).getUnformattedText();
                if (result.startsWith("Couldn't")) showcaseFailures++;
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_E2E|NATURAL_SCREENSHOT|{}|{}", id, result);
                naturalReviewShot++;
                naturalCapturedShots++;
                ticks = 0;
            }
        }
        if (state == 4 && ++ticks >= 40) {
            String result = minecraft.currentScreen instanceof GuiIndustrialAdvancements
                ? ((GuiIndustrialAdvancements) minecraft.currentScreen).exerciseTwoAxisPanForTest()
                : "FAIL|advancement_ui|reason=wrong_screen_"
                    + (minecraft.currentScreen == null ? "null"
                        : minecraft.currentScreen.getClass().getName());
            IndustrialCivilizationCore.LOGGER.info("IC_TEST|{}", result);
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|SNAPSHOT|{\"schema\":1,\"scenario\":\"advancement_ui\",\"result\":\"{}\"}",
                result.startsWith("PASS") ? "pass" : "fail");
            minecraft.getFramebuffer().bindFramebuffer(true);
            GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.clear(16640);
            minecraft.entityRenderer.setupOverlayRendering();
            minecraft.currentScreen.drawScreen(
                minecraft.currentScreen.width / 2, minecraft.currentScreen.height / 2, 0.0F);
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCREENSHOT|{}",
                ScreenShotHelper.saveScreenshot(minecraft.mcDataDir, minecraft.displayWidth,
                    minecraft.displayHeight, minecraft.getFramebuffer()).getUnformattedText());
            IndustrialCivilizationCore.LOGGER.info(
                "IC_E2E|SCENARIO_COMMANDS_COMPLETE|advancement_ui");
            state = 3;
        }
        if (state == 7 && ++ticks >= 40) {
            String screen = questUiShot == 0 ? "home" : "first_chapter";
            boolean correct = questUiShot == 0
                ? minecraft.currentScreen instanceof GuiHome
                : minecraft.currentScreen instanceof GuiQuestLines;
            if (!correct) {
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|FAIL|quest_ui|reason=wrong_{}_screen|actual={}", screen,
                    minecraft.currentScreen == null ? "null"
                        : minecraft.currentScreen.getClass().getName());
                state = 3;
                return;
            }
            String result = ScreenShotHelper.saveScreenshot(minecraft.mcDataDir,
                "quest-ui/" + (questUiShot + 1) + "-" + screen + ".png",
                minecraft.displayWidth, minecraft.displayHeight,
                minecraft.getFramebuffer()).getUnformattedText();
            if (result.startsWith("Couldn't")) questUiFailures++;
            IndustrialCivilizationCore.LOGGER.info("IC_E2E|SCREENSHOT|{}|{}", screen, result);
            if (questUiShot++ == 0) {
                minecraft.displayGuiScreen(new GuiQuestLines(minecraft.currentScreen));
                ticks = 0;
                return;
            }
            int lines = betterquesting.questing.QuestLineDatabase.INSTANCE.getSortedEntries().size();
            boolean pass = lines == 26 && questUiFailures == 0;
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|{}|quest_ui|quest_lines={}|expected=26|screenshots=2|capture_failures={}",
                pass ? "PASS" : "FAIL", lines, questUiFailures);
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|SNAPSHOT|{\"schema\":1,\"scenario\":\"quest_ui\",\"result\":\"{}\"}",
                pass ? "pass" : "fail");
            state = 3;
        }
    }

    private static String safeName(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }
}
