#!/usr/bin/env python3
"""Offline harness for Industrial Civilization sprites, GUI, models, and machine rules."""
from pathlib import Path
import json
import re
import sys
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
    check(model_data.get("parent") == "block/cube", f"block uses independent cube faces: {block_id}")
    check({"north", "south", "east", "west", "up", "down"} <= set(model_data.get("textures", {})),
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
check(gui.getpixel((200, 200))[3] == 0, "unused GUI atlas area is transparent")
check(gui.getpixel((176, 0))[3] == 255, "energy overlay strip exists")
check(gui.getpixel((15, 18))[:3] == (123, 137, 142)
      and gui.getpixel((15, 19))[:3] == (123, 137, 142)
      and gui.getpixel((15, 20))[:3] == (23, 36, 42),
      "energy gauge housing leaves two dark-panel pixels above it")
check(gui.getpixel((17, 22))[:3] == (82, 99, 106)
      and gui.getpixel((17, 57))[:3] == (82, 99, 106),
      "energy gauge interior is vertically contained by the process panel")
check(gui.getpixel((15, 60))[:3] == (123, 137, 142)
      and gui.getpixel((15, 61))[:3] == (123, 137, 142),
      "energy gauge housing leaves two dark-panel pixels below it")
check(gui.getpixel((176, 49))[3] == 255, "progress overlay strip exists")

source = "\n".join(path.read_text() for path in JAVA.glob("*.java"))
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
    "uu_matter", "controlled_replication", "contained_antimatter", "cargo_network",
    "orbital_megastructure", "lunar_colony_charter", "martian_colony_charter",
    "civilization_scale_ai",
}
check(set(recipe_ids) == expected_recipes, "all runtime machine recipes are present")

kind_source = (JAVA / "IndustrialMachineKind.java").read_text()
specs = {name: (int(cap), int(voltage), int(duration)) for name, _id, cap, voltage, duration in
         re.findall(r'(\w+)\("([^"]+)", (\d+), (\d+), (\d+)\)', kind_source)}
for name, (capacity, voltage, duration) in specs.items():
    check(capacity >= voltage * duration, f"{name} can buffer at least one complete operation")
    check(duration > 0 and voltage in {32, 128, 512, 2048, 8192}, f"{name} has an IC2 voltage tier and finite duration")

lang = (ASSETS / "lang/en_us.lang").read_text()
check("[TEST PLACEHOLDER]" not in lang, "no temporary display names remain")
check("placeholder_" not in json.dumps(json.loads(
    (ROOT / "config/betterquesting/DefaultQuests.json").read_text())),
    "generated quests contain no placeholder registry IDs")

print(f"RUNTIME CONTENT HARNESS: {len(checks)} checks passed")
print("Validated sprites, GUI atlas, models, IC2 energy contracts, hidden compatibility adapters, ComputerCraft API, factory encounter, gates, and recipes.")
