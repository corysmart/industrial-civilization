package com.industrialcivilization.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.settings.KeyModifier;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = IndustrialCivilizationCore.MODID,
    name = IndustrialCivilizationCore.NAME,
    version = IndustrialCivilizationCore.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:forge@[14.23.5.2860,);required-after:computercraft;required-after:galacticraftcore;required-after:galacticraftplanets"
)
public final class IndustrialCivilizationCore {
    public static final String MODID = "industrialcivilizationcore";
    public static final String NAME = "Industrial Civilization Core";
    public static final String VERSION = "0.1.0";
    public static Logger LOGGER;

    public static final Block MOLECULAR_ANALYZER = new BlockMolecularAnalyzer()
        .setRegistryName(MODID, "molecular_analyzer")
        .setUnlocalizedName(MODID + ".molecular_analyzer")
        .setHardness(5.0F)
        .setResistance(15.0F);
    public static final Item MATERIAL_PATTERN_RECORD = new ItemPatternRecord()
        .setRegistryName(MODID, "material_pattern_record")
        .setUnlocalizedName(MODID + ".material_pattern_record")
        .setMaxStackSize(1);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        GameRegistry.registerTileEntity(TileMolecularAnalyzer.class,
            new ResourceLocation(MODID, "molecular_analyzer"));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        AnalyzerPeripheralProvider.register();
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static final class Registration {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(MOLECULAR_ANALYZER);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(MATERIAL_PATTERN_RECORD);
            event.getRegistry().register(new ItemBlock(MOLECULAR_ANALYZER)
                .setRegistryName(MOLECULAR_ANALYZER.getRegistryName()));
        }

        @SubscribeEvent
        public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            event.player.sendMessage(new TextComponentTranslation(
                "message.industrialcivilization.quest_guide"));
        }

        @SubscribeEvent
        public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            String destination = event.player.world.provider.getDimensionType().getName().toLowerCase();
            if (destination.contains("moon")) {
                event.player.getEntityData().setBoolean("industrialcivilization.moon_visited", true);
                LOGGER.info("progression moon_visited player={}", event.player.getName());
            } else if (destination.contains("mars")) {
                boolean moonFirst = event.player.getEntityData().getBoolean("industrialcivilization.moon_visited");
                event.player.getEntityData().setBoolean("industrialcivilization.mars_visited", true);
                event.player.getEntityData().setBoolean("industrialcivilization.moon_before_mars", moonFirst);
                LOGGER.info("progression mars_visited player={} moon_before_mars={}", event.player.getName(), moonFirst);
                if (!moonFirst) {
                    event.player.sendStatusMessage(new TextComponentTranslation(
                        "message.industrialcivilization.sequence_violation"), false);
                }
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
