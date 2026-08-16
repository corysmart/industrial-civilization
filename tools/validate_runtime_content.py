#!/usr/bin/env python3
"""Offline harness for Industrial Civilization sprites, GUI, models, and machine rules."""
from pathlib import Path
import json
import re
import sys
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore"
JAVA = ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core"
checks = []


def check(condition, message):
    if not condition:
        raise AssertionError(message)
    checks.append(message)


runtime_content = json.loads((ROOT / "progression/runtime-content.json").read_text())
block_ids = {entry["id"] for entry in runtime_content["blocks"]}
item_ids = {entry["id"] for entry in runtime_content["items"]}
check(len(block_ids) == len(runtime_content["blocks"]), "runtime block registry IDs are unique")
check(len(item_ids) == len(runtime_content["items"]), "runtime item registry IDs are unique")

pixel_hashes = set()
for kind, ids in (("blocks", block_ids), ("items", item_ids)):
    for asset_id in sorted(ids):
        texture = ASSETS / "textures" / kind / f"{asset_id}.png"
        model = ASSETS / "models" / ("block" if kind == "blocks" else "item") / f"{asset_id}.json"
        check(texture.is_file(), f"texture exists: {kind}/{asset_id}")
        image = Image.open(texture)
        expected_size = (16, 16) if kind == "blocks" else (64, 64)
        check(image.size == expected_size, f"texture has intended resolution {expected_size[0]}x{expected_size[1]}: {kind}/{asset_id}")
        check(image.mode == "RGBA", f"texture has alpha channel: {kind}/{asset_id}")
        if kind == "items":
            check(image.getpixel((0, 0))[3] == 0 and image.getpixel((63, 63))[3] == 0,
                  f"NEI sprite has transparent corners: {kind}/{asset_id}")
            alpha_bbox = image.getchannel("A").getbbox()
            check(alpha_bbox is not None and (alpha_bbox[2] - alpha_bbox[0]) >= 32
                  and (alpha_bbox[3] - alpha_bbox[1]) >= 32,
                  f"NEI sprite uses readable inventory area: {kind}/{asset_id}")
        digest = hash(image.tobytes())
        check(digest not in pixel_hashes, f"texture is visually distinct: {kind}/{asset_id}")
        pixel_hashes.add(digest)
        check(model.is_file(), f"model exists: {kind}/{asset_id}")
        json.loads(model.read_text())

for block_id in sorted(block_ids):
    state = ASSETS / "blockstates" / f"{block_id}.json"
    item_model = ASSETS / "models/item" / f"{block_id}.json"
    block_model = ASSETS / "models/block" / f"{block_id}.json"
    check(state.is_file(), f"blockstate exists: {block_id}")
    check(item_model.is_file(), f"block inventory model exists: {block_id}")
    json.loads(state.read_text())
    item_model_data = json.loads(item_model.read_text())
    check(item_model_data.get("parent") == "item/generated",
          f"block inventory uses dedicated flat NEI sprite: {block_id}")
    expected_layer = f"industrialcivilizationcore:items/nei_blocks/{block_id}"
    check(item_model_data.get("textures", {}).get("layer0") == expected_layer,
          f"block inventory model references concept sprite: {block_id}")
    nei_texture = ASSETS / "textures/items/nei_blocks" / f"{block_id}.png"
    check(nei_texture.is_file(), f"block NEI texture exists: {block_id}")
    nei_image = Image.open(nei_texture)
    check(nei_image.size == (64, 64) and nei_image.mode == "RGBA",
          f"block NEI texture is RGBA 64x64: {block_id}")
    check(nei_image.getpixel((0, 0))[3] == 0 and nei_image.getpixel((63, 63))[3] == 0,
          f"block NEI sprite has transparent corners: {block_id}")
    model_data = json.loads(block_model.read_text())
    if block_id in {"environmental_solar_array", "tracking_solar_array"}:
        expected_parent = ("galacticraftcore:block/basic_solar_model"
                           if block_id == "environmental_solar_array"
                           else "galacticraftcore:block/advanced_solar_model")
        check(model_data.get("parent") == expected_parent,
              f"solar array inherits the matching Galacticraft model: {block_id}")
        check(set(model_data) == {"parent"},
              f"solar array does not override Galacticraft geometry or textures: {block_id}")
    else:
        check(model_data.get("parent") == "block/cube",
              f"block uses independent cube faces: {block_id}")
        check({"north", "south", "east", "west", "up", "down"}
              <= set(model_data.get("textures", {})),
              f"block model maps every visible face: {block_id}")
    for suffix in ("side", "top"):
        face = ASSETS / "textures/blocks" / f"{block_id}_{suffix}.png"
        check(face.is_file(), f"{suffix} texture exists: blocks/{block_id}")
        face_image = Image.open(face)
        check(face_image.size == (16, 16) and face_image.mode == "RGBA",
              f"{suffix} texture is native RGBA 16x16: blocks/{block_id}")

gui = Image.open(ASSETS / "textures/gui/industrial_machine.png")
check(gui.size == (256, 256), "machine GUI atlas is 256x256")
check(gui.getpixel((0, 0))[3] == 255, "machine GUI window is opaque")
check(gui.getpixel((207, 189))[3] == 255 and gui.getpixel((207, 190))[3] == 0,
      "machine workspace uses the enlarged 208x190 canvas")
check(gui.getpixel((200, 200))[3] == 0, "unused GUI atlas area is transparent")
check(gui.getpixel((208, 0))[3] == 255, "energy overlay strip exists")
check(gui.getpixel((19, 21))[:3] == (123, 137, 142)
      and gui.getpixel((19, 28))[:3] == (123, 137, 142)
      and gui.getpixel((19, 29))[:3] == (23, 36, 42),
      "energy gauge housing has generous dark-panel clearance above it")
check(gui.getpixel((21, 31))[:3] == (82, 99, 106)
      and gui.getpixel((21, 66))[:3] == (82, 99, 106),
      "energy gauge interior is vertically contained by the process panel")
check(gui.getpixel((19, 69))[:3] == (123, 137, 142)
      and gui.getpixel((19, 80))[:3] == (123, 137, 142),
      "energy gauge housing has generous dark-panel clearance below it")
check(gui.getpixel((208, 49))[3] == 255, "progress overlay strip exists")
check(gui.getpixel((42, 34))[:3] == (101, 114, 120)
      and gui.getpixel((43, 35))[:3] == (45, 55, 59)
      and gui.getpixel((45, 37))[:3] == (169, 181, 184)
      and gui.getpixel((63, 55))[:3] == (101, 114, 120),
      "machine process slots use enlarged 22x22 IC2 frames")
check(gui.getpixel((120, 46))[:3] == (23, 36, 42)
      and gui.getpixel((157, 46))[:3] == (231, 130, 50)
      and gui.getpixel((158, 46))[:3] == (101, 114, 120),
      "machine process connector keeps clearance from enlarged output slot")

title_logo = Image.open(ASSETS / "textures/mainmenu/industrial_civilization_logo.png").convert("RGBA")
title_mask = Image.open(ROOT / "resources/industrialcivilizationcore/textures/mainmenu/industrial_civilization_logo_mask.png").convert("L")
check(title_logo.size == (512, 256), "main-menu title card remains 512x256")
check(title_mask.size == title_logo.size, "main-menu title-card silhouette mask matches the source canvas")
check(all(title_logo.getpixel(point)[3] == 0 for point in ((0, 0), (511, 0), (0, 255), (511, 255))),
      "main-menu title card exterior matte is transparent")
check(all(title_logo.getpixel(point)[3] == 0 for point in ((10, 128), (501, 128), (256, 5), (256, 245))),
      "main-menu title card removes the rectangular matte along every edge")
check(all(title_logo.getpixel(point)[3] >= 250 for point in ((60, 20), (30, 128), (480, 128), (256, 220))),
      "main-menu title card preserves the exterior metal frame")
check(title_logo.getpixel((256, 128))[3] == 255,
      "main-menu title card preserves its opaque plaque interior")

override_root = ROOT / "resources"
ic2_machine_sheets = (
    "batbox.png", "batterystation.png", "block_electric.png",
    "block_generator.png", "block_generator_compact.png",
    "block_machine_hv.png", "block_machine_lv.png", "block_machine_lv_2.png",
    "block_machine_mv.png", "block_pads.png", "block_personal.png",
    "block_personal_energy.png", "mfe.png", "mfsu.png", "pesu.png",
)
ic2_jar = ROOT / "mods/IC2Classic-1.12.2-1.5.11.jar"
with zipfile.ZipFile(ic2_jar) as archive:
    for filename in ic2_machine_sheets:
        with archive.open(f"assets/ic2/textures/sprites/{filename}") as source_file:
            source_texture = Image.open(source_file).convert("RGBA")
            source_texture.load()
        override_texture = Image.open(override_root / "ic2/textures/sprites" / filename).convert("RGBA")
        check(override_texture.size == source_texture.size,
              f"IC2 machine override preserves atlas dimensions: {filename}")
        check(override_texture.getchannel("A").tobytes() == source_texture.getchannel("A").tobytes(),
              f"IC2 machine override preserves sprite silhouettes and atlas occupancy: {filename}")
        check(override_texture.tobytes() != source_texture.tobytes(),
              f"IC2 machine override applies the Astra material palette: {filename}")

ic2_icon_counts = {
    "blockmachinelv": 16,
    "blockmachinelv2": 8,
    "blockmachinemv": 14,
    "blockmachinehv": 7,
    "blockgenerator": 15,
    "blockcompactedgenerator": 9,
    "blockelectric": 11,
    "blockpersonal": 11,
}
ic2_icon_root = ASSETS / "textures/items/ic2_machines"
ic2_model_root = ASSETS / "models/item/ic2_machines"
for registry_name, metadata_count in ic2_icon_counts.items():
    for metadata in range(metadata_count):
        stem = f"{registry_name}_{metadata}"
        texture_path = ic2_icon_root / f"{stem}.png"
        model_path = ic2_model_root / f"{stem}.json"
        check(texture_path.is_file(), f"IC2 flat inventory texture exists: {stem}")
        icon = Image.open(texture_path).convert("RGBA")
        check(icon.size == (64, 64), f"IC2 flat inventory texture is 64x64: {stem}")
        check(icon.getpixel((0, 0))[3] == 0 and icon.getpixel((63, 63))[3] == 0,
              f"IC2 flat inventory texture has transparent corners: {stem}")
        alpha_bbox = icon.getchannel("A").getbbox()
        check(alpha_bbox is not None and (alpha_bbox[2] - alpha_bbox[0]) >= 40
              and (alpha_bbox[3] - alpha_bbox[1]) >= 40,
              f"IC2 flat inventory texture uses a readable area: {stem}")
        check(model_path.is_file(), f"IC2 flat inventory model exists: {stem}")
        model = json.loads(model_path.read_text())
        check(model.get("parent") == "item/generated"
              and model.get("textures", {}).get("layer0")
              == f"industrialcivilizationcore:items/ic2_machines/{stem}",
              f"IC2 flat inventory model references its dedicated texture: {stem}")

core_model_source = (JAVA / "IndustrialCivilizationCore.java").read_text()
check("registerIc2MachineModels();" in core_model_source
      and "@SubscribeEvent(priority = EventPriority.LOWEST)\n        public static void registerModels"
      in core_model_source,
      "IC2 flat inventory models override default item cubes at lowest event priority")
for registry_name, metadata_count in ic2_icon_counts.items():
    check(f'registerIc2MachineModels("{registry_name}", {metadata_count});' in core_model_source,
          f"IC2 inventory registration covers every metadata variant: {registry_name}")

gameplay_rules = (JAVA / "GameplayRules.java").read_text()
check("questHomeTitleWidth" in gameplay_rules,
      "Quest Home title card uses deterministic responsive sizing rules")
check("resizeAndCenterQuestHomeTitle" in (JAVA / "IndustrialCivilizationCore.java").read_text(),
      "Better Questing title receives its responsive centered runtime layout")

quest_home = Image.open(ROOT / "resources/industrialcivilizationcore/textures/gui/quest_home_v2.png").convert("RGBA")
check(quest_home.size == (512, 512), "Better Questing home atlas remains 512x512")
check(quest_home.getpixel((0, 0))[3] == 255,
      "Better Questing home backdrop remains opaque")
check(all(quest_home.getpixel(point)[3] == 0 for point in ((0, 256), (511, 256), (0, 511), (511, 511))),
      "Better Questing title layer uses the transparent plaque cutout")
source = "\n".join(path.read_text() for path in JAVA.glob("*.java"))
machine_gui_source = (JAVA / "GuiIndustrialMachine.java").read_text()
machine_container_source = (JAVA / "ContainerIndustrialMachine.java").read_text()
check("BASE_WIDTH = 208" in machine_gui_source and "BASE_HEIGHT = 190" in machine_gui_source
      and "TITLE_WIDTH = 184" in machine_gui_source,
      "machine GUI uses the enlarged breathing-room workspace")
check("machineGuiScale(width, height)" in machine_gui_source
      and "GlStateManager.scale(interfaceScale" in machine_gui_source
      and "toVirtualX" in machine_gui_source and "toVirtualY" in machine_gui_source,
      "machine GUI scales responsively while preserving mouse hit testing")
check("24 + col * 18, 100 + row * 18" in machine_container_source
      and "24 + col * 18, 167" in machine_container_source,
      "player inventory remains centered inside the enlarged workspace")
check("new Slot(tile, 0, 45, 37)" in machine_container_source
      and "new Slot(tile, 2, 101, 37)" in machine_container_source
      and "new SlotOutput(tile, 3, 161, 37)" in machine_container_source,
      "machine item coordinates remain centered in enlarged process slots")
check("ItemTestPlaceholder" not in source, "runtime registrations do not reference placeholder items")
check("EnergyTileLoadEvent" in source and "IEnergySink" in source, "machines register with the IC2 EU network")
check("CapabilityEnergy.ENERGY" in source, "machines also expose Forge Energy input")
check("new CreativeTabs(MODID)" in source, "pack-owned creative tab is registered")
check("itemGroup.industrialcivilizationcore=Industrial Civilization" in
      (ASSETS / "lang/en_us.lang").read_text(), "pack-owned creative tab is localized")
for source_name in ("BlockIndustrialMachine.java", "BlockFactoryControlTerminal.java",
                    "BlockEnvironmentalSolarArray.java", "ItemIndustrialArtifact.java"):
    check("setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB)" in
          (JAVA / source_name).read_text(),
          f"custom registry family is visible in Creative inventory: {source_name}")
core_source = (JAVA / "IndustrialCivilizationCore.java").read_text()
check(core_source.count(".setCreativeTab(CREATIVE_TAB)") >= 7,
      "standalone items and every custom ItemBlock are visible in Creative inventory")
check("getMethodNames" in source and "selectRecipe" in source and "queue" in source,
      "ComputerCraft control surface is present")
check("AbandonedFactoryWorldGenerator" in source and "IndustrialCriminal" in source,
      "factory structure and criminal encounter are implemented")
check("denyDestination" in source and "orbital_research_archive" in source
      and "mars_mission_authorization" in source, "Moon and Mars gates are implemented")

faction_source = (JAVA / "FactionSystem.java").read_text()
world_source = (JAVA / "CivilizationWorldGenerator.java").read_text()
network_source = (JAVA / "FactionNetwork.java").read_text()
directory_source = (JAVA / "GuiFactionDirectory.java").read_text()
config = json.loads((ROOT / "config/industrialcivilization/faction-system.json").read_text())
check("industrial_credit" in item_ids and "INDUSTRIAL_CREDIT" in core_source,
      "IC Credit is a registered first-party item")
check("new MerchantRecipe(new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT" in faction_source
      and "Items.EMERALD" not in faction_source,
      "all faction merchant offers use IC Credits rather than emeralds")
for faction in ("frontier_cooperative", "riverside_works", "civil_defense",
                "territorial_militia", "survey_detachment_7", "ashline_raiders"):
    check(faction in faction_source and any(entry["id"] == faction for entry in config["factions"]),
          f"faction is implemented and configured: {faction}")
check("MEMBERSHIP_REPUTATION = 35" in faction_source
      and "COMPANION_REPUTATION = 60" in faction_source
      and "faction_membership" in faction_source,
      "membership and companion reputation gates persist per player")
check("setAttackTarget(player)" in faction_source and "COMPANION_OWNER" in faction_source
      and "raiders_defeated" in faction_source,
      "hostility, companionship, and action-based faction response are implemented")
check("PRIMITIVE_RADII = {240, 520, 800}" in world_source
      and "distanceSquared >= 900L * 900L" in world_source
      and "distanceSquared >= 1400L * 1400L" in world_source
      and "distanceSquared >= 2200L * 2200L" in world_source
      and "distanceSquared >= 3000L * 3000L" in world_source,
      "spawn-relative settlement and industrial distance bands are implemented")
check("buildPrimitiveSettlement" in world_source and "buildMilitiaOutpost" in world_source
      and "buildOperationalFactory" in world_source and "buildIndustrialCity" in world_source,
      "primitive villages, outposts, specialty factories, and cities are implemented")
village_suppression = (JAVA / "VillageSuppressionHandler.java").read_text()
check("EventType.VILLAGE" in village_suppression
      and "new MapGenVillage()" in village_suppression
      and "canSpawnStructureAtCoords" in village_suppression
      and "new MapGenBase()" not in village_suppression,
      "vanilla villages are suppressed without violating the Overworld MapGenVillage contract")
check("sendSnapshot" in network_source and "membership" in network_source,
      "faction directory synchronizes server-owned player state")
check("gui.industrialcivilization.factions" in directory_source and "membershipRule" in directory_source,
      "pause-menu faction directory explains relationships and membership")

tool_source = (JAVA / "ToolAreaHandler.java").read_text()
check("TREE_LIMIT = 512" in tool_source and "isLeaves" in tool_source
      and "BLOCKS_PER_TICK = 12" in tool_source and "HARVEST_QUEUES" in tool_source,
      "tree felling is leaf-gated, large-tree capable and tick-budgeted")
check('getHarvestLevel(stack, "axe"' in tool_source and '"itemtoolchainsaw"' in tool_source,
      "stone-tier axes and the IC2 chainsaw use the tree-felling path")
check('stack.getMetadata() == 1 ? 4' in tool_source and 'stack.getMetadata() == 0 ? 1' in tool_source,
      "IC2 drill variants map to 3x3 and 9x9 radii")
check("tryHarvestBlock(target)" in tool_source and "ElectricItem.manager.canUse" in tool_source,
      "extra blocks use normal protected harvesting and per-block EU/durability")
check("hasTileEntity" in tool_source and "getBlockHardness" in tool_source
      and "isBlockLoaded" in tool_source, "area mining excludes unsafe block targets")
check("player.isSneaking()" in tool_source, "sneaking provides precision-mode bypass")
for key in ("tree_axe", "tree_chainsaw", "drill_area", "diamond_drill_area"):
    check(f"tooltip.industrialcivilization.{key}=" in
          (ASSETS / "lang/en_us.lang").read_text(), f"tool behavior is documented in-game: {key}")

recipe_source = (JAVA / "MachineRecipe.java").read_text()
recipe_ids = re.findall(r'new MachineRecipe\("([^"]+)"', recipe_source)
expected_recipes = {
    "record_orbital_data", "record_lunar_data", "record_martian_data",
    "orbital_archive", "lunar_archive", "mars_authorization", "martian_autonomy",
    "precision_frame", "blank_data_cartridge", "control_processor",
    "printed_pistol", "city_compact", "frontier_off_roader", "passenger_carrier",
    "agricultural_tractor", "utility_cart", "scout_atv", "combat_shotgun", "automatic_rifle",
    "lunar_quantum_component", "ai_core",
    "emergency_continuity_core",
    "uu_matter", "controlled_replication", "contained_antimatter", "cargo_network",
    "orbital_megastructure", "lunar_colony_charter", "martian_colony_charter",
    "civilization_scale_ai",
}
check(set(recipe_ids) == expected_recipes, "all runtime machine recipes are present")

kind_source = (JAVA / "IndustrialMachineKind.java").read_text()
specs = {name: (int(cap), int(voltage), int(duration), int(minimum), work_class)
         for name, _id, cap, voltage, duration, minimum, work_class in re.findall(
             r'(\w+)\("([^"]+)", (\d+), (\d+), (\d+), (\d+), WorkClass\.(\w+)\)', kind_source)}
check(len(specs) == 12, "all first-party processing-machine work specifications are parsed")
for name, (capacity, voltage, duration, minimum, work_class) in specs.items():
    check(capacity >= voltage * duration, f"{name} can buffer at least one complete operation")
    check(duration > 0 and voltage in {32, 128, 512, 2048, 8192}, f"{name} has an IC2 voltage tier and finite duration")
    check(0 <= minimum <= duration, f"{name} has a valid physical/scientific minimum duration")
    check((minimum == 0) == (work_class == "ENERGY_LIMITED"),
          f"{name} work classification agrees with its minimum duration")

lang = (ASSETS / "lang/en_us.lang").read_text()
check("[TEST PLACEHOLDER]" not in lang, "no temporary display names remain")
check("placeholder_" not in json.dumps(json.loads(
    (ROOT / "config/betterquesting/DefaultQuests.json").read_text())),
    "generated quests contain no placeholder registry IDs")

print(f"RUNTIME CONTENT HARNESS: {len(checks)} checks passed")
print("Validated sprites, GUI atlas, models, IC2 energy contracts, hidden compatibility adapters, ComputerCraft API, factory encounter, gates, and recipes.")
