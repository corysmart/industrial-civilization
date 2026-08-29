package com.industrialcivilization.core;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Creative-only gallery and deterministic camera positions for first-party world generation. */
public final class CommandIndustrialShowcase extends CommandBase {
    public static final String[] STRUCTURES = {
        "primitive_settlement",
        "militia_outpost",
        "industrial_city",
        "abandoned_factory",
        "factory_steel",
        "factory_electronics",
        "factory_fuel",
        "factory_armaments",
        "factory_research",
        "regional_road",
        "apollo_11_memorial",
        "industrial_city_variant_b"
    };
    public static final int ANGLES_PER_STRUCTURE = 3;
    // Keep neighboring exhibits outside even the vertical overview camera's wide field of view.
    private static final int SPACING = 192;
    private static final String BASE_X = "ICShowcaseBaseX";
    private static final String BASE_Y = "ICShowcaseBaseY";
    private static final String BASE_Z = "ICShowcaseBaseZ";

    @Override public String getName() { return "ic_showcase"; }
    @Override public String getUsage(ICommandSender sender) {
        return "/ic_showcase <build|list|view <structure> <1|2|3>>";
    }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        requireCreative(player);
        if (args.length == 1 && "list".equals(args[0])) {
            player.sendMessage(new TextComponentString("Worldgen showcase: "
                + String.join(", ", STRUCTURES)));
            return;
        }
        if (args.length == 1 && "build".equals(args[0])) {
            buildGallery(player);
            player.sendMessage(new TextComponentString("Built " + STRUCTURES.length
                + " worldgen showcases. Use /ic_showcase view <structure> <1|2|3>."));
            return;
        }
        if (args.length == 3 && "view".equals(args[0])) {
            int index = structureIndex(args[1]);
            int angle = parseInt(args[2], 1, ANGLES_PER_STRUCTURE) - 1;
            positionForView(player, index, angle);
            player.sendMessage(new TextComponentString("Viewing " + STRUCTURES[index]
                + " from angle " + (angle + 1) + "."));
            return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "build", "list", "view");
        if (args.length == 2 && "view".equals(args[0]))
            return getListOfStringsMatchingLastWord(args, STRUCTURES);
        if (args.length == 3 && "view".equals(args[0]))
            return getListOfStringsMatchingLastWord(args, "1", "2", "3");
        return Collections.emptyList();
    }

    public static void buildGallery(EntityPlayerMP player) throws CommandException {
        requireCreative(player);
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        int baseX = Math.floorDiv(playerPos.getX() + 48, 16) * 16 + 1;
        int baseZ = Math.floorDiv(playerPos.getZ(), 16) * 16 + 1;
        int baseY = world.getHeight(new BlockPos(baseX + 7, 0, baseZ + 7)).getY();
        BlockPos base = new BlockPos(baseX, baseY, baseZ);
        player.getEntityData().setInteger(BASE_X, baseX);
        player.getEntityData().setInteger(BASE_Y, baseY);
        player.getEntityData().setInteger(BASE_Z, baseZ);

        for (int index = 0; index < STRUCTURES.length; index++) {
            BlockPos origin = origin(base, index);
            prepareSite(world, origin);
            if (!CivilizationWorldGenerator.buildShowcaseStructure(world, origin, STRUCTURES[index]))
                throw new CommandException("Unknown showcase structure: " + STRUCTURES[index]);
            if ("regional_road".equals(STRUCTURES[index]) && countRoadBlocks(world, origin) != 48)
                throw new CommandException("Showcase road did not place all 48 surface blocks.");
            String utilityProblem = CivilizationWorldGenerator.validateShowcaseUtilitySpine(
                world, origin, STRUCTURES[index]);
            if (utilityProblem != null)
                throw new CommandException("Malformed showcase utilities for "
                    + STRUCTURES[index] + ": " + utilityProblem);
            int center = CivilizationWorldGenerator.structureWidth(STRUCTURES[index]) / 2;
            CivilizationWorldGenerator.placeShowcaseLabel(world, origin.add(center, 1, -3),
                displayName(STRUCTURES[index]));
        }
        long cityA = CivilizationWorldGenerator.structureVariationSignature(world,
            origin(base, structureIndexUnchecked("industrial_city")), "industrial_city");
        long cityB = CivilizationWorldGenerator.structureVariationSignature(world,
            origin(base, structureIndexUnchecked("industrial_city_variant_b")), "industrial_city");
        if (cityA == cityB) throw new CommandException("Showcase city variants produced the same layout signature.");
        IndustrialCivilizationCore.LOGGER.info(
            "IC_SHOWCASE|CITY_VARIATION_VALIDATED|primary={}|secondary={}", cityA, cityB);
        enableFlight(player);
        positionForView(player, 0, 0);
        IndustrialCivilizationCore.LOGGER.info("IC_SHOWCASE|BUILT|structures={}|base={},{},{}",
            STRUCTURES.length, baseX, baseY, baseZ);
    }

    public static void positionForView(EntityPlayerMP player, int structureIndex, int angle)
            throws CommandException {
        requireCreative(player);
        if (structureIndex < 0 || structureIndex >= STRUCTURES.length)
            throw new CommandException("Invalid showcase structure index: " + structureIndex);
        if (angle < 0 || angle >= ANGLES_PER_STRUCTURE)
            throw new CommandException("Invalid showcase angle: " + (angle + 1));
        BlockPos base = savedBase(player);
        BlockPos targetOrigin = origin(base, structureIndex);
        String id = STRUCTURES[structureIndex];
        int width = CivilizationWorldGenerator.structureWidth(id);
        double centerX = targetOrigin.getX() + width / 2.0D;
        double centerY = targetOrigin.getY() + Math.max(4.0D, width * 0.17D);
        double centerZ = targetOrigin.getZ() + width / 2.0D;
        double distance = Math.max(15.0D, width * 0.95D);
        double elevation = Math.max(10.0D, width * 0.72D);
        double[][] offsets = {{-distance, elevation, -distance},
            {distance, elevation, -distance}, {0, width * 0.82D, 0}};
        if ("regional_road".equals(id)) {
            centerX = targetOrigin.getX() + 8.0D;
            centerY = targetOrigin.getY() + 0.5D;
            offsets = new double[][] {{-10, 7, -10}, {10, 7, -10}, {0, 17, 0}};
        } else if ("apollo_11_memorial".equals(id)) {
            centerX = targetOrigin.getX() + 5.5D;
            centerY = targetOrigin.getY() + 3.0D;
            centerZ = targetOrigin.getZ() + 7.0D;
            offsets = new double[][] {{-8, 5, -8}, {8, 5, -8}, {0, 12, 0}};
        }
        double[] offset = offsets[angle];
        double cameraX = centerX + offset[0];
        double cameraY = centerY + offset[1];
        double cameraZ = centerZ + offset[2];
        double dx = centerX - cameraX;
        double dy = centerY - cameraY;
        double dz = centerZ - cameraZ;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        enableFlight(player);
        player.connection.setPlayerLocation(cameraX, cameraY, cameraZ, yaw, pitch);
    }

    public static int structureIndex(String id) throws CommandException {
        for (int index = 0; index < STRUCTURES.length; index++)
            if (STRUCTURES[index].equals(id)) return index;
        throw new CommandException("Unknown showcase structure: " + id);
    }

    private static int structureIndexUnchecked(String id) {
        for (int index = 0; index < STRUCTURES.length; index++)
            if (STRUCTURES[index].equals(id)) return index;
        throw new IllegalStateException("Missing showcase target: " + id);
    }

    /** Checks live IC2 delivery after the gallery has been allowed to tick. */
    public static void validatePoweredFactories(EntityPlayerMP player) throws CommandException {
        BlockPos base = savedBase(player);
        int powered = 0;
        for (int index = 0; index < STRUCTURES.length; index++) {
            if (!STRUCTURES[index].startsWith("factory_")) continue;
            if (!CivilizationWorldGenerator.showcaseFactoryHasPower(
                    player.world, origin(base, index)))
                throw new CommandException("Showcase factory did not receive IC2 power: "
                    + STRUCTURES[index]);
            powered++;
        }
        IndustrialCivilizationCore.LOGGER.info(
            "IC_SHOWCASE|UTILITIES_VALIDATED|powered_factories={}|pipe_sites={}", powered, 6);
    }

    private static BlockPos savedBase(EntityPlayerMP player) throws CommandException {
        if (!player.getEntityData().hasKey(BASE_X) || !player.getEntityData().hasKey(BASE_Y)
                || !player.getEntityData().hasKey(BASE_Z))
            throw new CommandException("Run /ic_showcase build first.");
        return new BlockPos(player.getEntityData().getInteger(BASE_X),
            player.getEntityData().getInteger(BASE_Y), player.getEntityData().getInteger(BASE_Z));
    }

    private static BlockPos origin(BlockPos base, int index) {
        int column = index % 4;
        int row = index / 4;
        return base.add(column * SPACING, 0, row * SPACING);
    }

    private static void prepareSite(World world, BlockPos origin) {
        int width = 79;
        for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(
                origin.add(-5, -2, -5), origin.add(width + 4, 24, width + 4))) {
            if (pos.getY() < origin.getY())
                world.setBlockState(pos, net.minecraft.init.Blocks.GRASS.getDefaultState(), 2);
            else
                world.setBlockToAir(pos);
        }
    }

    private static String displayName(String id) {
        StringBuilder text = new StringBuilder();
        for (String word : id.split("_")) {
            if (text.length() > 0) text.append(' ');
            text.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return text.toString();
    }

    private static int countRoadBlocks(World world, BlockPos origin) {
        int count = 0;
        for (int step = 0; step < 16; step++) for (int lane = -1; lane <= 1; lane++)
            if (world.getBlockState(origin.add(8 + lane, 0, step)).getBlock()
                    == net.minecraft.init.Blocks.DOUBLE_STONE_SLAB) count++;
        return count;
    }

    private static void enableFlight(EntityPlayerMP player) {
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        player.sendPlayerAbilities();
    }

    private static void requireCreative(EntityPlayerMP player) throws CommandException {
        if (!player.capabilities.isCreativeMode)
            throw new CommandException("The worldgen showcase command requires Creative mode.");
    }
}
