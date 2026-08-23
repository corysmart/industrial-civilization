package com.industrialcivilization.core;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.Advancement;
import mrtjp.projectred.expansion.TileBlockBreaker;
import mrtjp.projectred.expansion.TileBlockPlacer;
import mrtjp.projectred.expansion.TileFrameMotor;
import mrtjp.projectred.expansion.TileMachine;
import mrtjp.projectred.core.PowerConductor;
import mrtjp.projectred.api.IConditionallyMovable;
import mrtjp.projectred.api.ProjectRedAPI;
import mrtjp.projectred.relocation.MovementManager$;
import mrtjp.projectred.relocation.MovingTileRegistry$;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Disposable-world acceptance test for the complete BuildCraft Quarry lifecycle.
 * It is registered only when the development test bridge is enabled.
 */
public final class MobileQuarryLifecycleTest {
    public static final MobileQuarryLifecycleTest INSTANCE = new MobileQuarryLifecycleTest();
    private static final int TIMEOUT_TICKS = 6000;
    private static final long FIRST_LANE_POWER_PER_TICK = 1000L * MjAPI.MJ;
    private static final long SECOND_LANE_POWER_PER_TICK = 250L * MjAPI.MJ;
    private static final int RENDERED_SECOND_LANE_TICKS = 100;

    private UUID playerId;
    private World world;
    private BlockPos oldController;
    private BlockPos oldQuarry;
    private BlockPos newController;
    private BlockPos newQuarry;
    private Block buildCraftFrameBlock;
    private Block relocationFrameBlock;
    private Block projectRedMachineBlock;
    private Block worldspikeBlock;
    private Item quarryItem;
    private BlockPos frameMotorPos;
    private BlockPos breakerPos;
    private BlockPos placerPos;
    private BlockPos worldspikePos;
    private BlockPos outputPipePos;
    private BlockPos outputChestPos;
    private final Set<BlockPos> initialCarriageFrames = new HashSet<>();
    private int verifiedFrameStep;
    private int ticks;
    private int firstLaneInitial = -1;
    private int secondLaneInitial = -1;
    private boolean firstFrameBuilt;
    private boolean firstLaneMined;
    private boolean relocated;
    private boolean oldFrameCleared;
    private boolean secondFrameBuilt;
    private boolean secondLaneMined;
    private int offscreenInitial = -1;
    private int offscreenStartTick;
    private boolean offscreenStarted;
    private boolean chunkTicketActive;
    private boolean offscreenMining;
    private boolean active;
    private boolean projectRedHarnessMoved;
    private boolean breakerOperated;
    private boolean placerOperated;
    private boolean recoveredQuarryObserved;
    private boolean noSpareVerified;
    private boolean noTeleportBlocks;
    private boolean settledFramesClean;
    private boolean movementDiagnosticLogged;
    private boolean quarryOutputRouted;
    private boolean quarryOutputMoved;
    private boolean movementCameraSet;
    private boolean relocatedCameraSet;

    private MobileQuarryLifecycleTest() {}

    public static void start(EntityPlayerMP player) {
        INSTANCE.begin(player);
    }

    private void begin(EntityPlayerMP player) {
        reset();
        playerId = player.getUniqueID();
        world = player.world;
        if (!RuntimeAdvancements.loaded(player, "mobile_quarry_relocation")) {
            fail("side_quest_advancement_not_loaded");
            return;
        }
        Advancement loadedIndustry = player.getServer().getAdvancementManager()
            .getAdvancement(new ResourceLocation(IndustrialCivilizationCore.MODID,
                "loaded_industry"));
        if (loadedIndustry == null || !completeAdvancementChain(player, loadedIndustry)) {
            fail("side_quest_prerequisite_setup_failed");
            return;
        }
        // Keep the acceptance rig well outside vanilla's permanently loaded
        // spawn area. Otherwise offscreen mining could continue without a
        // Worldspike and give a false-positive chunk-loading result.
        BlockPos spawn = world.getSpawnPoint();
        int motorX = spawn.getX() + 768;
        int x = motorX - 20;
        int z = spawn.getZ() + 768;
        oldController = new BlockPos(x, 13, z);
        oldQuarry = oldController.east();
        newController = MobileQuarryRules.laneDestination(oldController, EnumFacing.EAST);
        newQuarry = newController.east();
        frameMotorPos = new BlockPos(motorX, 13, z - 3);
        breakerPos = oldQuarry.north();
        placerPos = oldQuarry.down();
        // Phase-one infrastructure keeps the non-teleporting chunk loader at
        // the fixed motor station. Its 3x3 chunk area covers both 16-block
        // lanes without making an active Forge ticket part of the frame move.
        worldspikePos = new BlockPos(motorX, 13, z + 10);
        outputPipePos = oldQuarry.up();
        outputChestPos = oldQuarry.up(2);
        Block quarryBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("buildcraftbuilders", "quarry"));
        buildCraftFrameBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("buildcraftbuilders", "frame"));
        relocationFrameBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("projectred-relocation", "frame"));
        projectRedMachineBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("projectred-expansion", "machine2"));
        worldspikeBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("railcraft", "worldspike"));
        quarryItem = quarryBlock == null ? null : Item.getItemFromBlock(quarryBlock);
        if (quarryBlock == null || quarryBlock == Blocks.AIR
                || buildCraftFrameBlock == null || buildCraftFrameBlock == Blocks.AIR
                || relocationFrameBlock == null || relocationFrameBlock == Blocks.AIR
                || projectRedMachineBlock == null || projectRedMachineBlock == Blocks.AIR
                || worldspikeBlock == null || worldspikeBlock == Blocks.AIR
                || quarryItem == null) {
            fail("missing_quarry_or_projectred_blocks");
            return;
        }

        // The scenario runs only in a disposable copy. Two shallow quarry lanes
        // make real frame construction and natural bedrock completion bounded.
        for (int clearX = motorX - 28; clearX <= motorX + 40; clearX++) {
            for (int clearZ = z - 18; clearZ <= z + 18; clearZ++) {
                for (int y = 1; y <= 35; y++) {
                    world.setBlockToAir(new BlockPos(clearX, y, clearZ));
                }
                world.setBlockState(new BlockPos(clearX, 1, clearZ),
                    Blocks.BEDROCK.getDefaultState(), 2);
                world.setBlockState(new BlockPos(clearX, 2, clearZ),
                    Blocks.STONE.getDefaultState(), 2);
            }
        }
        // A narrow tail keeps one frame under the stationary motor throughout
        // a 16-block hop and supports the carried signal bus.
        // The previous 33x5 platform overlapped most of its own old footprint,
        // which looked like ghost blocks even when movement was correct.
        for (int railX = oldController.getX() - 2; railX <= motorX; railX++) {
            world.setBlockState(new BlockPos(railX, 12, z - 6),
                relocationFrameBlock.getDefaultState(), 2);
            world.setBlockState(new BlockPos(railX, 12, z - 5),
                relocationFrameBlock.getDefaultState(), 2);
            world.setBlockState(new BlockPos(railX, 12, z - 4),
                relocationFrameBlock.getDefaultState(), 2);
            world.setBlockState(new BlockPos(railX, 12, z - 3),
                relocationFrameBlock.getDefaultState(), 2);
            world.setBlockState(new BlockPos(railX, 12, z - 2),
                relocationFrameBlock.getDefaultState(), 2);
        }
        for (int y = 12; y <= 15; y++) {
            world.setBlockState(new BlockPos(oldController.getX(), y, z - 1),
                relocationFrameBlock.getDefaultState(), 2);
        }
        world.setBlockState(new BlockPos(oldController.getX() - 1, 12, z - 1),
            relocationFrameBlock.getDefaultState(), 2);
        world.setBlockState(new BlockPos(oldController.getX() - 1, 12, z),
            relocationFrameBlock.getDefaultState(), 2);
        // Bypass the left-side opaque relay so the upper and lower frame
        // sections remain one carriage without occupying either redstone wire.
        for (int y = 12; y <= 14; y++) {
            world.setBlockState(new BlockPos(oldController.getX() - 2, y, z - 1),
                relocationFrameBlock.getDefaultState(), 2);
        }
        world.setBlockState(new BlockPos(oldController.getX() - 1, 14, z - 1),
            relocationFrameBlock.getDefaultState(), 2);
        world.setBlockState(oldController,
            IndustrialCivilizationCore.MOBILE_QUARRY_CONTROLLER.getDefaultState(), 3);
        // The left-side relay powers the breaker while the bottom relay powers
        // the placer. The top stays free for BuildCraft's output pipe.
        world.setBlockState(oldController.north(), Blocks.IRON_BLOCK.getDefaultState(), 3);
        world.setBlockState(oldController.down(), Blocks.IRON_BLOCK.getDefaultState(), 3);
        // The top frames grip the carried BuildCraft pipe and chest. A connected
        // outer column grips the breaker and placer themselves.
        world.setBlockState(oldController.up(), relocationFrameBlock.getDefaultState(), 3);
        world.setBlockState(oldController.up(2), relocationFrameBlock.getDefaultState(), 3);
        for (int y : new int[] {12, 14}) {
            world.setBlockState(new BlockPos(oldQuarry.getX() + 1, y, z),
                relocationFrameBlock.getDefaultState(), 2);
        }
        // Route the connection around, rather than through, the old Quarry
        // cell. A frame beside that cell would grip its post-break residue and
        // carry the residue into the new deployment target.
        for (int y = 12; y <= 14; y++) {
            world.setBlockState(new BlockPos(oldQuarry.getX() + 2, y, z),
                relocationFrameBlock.getDefaultState(), 2);
        }
        for (int frameX = oldController.getX(); frameX <= oldQuarry.getX() + 2; frameX++) {
            world.setBlockState(new BlockPos(frameX, 11, z),
                relocationFrameBlock.getDefaultState(), 2);
        }
        world.setBlockState(new BlockPos(oldController.getX(), 11, z - 1),
            relocationFrameBlock.getDefaultState(), 2);
        world.setBlockState(new BlockPos(oldQuarry.getX() + 1, 13, z - 1),
            relocationFrameBlock.getDefaultState(), 2);
        world.setBlockState(new BlockPos(oldQuarry.getX() + 1, 13, z - 2),
            relocationFrameBlock.getDefaultState(), 2);
        if (!placeMachine(breakerPos, 0, EnumFacing.NORTH.ordinal())
                || !placeMachine(placerPos, 2, EnumFacing.DOWN.ordinal())
                || !placeFrameMotor(frameMotorPos)
                || !placeRecoveryHoppers(oldQuarry)
                || !placeQuarryOutput(player)
                || !placeStandardWorldspike(player)) {
            fail("projectred_machine_placement_failed");
            return;
        }
        TileEntity placer = world.getTileEntity(placerPos);
        if (!(placer instanceof TileBlockPlacer)) {
            fail("projectred_placer_missing");
            return;
        }
        noSpareVerified = countQuarryItemsInHarness(oldController) == 0;
        if (!noSpareVerified) {
            fail("quarry_spare_present_before_break");
            return;
        }
        if (!placeMovementBus(motorX, x, z)) {
            fail("redstone_movement_bus_failed");
            return;
        }
        world.setBlockState(oldQuarry, quarryBlock.getDefaultState(), 3);
        TileEntity quarryTile = world.getTileEntity(oldQuarry);
        TileEntity controllerTile = world.getTileEntity(oldController);
        if (!(quarryTile instanceof TileQuarry)
                || !(controllerTile instanceof TileMobileQuarryController)) {
            fail("initial_tiles_missing");
            return;
        }
        ((TileQuarry) quarryTile).onPlacedBy(player, new ItemStack(quarryBlock));
        captureInitialCarriageFrames(motorX, z);
        noTeleportBlocks = !containsTeleportBlock(motorX, z);
        settledFramesClean = true;
        if (!noTeleportBlocks) {
            fail("teleport_block_present");
            return;
        }
        TileMobileQuarryController controller = (TileMobileQuarryController) controllerTile;
        controller.setFacing(EnumFacing.EAST);
        controller.interact(player, false);
        player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION,
            TIMEOUT_TICKS + 400, 0, false, false));
        player.capabilities.allowFlying = true;
        player.capabilities.isFlying = true;
        player.sendPlayerAbilities();
        player.connection.setPlayerLocation(motorX - 10.0D, 20.0D, z + 20.5D,
            180.0F, 28.0F);
        active = true;
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|STATE|mobile_quarry_relocation|phase=natural_lifecycle_started"
            + "|controller={}|motor={}|breaker={}|placer={}|projectred_frames=true"
            + "|frame_carried_redstone_bus=true", oldController, frameMotorPos,
            breakerPos, placerPos);
    }

    private boolean placeMachine(BlockPos target, int metadata, int side) {
        if (!world.setBlockState(target, projectRedMachineBlock.getStateFromMeta(metadata), 3)) {
            return false;
        }
        TileEntity tile = world.getTileEntity(target);
        if (!(tile instanceof TileMachine)) return false;
        TileMachine machine = (TileMachine) tile;
        machine.setSide(side);
        machine.setRotation(0);
        machine.markDirty();
        world.notifyNeighborsOfStateChange(target, projectRedMachineBlock, false);
        return true;
    }

    private boolean placeFrameMotor(BlockPos target) {
        // The motor stays stationary above the carriage and grips the frame
        // directly below it; the redstone bus can then approach from the side.
        if (!placeMachine(target, 8, EnumFacing.UP.ordinal())) return false;
        TileEntity tile = world.getTileEntity(target);
        if (!(tile instanceof TileFrameMotor)) return false;
        TileFrameMotor motor = (TileFrameMotor) tile;
        for (int rotation = 0; rotation < 4; rotation++) {
            motor.setRotation(rotation);
            if (motor.getMoveDir() == EnumFacing.EAST.ordinal()) {
                motor.markDirty();
                return true;
            }
        }
        return false;
    }

    private boolean placeMovementBus(int motorX, int controllerX, int z) {
        // Span the motor's full relative travel: it begins twenty blocks ahead
        // of the controller and remains four blocks ahead after a lane shift.
        // Keep the motor-adjacent row entirely dust. An isolated command line
        // feeds it through one south-facing repeater, which both regenerates
        // the signal and prevents the output rail from feeding back and
        // latching itself high. The injection point stays within 14 blocks of
        // the motor for the complete 16-block movement.
        for (int wireX = controllerX + 1; wireX <= motorX; wireX++) {
            if (!placeRedstone(new BlockPos(wireX, 13, z - 4))) return false;
        }
        for (int wireZ = z - 6; wireZ <= z; wireZ++) {
            if (!placeRedstone(new BlockPos(controllerX - 1, 13, wireZ))) return false;
        }
        for (int wireX = controllerX; wireX <= controllerX + 6; wireX++) {
            if (!placeRedstone(new BlockPos(wireX, 13, z - 6))) return false;
        }
        BlockPos injector = new BlockPos(controllerX + 6, 13, z - 5);
        return world.isAirBlock(injector)
            && world.setBlockState(injector,
                Blocks.UNPOWERED_REPEATER.getDefaultState()
                    .withProperty(BlockHorizontal.FACING, EnumFacing.NORTH), 3);
    }

    private boolean placeRecoveryHoppers(BlockPos quarry) {
        return placeHopper(quarry.north(2), EnumFacing.DOWN)
            && placeHopper(quarry.north(2).down(), EnumFacing.SOUTH)
            && placeHopper(quarry.north().down(), EnumFacing.SOUTH);
    }

    private boolean placeHopper(BlockPos target, EnumFacing output) {
        return world.setBlockState(target, Blocks.HOPPER.getDefaultState()
            .withProperty(BlockHopper.FACING, output), 3)
            && world.getBlockState(target).getBlock() == Blocks.HOPPER;
    }

    private boolean placeQuarryOutput(EntityPlayerMP player) {
        if (BCTransportBlocks.pipeHolder == null || BCTransportItems.pipeItemCobble == null) {
            return false;
        }
        if (!world.setBlockState(outputPipePos,
                BCTransportBlocks.pipeHolder.getDefaultState(), 3)) return false;
        TileEntity pipeTile = world.getTileEntity(outputPipePos);
        if (!(pipeTile instanceof TilePipeHolder)) return false;
        ((TilePipeHolder) pipeTile).onPlacedBy(player,
            new ItemStack(BCTransportItems.pipeItemCobble));
        if (((TilePipeHolder) pipeTile).getPipe() == null) return false;
        return world.setBlockState(outputChestPos, Blocks.CHEST.getDefaultState(), 3)
            && world.getTileEntity(outputChestPos) instanceof IInventory;
    }

    private boolean placeStandardWorldspike(EntityPlayerMP player) {
        world.setBlockState(worldspikePos.down(), Blocks.STONE.getDefaultState(), 2);
        // The standard variant uses a normal mod ticket. The passive variant
        // requests a player ticket, which is not issued for the disposable
        // offline Test Bed identity and therefore cannot prove chunk loading.
        net.minecraft.block.state.IBlockState state = worldspikeBlock.getStateFromMeta(3);
        if (!world.setBlockState(worldspikePos, state, 3)) return false;
        worldspikeBlock.onBlockPlacedBy(world, worldspikePos, state, player,
            new ItemStack(worldspikeBlock, 1, 1));
        TileEntity tile = world.getTileEntity(worldspikePos);
        Item fuel = ForgeRegistries.ITEMS.getValue(new ResourceLocation("railcraft", "dust"));
        if (!(tile instanceof IInventory) || fuel == null) return false;
        ((IInventory) tile).setInventorySlotContents(0, new ItemStack(fuel, 1, 6));
        tile.markDirty();
        return true;
    }

    private int countQuarryItemsInHarness(BlockPos controller) {
        if (controller == null || quarryItem == null) return 0;
        BlockPos quarry = controller.east();
        int count = countItem(world.getTileEntity(quarry.down()), quarryItem);
        for (BlockPos hopper : new BlockPos[] {
                quarry.north(2), quarry.north(2).down(), quarry.north().down()}) {
            count += countItem(world.getTileEntity(hopper), quarryItem);
        }
        return count;
    }

    private int countNonQuarryItemsInHarness(BlockPos controller) {
        if (controller == null || quarryItem == null) return 0;
        BlockPos quarry = controller.east();
        int count = countItemsExcept(world.getTileEntity(quarry.down()), quarryItem);
        for (BlockPos hopper : new BlockPos[] {
                quarry.north(2), quarry.north(2).down(), quarry.north().down()}) {
            count += countItemsExcept(world.getTileEntity(hopper), quarryItem);
        }
        return count;
    }

    private int countItem(TileEntity tile, Item item) {
        if (!(tile instanceof IInventory)) return 0;
        int count = 0;
        IInventory inventory = (IInventory) tile;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private int countItemsExcept(TileEntity tile, Item item) {
        if (!(tile instanceof IInventory)) return 0;
        int count = 0;
        IInventory inventory = (IInventory) tile;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() != item) count += stack.getCount();
        }
        return count;
    }

    private void captureInitialCarriageFrames(int motorX, int z) {
        initialCarriageFrames.clear();
        for (BlockPos scan : BlockPos.getAllInBox(
                new BlockPos(motorX - 24, 11, z - 6),
                new BlockPos(motorX + 2, 16, z + 3))) {
            if (world.getBlockState(scan).getBlock() == relocationFrameBlock) {
                initialCarriageFrames.add(scan.toImmutable());
            }
        }
    }

    private boolean verifySettledFrames(int step) {
        for (BlockPos initial : initialCarriageFrames) {
            BlockPos expected = initial.east(step);
            if (world.getBlockState(expected).getBlock() != relocationFrameBlock) return false;
            if (step > 0 && !initialCarriageFrames.contains(initial.west(step))
                    && world.getBlockState(initial).getBlock() == relocationFrameBlock) {
                return false;
            }
        }
        return true;
    }

    private boolean containsTeleportBlock(int motorX, int z) {
        for (BlockPos scan : BlockPos.getAllInBox(
                new BlockPos(motorX - 24, 10, z - 6),
                new BlockPos(motorX + MobileQuarryRules.LANE_STEP + 20, 18, z + 12))) {
            ResourceLocation name = world.getBlockState(scan).getBlock().getRegistryName();
            if (name == null) continue;
            String id = name.toString().toLowerCase(java.util.Locale.ROOT);
            if (id.contains("teleport") || id.contains("tether")) return true;
        }
        return false;
    }

    private boolean placeRedstone(BlockPos target) {
        if (!world.isAirBlock(target)) return false;
        return world.setBlockState(target, Blocks.REDSTONE_WIRE.getDefaultState(), 3)
            && world.getBlockState(target).getBlock() == Blocks.REDSTONE_WIRE;
    }

    private int redstoneLevel(BlockPos target) {
        if (world.getBlockState(target).getBlock() != Blocks.REDSTONE_WIRE) return -1;
        return world.getBlockState(target).getValue(BlockRedstoneWire.POWER);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (!active || event.phase != TickEvent.Phase.END) return;
        if (++ticks > TIMEOUT_TICKS) {
            fail("timeout");
            return;
        }
        EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
        if (player == null) {
            fail("owner_missing");
            return;
        }

        TileEntity motorTile = world.getTileEntity(frameMotorPos);
        if (!(motorTile instanceof TileFrameMotor)) {
            fail("frame_motor_missing");
            return;
        }
        // The harness uses the real ProjectRed motor and its real power
        // condition. A deterministic test supply keeps this acceptance test
        // independent of solar time while all redstone and movement remain physical.
        ((PowerConductor) ((TileFrameMotor) motorTile).cond()).applyPower(200000.0D);

        TileQuarry quarry = quarryAt(relocated ? newQuarry : oldQuarry);
        if (quarry != null) supplyPower(quarry);
        TileMobileQuarryController liveController = findController();
        if (liveController != null) {
            if (!movementCameraSet
                    && ("move_high".equals(liveController.getPhaseName())
                        || "move_wait".equals(liveController.getPhaseName()))) {
                movementCameraSet = true;
                player.connection.setPlayerLocation(frameMotorPos.getX() - 3.0D,
                    21.0D, frameMotorPos.getZ() + 29.5D, 180.0F, 24.0F);
            }
            int harnessQuarries = countQuarryItemsInHarness(liveController.getPos());
            if (countNonQuarryItemsInHarness(liveController.getPos()) > 0) {
                fail("recovery_path_contaminated");
                return;
            }
            if (harnessQuarries > 1) {
                fail("duplicate_quarry_item_detected");
                return;
            }
            recoveredQuarryObserved |= harnessQuarries == 1;
            if (!movementDiagnosticLogged && harnessQuarries == 1
                    && "recovery_wait".equals(liveController.getPhaseName())) {
                logMovementDiagnostic();
                movementDiagnosticLogged = true;
            }
            if ("move_settle".equals(liveController.getPhaseName())
                    && liveController.getMovementSteps() > verifiedFrameStep) {
                verifiedFrameStep = liveController.getMovementSteps();
                if (!verifySettledFrames(verifiedFrameStep)) {
                    settledFramesClean = false;
                    fail("frame_ghost_or_gap_step_" + verifiedFrameStep);
                    return;
                }
            }
        }
        if (liveController != null && "move_high".equals(liveController.getPhaseName())
                && ticks % 5 == 0) {
            if (!movementDiagnosticLogged) {
                logMovementDiagnostic();
                movementDiagnosticLogged = true;
            }
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|MOVE_PULSE|ticks={}|controller_query_level={}"
                + "|first_wire_power={}|motor_wire_power={}|motor_powered={}"
                + "|motor_moving={}|motor_can_work={}", ticks,
                liveController.redstoneLevel(EnumFacing.EAST),
                redstoneLevel(liveController.getPos().west()),
                redstoneLevel(frameMotorPos.north()),
                ((TileFrameMotor) motorTile).isPowered(),
                ((TileFrameMotor) motorTile).isMoving(),
                ((TileFrameMotor) motorTile).cond().canWork());
        }
        if (liveController != null && "deploy_high".equals(liveController.getPhaseName())
                && ticks % 5 == 0) {
            BlockPos movedPlacerPos = liveController.quarryPos().down();
            TileEntity movedPlacerTile = world.getTileEntity(movedPlacerPos);
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|DEPLOY_PULSE|ticks={}|placer_pos={}|placer_block={}"
                + "|quarry_items={}|side={}|device_powered={}|world_powered={}"
                + "|target_block={}|controller_query_level={}", ticks,
                movedPlacerPos,
                world.getBlockState(movedPlacerPos).getBlock().getRegistryName(),
                countItem(movedPlacerTile, quarryItem),
                movedPlacerTile instanceof TileBlockPlacer
                    ? ((TileBlockPlacer) movedPlacerTile).side() : -1,
                movedPlacerTile instanceof TileBlockPlacer
                    && ((TileBlockPlacer) movedPlacerTile).powered(),
                world.isBlockPowered(movedPlacerPos),
                world.getBlockState(liveController.quarryPos()).getBlock().getRegistryName(),
                liveController.redstoneLevel(EnumFacing.UP));
        }
        if (ticks % 20 == 0) showProgress(player, liveController, quarry);
        if (ticks == 1 || ticks % 100 == 0) {
            logProgress((TileFrameMotor) motorTile, quarry);
        }

        if (!relocated) {
            breakerOperated |= firstFrameBuilt && quarryAt(oldQuarry) == null;
            for (int step = 1; step <= MobileQuarryRules.LANE_STEP; step++) {
                if (controllerAt(oldController.east(step)) != null) {
                    projectRedHarnessMoved = true;
                    break;
                }
            }
            if (quarry != null && quarry.frameBox.isInitialized()) {
                if (firstLaneInitial < 0) firstLaneInitial = countMineLayer(quarry);
                firstFrameBuilt |= countFrames(quarry) > 0;
                firstLaneMined |= firstLaneInitial > 0 && countMineLayer(quarry) < firstLaneInitial;
                quarryOutputRouted |= countItem(world.getTileEntity(outputChestPos),
                    Item.getItemFromBlock(Blocks.COBBLESTONE)) > 0;
            }
            TileEntity moved = world.getTileEntity(newController);
            if (moved instanceof TileMobileQuarryController
                    && ((TileMobileQuarryController) moved).getRelocations() == 1) {
                relocated = true;
                placerOperated = quarryAt(newQuarry) != null;
                if (!recoveredQuarryObserved || countQuarryItemsInHarness(newController) != 0) {
                    fail("recovered_quarry_not_consumed_by_placer");
                    return;
                }
                if (world.getBlockState(worldspikePos).getBlock() != worldspikeBlock) {
                    fail("worldspike_station_missing");
                    return;
                }
                quarryOutputMoved = world.getTileEntity(newQuarry.up()) instanceof TilePipeHolder
                    && countItem(world.getTileEntity(newQuarry.up(2)),
                        Item.getItemFromBlock(Blocks.COBBLESTONE)) > 0;
                if (!quarryOutputMoved) {
                    fail("quarry_output_pipe_or_chest_not_moved");
                    return;
                }
                if (!relocatedCameraSet) {
                    relocatedCameraSet = true;
                    player.connection.setPlayerLocation(newQuarry.getX() + 3.0D,
                        19.0D, newQuarry.getZ() + 15.5D, 180.0F, 30.0F);
                }
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|STATE|mobile_quarry_relocation|phase=relocated|ticks={}"
                    + "|projectred_harness_moved={}|breaker_operated={}"
                    + "|placer_operated={}|same_quarry_reused={}"
                    + "|worldspike_stationary=true", ticks, projectRedHarnessMoved,
                    breakerOperated, placerOperated, recoveredQuarryObserved);
            }
            return;
        }

        TileMobileQuarryController controller = controllerAt(newController);
        quarry = quarryAt(newQuarry);
        if (controller == null || quarry == null) {
            fail("relocated_tiles_missing");
            return;
        }
        oldFrameCleared |= countFramesAround(oldQuarry, 7) == 0;
        if (quarry.frameBox.isInitialized()) {
            if (secondLaneInitial < 0) secondLaneInitial = countMineLayer(quarry);
            secondFrameBuilt |= countFrames(quarry) > 0;
            secondLaneMined |= secondLaneInitial > 0 && countMineLayer(quarry) < secondLaneInitial;
        }
        chunkTicketActive |= ForgeChunkManager.getPersistentChunksFor(world)
            .containsKey(new ChunkPos(newQuarry));
        if (secondFrameBuilt && offscreenStartTick == 0) offscreenStartTick = ticks;
        if (secondFrameBuilt && !offscreenStarted
                && ticks - offscreenStartTick >= RENDERED_SECOND_LANE_TICKS) {
            beginChunkLoaderCheck(player, quarry);
            return;
        }
        if (offscreenStarted) {
            chunkTicketActive |= ForgeChunkManager.getPersistentChunksFor(world)
                .containsKey(new ChunkPos(newQuarry));
            offscreenMining |= offscreenInitial > 0 && countMineLayer(quarry) < offscreenInitial;
        }
        if (firstFrameBuilt && firstLaneMined && oldFrameCleared
                && projectRedHarnessMoved && breakerOperated && placerOperated
                && recoveredQuarryObserved && noSpareVerified && noTeleportBlocks
                && settledFramesClean && quarryOutputRouted && quarryOutputMoved
                && secondFrameBuilt && secondLaneMined
                && chunkTicketActive && offscreenMining) {
            pass(controller);
        }
    }

    private void logMovementDiagnostic() {
        Set<BlockPos> structure = ProjectRedAPI.relocationAPI.getStickResolver()
            .getStructure(world, frameMotorPos.down(), frameMotorPos);
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|MOVE_STRUCTURE|size={}|controller_included={}|breaker_included={}"
            + "|placer_included={}|recovered_quarry_items={}|quarry_cell_block={}"
            + "|quarry_cell_included={}", structure.size(),
            structure.contains(oldController), structure.contains(breakerPos),
            structure.contains(placerPos), countQuarryItemsInHarness(oldController),
            world.getBlockState(oldQuarry).getBlock().getRegistryName(),
            structure.contains(oldQuarry));
        for (BlockPos pos : structure) {
            IConditionallyMovable conditional = MovementManager$.MODULE$
                .getConditionallyMovable(world, pos);
            if (conditional != null && !conditional.isMovable(world, pos)) {
                IndustrialCivilizationCore.LOGGER.error(
                    "IC_TEST|MOVE_REJECT|type=conditional|pos={}|block={}", pos,
                    world.getBlockState(pos).getBlock().getRegistryName());
            }
            BlockPos target = pos.east();
            if (!structure.contains(target)
                    && !MovingTileRegistry$.MODULE$.canRunOverBlock(world, target)) {
                IndustrialCivilizationCore.LOGGER.error(
                    "IC_TEST|MOVE_REJECT|type=destination|source={}|target={}|block={}",
                    pos, target, world.getBlockState(target).getBlock().getRegistryName());
            }
        }
    }

    private void beginChunkLoaderCheck(EntityPlayerMP player, TileQuarry quarry) {
        offscreenInitial = countMineLayer(quarry);
        offscreenStartTick = ticks;
        offscreenStarted = true;
        player.setPositionAndUpdate(player.posX + 512.0D, 100.0D, player.posZ);
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|STATE|mobile_quarry_relocation|phase=chunk_loader_offscreen"
            + "|initial_blocks={}|loader=railcraft_standard_worldspike"
            + "|no_teleport_blocks=true", offscreenInitial);
    }

    private void showProgress(EntityPlayerMP player,
            TileMobileQuarryController controller, TileQuarry quarry) {
        String message;
        if (offscreenStarted) {
            message = "TEST 5/5: Fixed Railcraft Worldspike active; player 512 blocks away; Quarry keeps mining";
        } else if (relocated) {
            message = "TEST 4/5: Block Placer redeployed the recovered Quarry; second excavation running";
        } else if (controller != null && "deploy_high".equals(controller.getPhaseName())
                || controller != null && "deploy_wait".equals(controller.getPhaseName())) {
            message = "TEST 4/5: ProjectRed Block Placer is deploying the stored Quarry";
        } else if (controller != null && ("move_high".equals(controller.getPhaseName())
                || "move_wait".equals(controller.getPhaseName())
                || "move_settle".equals(controller.getPhaseName()))) {
            message = "TEST 3/5: ProjectRed Frame Motor moved the carriage "
                + controller.getMovementSteps() + "/" + MobileQuarryRules.LANE_STEP + " blocks";
        } else if (controller != null && "recovery_wait".equals(controller.getPhaseName())) {
            message = "TEST 2/5: Broken Quarry item is traveling through the hopper return into the placer";
        } else if (controller != null && ("break_high".equals(controller.getPhaseName())
                || "break_wait".equals(controller.getPhaseName()))) {
            message = "TEST 2/5: ProjectRed Block Breaker is removing the completed Quarry";
        } else {
            int remaining = quarry == null ? 0 : countMineLayer(quarry);
            message = "TEST 1/5: BuildCraft Quarry excavating first lane — "
                + remaining + " stone blocks remain";
        }
        player.sendStatusMessage(new TextComponentString(message), true);
    }

    private void supplyPower(TileQuarry quarry) {
        for (EnumFacing side : EnumFacing.VALUES) {
            IMjReceiver receiver = quarry.getCapability(MjAPI.CAP_RECEIVER, side);
            if (receiver == null) continue;
            long requested = receiver.getPowerRequested();
            long available = relocated ? SECOND_LANE_POWER_PER_TICK : FIRST_LANE_POWER_PER_TICK;
            if (requested > 0) receiver.receivePower(Math.min(available, requested), false);
            return;
        }
    }

    private void logProgress(TileFrameMotor motor, TileQuarry quarry) {
        TileMobileQuarryController controller = findController();
        int frames = quarry == null ? -1 : countFrames(quarry);
        int mineLayer = quarry == null ? -1 : countMineLayer(quarry);
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|PROGRESS|mobile_quarry_relocation|ticks={}|controller={}|phase={}"
            + "|status={}|steps={}|relocations={}|quarry={}|frame_initialized={}"
            + "|bc_frames={}|mine_layer={}|motor_powered={}|motor_moving={}"
            + "|motor_charge={}|motor_bus_block={}|motor_bus_power={}"
            + "|breaker_present={}|placer_present={}",
            ticks, controller == null ? "missing" : controller.getPos(),
            controller == null ? "missing" : controller.getPhaseName(),
            controller == null ? "missing" : controller.getStatus(),
            controller == null ? -1 : controller.getMovementSteps(),
            controller == null ? -1 : controller.getRelocations(),
            quarry == null ? "missing" : quarry.getPos(),
            quarry != null && quarry.frameBox.isInitialized(), frames, mineLayer,
            motor.isPowered(), motor.isMoving(), motor.cond().charge(),
            world.getBlockState(frameMotorPos.north()).getBlock().getRegistryName(),
            world.getRedstonePower(frameMotorPos.north(), EnumFacing.NORTH),
            findMachine(TileBlockBreaker.class) != null,
            findMachine(TileBlockPlacer.class) != null);
    }

    private TileMobileQuarryController findController() {
        for (int step = 0; step <= MobileQuarryRules.LANE_STEP; step++) {
            TileMobileQuarryController controller = controllerAt(oldController.east(step));
            if (controller != null) return controller;
        }
        return null;
    }

    private TileEntity findMachine(Class<? extends TileEntity> type) {
        for (int step = 0; step <= MobileQuarryRules.LANE_STEP; step++) {
            for (BlockPos base : new BlockPos[] {breakerPos, placerPos}) {
                BlockPos target = base.east(step);
                TileEntity tile = world.getTileEntity(target);
                if (type.isInstance(tile)) return tile;
            }
        }
        return null;
    }

    private int countMineLayer(TileQuarry quarry) {
        if (!quarry.frameBox.isInitialized()) return 0;
        BlockPos min = quarry.frameBox.min();
        BlockPos max = quarry.frameBox.max();
        int count = 0;
        for (int x = min.getX() + 1; x < max.getX(); x++) {
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                if (world.getBlockState(new BlockPos(x, 2, z)).getBlock() == Blocks.STONE) count++;
            }
        }
        return count;
    }

    private int countFrames(TileQuarry quarry) {
        if (!quarry.frameBox.isInitialized()) return 0;
        BlockPos min = quarry.frameBox.min();
        BlockPos max = quarry.frameBox.max();
        int count = 0;
        for (BlockPos scan : BlockPos.getAllInBox(min, max)) {
            if (world.getBlockState(scan).getBlock() == buildCraftFrameBlock) count++;
        }
        return count;
    }

    private int countFramesAround(BlockPos center, int radius) {
        int count = 0;
        for (BlockPos scan : BlockPos.getAllInBox(center.add(-radius, -12, -radius),
                center.add(radius, 3, radius))) {
            if (world.getBlockState(scan).getBlock() == buildCraftFrameBlock) count++;
        }
        return count;
    }

    private TileQuarry quarryAt(BlockPos target) {
        TileEntity tile = world == null || target == null ? null : world.getTileEntity(target);
        return tile instanceof TileQuarry ? (TileQuarry) tile : null;
    }

    private boolean completeAdvancementChain(EntityPlayerMP player, Advancement advancement) {
        Advancement parent = advancement.getParent();
        if (parent != null && !completeAdvancementChain(player, parent)) return false;
        for (String criterion : advancement.getCriteria().keySet()) {
            player.getAdvancements().grantCriterion(advancement, criterion);
        }
        return player.getAdvancements().getProgress(advancement).isDone();
    }

    private TileMobileQuarryController controllerAt(BlockPos target) {
        TileEntity tile = world == null || target == null ? null : world.getTileEntity(target);
        return tile instanceof TileMobileQuarryController
            ? (TileMobileQuarryController) tile : null;
    }

    private void pass(TileMobileQuarryController controller) {
        EntityPlayerMP player = world == null || world.getMinecraftServer() == null ? null
            : world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
        if (player == null || !RuntimeAdvancements.completed(player, "mobile_quarry_relocation")) {
            fail("side_quest_advancement_not_awarded");
            return;
        }
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|PASS|mobile_quarry_relocation|lane_step={}|first_frame=true"
            + "|first_excavation=true|old_frame_cleared=true|second_frame=true"
            + "|second_excavation=true|chunk_ticket=true|offscreen_excavation=true"
            + "|projectred_harness_moved=true|breaker_operated=true|placer_operated=true"
            + "|same_quarry_reused=true|no_spare_quarry=true|no_teleport_blocks=true"
            + "|side_quest_advancement=true"
            + "|worldspike_stationary=true|settled_frames_clean=true"
            + "|quarry_output_routed=true|quarry_output_moved=true"
            + "|relocations={}|ticks={}",
            MobileQuarryRules.LANE_STEP, controller.getRelocations(), ticks);
        reset();
    }

    private void fail(String reason) {
        IndustrialCivilizationCore.LOGGER.error(
            "IC_TEST|FAIL|mobile_quarry_relocation|reason={}|first_frame={}"
            + "|first_excavation={}|relocated={}|old_frame_cleared={}"
            + "|second_frame={}|second_excavation={}|chunk_ticket={}"
            + "|offscreen_excavation={}|projectred_harness_moved={}"
            + "|breaker_operated={}|placer_operated={}|same_quarry_reused={}"
            + "|no_spare_quarry={}|no_teleport_blocks={}"
            + "|settled_frames_clean={}|quarry_output_routed={}"
            + "|quarry_output_moved={}|ticks={}", reason,
            firstFrameBuilt, firstLaneMined, relocated, oldFrameCleared,
            secondFrameBuilt, secondLaneMined, chunkTicketActive, offscreenMining,
            projectRedHarnessMoved, breakerOperated, placerOperated,
            recoveredQuarryObserved, noSpareVerified, noTeleportBlocks,
            settledFramesClean, quarryOutputRouted, quarryOutputMoved, ticks);
        reset();
    }

    private void reset() {
        active = false;
        ticks = 0;
        firstLaneInitial = -1;
        secondLaneInitial = -1;
        firstFrameBuilt = false;
        firstLaneMined = false;
        relocated = false;
        oldFrameCleared = false;
        secondFrameBuilt = false;
        secondLaneMined = false;
        offscreenInitial = -1;
        offscreenStartTick = 0;
        offscreenStarted = false;
        chunkTicketActive = false;
        offscreenMining = false;
        projectRedHarnessMoved = false;
        breakerOperated = false;
        placerOperated = false;
        recoveredQuarryObserved = false;
        noSpareVerified = false;
        noTeleportBlocks = false;
        settledFramesClean = false;
        movementDiagnosticLogged = false;
        quarryOutputRouted = false;
        quarryOutputMoved = false;
        movementCameraSet = false;
        relocatedCameraSet = false;
        verifiedFrameStep = 0;
        initialCarriageFrames.clear();
    }
}
