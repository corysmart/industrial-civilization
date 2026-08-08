package com.industrialcivilization.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Development-only deterministic scenarios and parseable runtime snapshots. */
public final class CommandIndustrialTest extends CommandBase {
    private static final String PREFIX = "IC_TEST|";

    @Override public String getName() { return "ic_test"; }
    @Override public String getUsage(ICommandSender sender) {
        return "/ic_test snapshot [radius] | scenario <workshop_adjacency|earth_ecology> | assert <workshop_adjacency|earth_ecology>";
    }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (args.length == 0) throw new WrongUsageException(getUsage(sender));
        if ("snapshot".equals(args[0])) {
            int radius = args.length > 1 ? parseInt(args[1], 4, 64) : 24;
            emit(player, "SNAPSHOT|" + snapshot(player, radius));
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "workshop_adjacency".equals(args[1])) {
            BlockPos base = createWorkshopScenario(player);
            int[] state = WorkshopSystem.inspect(player.world, base.add(4, 0, 0), 24);
            boolean pass = state[0] == 2 && state[1] == 1 && state[2] == 1;
            emit(player, (pass ? "PASS" : "FAIL") + "|workshop_adjacency|controllers="
                + state[0] + "|pairs=" + state[1] + "|connections=" + state[2]
                + "|base=" + coordinates(base));
            return;
        }
        if (args.length == 2 && "assert".equals(args[0]) && "workshop_adjacency".equals(args[1])) {
            int[] state = WorkshopSystem.inspect(player.world, player.getPosition(), 64);
            boolean pass = state[0] >= 2 && state[1] >= 1 && state[2] == state[1];
            emit(player, (pass ? "PASS" : "FAIL") + "|workshop_adjacency|controllers="
                + state[0] + "|pairs=" + state[1] + "|connections=" + state[2]);
            return;
        }
        if (args.length == 2 && ("scenario".equals(args[0]) || "assert".equals(args[0]))
                && "earth_ecology".equals(args[1])) {
            EcologyResult result = runEarthEcologyScenario(player);
            emit(player, (result.pass ? "PASS" : "FAIL") + "|earth_ecology|robbers="
                + result.robbers + "|patrols=" + result.patrols + "|vanilla_zombies="
                + result.vanillaZombies + "|vanilla_skeletons=" + result.vanillaSkeletons
                + "|other_vanilla_hostiles=" + result.otherVanillaHostiles);
            return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    private static EcologyResult runEarthEcologyScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos base = player.getPosition().add(0, 2, 10);
        AxisAlignedBB box = new AxisAlignedBB(base.add(-3, -2, -3), base.add(8, 4, 3));
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, box, candidate ->
                candidate instanceof EntityRobber || candidate instanceof EntityMilitiaPatrol)) {
            entity.setDead();
        }
        EntityZombie zombie = new EntityZombie(world);
        zombie.getEntityData().setBoolean(PlanetaryEcologySystem.FORCE_ROBBER_REPLACEMENT, true);
        EntitySkeleton skeleton = new EntitySkeleton(world);
        skeleton.getEntityData().setBoolean(PlanetaryEcologySystem.FORCE_PATROL_REPLACEMENT, true);
        EntityCreeper creeper = new EntityCreeper(world);
        EntitySpider spider = new EntitySpider(world);
        EntityEnderman enderman = new EntityEnderman(world);
        Entity[] sources = {zombie, skeleton, creeper, spider, enderman};
        for (int index = 0; index < sources.length; index++) {
            sources[index].setPosition(base.getX() + index * 1.5D, base.getY(), base.getZ());
            world.spawnEntity(sources[index]);
        }
        int robbers = world.getEntitiesWithinAABB(EntityRobber.class, box).size();
        int patrols = world.getEntitiesWithinAABB(EntityMilitiaPatrol.class, box).size();
        int vanillaZombies = world.getEntitiesWithinAABB(EntityZombie.class, box).size();
        int vanillaSkeletons = world.getEntitiesWithinAABB(EntitySkeleton.class, box).size();
        int otherVanillaHostiles = world.getEntitiesWithinAABB(EntityCreeper.class, box).size()
            + world.getEntitiesWithinAABB(EntitySpider.class, box).size()
            + world.getEntitiesWithinAABB(EntityEnderman.class, box).size();
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, box, candidate ->
                candidate instanceof EntityRobber || candidate instanceof EntityMilitiaPatrol)) {
            entity.setDead();
        }
        return new EcologyResult(robbers, patrols, vanillaZombies, vanillaSkeletons,
            otherVanillaHostiles);
    }

    private static final class EcologyResult {
        final int robbers;
        final int patrols;
        final int vanillaZombies;
        final int vanillaSkeletons;
        final int otherVanillaHostiles;
        final boolean pass;

        EcologyResult(int robbers, int patrols, int vanillaZombies, int vanillaSkeletons,
                int otherVanillaHostiles) {
            this.robbers = robbers;
            this.patrols = patrols;
            this.vanillaZombies = vanillaZombies;
            this.vanillaSkeletons = vanillaSkeletons;
            this.otherVanillaHostiles = otherVanillaHostiles;
            this.pass = robbers == 1 && patrols == 1 && vanillaZombies == 0
                && vanillaSkeletons == 0 && otherVanillaHostiles == 0;
        }
    }

    private static BlockPos createWorkshopScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos base = player.getPosition().add(-4, 0, 8);
        // Deliberately destructive only inside the documented disposable scenario volume.
        for (int x = -4; x <= 13; x++) for (int z = -3; z <= 3; z++) {
            world.setBlockState(base.add(x, -2, z), Blocks.STONE.getDefaultState(), 2);
            world.setBlockToAir(base.add(x, -1, z));
            for (int y = 0; y <= 4; y++) world.setBlockToAir(base.add(x, y, z));
        }
        BlockPos car = base;
        BlockPos gun = base.add(9, 0, 0);
        world.setBlockState(car, IndustrialCivilizationCore.CAR_WORKSHOP.getDefaultState(), 3);
        setFacing(world, car, EnumFacing.NORTH);
        WorkshopSystem.deploy(world, car, IndustrialMachineKind.CAR_WORKSHOP, EnumFacing.NORTH);
        world.setBlockState(gun, IndustrialCivilizationCore.GUN_FACTORY.getDefaultState(), 3);
        setFacing(world, gun, EnumFacing.NORTH);
        WorkshopSystem.deploy(world, gun, IndustrialMachineKind.GUN_FACTORY, EnumFacing.NORTH);
        WorkshopSystem.connectAdjacent(world, gun);
        return base;
    }

    private static void setFacing(World world, BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileIndustrialMachine) ((TileIndustrialMachine) tile).setWorkshopFacing(facing);
    }

    private static JsonObject snapshot(EntityPlayerMP player, int radius) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("player", player.getName());
        root.addProperty("dimension", player.dimension);
        root.addProperty("position", coordinates(player.getPosition()));
        root.addProperty("radius", radius);
        root.addProperty("ai_age", ProgressionState.has(player, "ai_age"));
        root.addProperty("active_ticks", ProgressionState.counter(player, "active_ticks"));
        int[] workshops = WorkshopSystem.inspect(player.world, player.getPosition(), radius);
        root.addProperty("workshop_controllers", workshops[0]);
        root.addProperty("workshop_pairs", workshops[1]);
        root.addProperty("workshop_connections", workshops[2]);
        JsonArray machines = new JsonArray();
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (!(tile instanceof TileIndustrialMachine)
                    || tile.getPos().distanceSq(player.getPosition()) > radius * radius) continue;
            TileIndustrialMachine machine = (TileIndustrialMachine) tile;
            JsonObject entry = new JsonObject();
            entry.addProperty("kind", machine.getKind().id);
            entry.addProperty("position", coordinates(tile.getPos()));
            entry.addProperty("energy_eu", machine.getEnergyStored());
            entry.addProperty("progress", machine.getProgress());
            entry.addProperty("completed", machine.getCompletedOperations());
            entry.addProperty("rusted", machine.isRusted());
            entry.addProperty("facing", machine.getWorkshopFacing().getName());
            machines.add(entry);
        }
        root.add("machines", machines);
        return root;
    }

    private static String coordinates(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static void emit(EntityPlayerMP player, String value) {
        String line = PREFIX + value;
        IndustrialCivilizationCore.LOGGER.info(line);
        player.sendMessage(new TextComponentString(line));
    }

    @Override
    public java.util.List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "snapshot", "scenario", "assert");
        if (args.length == 2 && ("scenario".equals(args[0]) || "assert".equals(args[0])))
            return getListOfStringsMatchingLastWord(args, "workshop_adjacency", "earth_ecology");
        return Arrays.asList();
    }
}
