package com.industrialcivilization.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

/** Operator aid for filming naturally generated civilization structures. */
public final class CommandIndustrialLocateAll extends CommandBase {
    public static final String[] TARGETS = {
        "primitive_settlement", "militia_outpost", "industrial_city", "abandoned_factory",
        "factory_steel", "factory_electronics", "factory_fuel", "factory_armaments",
        "factory_research", "regional_road"
    };
    private static final int DEFAULT_RADIUS_BLOCKS = 8192;
    private static final int CANDIDATES_PER_TARGET = 32;

    @Override public String getName() { return "ic_locate_all"; }
    @Override public String getUsage(ICommandSender sender) { return "/ic_locate_all [radius-blocks]"; }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        int radius = args.length == 0 ? DEFAULT_RADIUS_BLOCKS : parseInt(args[0], 1024, 32768);
        if (args.length > 1) throw new CommandException(getUsage(sender));
        locateAll(player, radius, true);
    }

    public static Map<String, BlockPos> locateAll(EntityPlayerMP player, int radiusBlocks,
            boolean sendChat) throws CommandException {
        if (player.dimension != 0)
            throw new CommandException("Run /ic_locate_all in the Overworld. Apollo memorials are Moon-only.");
        WorldServer world = player.getServerWorld();
        int centerChunkX = Math.floorDiv(player.getPosition().getX(), 16);
        int centerChunkZ = Math.floorDiv(player.getPosition().getZ(), 16);
        int radiusChunks = (radiusBlocks + 15) / 16;
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        Map<String, List<Candidate>> candidates = new LinkedHashMap<>();
        for (String target : TARGETS) candidates.put(target, new ArrayList<Candidate>());

        for (int chunkX = centerChunkX - radiusChunks; chunkX <= centerChunkX + radiusChunks; chunkX++) {
            for (int chunkZ = centerChunkZ - radiusChunks; chunkZ <= centerChunkZ + radiusChunks; chunkZ++) {
                long dx = (long) chunkX * 16L + 8L - player.getPosition().getX();
                long dz = (long) chunkZ * 16L + 8L - player.getPosition().getZ();
                long distanceSquared = dx * dx + dz * dz;
                if (distanceSquared > radiusSquared) continue;
                String structure = CivilizationWorldGenerator.predictedOverworldStructure(
                    world, chunkX, chunkZ);
                if (structure != null && candidates.containsKey(structure)
                        && CivilizationWorldGenerator.predictedOverworldBiomeSuitable(
                            world, chunkX, chunkZ, structure))
                    retainCandidate(candidates.get(structure),
                        new Candidate(chunkX, chunkZ, distanceSquared));
                if (CivilizationWorldGenerator.predictedOverworldRoad(world, chunkX, chunkZ)
                        && CivilizationWorldGenerator.predictedOverworldBiomeSuitable(
                            world, chunkX, chunkZ, "regional_road"))
                    retainCandidate(candidates.get("regional_road"),
                        new Candidate(chunkX, chunkZ, distanceSquared));
            }
        }

        Map<String, BlockPos> results = new LinkedHashMap<>();
        for (String target : TARGETS) {
            long targetStarted = System.nanoTime();
            int attempts = 0;
            if ("regional_road".equals(target)) {
                int prepared = 0;
                for (Candidate candidate : candidates.get(target)) {
                    if (nearLocatedStructure(candidate, results, 128)) continue;
                    populateCandidate(world, candidate.chunkX, candidate.chunkZ);
                    if (++prepared >= 8) break;
                }
            }
            for (Candidate candidate : candidates.get(target)) {
                attempts++;
                if ("regional_road".equals(target)
                        && nearLocatedStructure(candidate, results, 128)) continue;
                populateCandidate(world, candidate.chunkX, candidate.chunkZ);
                BlockPos anchor;
                if ("regional_road".equals(target)) {
                    anchor = new BlockPos(candidate.chunkX * 16 + 8, 0, candidate.chunkZ * 16 + 8);
                } else {
                    anchor = CivilizationStructureData.findGeneratedAnchor(
                        world, target, candidate.chunkX, candidate.chunkZ);
                    if (anchor == null) continue;
                }
                int width = "regional_road".equals(target) ? 16
                    : CivilizationWorldGenerator.structureWidth(target);
                BlockPos center = new BlockPos(anchor.getX() + width / 2, 0,
                    anchor.getZ() + width / 2);
                world.getChunkFromBlockCoords(center);
                BlockPos destination = world.getHeight(center);
                CivilizationWorldGenerator.prepareAccessRoad(world, anchor, target);
                results.put(target, destination);
                break;
            }
            IndustrialCivilizationCore.LOGGER.info(
                "IC_LOCATOR|TARGET|id={}|candidates={}|attempts={}|found={}|elapsed_ms={}",
                target, candidates.get(target).size(), attempts, results.containsKey(target),
                (System.nanoTime() - targetStarted) / 1000000L);
        }

        if (sendChat) sendResults(player, results, radiusBlocks);
        return results;
    }

    /** Locates multiple distinct naturally generated cities for nation-network validation. */
    public static List<BlockPos> locateIndustrialCities(EntityPlayerMP player, int radiusBlocks,
            int count) throws CommandException {
        if (player.dimension != 0) throw new CommandException("Run city location in the Overworld.");
        WorldServer world = player.getServerWorld();
        int centerChunkX = Math.floorDiv(player.getPosition().getX(), 16);
        int centerChunkZ = Math.floorDiv(player.getPosition().getZ(), 16);
        int radiusChunks = (radiusBlocks + 15) / 16;
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        List<Candidate> candidates = new ArrayList<>();
        for (int chunkX = centerChunkX - radiusChunks; chunkX <= centerChunkX + radiusChunks; chunkX++) {
            for (int chunkZ = centerChunkZ - radiusChunks; chunkZ <= centerChunkZ + radiusChunks; chunkZ++) {
                long dx = (long) chunkX * 16L + 8L - player.getPosition().getX();
                long dz = (long) chunkZ * 16L + 8L - player.getPosition().getZ();
                long distanceSquared = dx * dx + dz * dz;
                if (distanceSquared <= radiusSquared
                        && "industrial_city".equals(CivilizationWorldGenerator
                            .predictedOverworldStructure(world, chunkX, chunkZ))
                        && CivilizationWorldGenerator.predictedOverworldBiomeSuitable(
                            world, chunkX, chunkZ, "industrial_city"))
                    retainCandidate(candidates, new Candidate(chunkX, chunkZ, distanceSquared));
            }
        }
        List<BlockPos> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            populateCandidate(world, candidate.chunkX, candidate.chunkZ);
            BlockPos anchor = CivilizationStructureData.findGeneratedAnchor(world,
                "industrial_city", candidate.chunkX, candidate.chunkZ);
            if (anchor == null) continue;
            BlockPos center = world.getHeight(anchor.add(
                CivilizationWorldGenerator.structureWidth("industrial_city") / 2, 0,
                CivilizationWorldGenerator.structureWidth("industrial_city") / 2));
            populateCandidate(world, Math.floorDiv(center.getX(), 16),
                Math.floorDiv(center.getZ(), 16));
            boolean duplicate = false;
            for (BlockPos existing : results) if (existing.distanceSq(center) < 128D * 128D) {
                duplicate = true;
                break;
            }
            if (!duplicate) results.add(center);
            if (results.size() >= count) break;
        }
        return results;
    }

    /** Selects a road only after delayed world generators and road repairs have settled. */
    public static BlockPos locateConfirmedGeneratedRoad(EntityPlayerMP player, int radiusBlocks,
            Map<String, BlockPos> locatedStructures) {
        WorldServer world = player.getServerWorld();
        BlockPos searchCenter = world.getSpawnPoint();
        int centerChunkX = Math.floorDiv(searchCenter.getX(), 16);
        int centerChunkZ = Math.floorDiv(searchCenter.getZ(), 16);
        int radiusChunks = (radiusBlocks + 15) / 16;
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        List<Candidate> candidates = new ArrayList<>();
        for (int chunkX = centerChunkX - radiusChunks; chunkX <= centerChunkX + radiusChunks; chunkX++) {
            for (int chunkZ = centerChunkZ - radiusChunks; chunkZ <= centerChunkZ + radiusChunks; chunkZ++) {
                long dx = (long) chunkX * 16L + 8L - searchCenter.getX();
                long dz = (long) chunkZ * 16L + 8L - searchCenter.getZ();
                long distanceSquared = dx * dx + dz * dz;
                if (distanceSquared > radiusSquared
                        || !CivilizationWorldGenerator.predictedOverworldRoad(world, chunkX, chunkZ)
                        || !CivilizationWorldGenerator.predictedOverworldBiomeSuitable(
                            world, chunkX, chunkZ, "regional_road")) continue;
                retainCandidate(candidates, new Candidate(chunkX, chunkZ, distanceSquared));
            }
        }
        for (Candidate candidate : candidates) {
            if (nearLocatedStructure(candidate, locatedStructures, 128)
                    || !world.isChunkGeneratedAt(candidate.chunkX, candidate.chunkZ)) continue;
            Chunk chunk = world.getChunkProvider().provideChunk(candidate.chunkX, candidate.chunkZ);
            if (!chunk.isTerrainPopulated() || !hasIntactRoad(world, candidate)) continue;
            BlockPos center = new BlockPos(candidate.chunkX * 16 + 8, 0,
                candidate.chunkZ * 16 + 8);
            return world.getHeight(center);
        }
        return null;
    }

    private static boolean hasIntactRoad(WorldServer world, Candidate candidate) {
        boolean northSouth = Math.floorMod(candidate.chunkX, 8) == 0;
        BlockPos spawn = world.getSpawnPoint();
        long dx = (long) candidate.chunkX * 16L + 8L - spawn.getX();
        long dz = (long) candidate.chunkZ * 16L + 8L - spawn.getZ();
        Block expected = dx * dx + dz * dz < 1700L * 1700L
            ? net.minecraft.init.Blocks.DIRT : net.minecraft.init.Blocks.DOUBLE_STONE_SLAB;
        int intact = 0;
        for (int step = 0; step < 16; step++) for (int lane = -1; lane <= 1; lane++) {
            int x = candidate.chunkX * 16 + (northSouth ? 8 + lane : step);
            int z = candidate.chunkZ * 16 + (northSouth ? step : 8 + lane);
            BlockPos road = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).down();
            if (world.getBlockState(road).getBlock() == expected) intact++;
        }
        return intact >= 44;
    }

    private static boolean nearLocatedStructure(Candidate candidate,
            Map<String, BlockPos> results, int clearance) {
        long x = (long) candidate.chunkX * 16L + 8L;
        long z = (long) candidate.chunkZ * 16L + 8L;
        long clearanceSquared = (long) clearance * clearance;
        for (Map.Entry<String, BlockPos> entry : results.entrySet()) {
            if ("regional_road".equals(entry.getKey())) continue;
            long dx = x - entry.getValue().getX();
            long dz = z - entry.getValue().getZ();
            if (dx * dx + dz * dz < clearanceSquared) return true;
        }
        return false;
    }

    /** Positions the creative camera around a confirmed natural-world destination. */
    public static void positionForReview(EntityPlayerMP player, String id, BlockPos center,
            int angle) throws CommandException {
        if (angle < 0 || angle >= CommandIndustrialShowcase.ANGLES_PER_STRUCTURE)
            throw new CommandException("Invalid natural review angle: " + (angle + 1));
        int width = "regional_road".equals(id) ? 16
            : CivilizationWorldGenerator.structureWidth(id);
        double targetX = center.getX() + 0.5D;
        double targetY = center.getY() + Math.max(2.0D, width * 0.12D);
        double targetZ = center.getZ() + 0.5D;
        double distance = Math.max(15.0D, width * 0.95D);
        double elevation = Math.max(11.0D, width * 0.72D);
        double[][] offsets = {{0, elevation, -distance},
            {distance, elevation, 0}, {0, width * 0.82D, 0}};
        if ("regional_road".equals(id))
            offsets = new double[][] {{0, 16, -18}, {18, 16, 0}, {0, 26, 0}};
        double[] offset = offsets[angle];
        double cameraX = targetX + offset[0];
        double cameraY = targetY + offset[1];
        double cameraZ = targetZ + offset[2];
        double dx = targetX - cameraX;
        double dy = targetY - cameraY;
        double dz = targetZ - cameraZ;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        player.sendPlayerAbilities();
        player.connection.setPlayerLocation(cameraX, cameraY, cameraZ, yaw, pitch);
    }

    private static void populateCandidate(WorldServer world, int chunkX, int chunkZ) {
        ChunkProviderServer provider = world.getChunkProvider();
        Chunk chunk = provider.provideChunk(chunkX, chunkZ);
        if (!chunk.isTerrainPopulated()) {
            // Chunk#populate deliberately waits for this 2x2 terrain square so
            // decorations cannot write into chunks whose base terrain is absent.
            provider.provideChunk(chunkX + 1, chunkZ);
            provider.provideChunk(chunkX, chunkZ + 1);
            provider.provideChunk(chunkX + 1, chunkZ + 1);
            chunk.populate(provider, provider.chunkGenerator);
        }
    }

    private static void retainCandidate(List<Candidate> candidates, Candidate candidate) {
        int index = 0;
        while (index < candidates.size()
                && candidates.get(index).distanceSquared <= candidate.distanceSquared) index++;
        candidates.add(index, candidate);
        if (candidates.size() > CANDIDATES_PER_TARGET)
            candidates.remove(candidates.size() - 1);
    }

    private static void sendResults(EntityPlayerMP player, Map<String, BlockPos> results,
            int radiusBlocks) {
        player.sendMessage(new TextComponentString("Nearest confirmed Industrial Civilization structures"));
        for (String target : TARGETS) {
            BlockPos pos = results.get(target);
            if (pos == null) {
                player.sendMessage(new TextComponentString("- " + displayName(target)
                    + ": none within " + radiusBlocks + " blocks"));
                continue;
            }
            TextComponentString line = new TextComponentString("- " + displayName(target)
                + ": " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            String teleport = "/tp " + player.getName() + " "
                + pos.getX() + " " + pos.getY() + " " + pos.getZ();
            TextComponentString button = new TextComponentString(" [TELEPORT]");
            button.setStyle(new Style().setColor(TextFormatting.AQUA).setBold(true)
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleport))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new TextComponentString(teleport))));
            line.appendSibling(button);
            player.sendMessage(line);
        }
        player.sendMessage(new TextComponentString(
            "Apollo memorials are fixed lunar sites; run the showcase or travel to the Moon to film them."));
    }

    private static String displayName(String id) {
        StringBuilder text = new StringBuilder();
        for (String word : id.split("_")) {
            if (text.length() > 0) text.append(' ');
            text.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return text.toString();
    }

    private static final class Candidate {
        final int chunkX;
        final int chunkZ;
        final long distanceSquared;

        Candidate(int chunkX, int chunkZ, long distanceSquared) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.distanceSquared = distanceSquared;
        }
    }
}
