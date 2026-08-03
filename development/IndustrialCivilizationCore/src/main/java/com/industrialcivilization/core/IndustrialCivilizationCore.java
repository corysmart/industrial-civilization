package com.industrialcivilization.core;

import betterquesting.api.properties.NativeProps;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNone;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.handlers.SaveLoadHandler;
import betterquesting.storage.QuestSettings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
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
    public static final String VERSION = "0.2.0";
    /** Canonical pack conversion, matching IC2 Classic's RFPerEU setting. */
    public static final int FE_PER_EU = 8;
    public static final int GUI_INDUSTRIAL_MACHINE = 1;
    public static final int GUI_VEHICLE_STORAGE = 2;
    public static final int GUI_VEHICLE_CRAFTING = 3;
    public static boolean ENFORCE_SPACE_GATES = true;
    @Mod.Instance(MODID)
    public static IndustrialCivilizationCore INSTANCE;
    public static final String QUEST_HOME_IMAGE = MODID + ":textures/gui/quest_home_v2.png";
    public static final int QUEST_HOME_OFFSET_X = -128;
    public static final int QUEST_HOME_OFFSET_Y = 0;
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

    private static ItemIndustrialArtifact artifact(String id) {
        return new ItemIndustrialArtifact(id);
    }

    private static ItemIndustrialArtifact artifact(String id, boolean activatesMilestone) {
        return new ItemIndustrialArtifact(id, activatesMilestone);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        Configuration runtime = new Configuration(new java.io.File(
            event.getModConfigurationDirectory(), "industrialcivilization/runtime.cfg"));
        runtime.load();
        ENFORCE_SPACE_GATES = runtime.getBoolean("enforceSpaceResearchGates", "progression",
            true, "Return unauthorized players to Earth when entering the Moon or Mars.");
        if (runtime.hasChanged()) runtime.save();
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
        GameRegistry.registerWorldGenerator(new CivilizationWorldGenerator(), 50);
        MinecraftForge.TERRAIN_GEN_BUS.register(new VillageSuppressionHandler());
        MinecraftForge.TERRAIN_GEN_BUS.register(new MoonPurityHandler());
        FactionNetwork.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        AnalyzerPeripheralProvider.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new IndustrialGuiHandler());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandIndustrialStatus());
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
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(MATERIAL_PATTERN_RECORD);
            event.getRegistry().register(INDUSTRIAL_CREDIT);
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
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            migrateQuestHomeImage();
            event.player.sendMessage(new TextComponentTranslation(
                "message.industrialcivilization.quest_guide"));
        }

        private static void migrateQuestHomeImage() {
            String current = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE);
            int currentOffsetX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X);
            int currentOffsetY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y);
            boolean changed = false;
            if (!QUEST_HOME_IMAGE.equals(current)) {
                QuestSettings.INSTANCE.setProperty(NativeProps.HOME_IMAGE, QUEST_HOME_IMAGE);
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
                LOGGER.info("Migrated Better Questing home layout from '{}',({}, {}) to '{}',({}, {})",
                    current, currentOffsetX, currentOffsetY,
                    QUEST_HOME_IMAGE, QUEST_HOME_OFFSET_X, QUEST_HOME_OFFSET_Y);
            }
        }

        @SubscribeEvent
        public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            ProgressionState.increment(event.player, "dimension_transfers", 1);
            String destination = event.player.world.provider.getDimensionType().getName().toLowerCase();
            if (destination.contains("moon")) {
                if (ENFORCE_SPACE_GATES && !ProgressionState.has(event.player, "orbital_research_archive")) {
                    denyDestination(event, "message.industrialcivilization.gate.moon");
                    return;
                }
                ProgressionState.record(event.player, "moon_visited");
                RuntimeAdvancements.grant(event.player, "moon_access");
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
            if (ProgressionState.has(event.player, "artificial_industrial_intelligence_core")
                    && ProgressionState.has(event.player, "lite_matter_complete")) {
                ProgressionState.record(event.player, "ai_age");
            }
            FactionSystem.updatePlaystyleReputation(event.player);
            ProgressionState.increment(event.player, "active_ticks", 20);
        }

        @SubscribeEvent
        public static void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
            if (!event.player.world.isRemote) ProgressionState.increment(event.player, "manual_crafts", 1);
        }

        @SubscribeEvent
        public static void blockBroken(BlockEvent.BreakEvent event) {
            if (event.getPlayer() != null && !event.getWorld().isRemote) {
                ProgressionState.increment(event.getPlayer(), "blocks_mined_manual", 1);
            }
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

        @SubscribeEvent
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
            for (ItemIndustrialArtifact artifact : ARTIFACTS) {
                ModelLoader.setCustomModelResourceLocation(
                    artifact, 0,
                    new ModelResourceLocation(artifact.getRegistryName(), "inventory"));
            }
        }

        /** Make the pause menu use the pack's authoritative quest system. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void renameAdvancementsButton(GuiScreenEvent.InitGuiEvent.Post event) {
            if (!(event.getGui() instanceof GuiIngameMenu)) return;
            event.getButtonList().stream()
                .filter(button -> button.id == 5)
                .findFirst()
                .ifPresent(button -> button.displayString =
                    I18n.format("gui.industrialcivilization.quest_guide"));
            event.getButtonList().stream()
                .filter(button -> button.id == 6)
                .findFirst()
                .ifPresent(button -> button.displayString =
                    I18n.format("gui.industrialcivilization.factions"));
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void openQuestGuideFromPauseMenu(GuiScreenEvent.ActionPerformedEvent.Pre event) {
            if (!(event.getGui() instanceof GuiIngameMenu)) return;
            if (event.getButton().id == 6) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(new GuiFactionDirectory(event.getGui()));
                return;
            }
            if (event.getButton().id != 5) return;
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(ThemeRegistry.INSTANCE.getGui(
                PresetGUIs.HOME, new GArgsNone(event.getGui())));
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
