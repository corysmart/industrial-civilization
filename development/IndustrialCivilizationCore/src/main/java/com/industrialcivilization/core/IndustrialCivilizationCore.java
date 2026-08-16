package com.industrialcivilization.core;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.gui2.GuiQuestLines;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.advancements.GuiAdvancementTab;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = IndustrialCivilizationCore.MODID,
    name = IndustrialCivilizationCore.NAME,
    version = IndustrialCivilizationCore.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:forge@[14.23.5.2860,);required-after:betterquesting;required-after:computercraft;required-after:galacticraftcore;required-after:galacticraftplanets;required-after:vehicle"
)
public final class IndustrialCivilizationCore {
    public static final String MODID = "industrialcivilizationcore";
    public static final String NAME = "Industrial Civilization Core";
    public static final String VERSION = "0.5.1";
    /** Canonical pack conversion, matching IC2 Classic's RFPerEU setting. */
    public static final int FE_PER_EU = 8;
    public static final int GUI_INDUSTRIAL_MACHINE = 1;
    public static final int GUI_VEHICLE_STORAGE = 2;
    public static final int GUI_VEHICLE_CRAFTING = 3;
    public static boolean ENFORCE_SPACE_GATES = true;
    public static boolean NATIVE_IC2_POWER_SCALING = true;
    public static boolean ALLOW_MULTI_PACKET_THROUGHPUT = true;
    public static boolean TEST_BRIDGE_ENABLED = false;
    public static String E2E_AUTO_SCENARIO = "";
    public static int ROBBER_SPAWN_PERCENT = 25;
    public static int ROBBER_LOCAL_CAP = 4;
    public static int MILITIA_PATROL_RADIUS = 128;
    public static int MILITIA_PATROL_LOCAL_CAP = 6;
    @Mod.Instance(MODID)
    public static IndustrialCivilizationCore INSTANCE;
    public static final String QUEST_HOME_IMAGE = MODID + ":textures/gui/quest_home_v2.png";
    public static final float QUEST_HOME_ANCHOR_Y = 0.5F;
    public static final int QUEST_HOME_OFFSET_X = -128;
    public static final int QUEST_HOME_OFFSET_Y = -64;
    public static Logger LOGGER;
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(MOLECULAR_ANALYZER);
        }
    };

    public static final Block MOLECULAR_ANALYZER = new BlockMolecularAnalyzer()
        .setRegistryName(MODID, "molecular_analyzer")
        .setUnlocalizedName(MODID + ".molecular_analyzer")
        .setCreativeTab(CREATIVE_TAB)
        .setHardness(5.0F)
        .setResistance(15.0F);
    public static final Item MATERIAL_PATTERN_RECORD = new ItemPatternRecord()
        .setRegistryName(MODID, "material_pattern_record")
        .setUnlocalizedName(MODID + ".material_pattern_record")
        .setCreativeTab(CREATIVE_TAB)
        .setMaxStackSize(1);
    public static final ItemIndustrialCredit INDUSTRIAL_CREDIT = new ItemIndustrialCredit();
    public static final BlockIndustrialMachine RESEARCH_STATION = machine(IndustrialMachineKind.RESEARCH_STATION);
    public static final BlockIndustrialMachine ORBITAL_EXPERIMENT_MODULE = machine(IndustrialMachineKind.EXPERIMENT_MODULE);
    public static final BlockIndustrialMachine ELECTRIC_FABRICATOR = machine(IndustrialMachineKind.ELECTRIC_FABRICATOR);
    public static final BlockIndustrialMachine PROGRAMMABLE_ASSEMBLER = machine(IndustrialMachineKind.PROGRAMMABLE_ASSEMBLER);
    public static final BlockIndustrialMachine CAR_WORKSHOP = machine(IndustrialMachineKind.CAR_WORKSHOP);
    public static final BlockIndustrialMachine GUN_FACTORY = machine(IndustrialMachineKind.GUN_FACTORY);
    public static final BlockIndustrialMachine ROBOTIC_MANUFACTURING_CELL = machine(IndustrialMachineKind.ROBOTIC_CELL);
    public static final BlockIndustrialMachine MATTER_REPLICATOR = machine(IndustrialMachineKind.MATTER_REPLICATOR);
    public static final BlockIndustrialMachine FUSION_RESEARCH_CORE = machine(IndustrialMachineKind.FUSION_RESEARCH_CORE);
    public static final BlockIndustrialMachine INTERPLANETARY_CARGO_CONTROLLER = machine(IndustrialMachineKind.CARGO_CONTROLLER);
    public static final BlockIndustrialMachine ORBITAL_MEGASTRUCTURE_CONTROLLER = machine(IndustrialMachineKind.MEGASTRUCTURE_CONTROLLER);
    public static final BlockIndustrialMachine AUTONOMOUS_COLONY_BEACON = machine(IndustrialMachineKind.COLONY_BEACON);
    public static final BlockFactoryControlTerminal FACTORY_CONTROL_TERMINAL = new BlockFactoryControlTerminal();
    public static final BlockEnvironmentalSolarArray ENVIRONMENTAL_SOLAR_ARRAY =
        new BlockEnvironmentalSolarArray("environmental_solar_array", false);
    public static final BlockEnvironmentalSolarArray TRACKING_SOLAR_ARRAY =
        new BlockEnvironmentalSolarArray("tracking_solar_array", true);
    public static final BlockRepairBench REPAIR_BENCH = new BlockRepairBench();
    public static final BlockVehicleServiceDock VEHICLE_SERVICE_DOCK = new BlockVehicleServiceDock();
    public static final BlockWorkshopComponent STEEL_FRAME = component("steel_frame");
    public static final BlockWorkshopComponent STEEL_CASING = component("steel_casing");
    public static final BlockWorkshopComponent MACHINE_CASING = component("machine_casing");
    public static final BlockWorkshopComponent REINFORCED_PLATE = component("reinforced_plate");
    public static final BlockWorkshopComponent GRATED_PLATE = component("grated_plate");
    public static final BlockWorkshopComponent INDUSTRIAL_FLOOR = component("industrial_floor");
    public static final BlockWorkshopComponent HAZARD_STRIPE = component("hazard_stripe");
    public static final BlockWorkshopComponent CABLE_BLOCK = component("workshop_cable_block");
    public static final BlockWorkshopComponent CABLE_COVER = component("workshop_cable_cover");
    public static final BlockWorkshopComponent GLASS_PANEL = component("reinforced_glass_panel");
    public static final BlockWorkshopComponent TOOL_WALL = component("tool_wall");
    public static final BlockWorkshopComponent DRAWER_CABINET = component("drawer_cabinet");
    public static final BlockWorkshopComponent[] WORKSHOP_COMPONENTS = {
        STEEL_FRAME, STEEL_CASING, MACHINE_CASING, REINFORCED_PLATE,
        GRATED_PLATE, INDUSTRIAL_FLOOR, HAZARD_STRIPE, CABLE_BLOCK,
        CABLE_COVER, GLASS_PANEL, TOOL_WALL, DRAWER_CABINET
    };
    public static final BlockIndustrialMachine[] INDUSTRIAL_MACHINES = {
        RESEARCH_STATION, ORBITAL_EXPERIMENT_MODULE, ELECTRIC_FABRICATOR,
        PROGRAMMABLE_ASSEMBLER, CAR_WORKSHOP, GUN_FACTORY,
        ROBOTIC_MANUFACTURING_CELL, MATTER_REPLICATOR,
        FUSION_RESEARCH_CORE, INTERPLANETARY_CARGO_CONTROLLER,
        ORBITAL_MEGASTRUCTURE_CONTROLLER, AUTONOMOUS_COLONY_BEACON
    };
    public static final ItemIndustrialArtifact ORBITAL_RESEARCH_ARCHIVE = artifact("orbital_research_archive");
    public static final ItemIndustrialArtifact LUNAR_ENGINEERING_ARCHIVE = artifact("lunar_engineering_archive");
    public static final ItemIndustrialArtifact LUNAR_QUANTUM_COMPONENT = artifact("lunar_quantum_component");
    public static final ItemIndustrialArtifact MARS_MISSION_AUTHORIZATION = artifact("mars_mission_authorization");
    public static final ItemIndustrialArtifact MARTIAN_AUTONOMY_ARCHIVE = artifact("martian_autonomy_archive");
    public static final ItemIndustrialArtifact AI_CORE = artifact("artificial_industrial_intelligence_core");
    public static final ItemIndustrialArtifact BLANK_DATA_CARTRIDGE = artifact("blank_data_cartridge", false);
    public static final ItemIndustrialArtifact RESEARCH_DATA = artifact("research_data", false);
    public static final ItemIndustrialArtifact PRECISION_FRAME = artifact("precision_frame", false);
    public static final ItemIndustrialArtifact CONTROL_PROCESSOR = artifact("control_processor", false);
    public static final ItemIndustrialArtifact UNDERWORLD_DOSSIER = artifact("underworld_dossier");
    public static final ItemIndustrialArtifact CRIMINAL_NETWORK_LEDGER = artifact("criminal_network_ledger");
    public static final ItemIndustrialArtifact FACTORY_RESTORATION_CERTIFICATE = artifact("factory_restoration_certificate");
    public static final ItemIndustrialArtifact RECOVERED_FACTORY_CONTROL_SYSTEM = artifact("recovered_factory_control_system");
    public static final ItemIndustrialArtifact UU_MATTER_CAPSULE = artifact("uu_matter_capsule");
    public static final ItemIndustrialArtifact REPLICATION_RECORD = artifact("controlled_replication_record");
    public static final ItemIndustrialArtifact ANTIMATTER_CAPSULE = artifact("contained_antimatter_capsule");
    public static final ItemIndustrialArtifact CARGO_NETWORK_KEY = artifact("interplanetary_cargo_network_key");
    public static final ItemIndustrialArtifact MEGASTRUCTURE_CONTROL_RECORD = artifact("megastructure_control_record");
    public static final ItemIndustrialArtifact AUTONOMOUS_COLONY_CHARTER = artifact("autonomous_colony_charter");
    public static final ItemIndustrialArtifact CIVILIZATION_SCALE_AI_CORE = artifact("civilization_scale_ai_core");
    public static final ItemEmergencyContinuityCore EMERGENCY_CONTINUITY_CORE =
        new ItemEmergencyContinuityCore();
    public static final ItemIndustrialArtifact[] ARTIFACTS = {
        ORBITAL_RESEARCH_ARCHIVE, LUNAR_ENGINEERING_ARCHIVE, LUNAR_QUANTUM_COMPONENT,
        MARS_MISSION_AUTHORIZATION, MARTIAN_AUTONOMY_ARCHIVE, AI_CORE,
        BLANK_DATA_CARTRIDGE, RESEARCH_DATA, PRECISION_FRAME, CONTROL_PROCESSOR,
        UNDERWORLD_DOSSIER, CRIMINAL_NETWORK_LEDGER, FACTORY_RESTORATION_CERTIFICATE,
        RECOVERED_FACTORY_CONTROL_SYSTEM, UU_MATTER_CAPSULE, REPLICATION_RECORD,
        ANTIMATTER_CAPSULE, CARGO_NETWORK_KEY, MEGASTRUCTURE_CONTROL_RECORD,
        AUTONOMOUS_COLONY_CHARTER, CIVILIZATION_SCALE_AI_CORE
    };

    private static BlockIndustrialMachine machine(IndustrialMachineKind kind) {
        return new BlockIndustrialMachine(kind);
    }

    private static BlockWorkshopComponent component(String id) {
        return new BlockWorkshopComponent(id);
    }

    private static ItemIndustrialArtifact artifact(String id) {
        return new ItemIndustrialArtifact(id);
    }

    private static ItemIndustrialArtifact artifact(String id, boolean activatesMilestone) {
        return new ItemIndustrialArtifact(id, activatesMilestone);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        if (event.getSide() == Side.CLIENT) ClientRenderRegistration.register();
        Configuration runtime = new Configuration(new java.io.File(
            event.getModConfigurationDirectory(), "industrialcivilization/runtime.cfg"));
        runtime.load();
        ENFORCE_SPACE_GATES = runtime.getBoolean("enforceSpaceResearchGates", "progression",
            true, "Return unauthorized players to Earth when entering the Moon or Mars.");
        NATIVE_IC2_POWER_SCALING = runtime.getBoolean("nativeIc2PowerScaling", "energy",
            true, "Convert machine progress into total-EU work so legal aggregate IC2 input increases throughput.");
        ALLOW_MULTI_PACKET_THROUGHPUT = runtime.getBoolean("allowMultiPacketThroughput", "energy",
            true, "Allow independently legal IC2 packets received between machine ticks to contribute aggregate work.");
        ROBBER_SPAWN_PERCENT = runtime.getInt("robberSpawnPercent", "ecology", 25, 0, 100,
            "Percent of ordinary Earth zombie spawn attempts converted into Robbers.");
        ROBBER_LOCAL_CAP = runtime.getInt("robberLocalCap", "ecology", 4, 1, 16,
            "Maximum naturally converted Robbers within 64 blocks before new attempts are rejected.");
        MILITIA_PATROL_RADIUS = runtime.getInt("militiaPatrolRadius", "ecology", 128, 16, 512,
            "Maximum distance from a registered militia outpost where patrols may naturally appear.");
        MILITIA_PATROL_LOCAL_CAP = runtime.getInt("militiaPatrolLocalCap", "ecology", 6, 1, 24,
            "Maximum naturally converted militia patrols within the configured outpost patrol radius.");
        TEST_BRIDGE_ENABLED = runtime.getBoolean("enableTestBridge", "testing", false,
            "Enable /ic_test deterministic scenarios and snapshots. Use only in disposable development worlds.");
        E2E_AUTO_SCENARIO = runtime.getString("autoScenario", "testing", "",
            "Automatically run this scenario in a disposable client world. Leave blank during normal play.").trim();
        if (event.getSide() == Side.CLIENT && !E2E_AUTO_SCENARIO.isEmpty()) {
            FMLCommonHandler.instance().bus().register(new ClientE2ERunner());
            LOGGER.info("IC_E2E|RUNNER_ARMED|scenario={}", E2E_AUTO_SCENARIO);
        }
        if (runtime.hasChanged()) runtime.save();
        // Preserve vanilla/mod recipe compatibility while making every pearl a
        // visibly technical, AI-manufactured phase component.
        Items.ENDER_PEARL.setUnlocalizedName(MODID + ".technical_phase_pearl");
        Items.TOTEM_OF_UNDYING.setCreativeTab(null);
        GameRegistry.registerTileEntity(TileMolecularAnalyzer.class,
            new ResourceLocation(MODID, "molecular_analyzer"));
        GameRegistry.registerTileEntity(TileIndustrialMachine.class,
            new ResourceLocation(MODID, "industrial_machine"));
        GameRegistry.registerTileEntity(TileFactoryControlTerminal.class,
            new ResourceLocation(MODID, "factory_control_terminal"));
        GameRegistry.registerTileEntity(TileEnvironmentalSolarArray.class,
            new ResourceLocation(MODID, "environmental_solar_array"));
        GameRegistry.registerTileEntity(TileVehicleServiceDock.class,
            new ResourceLocation(MODID, "vehicle_service_dock"));
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "robber"),
            EntityRobber.class, "robber", 1, this, 80, 3, true, 0x273029, 0x8A3C28);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "militia_patrol"),
            EntityMilitiaPatrol.class, "militia_patrol", 2, this, 96, 2, true, 0x3A3025, 0x7D6B43);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "space_pirate"),
            EntitySpacePirate.class, "space_pirate", 3, this, 96, 2, true, 0xD7DDE0, 0x8A3C28);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "space_militia"),
            EntitySpaceMilitia.class, "space_militia", 4, this, 96, 2, true, 0xD7DDE0, 0x486A78);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "space_citizen"),
            EntitySpaceCitizen.class, "space_citizen", 5, this, 80, 3, true, 0xD7DDE0, 0x36DBE8);
        GameRegistry.registerWorldGenerator(new CivilizationWorldGenerator(), 50);
        MinecraftForge.TERRAIN_GEN_BUS.register(new VillageSuppressionHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new MoonPurityHandler());
        FactionNetwork.init();
        ProgressionNetwork.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        removeVanillaHostileSpawnEggs();
        AnalyzerPeripheralProvider.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new IndustrialGuiHandler());
    }

    /** Creative exposes Industrial identities, never the vanilla monsters they replace. */
    private static void removeVanillaHostileSpawnEggs() {
        EntityList.ENTITY_EGGS.keySet().removeIf(id -> {
            if (!"minecraft".equals(id.getResourceDomain())) return false;
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(id);
            return entry != null && IMob.class.isAssignableFrom(entry.getEntityClass());
        });
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandIndustrialStatus());
        if (TEST_BRIDGE_ENABLED) event.registerServerCommand(new CommandIndustrialTest());
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static final class Registration {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(MOLECULAR_ANALYZER);
            event.getRegistry().registerAll(INDUSTRIAL_MACHINES);
            event.getRegistry().register(FACTORY_CONTROL_TERMINAL);
            event.getRegistry().register(ENVIRONMENTAL_SOLAR_ARRAY);
            event.getRegistry().register(TRACKING_SOLAR_ARRAY);
            event.getRegistry().register(REPAIR_BENCH);
            event.getRegistry().register(VEHICLE_SERVICE_DOCK);
            event.getRegistry().registerAll(WORKSHOP_COMPONENTS);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(MATERIAL_PATTERN_RECORD);
            event.getRegistry().register(INDUSTRIAL_CREDIT);
            event.getRegistry().register(EMERGENCY_CONTINUITY_CORE);
            event.getRegistry().registerAll(ARTIFACTS);
            event.getRegistry().register(new ItemBlock(MOLECULAR_ANALYZER)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(MOLECULAR_ANALYZER.getRegistryName()));
            for (BlockIndustrialMachine machine : INDUSTRIAL_MACHINES) {
                event.getRegistry().register(new ItemBlock(machine)
                    .setCreativeTab(CREATIVE_TAB)
                    .setRegistryName(machine.getRegistryName()));
            }
            event.getRegistry().register(new ItemBlock(FACTORY_CONTROL_TERMINAL)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(FACTORY_CONTROL_TERMINAL.getRegistryName()));
            event.getRegistry().register(new ItemBlock(ENVIRONMENTAL_SOLAR_ARRAY)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(ENVIRONMENTAL_SOLAR_ARRAY.getRegistryName()));
            event.getRegistry().register(new ItemBlock(TRACKING_SOLAR_ARRAY)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(TRACKING_SOLAR_ARRAY.getRegistryName()));
            event.getRegistry().register(new ItemBlock(REPAIR_BENCH)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(REPAIR_BENCH.getRegistryName()));
            event.getRegistry().register(new ItemBlock(VEHICLE_SERVICE_DOCK)
                .setCreativeTab(CREATIVE_TAB)
                .setRegistryName(VEHICLE_SERVICE_DOCK.getRegistryName()));
            for (BlockWorkshopComponent component : WORKSHOP_COMPONENTS) {
                event.getRegistry().register(new ItemBlock(component)
                    .setCreativeTab(CREATIVE_TAB)
                    .setRegistryName(component.getRegistryName()));
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            migrateQuestHomeImage();
            event.player.sendMessage(new TextComponentTranslation(
                "message.industrialcivilization.quest_guide"));
            if (event.player instanceof EntityPlayerMP) {
                UnifiedAdvancementSystem.synchronize((EntityPlayerMP) event.player);
            }
        }

        private static void migrateQuestHomeImage() {
            String current = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE);
            float currentAnchorY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_Y);
            int currentOffsetX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X);
            int currentOffsetY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y);
            boolean changed = false;
            if (!QUEST_HOME_IMAGE.equals(current)) {
                QuestSettings.INSTANCE.setProperty(NativeProps.HOME_IMAGE, QUEST_HOME_IMAGE);
                changed = true;
            }
            if (Float.compare(currentAnchorY, QUEST_HOME_ANCHOR_Y) != 0) {
                QuestSettings.INSTANCE.setProperty(NativeProps.HOME_ANC_Y, QUEST_HOME_ANCHOR_Y);
                changed = true;
            }
            if (currentOffsetX != QUEST_HOME_OFFSET_X) {
                QuestSettings.INSTANCE.setProperty(NativeProps.HOME_OFF_X, QUEST_HOME_OFFSET_X);
                changed = true;
            }
            if (currentOffsetY != QUEST_HOME_OFFSET_Y) {
                QuestSettings.INSTANCE.setProperty(NativeProps.HOME_OFF_Y, QUEST_HOME_OFFSET_Y);
                changed = true;
            }
            if (changed) {
                SaveLoadHandler.INSTANCE.markDirty();
                LOGGER.info("Migrated Better Questing home layout from '{}',({}, {}, {}) to '{}',({}, {}, {})",
                    current, currentAnchorY, currentOffsetX, currentOffsetY,
                    QUEST_HOME_IMAGE, QUEST_HOME_ANCHOR_Y, QUEST_HOME_OFFSET_X, QUEST_HOME_OFFSET_Y);
            }
        }

        @SubscribeEvent
        public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.toDim == 1) {
                denyDestination(event, "message.industrialcivilization.gate.end");
                return;
            }
            String destination = event.player.world.provider.getDimensionType().getName().toLowerCase();
            String provider = event.player.world.provider.getClass().getName().toLowerCase();
            boolean supportedSpace = destination.contains("moon") || destination.contains("mars")
                || destination.contains("orbit") || destination.contains("space station")
                || destination.contains("overworld");
            if (provider.startsWith("micdoodle8.mods.galacticraft") && !supportedSpace) {
                denyDestination(event, "message.industrialcivilization.gate.space_destination");
                return;
            }
            ProgressionState.increment(event.player, "dimension_transfers", 1);
            if (destination.contains("moon")) {
                if (ENFORCE_SPACE_GATES && !ProgressionState.has(event.player, "orbital_research_archive")) {
                    denyDestination(event, "message.industrialcivilization.gate.moon");
                    return;
                }
                ProgressionState.record(event.player, "moon_visited");
                RuntimeAdvancements.grant(event.player, "moon_access");
                RuntimeAdvancements.grant(event.player, "lunar_landing");
                LOGGER.info("progression moon_visited player={}", event.player.getName());
            } else if (destination.contains("mars")) {
                boolean authorized = ProgressionState.has(event.player, "lunar_quantum_component")
                    && ProgressionState.has(event.player, "mars_mission_authorization");
                if (ENFORCE_SPACE_GATES && !authorized) {
                    denyDestination(event, "message.industrialcivilization.gate.mars");
                    return;
                }
                boolean moonFirst = ProgressionState.has(event.player, "moon_visited");
                ProgressionState.record(event.player, "mars_visited");
                RuntimeAdvancements.grant(event.player, "mars_access");
                if (moonFirst) ProgressionState.record(event.player, "moon_before_mars");
                LOGGER.info("progression mars_visited player={} moon_before_mars={}", event.player.getName(), moonFirst);
                if (!moonFirst) {
                    event.player.sendStatusMessage(new TextComponentTranslation(
                        "message.industrialcivilization.sequence_violation"), false);
                }
            }
        }

        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.world.isRemote
                    || event.player.ticksExisted % 20 != 0) return;
            for (ItemStack stack : event.player.inventory.mainInventory) {
                if (!stack.isEmpty() && stack.getItem() instanceof ItemIndustrialArtifact) {
                    String id = ((ItemIndustrialArtifact) stack.getItem()).getArtifactId();
                    if (!ProgressionState.has(event.player, id)) {
                        ProgressionState.increment(event.player, "artifacts_recorded", 1);
                    }
                    ProgressionState.record(event.player, id);
                }
            }
            if (event.player instanceof EntityPlayerMP
                    && ProgressionState.has(event.player, "mars_mission_authorization")
                    && !ProgressionState.has(event.player, "tier2_schematic_unlocked")) {
                micdoodle8.mods.galacticraft.api.recipe.ISchematicPage tier2 =
                    micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry
                        .getMatchingRecipeForItemStack(new ItemStack(
                            micdoodle8.mods.galacticraft.core.GCItems.schematic, 1, 1));
                if (tier2 != null) {
                    micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry.addUnlockedPage(
                        (EntityPlayerMP) event.player, tier2);
                    ProgressionState.record(event.player, "tier2_schematic_unlocked");
                    event.player.sendStatusMessage(new TextComponentTranslation(
                        "message.industrialcivilization.schematic.tier2"), false);
                }
            }
            boolean aiReady = GameplayRules.aiAgeReady(
                ProgressionState.has(event.player, "artificial_industrial_intelligence_core"),
                ProgressionState.has(event.player, "lite_matter_complete"),
                ProgressionState.has(event.player, "martian_autonomy_archive"));
            if (aiReady) {
                ProgressionState.record(event.player, "ai_age");
                RuntimeAdvancements.grant(event.player, "ai_age_entry");
                if (!ProgressionState.has(event.player, "ai_credits_shown")
                        && event.player instanceof EntityPlayerMP) {
                    ProgressionState.record(event.player, "ai_credits_shown");
                    ProgressionNetwork.showCredits((EntityPlayerMP) event.player);
                }
            } else if (!event.player.capabilities.isCreativeMode) {
                removePrematurePhasePearls(event.player);
            }
            FactionSystem.updatePlaystyleReputation(event.player);
            if (event.player instanceof EntityPlayerMP && event.player.ticksExisted % 100 == 0) {
                UnifiedAdvancementSystem.synchronize((EntityPlayerMP) event.player);
            }
            ProgressionState.increment(event.player, "active_ticks", 20);
        }

        @SubscribeEvent
        public static void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
            if (!event.player.world.isRemote) {
                ProgressionState.increment(event.player, "manual_crafts", 1);
                if (event.crafting.getItem() == Items.ENDER_PEARL
                        && ProgressionState.has(event.player, "ai_age_entry")) {
                    RuntimeAdvancements.grant(event.player, "technical_phase_pearl");
                }
            }
        }

        @SubscribeEvent
        public static void blockBroken(BlockEvent.BreakEvent event) {
            if (event.getPlayer() != null && !event.getWorld().isRemote) {
                ProgressionState.increment(event.getPlayer(), "blocks_mined_manually", 1);
            }
        }

        /** Endermen and inherited mobs cannot bypass AI manufacturing. */
        @SubscribeEvent
        public static void suppressNaturalPhasePearls(LivingDropsEvent event) {
            event.getDrops().removeIf(drop -> drop.getItem().getItem() == Items.ENDER_PEARL
                || drop.getItem().getItem() == Items.TOTEM_OF_UNDYING);
        }

        /** Industrial AI failover replaces the removed magical Totem of Undying. */
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void emergencyContinuity(LivingDamageEvent event) {
            if (event.getEntityLiving().world.isRemote
                    || !(event.getEntityLiving() instanceof net.minecraft.entity.player.EntityPlayer)
                    || event.getAmount() < event.getEntityLiving().getHealth()) return;
            net.minecraft.entity.player.EntityPlayer player =
                (net.minecraft.entity.player.EntityPlayer) event.getEntityLiving();
            int slot = -1;
            for (int index = 0; index < player.inventory.getSizeInventory(); index++) {
                ItemStack stack = player.inventory.getStackInSlot(index);
                if (!stack.isEmpty() && stack.getItem() == EMERGENCY_CONTINUITY_CORE) {
                    slot = index;
                    break;
                }
            }
            if (slot < 0) return;
            player.inventory.decrStackSize(slot, 1);
            event.setCanceled(true);
            player.setHealth(1.0F);
            player.clearActivePotions();
            player.addPotionEffect(new net.minecraft.potion.PotionEffect(
                net.minecraft.init.MobEffects.REGENERATION, 900, 1));
            player.addPotionEffect(new net.minecraft.potion.PotionEffect(
                net.minecraft.init.MobEffects.ABSORPTION, 100, 1));
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                net.minecraft.init.SoundEvents.BLOCK_NOTE_PLING,
                net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.35F);
            RuntimeAdvancements.grant(player, "minecraft", "adventure/totem_of_undying",
                "ai_emergency_continuity_activated");
        }

        /** The End is outside this pack's progression and its portal cannot be armed. */
        @SubscribeEvent
        public static void disableEndPortal(PlayerInteractEvent.RightClickBlock event) {
            if (event.getWorld().getBlockState(event.getPos()).getBlock() != Blocks.END_PORTAL_FRAME) return;
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
            if (!event.getWorld().isRemote) {
                event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.gate.end"), false);
            }
        }

        /** IC2's original End-only paradise achievement is reassigned to Mars. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void awardMarsCultivationParadise(PlayerInteractEvent.RightClickBlock event) {
            if (event.getWorld().isRemote || event.isCanceled()) return;
            ItemStack held = event.getItemStack();
            ResourceLocation heldId = held.isEmpty() ? null : held.getItem().getRegistryName();
            TileEntity tile = event.getWorld().getTileEntity(event.getPos());
            String dimension = event.getWorld().provider.getDimensionType().getName();
            if (GameplayRules.marsCultivationAchievement(
                    dimension,
                    heldId == null ? null : heldId.toString(),
                    held.getMetadata(),
                    tile == null ? null : tile.getClass().getName())) {
                RuntimeAdvancements.grant(event.getEntityPlayer(), "ic2",
                    "basic/terraformendcultivation", "mars_cultivation_tfbp_inserted");
            }
        }

        private static void removePrematurePhasePearls(net.minecraft.entity.player.EntityPlayer player) {
            boolean removed = false;
            for (ItemStack stack : player.inventory.mainInventory) {
                if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                    stack.setCount(0);
                    removed = true;
                }
            }
            for (ItemStack stack : player.inventory.offHandInventory) {
                if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                    stack.setCount(0);
                    removed = true;
                }
            }
            if (removed) player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.phase_pearl.locked"), false);
        }

        private static void denyDestination(PlayerEvent.PlayerChangedDimensionEvent event, String message) {
            event.player.sendMessage(new TextComponentTranslation(message));
            LOGGER.warn("Blocked unauthorized dimension entry player={} destination={}",
                event.player.getName(), event.player.world.provider.getDimensionType().getName());
            if (event.player instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) event.player;
                WorldServer earth = player.getServer().getWorld(0);
                player.getServer().getPlayerList().transferPlayerToDimension(player, 0, new Teleporter(earth));
            }
        }
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = MODID)
    public static final class ClientRegistration {
        private static final java.lang.reflect.Field ADVANCEMENT_TABS = ReflectionHelper.findField(
            GuiScreenAdvancements.class, "tabs", "field_191947_i");
        private static final java.lang.reflect.Field SELECTED_ADVANCEMENT_TAB = ReflectionHelper.findField(
            GuiScreenAdvancements.class, "selectedTab", "field_191940_s");
        private static final java.lang.reflect.Field ADVANCEMENT_TAB_PAGE = ReflectionHelper.findField(
            GuiScreenAdvancements.class, "tabPage");
        private static final java.lang.reflect.Field ADVANCEMENT_TAB_INSTANCE_PAGE = ReflectionHelper.findField(
            GuiAdvancementTab.class, "page");
        private static final java.lang.reflect.Field ADVANCEMENT_TAB_SCROLL_X = ReflectionHelper.findField(
            GuiAdvancementTab.class, "scrollX", "field_191811_n");
        private static final java.lang.reflect.Field ADVANCEMENT_TAB_SCROLL_Y = ReflectionHelper.findField(
            GuiAdvancementTab.class, "scrollY", "field_191812_o");
        private static final java.lang.reflect.Field ADVANCEMENT_TAB_CENTERED = ReflectionHelper.findField(
            GuiAdvancementTab.class, "centered", "field_192992_s");
        private static final java.lang.reflect.Field SELECTED_QUEST_LINE = ReflectionHelper.findField(
            GuiQuestLines.class, "selectedLine");
        private static final java.lang.reflect.Field SELECTED_QUEST_LINE_ID = ReflectionHelper.findField(
            GuiQuestLines.class, "selectedLineId");
        private static final java.lang.reflect.Field QUEST_CANVAS = ReflectionHelper.findField(
            GuiQuestLines.class, "cvQuest");
        private static final KeyMigration[] KEY_MIGRATIONS = {
            // Tutorial and interface access.
            key("key.betterquesting.quests", 41, KeyModifier.NONE, 64, KeyModifier.NONE),
            key("key.toggle_focus.desc", 15, KeyModifier.NONE, 15, KeyModifier.ALT),
            key("key.jei.bookmark", 30, KeyModifier.NONE, 30, KeyModifier.ALT),
            key("Quest Log", 38, KeyModifier.NONE, 38, KeyModifier.SHIFT),

            // Inventory, equipment, and combat.
            key("invtweaks.key.sort", 19, KeyModifier.NONE, 19, KeyModifier.ALT),
            key("techguns.key.forceReload", 19, KeyModifier.NONE, 19, KeyModifier.SHIFT),
            key("key.vehicle.horn", 35, KeyModifier.NONE, 37, KeyModifier.ALT),
            key("key.vehicle.cycle_seats", 46, KeyModifier.NONE, 38, KeyModifier.ALT),
            key("Open Spaceship Inventory", 33, KeyModifier.NONE, 33, KeyModifier.ALT),
            key("Boost Key", 29, KeyModifier.NONE, 57, KeyModifier.ALT),
            key("key.control", 29, KeyModifier.NONE, 34, KeyModifier.NONE),
            key("Mode Switch Key", 48, KeyModifier.NONE, 48, KeyModifier.SHIFT),
            key("Hub Expand Key", 48, KeyModifier.NONE, 48, KeyModifier.ALT),
            key("key.minimap.waypointhotkey", 49, KeyModifier.NONE, 49, KeyModifier.SHIFT),
            key("mod.chiselsandbits.other.mode", 56, KeyModifier.NONE, 0, KeyModifier.NONE),
            key("schematica.key.control", 74, KeyModifier.NONE, 34, KeyModifier.ALT),
            key("RecipeSwitch", 78, KeyModifier.NONE, 31, KeyModifier.ALT),

            // MacBook-friendly top-row replacements for inherited numpad bindings.
            key("waila.keybind.wailadisplay", 79, KeyModifier.NONE, 2, KeyModifier.ALT),
            key("waila.keybind.liquid", 80, KeyModifier.NONE, 3, KeyModifier.ALT),
            key("waila.keybind.recipe", 81, KeyModifier.NONE, 4, KeyModifier.ALT),
            key("waila.keybind.wailaconfig", 82, KeyModifier.NONE, 11, KeyModifier.ALT),
            key("waila.keybind.usage", 75, KeyModifier.NONE, 6, KeyModifier.ALT),
            key("Scene1 start/pause", 79, KeyModifier.NONE, 2, KeyModifier.SHIFT),
            key("Scene2 start/pause", 80, KeyModifier.NONE, 3, KeyModifier.SHIFT),
            key("Scene3 start/pause", 81, KeyModifier.NONE, 4, KeyModifier.SHIFT),
            key("Scene reset", 82, KeyModifier.NONE, 11, KeyModifier.SHIFT),
            key("key.music_player_previous", 79, KeyModifier.NONE, 2, KeyModifier.CONTROL),
            key("key.music_player_toggle", 80, KeyModifier.NONE, 3, KeyModifier.CONTROL),
            key("key.music_player_next", 81, KeyModifier.NONE, 4, KeyModifier.CONTROL),
            key("key.music_player_gui", 82, KeyModifier.NONE, 11, KeyModifier.CONTROL),
            key("keybind.universaltweaks.clear_toasts", 11, KeyModifier.CONTROL, 10, KeyModifier.CONTROL),

            // Upgrade profiles that briefly received the first cross-platform layout.
            key("key.toggle_focus.desc", 15, KeyModifier.CONTROL, 15, KeyModifier.ALT),
            key("key.jei.bookmark", 30, KeyModifier.CONTROL, 30, KeyModifier.ALT),
            key("Boost Key", 157, KeyModifier.NONE, 57, KeyModifier.ALT),
            key("key.control", 184, KeyModifier.NONE, 34, KeyModifier.NONE),
            key("Hub Expand Key", 48, KeyModifier.CONTROL, 48, KeyModifier.ALT),
            key("Scene1 start/pause", 79, KeyModifier.SHIFT, 2, KeyModifier.SHIFT),
            key("Scene2 start/pause", 80, KeyModifier.SHIFT, 3, KeyModifier.SHIFT),
            key("Scene3 start/pause", 81, KeyModifier.SHIFT, 4, KeyModifier.SHIFT),
            key("Scene reset", 82, KeyModifier.SHIFT, 11, KeyModifier.SHIFT),
            key("key.music_player_previous", 79, KeyModifier.CONTROL, 2, KeyModifier.CONTROL),
            key("key.music_player_toggle", 80, KeyModifier.CONTROL, 3, KeyModifier.CONTROL),
            key("key.music_player_next", 81, KeyModifier.CONTROL, 4, KeyModifier.CONTROL),
            key("key.music_player_gui", 82, KeyModifier.CONTROL, 11, KeyModifier.CONTROL),

            // Final macOS layout, verified against Forge's live conflict contexts.
            key("ALT Key", 56, KeyModifier.NONE, 43, KeyModifier.NONE),
            key("Boost Key", 57, KeyModifier.ALT, 35, KeyModifier.ALT),
            key("Hub Expand Key", 48, KeyModifier.ALT, 24, KeyModifier.ALT),
            key("Open Spaceship Inventory", 33, KeyModifier.ALT, 23, KeyModifier.CONTROL),
            key("schematica.key.control", 34, KeyModifier.ALT, 25, KeyModifier.ALT),
            key("key.jei.bookmark", 30, KeyModifier.ALT, 23, KeyModifier.ALT),
            key("key.toggle_focus.desc", 15, KeyModifier.ALT, 25, KeyModifier.CONTROL),
            key("Mode Switch Key", 48, KeyModifier.SHIFT, 68, KeyModifier.NONE),
            key("Quest Log", 38, KeyModifier.SHIFT, 66, KeyModifier.NONE),
            key("techguns.key.forceReload", 19, KeyModifier.SHIFT, 21, KeyModifier.ALT),
            key("key.minimap.waypointhotkey", 49, KeyModifier.SHIFT, 21, KeyModifier.CONTROL),
            key("schematica.key.pickBlock", -98, KeyModifier.SHIFT, 0, KeyModifier.NONE),
            key("Scene1 start/pause", 2, KeyModifier.SHIFT, 0, KeyModifier.NONE),
            key("Scene2 start/pause", 3, KeyModifier.SHIFT, 0, KeyModifier.NONE),
            key("Scene3 start/pause", 4, KeyModifier.SHIFT, 0, KeyModifier.NONE),
            key("Scene reset", 11, KeyModifier.SHIFT, 0, KeyModifier.NONE),
            key("waila.keybind.wailadisplay", 2, KeyModifier.ALT, 26, KeyModifier.CONTROL),
            key("waila.keybind.liquid", 3, KeyModifier.ALT, 27, KeyModifier.CONTROL),
            key("waila.keybind.recipe", 4, KeyModifier.ALT, 39, KeyModifier.CONTROL),
            key("waila.keybind.wailaconfig", 11, KeyModifier.ALT, 11, KeyModifier.CONTROL),
            key("waila.keybind.usage", 6, KeyModifier.ALT, 40, KeyModifier.CONTROL),
            key("key.music_player_previous", 2, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("key.music_player_toggle", 3, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("key.music_player_next", 4, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("key.music_player_gui", 11, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("keybind.groovyscript.copy", 46, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("keybind.universaltweaks.clear_toasts", 10, KeyModifier.CONTROL, 0, KeyModifier.NONE),
            key("keybind.railcraft.loco.reverse", 53, KeyModifier.ALT, 26, KeyModifier.ALT),
            key("keybind.railcraft.loco.slower", 51, KeyModifier.ALT, 27, KeyModifier.ALT),
            key("keybind.railcraft.loco.mode", 50, KeyModifier.ALT, 39, KeyModifier.ALT),
            key("keybind.railcraft.loco.whistle", 49, KeyModifier.ALT, 40, KeyModifier.ALT)
        };
        private static boolean keyBindingsChecked;
        private static net.minecraft.client.multiplayer.WorldClient terrainWarmupWorld;
        private static boolean terrainWarmupShown;

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void registerModels(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(MOLECULAR_ANALYZER), 0,
                new ModelResourceLocation(MOLECULAR_ANALYZER.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                MATERIAL_PATTERN_RECORD, 0,
                new ModelResourceLocation(MATERIAL_PATTERN_RECORD.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                INDUSTRIAL_CREDIT, 0,
                new ModelResourceLocation(INDUSTRIAL_CREDIT.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                EMERGENCY_CONTINUITY_CORE, 0,
                new ModelResourceLocation(EMERGENCY_CONTINUITY_CORE.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                Items.ENDER_PEARL, 0,
                new ModelResourceLocation(MODID + ":technical_phase_pearl", "inventory"));
            for (BlockIndustrialMachine machine : INDUSTRIAL_MACHINES) {
                ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(machine), 0,
                    new ModelResourceLocation(machine.getRegistryName(), "inventory"));
            }
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(FACTORY_CONTROL_TERMINAL), 0,
                new ModelResourceLocation(FACTORY_CONTROL_TERMINAL.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(ENVIRONMENTAL_SOLAR_ARRAY), 0,
                new ModelResourceLocation(ENVIRONMENTAL_SOLAR_ARRAY.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(TRACKING_SOLAR_ARRAY), 0,
                new ModelResourceLocation(TRACKING_SOLAR_ARRAY.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(REPAIR_BENCH), 0,
                new ModelResourceLocation(REPAIR_BENCH.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(VEHICLE_SERVICE_DOCK), 0,
                new ModelResourceLocation(VEHICLE_SERVICE_DOCK.getRegistryName(), "inventory"));
            for (BlockWorkshopComponent component : WORKSHOP_COMPONENTS) {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(component), 0,
                    new ModelResourceLocation(component.getRegistryName(), "inventory"));
            }
            for (ItemIndustrialArtifact artifact : ARTIFACTS) {
                ModelLoader.setCustomModelResourceLocation(
                    artifact, 0,
                    new ModelResourceLocation(artifact.getRegistryName(), "inventory"));
            }
            registerIc2MachineModels();
        }

        /**
         * IC2 Classic keeps its familiar placed-block renderer, but its tiny
         * isometric inventory cubes are hard to distinguish in HEI. Map each
         * metadata variant to a flat, front-face Astra icon generated from the
         * same source atlas. LOWEST event priority ensures this intentional
         * resource-pack presentation wins over IC2's default item model setup.
         */
        private static void registerIc2MachineModels() {
            registerIc2MachineModels("blockmachinelv", 16);
            registerIc2MachineModels("blockmachinelv2", 8);
            registerIc2MachineModels("blockmachinemv", 14);
            registerIc2MachineModels("blockmachinehv", 7);
            registerIc2MachineModels("blockgenerator", 15);
            registerIc2MachineModels("blockcompactedgenerator", 9);
            registerIc2MachineModels("blockelectric", 11);
            registerIc2MachineModels("blockpersonal", 11);
        }

        private static void registerIc2MachineModels(String registryName, int metadataCount) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2", registryName));
            if (item == null) {
                LOGGER.warn("Could not install Astra inventory icons for missing IC2 item {}", registryName);
                return;
            }
            for (int metadata = 0; metadata < metadataCount; metadata++) {
                ModelLoader.setCustomModelResourceLocation(item, metadata,
                    new ModelResourceLocation(MODID + ":ic2_machines/"
                        + registryName + "_" + metadata, "inventory"));
            }
        }

        /**
         * Better Questing fixes the home title at 256x128. Resize its deepest
         * textured panel after initialization so large windows use their space,
         * while retaining that native size at the minimum supported window.
         */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void resizeAndCenterQuestHomeTitle(GuiScreenEvent.InitGuiEvent.Post event) {
            if (!(event.getGui() instanceof GuiHome)) return;
            IGuiPanel title = findQuestHomeTitle((GuiHome) event.getGui());
            if (title == null || !(title.getTransform() instanceof GuiTransform)) {
                LOGGER.warn("Could not locate Better Questing home title panel for responsive layout");
                return;
            }
            GuiTransform transform = (GuiTransform) title.getTransform();
            int backdropWidth = transform.getParent().getWidth();
            int backdropHeight = transform.getParent().getHeight();
            int width = GameplayRules.questHomeTitleWidth(backdropWidth, backdropHeight);
            int height = width / 2;
            transform.getAnchor().set(0.5F, 0.5F, 0.5F, 0.5F);
            int left = -width / 2;
            int top = -height / 2;
            transform.getPadding().setPadding(left, top, -width - left, -height - top);
        }

        private static IGuiPanel findQuestHomeTitle(GuiHome home) {
            for (IGuiPanel root : home.getChildren()) {
                if (!(root instanceof IGuiCanvas)) continue;
                for (IGuiPanel content : ((IGuiCanvas) root).getChildren()) {
                    if (!(content instanceof IGuiCanvas)) continue;
                    for (IGuiPanel backdrop : ((IGuiCanvas) content).getChildren()) {
                        if (!(backdrop instanceof CanvasTextured) || !(backdrop instanceof IGuiCanvas)) continue;
                        for (IGuiPanel child : ((IGuiCanvas) backdrop).getChildren()) {
                            if (child instanceof CanvasTextured) return child;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Better Questing has no selected line in a fresh client session. Its
         * home-screen Quests button consequently opens an empty black canvas
         * until the player manually expands the quest-line tray. Seed that
         * navigation state with the canonical first chapter.
         */
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void openQuestGuideAtFirstChapter(GuiOpenEvent event) {
            if (!(event.getGui() instanceof GuiQuestLines)
                    || !(Minecraft.getMinecraft().currentScreen instanceof GuiHome)) return;
            IQuestLine firstChapter = QuestLineDatabase.INSTANCE.getValue(0);
            if (firstChapter != null) {
                try {
                    SELECTED_QUEST_LINE.set(event.getGui(), firstChapter);
                    SELECTED_QUEST_LINE_ID.setInt(event.getGui(), 0);
                } catch (IllegalAccessException exception) {
                    LOGGER.warn("Could not select the first Better Questing chapter", exception);
                }
            }
        }

        /** Prevent zooming far enough to expose empty canvas beyond the backdrop. */
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void clampQuestBackgroundZoom(GuiScreenEvent.DrawScreenEvent.Pre event) {
            if (!(event.getGui() instanceof GuiQuestLines)) return;
            try {
                CanvasQuestLine canvas = (CanvasQuestLine) QUEST_CANVAS.get(event.getGui());
                if (canvas == null || canvas.getQuestLine() == null) return;
                int backgroundSize = canvas.getQuestLine().getProperty(NativeProps.BG_SIZE);
                int viewportWidth = canvas.getTransform().getWidth();
                int viewportHeight = canvas.getTransform().getHeight();
                float oldZoom = canvas.getZoom();
                float minimumZoom = GameplayRules.questMinimumZoom(
                    viewportWidth, viewportHeight, backgroundSize);
                if (oldZoom + 0.0001F < minimumZoom) {
                    int centerX = canvas.getScrollX()
                        + Math.round(viewportWidth / (2.0F * oldZoom));
                    int centerY = canvas.getScrollY()
                        + Math.round(viewportHeight / (2.0F * oldZoom));
                    canvas.setZoom(minimumZoom);
                    canvas.setScrollX(centerX
                        - Math.round(viewportWidth / (2.0F * minimumZoom)));
                    canvas.setScrollY(centerY
                        - Math.round(viewportHeight / (2.0F * minimumZoom)));
                    canvas.refreshScrollBounds();
                }
                int boundedX = GameplayRules.questBoundedScroll(canvas.getScrollX(),
                    viewportWidth, backgroundSize, canvas.getZoom());
                int boundedY = GameplayRules.questBoundedScroll(canvas.getScrollY(),
                    viewportHeight, backgroundSize, canvas.getZoom());
                if (canvas.getScrollX() != boundedX) canvas.setScrollX(boundedX);
                if (canvas.getScrollY() != boundedY) canvas.setScrollY(boundedY);
                canvas.updatePanelScroll();
            } catch (IllegalAccessException exception) {
                LOGGER.warn("Could not enforce Better Questing background bounds", exception);
            }
        }

        /** Keep the vanilla Advancements screen and reserve Statistics for factions. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void renameFactionDirectoryButton(GuiScreenEvent.InitGuiEvent.Post event) {
            if (event.getGui() instanceof micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection) {
                ProgressionNetwork.requestSpaceAccess();
            }
            if (!(event.getGui() instanceof GuiIngameMenu)) return;
            GuiButton advancements = null;
            GuiButton factions = null;
            int maxBottom = 0;
            for (GuiButton button : event.getButtonList()) {
                if (button.id == 5) advancements = button;
                if (button.id == 6) factions = button;
                maxBottom = Math.max(maxBottom, button.y + button.height);
            }
            if (advancements == null || factions == null) return;
            factions.displayString = I18n.format("gui.industrialcivilization.factions");
            int left = Math.min(advancements.x, factions.x);
            int right = Math.max(advancements.x + advancements.width, factions.x + factions.width);
            int rowY = Math.min(advancements.y, factions.y);
            int rowStep = Math.max(24, Math.max(advancements.height, factions.height) + 4);
            if (maxBottom + rowStep <= event.getGui().height - 4) {
                for (GuiButton button : event.getButtonList()) {
                    if (button != advancements && button != factions && button.y > rowY) {
                        button.y += rowStep;
                    }
                }
                advancements.x = left;
                advancements.y = rowY;
                advancements.width = right - left;
                factions.x = left;
                factions.y = rowY + rowStep;
                factions.width = right - left;
            } else {
                fitPauseMenuPair(advancements, factions, left, right);
            }
        }

        private static void fitPauseMenuPair(GuiButton advancements, GuiButton factions,
                int left, int right) {
            int gap = 4;
            int available = right - left - gap;
            int advancementsNeed = Minecraft.getMinecraft().fontRenderer
                .getStringWidth(advancements.displayString) + 12;
            int factionsNeed = Minecraft.getMinecraft().fontRenderer
                .getStringWidth(factions.displayString) + 12;
            if (advancementsNeed + factionsNeed > available) {
                factions.displayString = I18n.format("gui.industrialcivilization.factions.short");
                factionsNeed = Minecraft.getMinecraft().fontRenderer
                    .getStringWidth(factions.displayString) + 12;
            }
            int factionWidth = Math.max(available / 2, factionsNeed);
            factionWidth = Math.min(factionWidth, Math.max(1, available - advancementsNeed));
            advancements.x = left;
            advancements.width = available - factionWidth;
            factions.x = advancements.x + advancements.width + gap;
            factions.width = factionWidth;
        }

        /** Every standalone tree is represented inside the unified IC campaign. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        @SuppressWarnings("unchecked")
        public static void showOnlyPackAdvancementTabs(GuiScreenEvent.InitGuiEvent.Post event) {
            if (!(event.getGui() instanceof GuiScreenAdvancements)) return;
            try {
                GuiScreenAdvancements screen = (GuiScreenAdvancements) event.getGui();
                java.util.Map<net.minecraft.advancements.Advancement, GuiAdvancementTab> tabs =
                    (java.util.Map<net.minecraft.advancements.Advancement, GuiAdvancementTab>)
                        ADVANCEMENT_TABS.get(screen);
                GuiAdvancementTab selectedTab = (GuiAdvancementTab) SELECTED_ADVANCEMENT_TAB.get(screen);
                GuiAdvancementTab packTab = null;
                for (GuiAdvancementTab tab : tabs.values()) {
                    if (isPackAdvancement(tab.getAdvancement())) {
                        packTab = tab;
                        ADVANCEMENT_TAB_INSTANCE_PAGE.setInt(tab, 0);
                    } else {
                        // Preserve vanilla's complete graph while moving foreign roots to a
                        // page the screen never renders. Removing map entries breaks child
                        // lookup and leaves the selected IC tree visually empty.
                        ADVANCEMENT_TAB_INSTANCE_PAGE.setInt(tab, -1);
                    }
                }
                if (packTab != null) {
                    ADVANCEMENT_TAB_PAGE.setInt(null, 0);
                    SELECTED_ADVANCEMENT_TAB.set(screen, packTab);
                    // The unified tree is intentionally much larger than a vanilla tab. Its
                    // geometric center is often empty, so start at the root (x=0, y=0).
                    ADVANCEMENT_TAB_SCROLL_X.setInt(packTab, 103);
                    ADVANCEMENT_TAB_SCROLL_Y.setInt(packTab, 43);
                    ADVANCEMENT_TAB_CENTERED.setBoolean(packTab, true);
                    LOGGER.info("Unified advancement GUI root={} totalRoots={} selected={} page={}",
                        packTab.getAdvancement().getId(), tabs.size(),
                        ((GuiAdvancementTab) SELECTED_ADVANCEMENT_TAB.get(screen)).getAdvancement().getId(),
                        packTab.getPage());
                }
                event.getButtonList().removeIf(button -> button.id == 101 || button.id == 102);
            } catch (ReflectiveOperationException exception) {
                LOGGER.warn("Could not filter foreign advancement tabs", exception);
            }
        }

        private static boolean isPackAdvancement(net.minecraft.advancements.Advancement advancement) {
            return MODID.equals(advancement.getId().getResourceDomain());
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void openFactionDirectoryFromPauseMenu(GuiScreenEvent.ActionPerformedEvent.Pre event) {
            if (!(event.getGui() instanceof GuiIngameMenu)) return;
            if (event.getButton().id == 6) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(new GuiFactionDirectory(event.getGui()));
            }
        }

        @SubscribeEvent
        public static void explainTechnicalPhasePearl(ItemTooltipEvent event) {
            if (event.getItemStack().getItem() == Items.ENDER_PEARL) {
                event.getToolTip().add(I18n.format(
                    "item.industrialcivilizationcore.technical_phase_pearl.tooltip"));
            }
        }

        /** Narrow Galacticraft's own travel whitelist; bodies may still render as aspirations. */
        public static void applySpaceAccess(boolean moon, boolean mars) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof
                    micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection)) return;
            micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection gui =
                (micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection)
                    Minecraft.getMinecraft().currentScreen;
            if (gui.possibleBodies == null) return;
            java.util.List<micdoodle8.mods.galacticraft.api.galaxies.CelestialBody> allowed =
                new java.util.ArrayList<>();
            for (micdoodle8.mods.galacticraft.api.galaxies.CelestialBody body : gui.possibleBodies) {
                if (allowedSpaceBody(body, moon, mars)) allowed.add(body);
            }
            gui.possibleBodies = allowed;
        }

        private static boolean allowedSpaceBody(
                micdoodle8.mods.galacticraft.api.galaxies.CelestialBody body,
                boolean moon, boolean mars) {
            if (body instanceof micdoodle8.mods.galacticraft.api.galaxies.Satellite) return true;
            String name = body.getUnlocalizedName().toLowerCase();
            if (body.getDimensionID() == 0 || name.contains("earth") || name.contains("overworld")) return true;
            if (name.contains("moon")) return moon;
            if (name.contains("mars")) return mars;
            return false;
        }

        @SubscribeEvent
        public static void setConflictFreeQuestKey(TickEvent.ClientTickEvent event) {
            if (keyBindingsChecked || event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.gameSettings == null) {
                return;
            }

            int changes = 0;
            for (KeyMigration migration : KEY_MIGRATIONS) {
                for (KeyBinding binding : minecraft.gameSettings.keyBindings) {
                    if (migration.matches(binding)) {
                        binding.setKeyModifierAndCode(migration.newModifier, migration.newCode);
                        changes++;
                        break;
                    }
                }
            }

            if (changes > 0) {
                KeyBinding.resetKeyBindingArrayAndHash();
                minecraft.gameSettings.saveOptions();
                LOGGER.info("Applied {} conflict-free inherited keybind migrations", changes);
            }
            keyBindingsChecked = true;
        }

        /** Delay player control until visible chunks and their render meshes are ready. */
        @SubscribeEvent
        public static void holdTerrainLoadingScreen(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.world == null || minecraft.player == null) {
                terrainWarmupWorld = null;
                terrainWarmupShown = false;
                return;
            }
            if (terrainWarmupWorld != minecraft.world) {
                terrainWarmupWorld = minecraft.world;
                terrainWarmupShown = false;
            }
            if (!terrainWarmupShown && minecraft.currentScreen == null) {
                terrainWarmupShown = true;
                minecraft.displayGuiScreen(new GuiTerrainWarmup());
            }
        }

        private static KeyMigration key(String description, int oldCode, KeyModifier oldModifier,
                                        int newCode, KeyModifier newModifier) {
            return new KeyMigration(description, oldCode, oldModifier, newCode, newModifier);
        }

        private static final class KeyMigration {
            private final String description;
            private final int oldCode;
            private final KeyModifier oldModifier;
            private final int newCode;
            private final KeyModifier newModifier;

            private KeyMigration(String description, int oldCode, KeyModifier oldModifier,
                                 int newCode, KeyModifier newModifier) {
                this.description = description;
                this.oldCode = oldCode;
                this.oldModifier = oldModifier;
                this.newCode = newCode;
                this.newModifier = newModifier;
            }

            private boolean matches(KeyBinding binding) {
                return description.equals(binding.getKeyDescription())
                    && oldCode == binding.getKeyCode()
                    && oldModifier == binding.getKeyModifier();
            }
        }
    }

    // Forge 1.12's Java language adapter constructs the @Mod class reflectively.
    // This constructor must therefore be public (Class#newInstance cannot invoke
    // the private utility-style constructor previously used here).
    public IndustrialCivilizationCore() {}
}
