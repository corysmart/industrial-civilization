package com.industrialcivilization.core;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mrcrayfish.vehicle.entity.vehicle.EntityMiniBus;
import com.mrcrayfish.vehicle.tileentity.TileEntityVehicleCrate;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.core.block.crop.TileEntityCrop;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.BlockEvent;

/** Development-only deterministic scenarios and parseable runtime snapshots. */
public final class CommandIndustrialTest extends CommandBase {
    private static final String PREFIX = "IC_TEST|";
    static final VehicleLogisticsLifecycle VEHICLE_LIFECYCLE = new VehicleLogisticsLifecycle();

    @Override public String getName() { return "ic_test"; }
    @Override public String getUsage(ICommandSender sender) {
        return "/ic_test snapshot [radius] | scenario <workshop_adjacency|earth_ecology|release_recipes|robber_wall_theft|mobile_quarry_relocation|teleport_gate|faction_side_path|faction_gameplay_path|vehicle_logistics_path|strategic_defense_path|agricultural_side_path|automated_agriculture_path|settlement_economy_path|civilization_systems> | assert <workshop_adjacency|earth_ecology>";
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
            EcologyResult result = runEarthEcologyScenario(server, player);
            emit(player, (result.pass ? "PASS" : "FAIL") + "|earth_ecology|robbers="
                + result.robbers + "|patrols=" + result.patrols + "|vanilla_zombies="
                + result.vanillaZombies + "|vanilla_skeletons=" + result.vanillaSkeletons
                + "|other_vanilla_hostiles=" + result.otherVanillaHostiles);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "release_recipes".equals(args[1])) {
            RecipeResult result = runReleaseRecipeScenario(player);
            emit(player, (result.failures.isEmpty() ? "PASS" : "FAIL")
                + "|release_recipes|explicit=" + result.explicitPassed + "/" + result.explicitTotal
                + "|modified=" + result.modifiedPassed + "/" + result.modifiedTotal
                + "|forbidden_inputs=" + result.forbiddenInputs
                + (result.failures.isEmpty() ? "" : "|failures=" + String.join(",", result.failures)));
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "robber_wall_theft".equals(args[1])) {
            int[] result = runRobberWallTheftScenario(player);
            boolean pass = result[0] == 9 && result[1] < 9;
            emit(player, (pass ? "PASS" : "FAIL") + "|robber_wall_theft|blocked_remaining="
                + result[0] + "|open_remaining=" + result[1]);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "mobile_quarry_relocation".equals(args[1])) {
            MobileQuarryLifecycleTest.start(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "teleport_gate".equals(args[1])) {
            int[] result = inspectTeleportGate();
            boolean pass = result[0] == 0 && result[1] == 1 && result[2] == 1
                && result[3] == 0 && result[4] == 0 && result[5] == 1 && result[6] == 1;
            emit(player, (pass ? "PASS" : "FAIL") + "|teleport_gate|legacy_assembly="
                + result[0] + "|ai_recipe=" + result[1] + "|ai_ingredients=" + result[2]
                + "|ic2_native=" + result[3] + "|teleport_tether=" + result[4]
                + "|phase_pearl_sources=" + result[5] + "|phase_pearl_ai=" + result[6]);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "faction_side_path".equals(args[1])) {
            FactionSidePathResult result = runFactionSidePathScenario(player);
            emit(player, (result.pass ? "PASS" : "FAIL") + "|faction_side_path|locations="
                + result.locations + "/4|contacts=" + result.contacts + "/3|factory_stages="
                + result.factoryStages + "/4|outpost=" + result.outpost + "|membership="
                + result.membership + "|companion=" + result.companion + "|follow="
                + result.follow + "|persistence=" + result.persistence + "|advancements="
                + result.advancements + "/6");
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "faction_gameplay_path".equals(args[1])) {
            runFactionGameplayScenario(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "faction_persistence_check".equals(args[1])) {
            runFactionPersistenceCheck(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "vehicle_logistics_path".equals(args[1])) {
            VEHICLE_LIFECYCLE.begin(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "strategic_defense_path".equals(args[1])) {
            runStrategicDefenseScenario(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "agricultural_side_path".equals(args[1])) {
            runAgriculturalSidePathScenario(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "automated_agriculture_path".equals(args[1])) {
            runAutomatedAgricultureScenario(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "quest_persistence_check".equals(args[1])) {
            runQuestPersistenceCheck(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "settlement_economy_path".equals(args[1])) {
            runSettlementEconomyScenario(player);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0])
                && "civilization_systems".equals(args[1])) {
            runCivilizationSystemsScenario(player);
            return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    private static VehicleLogisticsSetup prepareVehicleLogisticsScenario(EntityPlayerMP player)
            throws CommandException {
        List<BlockPos> cities = CommandIndustrialLocateAll.locateIndustrialCities(player, 8192, 2);
        World world = player.world;
        BlockPos base = world.getHeight(player.getPosition().add(20, 0, 0));
        for (BlockPos pos : BlockPos.getAllInBoxMutable(base.add(-6, 0, -5), base.add(16, 6, 5)))
            world.setBlockToAir(pos);
        world.setBlockState(base, IndustrialCivilizationCore.CAR_WORKSHOP.getDefaultState(), 3);
        boolean workshopStructure = WorkshopSystem.deployForTest(world, base,
            IndustrialMachineKind.CAR_WORKSHOP, EnumFacing.NORTH, player);
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++)
            world.setBlockState(base.add(x, 4, z), Blocks.IRON_BLOCK.getDefaultState(), 2);
        TileEntity tile = world.getTileEntity(base);
        TileIndustrialMachine workshop = tile instanceof TileIndustrialMachine
            ? (TileIndustrialMachine) tile : null;
        if (workshop != null) {
            workshop.injectEnergy(EnumFacing.UP, 128D, 128D);
            workshop.update();
        }
        boolean workshopReady = workshopStructure
            && RuntimeAdvancements.completed(player, "car_workshop_deployed");

        ItemStack crate = ItemStack.EMPTY;
        if (workshop != null) {
            net.minecraft.item.Item steel = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("railcraft", "ingot"));
            if (steel != null) {
                workshop.setInventorySlotContents(0,
                    new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 28));
                workshop.setInventorySlotContents(1,
                    new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR, 16));
                workshop.setInventorySlotContents(2, new ItemStack(steel, 64));
                workshop.setLastUser(player);
                workshop.selectRecipeForTest("passenger_carrier");
                for (int tick = 0; tick < 620 && workshop.getCompletedOperations() == 0; tick++) {
                    workshop.injectEnergy(EnumFacing.UP, 128D, 128D);
                    workshop.update();
                }
                crate = workshop.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).copy();
            }
        }
        boolean manufactured = !crate.isEmpty() && stackIs(crate, "vehicle:vehicle_crate")
            && ProgressionState.has(player, "industrial_service_carrier_manufactured");

        BlockPos cratePos = base.add(12, 0, 0);
        boolean crateOpened = deployVehicleCrate(player, crate, cratePos);
        return new VehicleLogisticsSetup(player.getUniqueID(), world, cities, cratePos,
            workshopReady, manufactured, crateOpened);
    }

    private static VehicleLogisticsResult finishVehicleLogisticsScenario(EntityPlayerMP player,
            VehicleLogisticsSetup setup, EntityMiniBus vehicle) {
        boolean deployed = vehicle != null
            && ProgressionState.has(player, "industrial_service_carrier_deployed");
        boolean mobility = false;
        boolean storage = false;
        boolean crafting = false;
        boolean dockItem = false;
        boolean dockFluid = false;
        if (vehicle != null) {
            vehicle.setCurrentFuel(100F);
            vehicle.currentSpeed = 0.35F;
            vehicle.setSpeed(0.35F);
            VehicleIntegrationSystem.updateVehicleForTest(vehicle);
            vehicle.currentSpeed = 0F;
            vehicle.setSpeed(0F);
            VehicleIntegrationSystem.updateVehicleForTest(vehicle);
            mobility = RuntimeAdvancements.completed(player, "regional_mobility");
            player.connection.setPlayerLocation(vehicle.posX + 1, vehicle.posY,
                vehicle.posZ + 1, 0F, 0F);
            player.inventory.currentItem = 0;
            player.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
            player.setSneaking(true);
            VehicleIntegrationSystem.interactForTest(player, vehicle, EnumHand.MAIN_HAND);
            storage = player.openContainer instanceof ContainerChest
                && vehicle.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
                    .getSlots() == 54;
            player.closeScreen();
            player.inventory.setInventorySlotContents(0,
                new ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.CRAFTING_TABLE)));
            VehicleIntegrationSystem.interactForTest(player, vehicle, EnumHand.MAIN_HAND);
            crafting = player.openContainer instanceof ContainerMobileWorkbench;
            player.closeScreen();
            player.setSneaking(false);

            BlockPos dockPos = new BlockPos(vehicle.posX + 2, vehicle.posY, vehicle.posZ);
            setup.world.setBlockState(dockPos,
                IndustrialCivilizationCore.VEHICLE_SERVICE_DOCK.getDefaultState(), 3);
            TileEntity dock = setup.world.getTileEntity(dockPos);
            if (dock != null) {
                IItemHandler items = dock.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                if (items != null) dockItem = items.insertItem(0,
                    new ItemStack(Items.IRON_INGOT), false).isEmpty();
                IFluidHandler fluids = dock.getCapability(
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
                if (fluids != null) dockFluid = fluids.fill(
                    new FluidStack(FluidRegistry.WATER, 1000), true) == 1000
                    && fluids.getTankProperties().length > 0
                    && fluids.getTankProperties()[0].getCapacity() == 64000;
            }
        }

        boolean nationTransfer = exerciseNationExchange(player, setup.cities);
        String[] milestones = {"car_workshop_deployed", "regional_mobility",
            "industrial_service_carrier", "nation_trade_network"};
        int advancements = 0;
        for (String milestone : milestones)
            if (RuntimeAdvancements.completed(player, milestone)) advancements++;
        return new VehicleLogisticsResult(setup.cities.size(), setup.workshop ? 1 : 0,
            setup.manufactured ? 1 : 0, deployed ? 1 : 0, mobility ? 1 : 0,
            storage ? 1 : 0, crafting ? 1 : 0, dockItem ? 1 : 0,
            dockFluid ? 1 : 0, nationTransfer ? 1 : 0, advancements);
    }

    private static boolean deployVehicleCrate(EntityPlayerMP player, ItemStack crate,
            BlockPos pos) {
        if (crate.isEmpty() || !crate.hasTagCompound()) return false;
        Block crateBlock = Block.getBlockFromItem(crate.getItem());
        if (crateBlock == Blocks.AIR) return false;
        net.minecraft.block.state.IBlockState crateState = crateBlock.getDefaultState();
        player.world.setBlockState(pos, crateState, 2);
        TileEntity tile = player.world.getTileEntity(pos);
        if (!(tile instanceof TileEntityVehicleCrate)) return false;
        NBTTagCompound blockEntity = crate.getTagCompound().getCompoundTag("BlockEntityTag").copy();
        blockEntity.setInteger("x", pos.getX());
        blockEntity.setInteger("y", pos.getY());
        blockEntity.setInteger("z", pos.getZ());
        TileEntityVehicleCrate vehicleCrate = (TileEntityVehicleCrate) tile;
        vehicleCrate.readFromNBT(blockEntity);
        vehicleCrate.markDirty();
        player.world.notifyBlockUpdate(pos, crateState, crateState, 3);
        return true;
    }

    private static void emitVehicleResult(EntityPlayerMP player, VehicleLogisticsResult result) {
        emit(player, (result.pass ? "PASS" : "FAIL") + "|vehicle_logistics_path|cities="
            + result.cities + "/2|workshop=" + result.workshop + "|manufactured="
            + result.manufactured + "|deployed=" + result.deployed + "|mobility="
            + result.mobility + "|storage=" + result.storage + "|crafting="
            + result.crafting + "|dock_item=" + result.dockItem + "|dock_fluid="
            + result.dockFluid + "|nation_transfer=" + result.nationTransfer
            + "|advancements=" + result.advancements + "/4");
    }

    private static void runStrategicDefenseScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos origin = world.getHeight(player.getPosition().add(20, 0, 20));
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-6, 0, -6), origin.add(30, 7, 8)))
            world.setBlockToAir(pos);

        world.setBlockState(origin, IndustrialCivilizationCore.GUN_FACTORY.getDefaultState(), 3);
        boolean factoryStructure = WorkshopSystem.deployForTest(world, origin,
            IndustrialMachineKind.GUN_FACTORY, EnumFacing.NORTH, player);
        for (int x = -4; x <= 4; x++) for (int z = -3; z <= 3; z++)
            world.setBlockState(origin.add(x, 4, z), Blocks.IRON_BLOCK.getDefaultState(), 2);
        TileEntity factoryTile = world.getTileEntity(origin);
        TileIndustrialMachine factory = factoryTile instanceof TileIndustrialMachine
            ? (TileIndustrialMachine) factoryTile : null;
        if (factory != null) {
            net.minecraft.item.Item steel = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("railcraft", "ingot"));
            if (steel != null) {
                factory.setInventorySlotContents(0,
                    new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 10));
                factory.setInventorySlotContents(1,
                    new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR, 6));
                factory.setInventorySlotContents(2, new ItemStack(steel, 32));
                factory.setLastUser(player);
                factory.selectRecipeForTest("combat_shotgun");
                for (int tick = 0; tick < 900 && factory.getCompletedOperations() == 0; tick++) {
                    factory.injectEnergy(EnumFacing.UP, 512D, 512D);
                    factory.update();
                }
            }
        }
        boolean factoryReady = factoryStructure
            && RuntimeAdvancements.completed(player, "advanced_armament_factory");

        Block launcherBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("icbmclassic", "launcherbase"));
        Block screenBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("icbmclassic", "launcherscreen"));
        Block radarBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("icbmclassic", "radarstation"));
        BlockPos basePos = origin.add(18, 0, 0);
        BlockPos screenPos = basePos.east();
        BlockPos radarPos = basePos.east(2);
        if (launcherBlock != null && screenBlock != null && radarBlock != null) {
            world.setBlockState(basePos, launcherBlock.getDefaultState(), 3);
            world.setBlockState(screenPos, screenBlock.getDefaultState(), 3);
            world.setBlockState(radarPos, radarBlock.getDefaultState(), 3);
        }
        TileEntity baseTile = world.getTileEntity(basePos);
        TileEntity screenTile = world.getTileEntity(screenPos);
        TileEntity radarTile = world.getTileEntity(radarPos);
        if (baseTile instanceof TileLauncherBase && screenTile instanceof TileLauncherScreen
                && radarTile instanceof TileRadarStation) {
            TileLauncherBase base = (TileLauncherBase) baseTile;
            TileLauncherScreen screen = (TileLauncherScreen) screenTile;
            TileRadarStation radar = (TileRadarStation) radarTile;
            StrategicDefenseSystem.markForTest(base, player);
            StrategicDefenseSystem.markForTest(screen, player);
            StrategicDefenseSystem.markForTest(radar, player);
            base.getNetworkNode().connectToTiles();
            screen.getNetworkNode().connectToTiles();
            base.energyStorage.setEnergyStored(50000);
            screen.energyStorage.setEnergyStored(50000);
            radar.energyStorage.setEnergyStored(50000);
            screen.setTarget(new Vec3d(basePos.getX() + 96, basePos.getY(), basePos.getZ()));
            radar.setDetectionRange(128);
            radar.setTriggerRange(96);
            net.minecraft.item.Item missile = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("icbmclassic", "explosive_missile"));
            if (missile != null) base.tryInsertMissile(player, EnumHand.MAIN_HAND,
                new ItemStack(missile));
            StrategicDefenseSystem.evaluateForTest(base);
        }
        int passed = 0;
        String[] advancements = {"advanced_armament_factory", "icbm_launch_control",
            "icbm_radar_defense", "icbm_conventional_missile"};
        for (String advancement : advancements)
            if (RuntimeAdvancements.completed(player, advancement)) passed++;
        emit(player, (passed == 4 ? "PASS" : "FAIL") + "|strategic_defense_path|factory="
            + (factoryReady ? 1 : 0) + "|launch_control="
            + (RuntimeAdvancements.completed(player, "icbm_launch_control") ? 1 : 0)
            + "|radar=" + (RuntimeAdvancements.completed(player, "icbm_radar_defense") ? 1 : 0)
            + "|missile=" + (RuntimeAdvancements.completed(player, "icbm_conventional_missile") ? 1 : 0)
            + "|advancements=" + passed + "/4");
    }

    private static void runAgriculturalSidePathScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos origin = world.getHeight(player.getPosition().add(20, 0, 20));
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-8, -1, -8), origin.add(32, 10, 12)))
            world.setBlockToAir(pos);
        Block cropBlock = ((ic2.core.item.crop.ItemCropStick)
            ic2.core.platform.registry.Ic2Items.cropStick.getItem()).getBlock();
        CropCard wheat = crop("Wheat");
        CropCard cocoa = crop("Cocoa");
        CropCard hemp = crop("Hemp");
        TileEntityCrop center = null;
        TileEntityCrop hempTile = null;
        if (cropBlock != null && wheat != null && cocoa != null && hemp != null) {
            BlockPos[] crops = {origin.west(), origin, origin.east(), origin.north(3)};
            world.setBlockState(origin.south(2), Blocks.WATER.getDefaultState(), 3);
            for (BlockPos pos : crops) {
                world.setBlockState(pos.down(), Blocks.FARMLAND.getDefaultState(), 3);
                world.setBlockState(pos, cropBlock.getDefaultState(), 3);
            }
            TileEntity westTile = world.getTileEntity(origin.west());
            TileEntity centerTile = world.getTileEntity(origin);
            TileEntity eastTile = world.getTileEntity(origin.east());
            TileEntity northTile = world.getTileEntity(origin.north(3));
            if (westTile instanceof TileEntityCrop && centerTile instanceof TileEntityCrop
                    && eastTile instanceof TileEntityCrop && northTile instanceof TileEntityCrop) {
                TileEntityCrop west = (TileEntityCrop) westTile;
                center = (TileEntityCrop) centerTile;
                TileEntityCrop east = (TileEntityCrop) eastTile;
                hempTile = (TileEntityCrop) northTile;
                west.setCrop(wheat); west.setCurrentSize(wheat.getMaxSize());
                east.setCrop(cocoa); east.setCurrentSize(cocoa.getMaxSize());
                center.setCrossingBase(true); center.breeder = player.getUniqueID();
                hempTile.setCrop(hemp); hempTile.setCurrentSize(hemp.getMaxSize());
                hempTile.breeder = player.getUniqueID();
                AgriculturalSidePathSystem.evaluateCropForTest(player, origin);
                AgriculturalSidePathSystem.harvestHempForTest(player, hempTile);
            }
        }

        net.minecraft.item.Item hempItem = ForgeRegistries.ITEMS.getValue(
            new ResourceLocation("ic2", "itemmisc"));
        InventoryBasic hempInput = new InventoryBasic("hemp", false, 1);
        if (hempItem != null) hempInput.setInventorySlotContents(0, new ItemStack(hempItem, 1, 159));
        AgriculturalSidePathSystem.recordCraftForTest(player, new ItemStack(Items.STRING), hempInput);
        InventoryBasic leadInput = new InventoryBasic("lead", false, 5);
        for (int slot = 0; slot < 4; slot++) leadInput.setInventorySlotContents(slot,
            new ItemStack(Items.STRING));
        net.minecraft.item.Item resin = ForgeRegistries.ITEMS.getValue(
            new ResourceLocation("ic2", "itemharz"));
        if (resin != null) leadInput.setInventorySlotContents(4, new ItemStack(resin));
        AgriculturalSidePathSystem.recordCraftForTest(player, new ItemStack(Items.LEAD, 2), leadInput);
        EntityCow cow = new EntityCow(world);
        cow.setPosition(origin.getX() + 5.5D, origin.getY(), origin.getZ() + 0.5D);
        world.spawnEntity(cow);
        cow.setLeashHolder(player, true);
        AgriculturalSidePathSystem.completeLivestockForTest(player, cow);

        BlockPos sowerPos = origin.add(14, 0, 0);
        BlockPos gathererPos = origin.add(18, 0, 0);
        BlockPos furnacePos = origin.add(16, 0, 3);
        placeRegistered(world, sowerPos, "industrialforegoing:crop_sower");
        placeRegistered(world, gathererPos, "industrialforegoing:crop_recolector");
        placeRegistered(world, furnacePos, "industrialforegoing:resourceful_furnace");
        ItemStack generatorStack = ic2.core.platform.registry.Ic2Items.generator.copy();
        Block generatorBlock = Block.getBlockFromItem(generatorStack.getItem());
        BlockPos generatorPos = origin.add(16, 0, 6);
        if (generatorBlock != Blocks.AIR && generatorStack.getItem() instanceof net.minecraft.item.ItemBlock) {
            net.minecraft.block.state.IBlockState generatorState = generatorBlock.getStateForPlacement(
                world, generatorPos, EnumFacing.UP, 0.5F, 0.5F, 0.5F,
                generatorStack.getMetadata(), player, EnumHand.MAIN_HAND);
            ((net.minecraft.item.ItemBlock) generatorStack.getItem()).placeBlockAt(generatorStack,
                player, world, generatorPos, EnumFacing.UP, 0.5F, 0.5F, 0.5F, generatorState);
        }
        TileEntity sower = world.getTileEntity(sowerPos);
        TileEntity gatherer = world.getTileEntity(gathererPos);
        TileEntity furnace = world.getTileEntity(furnacePos);
        TileEntity generator = world.getTileEntity(generatorPos);
        AgriculturalSidePathSystem.markForestryForTest(sower, player);
        AgriculturalSidePathSystem.markForestryForTest(gatherer, player);
        AgriculturalSidePathSystem.markForestryForTest(furnace, player);
        fillEnergy(sower); fillEnergy(gatherer); fillEnergy(furnace);
        insertAny(sower, new ItemStack(Blocks.SAPLING));
        if (furnace instanceof com.buuz135.industrial.tile.misc.ResourcefulFurnaceTile)
            ((com.buuz135.industrial.tile.misc.ResourcefulFurnaceTile) furnace).output
                .insertItem(0, new ItemStack(Items.COAL, 1, 1), false);
        else insertAny(furnace, new ItemStack(Items.COAL, 1, 1));
        insertAny(generator, new ItemStack(Items.COAL, 1, 1));
        world.setBlockState(sowerPos.up(), Blocks.SAPLING.getDefaultState(), 3);
        player.connection.setPlayerLocation(furnacePos.getX() + 0.5D,
            furnacePos.getY() + 1D, furnacePos.getZ() + 0.5D, 0F, 0F);
        AgriculturalSidePathSystem.evaluateForestryForTest(player);

        String[] advancements = {"crop_engineering", "breed_hemp", "renewable_string",
            "controlled_livestock", "lv_tree_planting", "lv_charcoal_tree_farm"};
        int passed = 0;
        for (String advancement : advancements)
            if (RuntimeAdvancements.completed(player, advancement)) passed++;
        flushQuestProgressForTest(player);
        emit(player, (passed == advancements.length ? "PASS" : "FAIL")
            + "|agricultural_side_path|crop=" + yes(player, "crop_engineering")
            + "|hemp=" + yes(player, "breed_hemp") + "|string=" + yes(player, "renewable_string")
            + "|livestock=" + yes(player, "controlled_livestock") + "|planting="
            + yes(player, "lv_tree_planting") + "|charcoal_loop="
            + yes(player, "lv_charcoal_tree_farm") + "|advancements=" + passed + "/6"
            + "|crop_block=" + (cropBlock == null ? "missing" : cropBlock.getRegistryName()));
    }

    private static CropCard crop(String id) {
        if (Crops.instance == null) return null;
        for (CropCard crop : Crops.instance.getCrops()) if (id.equalsIgnoreCase(crop.getId())) return crop;
        return null;
    }

    private static void runAutomatedAgricultureScenario(EntityPlayerMP player) {
        boolean questPrerequisites = seedAgricultureQuestPrerequisitesForTest(player);
        // Establish the actual prerequisite chain (quests 134-139) before
        // validating 140-143 so reload detection matches a reachable save.
        runAgriculturalSidePathScenario(player);
        World world = player.world;
        BlockPos origin = world.getHeight(player.getPosition().add(20, 0, 20));
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-5, -1, -8), origin.add(23, 6, 8)))
            world.setBlockToAir(pos);

        String[] ids = {"industrialforegoing:plant_interactor",
            "industrialforegoing:crop_enrich_material_injector",
            "industrialforegoing:animal_stock_increaser",
            "industrialforegoing:animal_growth_increaser",
            "industrialforegoing:animal_independence_selector",
            "industrialforegoing:animal_resource_harvester",
            "industrialforegoing:animal_byproduct_recolector",
            "industrialforegoing:sewage_composter_solidifier",
            "industrialforegoing:water_resources_collector"};
        BlockPos[] positions = {origin, origin.add(0, 0, 4), origin.add(8, 0, 0),
            origin.add(8, 0, 3), origin.add(8, 0, 6), origin.add(13, 0, 0),
            origin.add(13, 0, 3), origin.add(13, 0, 6), origin.add(20, 0, 0)};
        for (int i = 0; i < ids.length; i++) {
            placeRegistered(world, positions[i], ids[i]);
            TileEntity tile = world.getTileEntity(positions[i]);
            AgriculturalSidePathSystem.markAutomationForTest(tile, player);
            fillEnergy(tile);
        }

        // A persistent crop plot plus fertilizer input and connected harvested-output storage.
        for (int x = -2; x <= 2; x++) for (int z = -3; z <= -2; z++) {
            BlockPos cropPos = origin.add(x, 0, z);
            world.setBlockState(cropPos.down(), Blocks.FARMLAND.getDefaultState(), 3);
            world.setBlockState(cropPos, Blocks.WHEAT.getDefaultState()
                .withProperty(net.minecraft.block.BlockCrops.AGE, 7), 3);
        }
        insertAny(world.getTileEntity(positions[1]), new ItemStack(Items.DYE, 8, 15));
        placeOutputChest(world, positions[0].east(), new ItemStack(Items.WHEAT, 8));

        // Two adults, a routed juvenile, and food establish a population-controlled herd.
        for (int i = 0; i < 3; i++) {
            EntityCow cow = new EntityCow(world);
            cow.setGrowingAge(i == 2 ? -12000 : 0);
            cow.setPosition(origin.getX() + 9.5D + i, origin.getY(), origin.getZ() + 0.5D);
            world.spawnEntity(cow);
        }
        insertAny(world.getTileEntity(positions[2]), new ItemStack(Items.WHEAT, 16));

        // Renewable output, sewage transfer, and finished fertilizer prove the peaceful loop.
        placeOutputChest(world, positions[5].east(), new ItemStack(Items.MILK_BUCKET));
        fillFluid(world.getTileEntity(positions[6]), "sewage", 1000);
        placeOutputChest(world, positions[7].east(), new ItemStack(Items.DYE, 4, 15));

        // The final collector faces an actual water body and a connected aquatic output chest.
        for (int x = 18; x <= 22; x++) for (int z = -4; z <= -2; z++) {
            world.setBlockState(origin.add(x, -1, z), Blocks.STONE.getDefaultState(), 3);
            world.setBlockState(origin.add(x, 0, z), Blocks.WATER.getDefaultState(), 3);
        }
        placeOutputChest(world, positions[8].east(), new ItemStack(Items.FISH, 2));

        player.connection.setPlayerLocation(origin.getX() + 11.5D, origin.getY() + 1D,
            origin.getZ() + 2.5D, 0F, 0F);
        AgriculturalSidePathSystem.evaluateAutomationForTest(player);
        String[] advancements = {"automated_field_agriculture", "automated_animal_husbandry",
            "automated_animal_resources", "automated_water_resources"};
        int passed = 0;
        for (String advancement : advancements)
            if (RuntimeAdvancements.completed(player, advancement)) passed++;
        flushQuestProgressForTest(player);
        emit(player, (passed == advancements.length && questPrerequisites ? "PASS" : "FAIL")
            + "|automated_agriculture_path|field=" + yes(player, advancements[0])
            + "|husbandry=" + yes(player, advancements[1])
            + "|resources=" + yes(player, advancements[2])
            + "|water=" + yes(player, advancements[3]) + "|advancements=" + passed + "/4"
            + "|seeded_main_anchors=" + (questPrerequisites ? 1 : 0));
    }

    private static boolean seedAgricultureQuestPrerequisitesForTest(EntityPlayerMP player) {
        addTestStack(player, "minecraft:coal", 0, 8);
        addTestStack(player, "minecraft:iron_ingot", 0, 8);
        addTestStack(player, "ic2:blockmetal", 0, 1);
        addTestStack(player, "ic2:blockmetal", 1, 1);
        addTestStack(player, "ic2:itemmisc", 450, 1);
        addTestStack(player, "ironchest:iron_chest", 0, 1);
        addTestStack(player, "minecraft:crafting_table", 0, 1);
        addTestStack(player, "minecraft:furnace", 0, 1);
        addTestStack(player, "minecraft:chest", 0, 1);
        addTestStack(player, "minecraft:iron_door", 0, 1);
        addTestStack(player, "minecraft:torch", 0, 16);
        addTestStack(player, "ic2:blockgenerator", 0, 1);
        flushQuestProgressForTest(player);
        for (int id : new int[] {0, 1, 3, 4, 5}) {
            betterquesting.api.questing.IQuest quest =
                betterquesting.questing.QuestDatabase.INSTANCE.getValue(id);
            if (quest == null) return false;
            if (!quest.isComplete(player.getUniqueID())) {
                // Fixture-only setup: these early anchors already have separate
                // main-campaign acceptance and are not assertions in this path.
                quest.setComplete(player.getUniqueID(), player.world.getTotalWorldTime());
            }
        }
        betterquesting.handlers.SaveLoadHandler.INSTANCE.markDirty();
        betterquesting.handlers.SaveLoadHandler.INSTANCE.saveDatabases();
        return true;
    }

    private static void addTestStack(EntityPlayerMP player, String id, int metadata, int count) {
        net.minecraft.item.Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item != null) player.inventory.addItemStackToInventory(
            new ItemStack(item, count, metadata));
    }

    private static void placeOutputChest(World world, BlockPos pos, ItemStack stack) {
        world.setBlockState(pos, Blocks.CHEST.getDefaultState(), 3);
        insertAny(world.getTileEntity(pos), stack);
    }

    private static boolean fillFluid(TileEntity tile, String fluidName, int amount) {
        if (tile == null || FluidRegistry.getFluid(fluidName) == null) return false;
        for (EnumFacing side : EnumFacing.values()) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if (handler != null && handler.fill(new FluidStack(FluidRegistry.getFluid(fluidName), amount), true) > 0)
                return true;
        }
        IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        return handler != null
            && handler.fill(new FluidStack(FluidRegistry.getFluid(fluidName), amount), true) > 0;
    }

    private static void runSettlementEconomyScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos origin = world.getHeight(player.getPosition().add(20, 0, 20));
        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-3, -1, -3),
                origin.add(48, 10, 18))) world.setBlockToAir(pos);
        SettlementEconomySystem.register(world, origin, "primitive", "food", 0);
        BlockPos stockpile = origin.add(2, 0, 2);
        world.setBlockState(stockpile, Blocks.CHEST.getDefaultState(), 3);
        TileEntity chest = world.getTileEntity(stockpile);
        insertAny(chest, new ItemStack(Blocks.PLANKS, 40));
        insertAny(chest, new ItemStack(Blocks.COBBLESTONE, 32));
        insertAny(chest, new ItemStack(Items.BREAD, 8));

        int absorbed = 0, largestCycle = 0;
        boolean premature = false;
        for (int cycle = 0; cycle < 10; cycle++) {
            int moved = SettlementEconomySystem.absorbForTest(world, origin);
            absorbed += moved;
            largestCycle = Math.max(largestCycle, moved);
            if (cycle == 0) premature = SettlementEconomySystem.upgradeForTest(world, origin);
        }
        boolean upgraded = SettlementEconomySystem.upgradeForTest(world, origin);
        long[] paid = SettlementEconomySystem.snapshotForTest(world, origin);
        boolean exactBill = paid.length == 8 && paid[0] == 1 && paid[1] == 0
            && paid[2] == 0 && paid[6] == 0;
        boolean physicalExpansion = world.getBlockState(origin.add(32, 1, 2)).getBlock()
            == Blocks.PLANKS && world.getBlockState(origin.add(35, 2, 6)).getBlock() == Blocks.CHEST;
        player.connection.setPlayerLocation(origin.getX() + 0.5D, origin.getY() + 1D,
            origin.getZ() + 0.5D, 0F, 0F);
        long creditsBefore = paid.length == 8 ? paid[7] : -1;
        SettlementEconomySystem.recordTrade(player, 7);
        long[] traded = SettlementEconomySystem.snapshotForTest(world, origin);
        boolean circulation = traded.length == 8 && traded[7] == creditsBefore + 7;
        boolean persistence = SettlementEconomySystem.roundTripForTest(world, origin);
        boolean pass = absorbed == 80 && largestCycle <= 16 && !premature && upgraded
            && exactBill && physicalExpansion && circulation && persistence;
        emit(player, (pass ? "PASS" : "FAIL") + "|settlement_economy_path|absorbed="
            + absorbed + "|max_cycle=" + largestCycle + "|premature=" + premature
            + "|tier=" + (paid.length == 0 ? -1 : paid[0]) + "|exact_bill="
            + (exactBill ? 1 : 0) + "|physical_expansion=" + (physicalExpansion ? 1 : 0)
            + "|circulation=" + (circulation ? 1 : 0) + "|persistence="
            + (persistence ? 1 : 0));
    }

    private static void runCivilizationSystemsScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos base = world.getHeight(player.getPosition().add(40, 0, 40));
        for (BlockPos clear : BlockPos.getAllInBoxMutable(base.add(-3, -2, -3), base.add(28, 8, 12)))
            world.setBlockToAir(clear);
        for (BlockPos floor : BlockPos.getAllInBoxMutable(base.add(-3, -1, -3), base.add(28, -1, 12)))
            world.setBlockState(floor, Blocks.STONE.getDefaultState(), 2);

        BlockPos sourcePos = base;
        BlockPos destinationPos = base.add(6, 0, 0);
        BlockPos assemblerPos = destinationPos.add(0, 0, 1);
        BlockPos highPriorityPos = base.add(10, 0, 6);
        BlockPos lowPriorityPos = base.add(14, 0, 6);
        BlockPos servicePos = base.add(20, 0, 0);
        BlockPos serviceCargoPos = servicePos.add(1, 0, 0);
        String testRoute = "civilization_test_route_" + base.getX() + "_" + base.getZ();
        String highPriorityName = "Critical Fabrication " + base.getX() + " " + base.getZ();
        world.setBlockState(sourcePos, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState(), 3);
        world.setBlockState(destinationPos, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState(), 3);
        world.setBlockState(assemblerPos, IndustrialCivilizationCore.PROGRAMMABLE_ASSEMBLER.getDefaultState(), 3);
        world.setBlockState(highPriorityPos, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState(), 3);
        world.setBlockState(lowPriorityPos, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState(), 3);
        world.setBlockState(servicePos, IndustrialCivilizationCore.CIVILIZATION_SERVICE_INTERFACE.getDefaultState(), 3);
        world.setBlockState(serviceCargoPos, IndustrialCivilizationCore.INTERPLANETARY_CARGO_CONTROLLER.getDefaultState(), 3);
        TileIndustrialMachine source = (TileIndustrialMachine) world.getTileEntity(sourcePos);
        TileIndustrialMachine destination = (TileIndustrialMachine) world.getTileEntity(destinationPos);
        TileIndustrialMachine assembler = (TileIndustrialMachine) world.getTileEntity(assemblerPos);
        TileIndustrialMachine highPriority = (TileIndustrialMachine) world.getTileEntity(highPriorityPos);
        TileIndustrialMachine lowPriority = (TileIndustrialMachine) world.getTileEntity(lowPriorityPos);
        TileIndustrialMachine service = (TileIndustrialMachine) world.getTileEntity(servicePos);
        TileIndustrialMachine serviceCargo = (TileIndustrialMachine) world.getTileEntity(serviceCargoPos);
        source.setLastUser(player); destination.setLastUser(player); service.setLastUser(player);
        source.seedNationExchange(testRoute, "industrialcivilizationcore:precision_frame");
        destination.seedNationExchange(testRoute, "minecraft:iron_ingot");
        source.configureFacilityForTest("Earth Components", "source factory");
        destination.configureFacilityForTest("Mars Spares", "critical spares reserve");
        source.requestManifestForTest("Mars Spares", "industrialcivilizationcore:precision_frame", 1);
        source.setEnergyForTest(source.getCapacity());
        boolean freight = source.transferNationCargoForTest();
        String[] manifest = source.policySnapshotForTest();
        boolean manifestProof = freight && "delivered".equals(manifest[2])
            && "1".equals(manifest[4])
            && destination.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).getItem()
                == IndustrialCivilizationCore.PRECISION_FRAME;

        destination.configurePolicyForTest("industrialcivilizationcore:control_processor", 1, 1,
            1024, "west");
        destination.setEnergyForTest(0);
        destination.runPolicyForTest();
        boolean shed = destination.policyRedstonePower(EnumFacing.WEST) == 15;
        assembler.setInventorySlotContents(0, new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 3));
        assembler.setInventorySlotContents(1, new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE, 3));
        assembler.setInventorySlotContents(2, new ItemStack(Items.REDSTONE, 3));
        destination.setEnergyForTest(destination.getCapacity());
        destination.runPolicyForTest();
        assembler.setEnergyForTest(assembler.getCapacity());
        for (int tick = 0; tick < assembler.getDuration() * 3; tick++) assembler.update();
        destination.runPolicyForTest();
        String[] policy = destination.policySnapshotForTest();
        boolean production = assembler.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).getItem()
            == IndustrialCivilizationCore.CONTROL_PROCESSOR;
        boolean recovery = "reserve satisfied".equals(policy[0]) && policy[1].isEmpty();

        highPriority.seedNationExchange(testRoute, "minecraft:iron_ingot");
        lowPriority.seedNationExchange(testRoute, "minecraft:iron_ingot");
        highPriority.configureFacilityForTest(highPriorityName, "priority eight reserve");
        lowPriority.configureFacilityForTest("Deferred Workshop", "priority two reserve");
        highPriority.configurePolicyForTest("industrialcivilizationcore:precision_frame",
            1, 3, 0, "north", 8);
        lowPriority.configurePolicyForTest("industrialcivilizationcore:precision_frame",
            1, 3, 0, "north", 2);
        lowPriority.runPolicyForTest();
        boolean priorityDeferred = lowPriority.policySnapshotForTest()[1]
            .contains("higher-priority facility " + highPriorityName);
        source.setInventorySlotContents(0,
            new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME));
        highPriority.runPolicyForTest();
        String[] priorityManifest = source.policySnapshotForTest();
        String[] priorityDecision = highPriority.policySnapshotForTest();
        String priorityHighItem = stackId(highPriority.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT));
        String priorityLowItem = stackId(lowPriority.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT));
        String prioritySourceItem = stackId(source.getStackInSlot(0));
        boolean priorityServed = highPriority.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT)
            .getItem() == IndustrialCivilizationCore.PRECISION_FRAME
            && lowPriority.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).isEmpty();
        boolean priority = priorityDeferred && priorityServed;

        BlockPos settlementOrigin = base.add(20, 0, 0);
        SettlementEconomySystem.register(world, settlementOrigin, "primitive", "machine_service", 3);
        boolean configured = service.configureServiceForTest("earth_machine_service");
        service.setInventorySlotContents(0, new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 16));
        service.setInventorySlotContents(1, new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR, 8));
        service.setInventorySlotContents(2, new ItemStack(Items.IRON_INGOT, 32));
        service.update();
        service.setInventorySlotContents(2, new ItemStack(IndustrialCivilizationCore.AI_CORE));
        boolean started = service.startCommissioningForTest();
        service.setEnergyForTest(service.getCapacity());
        service.update();
        boolean registeredRouteGuard = service.policySnapshotForTest()[7]
            .contains("route is unregistered");
        serviceCargo.seedNationExchange(testRoute, "minecraft:iron_ingot");
        for (int tick = 0; tick < 205; tick++) service.update();
        String[] serviceState = service.policySnapshotForTest();
        boolean commissioned = "operating".equals(serviceState[6])
            && world.getBlockState(servicePos.add(2, -1, 2)).getBlock()
                == IndustrialCivilizationCore.INDUSTRIAL_FLOOR;
        service.setInventorySlotContents(2, ItemStack.EMPTY);
        service.update();
        String[] suspendedState = service.policySnapshotForTest();
        boolean suspended = "suspended".equals(suspendedState[6])
            && suspendedState[7].contains("AI Core authorization removed");
        service.setInventorySlotContents(2, new ItemStack(IndustrialCivilizationCore.AI_CORE));
        service.setEnergyForTest(service.getCapacity());
        service.update();
        boolean resumed = "operating".equals(service.policySnapshotForTest()[6]);
        boolean suspension = suspended && resumed;
        boolean settlementService = SettlementEconomySystem.hasTierThreeSettlement(world,
            servicePos, 64D) && SettlementEconomySystem.machineServiceForTest(world,
                settlementOrigin);
        boolean persistence = source.policyRoundTripForTest()
            && destination.policyRoundTripForTest() && service.policyRoundTripForTest();
        boolean pass = manifestProof && shed && production && recovery && priority && configured
            && started && registeredRouteGuard && commissioned && suspension
            && settlementService && persistence;
        player.connection.setPlayerLocation(base.getX() + 10.5D, base.getY() + 9D,
            base.getZ() - 14.5D, 0F, 24F);
        String result = (pass ? "PASS" : "FAIL") + "|civilization_systems|manifest="
            + (manifestProof ? 1 : 0) + "|load_shed=" + (shed ? 1 : 0)
            + "|production=" + (production ? 1 : 0)
            + "|recovery=" + (recovery ? 1 : 0) + "|priority=" + (priority ? 1 : 0)
            + "|priority_deferred=" + (priorityDeferred ? 1 : 0)
            + "|priority_served=" + (priorityServed ? 1 : 0)
            + "|priority_manifest=" + priorityManifest[2]
            + "|priority_failure=" + token(priorityManifest[3])
            + "|priority_action=" + token(priorityDecision[0])
            + "|priority_blocker=" + token(priorityDecision[1])
            + "|priority_source_item=" + prioritySourceItem
            + "|priority_high_item=" + priorityHighItem
            + "|priority_low_item=" + priorityLowItem
            + "|route_guard=" + (registeredRouteGuard ? 1 : 0)
            + "|service=" + (commissioned ? 1 : 0)
            + "|suspension=" + (suspension ? 1 : 0)
            + "|physical=" + (commissioned ? 1 : 0) + "|persistence=" + (persistence ? 1 : 0)
            + "|base=" + coordinates(base);
        emitDetailed(player, result, (pass ? "PASS" : "FAIL")
            + "|civilization_systems|all_checks=" + (pass ? 1 : 0));
    }

    private static void placeRegistered(World world, BlockPos pos, String id) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        if (block != null) world.setBlockState(pos, block.getDefaultState(), 3);
    }

    private static void fillEnergy(TileEntity tile) {
        if (tile == null) return;
        for (EnumFacing side : EnumFacing.values()) {
            IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (energy != null && energy.receiveEnergy(1000000, false) > 0) return;
        }
        for (EnumFacing side : EnumFacing.values()) {
            net.modcrafters.mclib.energy.IGenericEnergyStorage generic =
                net.ndrei.teslacorelib.energy.EnergySystemFactory.INSTANCE.wrapTileEntity(tile, side);
            if (generic != null && generic.givePower(1000000L, false) > 0L) return;
        }
    }

    private static boolean insertAny(TileEntity tile, ItemStack stack) {
        if (tile == null) return false;
        for (EnumFacing side : EnumFacing.values()) {
            IItemHandler items = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (items == null) continue;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack remaining = items.insertItem(slot, stack.copy(), false);
                if (remaining.isEmpty()) return true;
            }
        }
        IItemHandler unsided = tile.getCapability(
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (unsided != null) for (int slot = 0; slot < unsided.getSlots(); slot++) {
            ItemStack remaining = unsided.insertItem(slot, stack.copy(), false);
            if (remaining.isEmpty()) return true;
        }
        if (tile instanceof IInventory) {
            IInventory inventory = (IInventory) tile;
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
                if (inventory.isItemValidForSlot(slot, stack)) {
                    inventory.setInventorySlotContents(slot, stack.copy());
                    return true;
                }
        }
        return false;
    }

    private static int yes(EntityPlayerMP player, String advancement) {
        return RuntimeAdvancements.completed(player, advancement) ? 1 : 0;
    }

    static final class VehicleLogisticsLifecycle {
        private VehicleLogisticsSetup setup;
        private int ticks;
        private boolean crateOpened;

        void begin(EntityPlayerMP player) throws CommandException {
            setup = prepareVehicleLogisticsScenario(player);
            ticks = 0;
            crateOpened = false;
            if (!setup.crateOpened) finish(player, null);
        }

        @SubscribeEvent
        public void serverTick(TickEvent.WorldTickEvent event) {
            if (setup == null || event.phase != TickEvent.Phase.END
                    || event.world != setup.world || event.world.isRemote) return;
            ticks++;
            if (!crateOpened && ticks >= 10) {
                TileEntity tile = event.world.getTileEntity(setup.cratePos);
                if (tile instanceof TileEntityVehicleCrate) {
                    ((TileEntityVehicleCrate) tile).open(setup.playerId);
                    crateOpened = true;
                } else {
                    EntityPlayerMP player = event.world.getMinecraftServer().getPlayerList()
                        .getPlayerByUUID(setup.playerId);
                    if (player != null) finish(player, null);
                    return;
                }
            }
            if (!crateOpened) return;
            List<EntityMiniBus> vehicles = event.world.getEntitiesWithinAABB(EntityMiniBus.class,
                new AxisAlignedBB(setup.cratePos).grow(8));
            if (!vehicles.isEmpty()) {
                EntityPlayerMP player = event.world.getMinecraftServer().getPlayerList()
                    .getPlayerByUUID(setup.playerId);
                if (player != null) finish(player, vehicles.get(0));
            } else if (ticks >= 200) {
                EntityPlayerMP player = event.world.getMinecraftServer().getPlayerList()
                    .getPlayerByUUID(setup.playerId);
                if (player != null) finish(player, null);
            }
        }

        private void finish(EntityPlayerMP player, EntityMiniBus vehicle) {
            VehicleLogisticsSetup completed = setup;
            setup = null;
            emitVehicleResult(player, finishVehicleLogisticsScenario(player, completed, vehicle));
        }
    }

    private static final class VehicleLogisticsSetup {
        final UUID playerId;
        final World world;
        final List<BlockPos> cities;
        final BlockPos cratePos;
        final boolean workshop, manufactured, crateOpened;

        VehicleLogisticsSetup(UUID playerId, World world, List<BlockPos> cities,
                BlockPos cratePos, boolean workshop, boolean manufactured, boolean crateOpened) {
            this.playerId = playerId;
            this.world = world;
            this.cities = cities;
            this.cratePos = cratePos;
            this.workshop = workshop;
            this.manufactured = manufactured;
            this.crateOpened = crateOpened;
        }
    }

    private static boolean exerciseNationExchange(EntityPlayerMP player, List<BlockPos> cities) {
        if (cities.size() < 2) return false;
        List<TileIndustrialMachine> controllers = new ArrayList<>();
        for (BlockPos city : cities) {
            TileIndustrialMachine controller = null;
            for (TileEntity tile : player.world.loadedTileEntityList) {
                if (tile instanceof TileIndustrialMachine
                        && ((TileIndustrialMachine) tile).getKind()
                            == IndustrialMachineKind.CARGO_CONTROLLER
                        && ((TileIndustrialMachine) tile).isNationManagedForTest()
                        && tile.getPos().distanceSq(city) < 96D * 96D) {
                    controller = (TileIndustrialMachine) tile;
                    break;
                }
            }
            if (controller != null && !controllers.contains(controller)) controllers.add(controller);
        }
        if (controllers.size() < 2) {
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|NATION|cities={}|controllers={}|reason=missing_controllers",
                cities.size(), controllers.size());
            return false;
        }
        TileIndustrialMachine source = controllers.get(0);
        player.connection.setPlayerLocation(source.getPos().getX() + 2,
            source.getPos().getY(), source.getPos().getZ() + 2, 0F, 0F);
        int before = 0;
        for (TileIndustrialMachine controller : controllers)
            before += controller.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).getCount();
        boolean transferred = source.transferNationCargoForTest();
        int after = 0;
        for (TileIndustrialMachine controller : controllers)
            after += controller.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT).getCount();
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|NATION|cities={}|controllers={}|transferred={}|before={}|after={}|advancement={}",
            cities.size(), controllers.size(), transferred, before, after,
            RuntimeAdvancements.completed(player, "nation_trade_network"));
        return transferred && after == before + 1
            && RuntimeAdvancements.completed(player, "nation_trade_network");
    }

    private static final class VehicleLogisticsResult {
        final int cities, workshop, manufactured, deployed, mobility, storage, crafting,
            dockItem, dockFluid, nationTransfer, advancements;
        final boolean pass;

        VehicleLogisticsResult(int cities, int workshop, int manufactured, int deployed,
                int mobility, int storage, int crafting, int dockItem, int dockFluid,
                int nationTransfer, int advancements) {
            this.cities = cities;
            this.workshop = workshop;
            this.manufactured = manufactured;
            this.deployed = deployed;
            this.mobility = mobility;
            this.storage = storage;
            this.crafting = crafting;
            this.dockItem = dockItem;
            this.dockFluid = dockFluid;
            this.nationTransfer = nationTransfer;
            this.advancements = advancements;
            pass = cities >= 2 && workshop == 1 && manufactured == 1 && deployed == 1
                && mobility == 1 && storage == 1 && crafting == 1 && dockItem == 1
                && dockFluid == 1 && nationTransfer == 1 && advancements == 4;
        }
    }

    private static FactionSidePathResult runFactionSidePathScenario(EntityPlayerMP player)
            throws CommandException {
        Map<String, BlockPos> locations = CommandIndustrialLocateAll.locateAll(player, 8192, false);
        String[][] contacts = {
            {"primitive_settlement", "frontier_cooperative"},
            {"industrial_city", "civil_defense"},
            {"militia_outpost", "territorial_militia"},
            {"abandoned_factory", "ashline_raiders"}
        };
        int contactCount = 0;
        EntityVillager frontier = null;
        for (String[] contact : contacts) {
            BlockPos center = locations.get(contact[0]);
            EntityVillager villager = center == null ? null
                : findFactionVillager(player.world, center, contact[1]);
            if (villager == null) continue;
            FactionSystem.interactForTest(player, villager);
            if (FactionSystem.known(player, contact[1])) contactCount++;
            if ("frontier_cooperative".equals(contact[1])) frontier = villager;
        }

        // Each completed trade must occur on a different world day, exactly as in normal play.
        for (int trade = 0; trade < 9; trade++) {
            player.world.setTotalWorldTime(player.world.getTotalWorldTime() + 24000L);
            FactionSystem.recordCompletedTradeForTest(player, "frontier_cooperative", 1);
        }
        player.setSneaking(true);
        if (frontier != null) FactionSystem.interactForTest(player, frontier);
        player.setSneaking(false);
        boolean membership = "frontier_cooperative".equals(FactionSystem.membership(player));

        for (int trade = 0; trade < 5; trade++) {
            player.world.setTotalWorldTime(player.world.getTotalWorldTime() + 24000L);
            FactionSystem.recordCompletedTradeForTest(player, "frontier_cooperative", 1);
        }
        player.inventory.currentItem = 0;
        player.inventory.setInventorySlotContents(0,
            new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT, 8));
        player.setSneaking(true);
        if (frontier != null) FactionSystem.interactForTest(player, frontier);
        player.setSneaking(false);
        boolean companion = frontier != null
            && frontier.getEntityData().getBoolean("IndustrialCompanion")
            && frontier.getEntityData().hasUniqueId("IndustrialCompanionOwner")
            && player.getUniqueID().equals(
                frontier.getEntityData().getUniqueId("IndustrialCompanionOwner"));
        boolean follow = false;
        boolean persistence = false;
        if (companion) {
            frontier.setPosition(player.posX + 40, player.posY, player.posZ + 40);
            FactionSystem.updateCompanionForTest(frontier, player);
            follow = frontier.getDistanceSq(player) < 9;
            NBTTagCompound persisted = new NBTTagCompound();
            frontier.writeToNBT(persisted);
            NBTTagCompound forgeData = persisted.getCompoundTag("ForgeData");
            persistence = forgeData.getBoolean("IndustrialCompanion")
                && forgeData.hasUniqueId("IndustrialCompanionOwner")
                && player.getUniqueID().equals(forgeData.getUniqueId("IndustrialCompanionOwner"));
        }

        int outpost = dismantleLocatedOutpost(player, locations.get("militia_outpost")) ? 1 : 0;
        int factoryStages = exerciseFactoryTerminal(player, locations.get("abandoned_factory"));
        String[] milestones = {"faction_contacts", "civil_defense_contact",
            "territorial_militia_contact", "militia_outpost_takedown", "faction_membership",
            "faction_companion"};
        int advancements = 0;
        for (String milestone : milestones) {
            if (RuntimeAdvancements.completed(player, milestone)) advancements++;
        }
        return new FactionSidePathResult(locations.containsKey("primitive_settlement")
            && locations.containsKey("industrial_city") && locations.containsKey("militia_outpost")
            && locations.containsKey("abandoned_factory") ? 4 : 0,
            contactCount >= 3 ? 3 : contactCount, factoryStages, outpost, membership ? 1 : 0,
            companion ? 1 : 0, follow ? 1 : 0, persistence ? 1 : 0, advancements);
    }

    private static void runFactionGameplayScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos origin = world.getHeight(player.getPosition().add(20, 0, 20));
        player.connection.setPlayerLocation(origin.getX() + 0.5D, origin.getY() + 1D,
            origin.getZ() + 0.5D, 0F, 0F);
        SettlementEconomySystem.register(world, origin, "primitive", "food", 0);
        EntityVillager frontier = FactionSystem.spawnCitizen(world, origin.getX() + 2.5D,
            origin.getY(), origin.getZ() + 0.5D, "frontier_cooperative", "merchant", "food",
            "Acceptance Trader");

        boolean recipeTrade = true;
        String tradeFailure = "none";
        for (int trade = 0; trade < 14; trade++) {
            world.setTotalWorldTime((trade + 1L) * 24000L);
            EntityVillager tradeMerchant = trade == 0 ? frontier : FactionSystem.spawnCitizen(world,
                origin.getX() + 2.5D + trade, origin.getY(), origin.getZ() + 0.5D,
                "frontier_cooperative", "merchant", "food", "Acceptance Trader " + trade);
            player.inventory.addItemStackToInventory(new ItemStack(Items.WHEAT, 12));
            player.setSneaking(false);
            FactionSystem.interactForTest(player, tradeMerchant);
            net.minecraft.village.MerchantRecipe selected = null;
            for (net.minecraft.village.MerchantRecipe recipe : tradeMerchant.getRecipes(player))
                if (recipe.getItemToBuy().getItem() == Items.WHEAT
                        && recipe.getItemToSell().getItem()
                            == IndustrialCivilizationCore.INDUSTRIAL_CREDIT) {
                    selected = recipe;
                    break;
                }
            if (selected == null) {
                tradeFailure = "missing_wheat_offer_" + tradeMerchant.getRecipes(player).size();
                recipeTrade = false;
                break;
            }
            if (!consumeInventory(player, selected.getItemToBuy())) {
                tradeFailure = "payment_" + selected.getItemToBuy().getCount() + "_available_"
                    + countInventory(player, selected.getItemToBuy());
                recipeTrade = false;
                break;
            }
            player.inventory.addItemStackToInventory(selected.getItemToSell().copy());
            selected.incrementToolUses();
            FactionSystem.completePendingTradeForTest(player);
            if (trade == 8) {
                player.setSneaking(true);
                FactionSystem.interactForTest(player, frontier);
                player.setSneaking(false);
            }
        }
        boolean membership = "frontier_cooperative".equals(FactionSystem.membership(player));
        player.inventory.currentItem = 0;
        player.inventory.setInventorySlotContents(0,
            new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT, 8));
        boolean wasCreative = player.capabilities.isCreativeMode;
        player.capabilities.isCreativeMode = false;
        player.setSneaking(true);
        FactionSystem.interactForTest(player, frontier);
        player.setSneaking(false);
        player.capabilities.isCreativeMode = wasCreative;
        boolean companionCost = player.inventory.getStackInSlot(0).isEmpty();
        boolean companion = frontier.getEntityData().getBoolean("IndustrialCompanion")
            && frontier.getEntityData().hasUniqueId("IndustrialCompanionOwner");
        frontier.setPosition(player.posX + 40, player.posY, player.posZ + 40);
        FactionSystem.updateCompanionForTest(frontier, player);
        boolean follow = frontier.getDistanceSq(player) < 9;
        NBTTagCompound companionNbt = new NBTTagCompound();
        frontier.writeToNBT(companionNbt);
        boolean companionPersistence = companionNbt.getCompoundTag("ForgeData")
            .getBoolean("IndustrialCompanion");

        FakePlayer second = FakePlayerFactory.get((WorldServer) world,
            new GameProfile(UUID.fromString("22222222-2222-4222-8222-222222222222"),
                "IsolationPlayer"));
        boolean isolated = FactionSystem.membership(second).isEmpty()
            && FactionSystem.reputation(second, "frontier_cooperative") == 10
            && ProgressionState.counter(second, "faction_trade_contacts") == 0;

        EntityVillager militia = FactionSystem.spawnCitizen(world, origin.getX() + 5.5D,
            origin.getY(), origin.getZ(), "territorial_militia", "militia", "armaments",
            "Acceptance Militia");
        boolean unarmedNeutral = !FactionSystem.isHostileTo(player, "territorial_militia");
        net.minecraft.item.Item pistol = ForgeRegistries.ITEMS.getValue(
            new ResourceLocation("techguns", "pistol"));
        if (pistol != null) player.inventory.setInventorySlotContents(5, new ItemStack(pistol));
        boolean armedHostile = pistol != null && FactionSystem.isHostileTo(player,
            "territorial_militia");
        player.inventory.setInventorySlotContents(5, ItemStack.EMPTY);
        int civilBefore = FactionSystem.reputation(player, "civil_defense");
        FactionSystem.attacked(new net.minecraftforge.event.entity.living.LivingAttackEvent(
            militia, net.minecraft.util.DamageSource.causePlayerDamage(player), 1F));
        boolean militiaIndependent = FactionSystem.reputation(player, "civil_defense") == civilBefore;

        EntityVillager civilian = FactionSystem.spawnCitizen(world, origin.getX() + 7.5D,
            origin.getY(), origin.getZ(), "riverside_works", "merchant", "steel",
            "Acceptance Civilian");
        long harmBefore = ProgressionState.counter(player, "faction_civilian_harm");
        FactionSystem.died(new net.minecraftforge.event.entity.living.LivingDeathEvent(
            civilian, net.minecraft.util.DamageSource.CACTUS));
        boolean environmentSafe = ProgressionState.counter(player,
            "faction_civilian_harm") == harmBefore;
        FactionSystem.attacked(new net.minecraftforge.event.entity.living.LivingAttackEvent(
            civilian, net.minecraft.util.DamageSource.causePlayerDamage(player), 1F));
        FactionSystem.attacked(new net.minecraftforge.event.entity.living.LivingAttackEvent(
            civilian, net.minecraft.util.DamageSource.causePlayerDamage(player), 1F));
        FactionSystem.attacked(new net.minecraftforge.event.entity.living.LivingAttackEvent(
            civilian, net.minecraft.util.DamageSource.causePlayerDamage(player), 1F));
        boolean civilNetworkHostile = FactionSystem.isHostileTo(player, "civil_defense");

        for (int outpost = 0; outpost < 3; outpost++) {
            BlockPos site = origin.add(20 + outpost * 40, 0, 0);
            MilitiaOutpostRegistry.record(world, site);
            for (int broken = 0; broken < 16; broken++) {
                BlockPos pos = site.add(broken, 1, 0);
                FactionSystem.blockBroken(new BlockEvent.BreakEvent(world, pos,
                    world.getBlockState(pos), player));
            }
        }
        boolean outpostHostility = ProgressionState.counter(player,
            "militia_outposts_taken_down") == 3
            && FactionSystem.isHostileTo(player, "territorial_militia");
        boolean playerPersistence = ProgressionState.data(player)
            .getString("faction_membership_id").equals("frontier_cooperative")
            && ProgressionState.data(player).hasUniqueId("faction_companion");
        boolean pass = recipeTrade && membership && companionCost && companion && follow
            && companionPersistence && isolated && unarmedNeutral && armedHostile
            && militiaIndependent && environmentSafe && civilNetworkHostile
            && outpostHostility && playerPersistence;
        emit(player, (pass ? "PASS" : "FAIL") + "|faction_gameplay_path|real_recipe_trade="
            + (recipeTrade ? 1 : 0) + "|membership=" + (membership ? 1 : 0)
            + "|trade_failure=" + tradeFailure
            + "|companion_cost=" + (companionCost ? 1 : 0) + "|follow=" + (follow ? 1 : 0)
            + "|companion_persistence=" + (companionPersistence ? 1 : 0)
            + "|multiplayer_isolation=" + (isolated ? 1 : 0) + "|unarmed_neutral="
            + (unarmedNeutral ? 1 : 0) + "|armed_hostile=" + (armedHostile ? 1 : 0)
            + "|militia_independent=" + (militiaIndependent ? 1 : 0)
            + "|environment_safe=" + (environmentSafe ? 1 : 0)
            + "|civil_network_hostile=" + (civilNetworkHostile ? 1 : 0)
            + "|three_outposts=" + (outpostHostility ? 1 : 0)
            + "|player_persistence=" + (playerPersistence ? 1 : 0));
    }

    private static void runFactionPersistenceCheck(EntityPlayerMP player) {
        NBTTagCompound data = ProgressionState.data(player);
        boolean membership = "frontier_cooperative".equals(FactionSystem.membership(player));
        boolean companionId = data.hasUniqueId("faction_companion");
        boolean companionEntity = false;
        for (Entity entity : player.world.loadedEntityList) {
            if (!(entity instanceof EntityVillager)) continue;
            NBTTagCompound tag = entity.getEntityData();
            if (tag.getBoolean("IndustrialCompanion")
                    && tag.hasUniqueId("IndustrialCompanionOwner")
                    && player.getUniqueID().equals(tag.getUniqueId("IndustrialCompanionOwner"))) {
                companionEntity = true;
                break;
            }
        }
        boolean trades = ProgressionState.counter(player, "faction_trade_contacts") >= 4;
        boolean outposts = ProgressionState.counter(player, "militia_outposts_taken_down") == 3;
        boolean pass = membership && companionId && companionEntity && trades && outposts;
        emit(player, (pass ? "PASS" : "FAIL") + "|faction_persistence_check|membership="
            + (membership ? 1 : 0) + "|companion_id=" + (companionId ? 1 : 0)
            + "|companion_entity=" + (companionEntity ? 1 : 0) + "|trade_contacts="
            + ProgressionState.counter(player, "faction_trade_contacts") + "|outposts="
            + ProgressionState.counter(player, "militia_outposts_taken_down"));
    }

    private static void runQuestPersistenceCheck(EntityPlayerMP player) {
        String[] milestones = {"automated_field_agriculture", "automated_animal_husbandry",
            "automated_animal_resources", "automated_water_resources"};
        int completed = 0;
        for (String milestone : milestones)
            if (RuntimeAdvancements.completed(player, milestone)) completed++;
        flushQuestProgressForTest(player);
        boolean pass = completed == milestones.length;
        emit(player, (pass ? "PASS" : "FAIL")
            + "|quest_persistence_check|advancements=" + completed + "/" + milestones.length);
    }

    private static void flushQuestProgressForTest(EntityPlayerMP player) {
        for (betterquesting.api2.storage.DBEntry<betterquesting.api.questing.IQuest> entry
                : betterquesting.questing.QuestDatabase.INSTANCE.getEntries()) {
            entry.getValue().detect(player);
        }
        betterquesting.handlers.SaveLoadHandler.INSTANCE.markDirty();
        betterquesting.handlers.SaveLoadHandler.INSTANCE.saveDatabases();
    }

    private static boolean consumeInventory(EntityPlayerMP player, ItemStack required) {
        int available = countInventory(player, required);
        if (available < required.getCount()) return false;
        int remaining = required.getCount();
        for (int slot = 0; slot < player.inventory.mainInventory.size() && remaining > 0; slot++) {
            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (stack.isEmpty() || stack.getItem() != required.getItem()
                    || (required.getMetadata() != 32767
                        && stack.getMetadata() != required.getMetadata())) continue;
            int moved = Math.min(remaining, stack.getCount());
            stack.shrink(moved);
            remaining -= moved;
        }
        player.inventory.markDirty();
        return remaining == 0;
    }

    private static int countInventory(EntityPlayerMP player, ItemStack required) {
        int available = 0;
        for (ItemStack stack : player.inventory.mainInventory)
            if (!stack.isEmpty() && stack.getItem() == required.getItem()
                    && (required.getMetadata() == 32767
                        || stack.getMetadata() == required.getMetadata())) available += stack.getCount();
        return available;
    }

    private static EntityVillager findFactionVillager(World world, BlockPos center, String faction) {
        List<EntityVillager> villagers = world.getEntitiesWithinAABB(EntityVillager.class,
            new AxisAlignedBB(center).grow(64, 32, 64), entity ->
                faction.equals(entity.getEntityData().getString("IndustrialFaction")));
        EntityVillager nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityVillager villager : villagers) {
            double distance = villager.getDistanceSq(center);
            if (distance < nearestDistance) {
                nearest = villager;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean dismantleLocatedOutpost(EntityPlayerMP player, BlockPos center) {
        if (center == null) return false;
        BlockPos registered = null;
        for (int y = center.getY(); y >= center.getY() - 24 && registered == null; y--) {
            BlockPos candidate = new BlockPos(center.getX() - 15, y, center.getZ() - 15);
            if (MilitiaOutpostRegistry.nearby(player.world, candidate, 3) != null) registered = candidate;
        }
        if (registered == null) return false;
        for (int index = 0; index < 16; index++) {
            BlockPos pos = registered.add(index, 1, 0);
            FactionSystem.blockBroken(new BlockEvent.BreakEvent(player.world, pos,
                player.world.getBlockState(pos), player));
            player.world.setBlockToAir(pos);
        }
        return RuntimeAdvancements.completed(player, "militia_outpost_takedown");
    }

    private static int exerciseFactoryTerminal(EntityPlayerMP player, BlockPos center) {
        if (center == null) return 0;
        TileFactoryControlTerminal terminal = null;
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (tile instanceof TileFactoryControlTerminal
                    && tile.getPos().distanceSq(center) < 64 * 64) {
                terminal = (TileFactoryControlTerminal) tile;
                break;
            }
        }
        if (terminal == null) return 0;
        terminal.interact(player);
        int stages = hasItem(player, IndustrialCivilizationCore.UNDERWORLD_DOSSIER) ? 1 : 0;
        for (EntityVindicator criminal : player.world.getEntitiesWithinAABB(EntityVindicator.class,
                new AxisAlignedBB(terminal.getPos()).grow(18), entity ->
                    entity.getEntityData().getBoolean("IndustrialCriminal"))) {
            criminal.setDead();
            player.world.removeEntity(criminal);
        }
        terminal.interact(player);
        if (hasItem(player, IndustrialCivilizationCore.CRIMINAL_NETWORK_LEDGER)) stages++;
        player.inventory.addItemStackToInventory(new ItemStack(Items.IRON_INGOT, 16));
        player.inventory.addItemStackToInventory(new ItemStack(Items.REDSTONE, 8));
        terminal.interact(player);
        if (hasItem(player, IndustrialCivilizationCore.FACTORY_RESTORATION_CERTIFICATE)) stages++;
        player.inventory.addItemStackToInventory(
            new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR));
        terminal.interact(player);
        if (hasItem(player, IndustrialCivilizationCore.RECOVERED_FACTORY_CONTROL_SYSTEM)) stages++;
        return stages;
    }

    private static boolean hasItem(EntityPlayerMP player, net.minecraft.item.Item item) {
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == item) return true;
        }
        return false;
    }

    private static final class FactionSidePathResult {
        final int locations, contacts, factoryStages, outpost, membership, companion, follow,
            persistence, advancements;
        final boolean pass;

        FactionSidePathResult(int locations, int contacts, int factoryStages, int outpost,
                int membership, int companion, int follow, int persistence, int advancements) {
            this.locations = locations;
            this.contacts = contacts;
            this.factoryStages = factoryStages;
            this.outpost = outpost;
            this.membership = membership;
            this.companion = companion;
            this.follow = follow;
            this.persistence = persistence;
            this.advancements = advancements;
            pass = locations == 4 && contacts == 3 && factoryStages == 4 && outpost == 1
                && membership == 1 && companion == 1 && follow == 1 && persistence == 1
                && advancements == 6;
        }
    }

    private static int[] inspectTeleportGate() {
        int legacyAssembly = 0;
        for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
            for (ItemStack output : recipe.getOutputPreviews()) {
                if (stackIs(output, "additionalpipes:pipe_items_teleport")) legacyAssembly++;
            }
        }
        IRecipe gated = findRecipe("ai_gated_item_teleport_pipe",
            "additionalpipes:pipe_items_teleport");
        int aiRecipe = gated == null ? 0 : 1;
        net.minecraft.item.Item diamondPipe = ForgeRegistries.ITEMS.getValue(
            new ResourceLocation("buildcrafttransport", "pipe_diamond_item"));
        int aiIngredients = gated != null
            && recipeAccepts(gated, new ItemStack(IndustrialCivilizationCore.AI_CORE))
            && recipeAccepts(gated, new ItemStack(Items.ENDER_PEARL))
            && diamondPipe != null && recipeAccepts(gated, new ItemStack(diamondPipe)) ? 1 : 0;
        int ic2Native = 0;
        for (String id : Arrays.asList(
                "ic2:shaped_tile.blockteleporter_1949721530",
                "ic2:shaped_tile.blockteleporterhub_-665861465",
                "ic2:shaped_item.itemportableteleporter_-869928001")) {
            if (ForgeRegistries.RECIPES.containsKey(new ResourceLocation(id))) ic2Native++;
        }
        int tether = ForgeRegistries.RECIPES.containsKey(
            new ResourceLocation("additionalpipes", "teleport_tether")) ? 1 : 0;
        int phasePearlSources = 0;
        int phasePearlAi = 0;
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (stackIs(recipe.getRecipeOutput(), "minecraft:ender_pearl")) {
                phasePearlSources++;
                if (recipeAccepts(recipe, new ItemStack(IndustrialCivilizationCore.AI_CORE))) {
                    phasePearlAi++;
                }
            }
        }
        return new int[] {legacyAssembly, aiRecipe, aiIngredients, ic2Native, tether,
            phasePearlSources, phasePearlAi};
    }

    private static boolean recipeAccepts(IRecipe recipe, ItemStack stack) {
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.apply(stack)) return true;
        }
        return false;
    }

    private static boolean stackIs(ItemStack stack, String id) {
        return !stack.isEmpty() && stack.getItem().getRegistryName() != null
            && id.equals(stack.getItem().getRegistryName().toString());
    }

    private static String stackId(ItemStack stack) {
        return stack.isEmpty() || stack.getItem().getRegistryName() == null
            ? "empty" : stack.getItem().getRegistryName().toString();
    }

    private static String token(String value) {
        return value == null || value.isEmpty() ? "none" : value.replace(' ', '_');
    }

    private static int[] runRobberWallTheftScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos base = player.getPosition().add(0, 2, 10);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(base.add(-1, -1, -1), base.add(3, 3, 1))) {
            world.setBlockToAir(pos);
        }
        BlockPos chestPos = base.add(2, 0, 0);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 3);
        IInventory chest = (IInventory) world.getTileEntity(chestPos);
        chest.setInventorySlotContents(0, new ItemStack(net.minecraft.init.Items.IRON_INGOT, 9));
        world.setBlockState(base.add(1, 0, 0), Blocks.STONE.getDefaultState(), 3);
        world.setBlockState(base.add(1, 1, 0), Blocks.STONE.getDefaultState(), 3);
        EntityRobber robber = new EntityRobber(world);
        robber.setPosition(base.getX() + 0.5D, base.getY(), base.getZ() + 0.5D);
        PlanetaryEcologySystem.stealForTest(robber);
        int blockedRemaining = chest.getStackInSlot(0).getCount();
        world.setBlockToAir(base.add(1, 0, 0));
        world.setBlockToAir(base.add(1, 1, 0));
        PlanetaryEcologySystem.stealForTest(robber);
        int openRemaining = chest.getStackInSlot(0).getCount();
        world.setBlockToAir(chestPos);
        return new int[] {blockedRemaining, openRemaining};
    }

    private static RecipeResult runReleaseRecipeScenario(EntityPlayerMP player) {
        String[][] explicit = {
            {"lv_plant_sower", "industrialforegoing:crop_sower", "1"},
            {"lv_plant_gatherer", "industrialforegoing:crop_recolector", "1"},
            {"lv_resourceful_furnace", "industrialforegoing:resourceful_furnace", "1"},
            {"lv_plant_interactor", "industrialforegoing:plant_interactor", "1"},
            {"lv_plant_fertilizer", "industrialforegoing:crop_enrich_material_injector", "1"},
            {"lv_animal_breeder", "industrialforegoing:animal_stock_increaser", "1"},
            {"lv_animal_growth", "industrialforegoing:animal_growth_increaser", "1"},
            {"lv_animal_separator", "industrialforegoing:animal_independence_selector", "1"},
            {"lv_animal_harvester", "industrialforegoing:animal_resource_harvester", "1"},
            {"lv_sewage_collector", "industrialforegoing:animal_byproduct_recolector", "1"},
            {"lv_sewage_composter", "industrialforegoing:sewage_composter_solidifier", "1"},
            {"lv_water_resource_collector", "industrialforegoing:water_resources_collector", "1"},
            {"industrial_civilization_gunpowder", "minecraft:gunpowder", "3"},
            {"colorful_lamp", "computronics:colorful_lamp", "1"},
            {"quantum_tape_diamond", "computronics:tape", "1"},
            {"quantum_tape_dense", "computronics:tape", "1"},
            {"earth_purpur_stairs", "minecraft:purpur_stairs", "4"},
            {"earth_purpur_slab", "minecraft:purpur_slab", "6"},
            {"earth_nether_brick", "minecraft:nether_brick", "1"},
            {"earth_glowstone_torch", "galacticraftcore:glowstone_torch", "4"},
            {"earth_mirrorprint", "chiselsandbits:mirrorprint", "1"},
            {"earth_neon_light", "techguns:neonlights", "1"},
            {"earth_purpur_wall", "quark:purpur_block_wall", "1"},
            {"earth_soul_sandstone", "quark:soul_sandstone", "1"},
            {"earth_antiblock", "chisel:antiblock", "1"}
        };
        RecipeResult result = new RecipeResult(explicit.length);
        for (String[] target : explicit) {
            IRecipe recipe = findRecipe(target[0], target[1]);
            if (recipe == null) {
                result.failures.add(target[0] + ":missing");
                continue;
            }
            String failure = craftFailure(recipe, player.world, target[1], Integer.parseInt(target[2]));
            if (failure == null) result.explicitPassed++;
            else result.failures.add(target[0] + ":" + failure);
        }
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            ResourceLocation name = recipe.getRegistryName();
            if (name == null || !"crafttweaker".equals(name.getResourceDomain())
                    || !name.getResourcePath().endsWith("_modified")) continue;
            if (isSupersededModifiedRecipe(name.toString())) continue;
            ItemStack expected = recipe.getRecipeOutput();
            String expectedId = expected.isEmpty() || expected.getItem().getRegistryName() == null
                ? "" : expected.getItem().getRegistryName().toString();
            result.modifiedTotal++;
            String failure = craftFailure(recipe, player.world, expectedId, expected.getCount());
            if (failure == null) result.modifiedPassed++;
            else if (result.failures.size() < 12) result.failures.add(name + ":" + failure);
        }
        result.forbiddenInputs = forbiddenInputCount();
        if (result.forbiddenInputs != 0) result.failures.add("forbidden_inputs:" + result.forbiddenInputs);
        return result;
    }

    private static boolean isSupersededModifiedRecipe(String recipeId) {
        return Arrays.asList(
            "crafttweaker:minecraft_purpur_stairs_modified",
            "crafttweaker:minecraft_purpur_slab_modified",
            "crafttweaker:minecraft_nether_brick_modified",
            "crafttweaker:galacticraftcore_glowstone_torch_modified",
            "crafttweaker:chiselsandbits_mirrorprint_modified",
            "crafttweaker:icbmclassic_parts/circuit.elite_modified",
            "crafttweaker:techguns_neonlights_0_modified",
            "crafttweaker:quark_purpur_block_wall_modified",
            "crafttweaker:quark_soul_sandstone_modified",
            "crafttweaker:chisel_antiblock_modified"
        ).contains(recipeId);
    }

    private static IRecipe findRecipe(String token, String outputId) {
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            ResourceLocation name = recipe.getRegistryName();
            ItemStack output = recipe.getRecipeOutput();
            ResourceLocation outputName = output.isEmpty() ? null : output.getItem().getRegistryName();
            if (name != null && name.toString().contains(token)
                    && outputName != null && outputId.equals(outputName.toString())) return recipe;
        }
        return null;
    }

    private static String craftFailure(IRecipe recipe, World world, String expectedId, int expectedCount) {
        final Container container = new Container() {
            @Override public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) { return true; }
        };
        InventoryCrafting grid = new InventoryCrafting(container, 3, 3);
        List<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : 3;
        if (width < 1 || width > 3 || ingredients.size() > 9) return "unsupported_grid";
        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient ingredient = ingredients.get(index);
            if (ingredient == Ingredient.EMPTY) continue;
            ItemStack[] candidates = ingredient.getMatchingStacks();
            if (candidates.length == 0) return "empty_ingredient_" + index;
            int slot = recipe instanceof IShapedRecipe
                ? (index / width) * 3 + index % width : index;
            grid.setInventorySlotContents(slot, candidates[0].copy());
        }
        IRecipe matched = CraftingManager.findMatchingRecipe(grid, world);
        if (matched == null) return "no_match";
        ItemStack output = matched.getCraftingResult(grid);
        ResourceLocation outputName = output.isEmpty() ? null : output.getItem().getRegistryName();
        if (outputName == null || !expectedId.equals(outputName.toString()))
            return "wrong_output_" + (outputName == null ? "empty" : outputName);
        if (output.getCount() != expectedCount) return "wrong_count_" + output.getCount();
        return null;
    }

    private static int forbiddenInputCount() {
        List<String> forbidden = Arrays.asList(
            "minecraft:slime_ball", "minecraft:ghast_tear", "minecraft:blaze_rod",
            "minecraft:blaze_powder", "minecraft:magma_cream", "minecraft:nether_star",
            "minecraft:quartz", "minecraft:netherrack", "minecraft:soul_sand",
            "minecraft:nether_wart", "minecraft:netherbrick", "minecraft:glowstone_dust",
            "minecraft:end_stone", "minecraft:chorus_fruit", "minecraft:chorus_fruit_popped",
            "minecraft:purpur_block", "minecraft:purpur_pillar", "minecraft:shulker_shell",
            "minecraft:dragon_breath", "minecraft:dragon_egg", "minecraft:end_crystal");
        int count = 0;
        for (IRecipe recipe : ForgeRegistries.RECIPES) for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack candidate : ingredient.getMatchingStacks()) {
                ResourceLocation name = candidate.getItem().getRegistryName();
                if (name != null && forbidden.contains(name.toString())) count++;
            }
        }
        return count;
    }

    private static final class RecipeResult {
        final int explicitTotal;
        final List<String> failures = new ArrayList<>();
        int explicitPassed;
        int modifiedTotal;
        int modifiedPassed;
        int forbiddenInputs;
        RecipeResult(int explicitTotal) { this.explicitTotal = explicitTotal; }
    }

    private static EcologyResult runEarthEcologyScenario(MinecraftServer server, EntityPlayerMP player) {
        // Test the Earth-only replacement rules in the Overworld even when a
        // seeded test-bed player was last saved on the Moon or Mars.
        World world = server.getWorld(0);
        BlockPos anchor = player.dimension == 0 ? player.getPosition() : world.getSpawnPoint();
        BlockPos base = anchor.add(0, 2, 10);
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
        root.addProperty("dimension_type", player.world.provider.getDimensionType().getName());
        root.addProperty("position", coordinates(player.getPosition()));
        root.addProperty("radius", radius);
        root.addProperty("habitat_detector_active", SpaceSurvivalSystem.protectedByHabitat(player));
        root.addProperty("functional_stable_samples",
            ProgressionState.counter(player, environment(player) + "_functional_stable_samples"));
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

    private static String environment(EntityPlayerMP player) {
        String name = player.world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("moon")) return "lunar";
        if (name.contains("mars")) return "martian";
        if (name.contains("orbit") || name.contains("space station")) return "orbit";
        return "earth";
    }

    private static void emit(EntityPlayerMP player, String value) {
        String line = PREFIX + value;
        IndustrialCivilizationCore.LOGGER.info(line);
        player.sendMessage(new TextComponentString(line));
    }

    private static void emitDetailed(EntityPlayerMP player, String loggedValue, String chatValue) {
        IndustrialCivilizationCore.LOGGER.info(PREFIX + loggedValue);
        player.sendMessage(new TextComponentString(PREFIX + chatValue));
    }

    @Override
    public java.util.List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "snapshot", "scenario", "assert");
        if (args.length == 2 && ("scenario".equals(args[0]) || "assert".equals(args[0])))
            return getListOfStringsMatchingLastWord(args, "workshop_adjacency", "earth_ecology",
                "release_recipes", "robber_wall_theft", "mobile_quarry_relocation",
                "teleport_gate", "faction_side_path", "faction_gameplay_path",
                "faction_persistence_check", "vehicle_logistics_path",
                "strategic_defense_path", "agricultural_side_path", "automated_agriculture_path",
                "quest_persistence_check", "settlement_economy_path", "civilization_systems");
        return Arrays.asList();
    }
}
