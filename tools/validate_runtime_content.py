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
        check(image.size == (16, 16), f"texture is native 16x16: {kind}/{asset_id}")
        check(image.mode == "RGBA", f"texture has alpha channel: {kind}/{asset_id}")
        digest = hash(image.tobytes())
        check(digest not in pixel_hashes, f"texture is visually distinct: {kind}/{asset_id}")
        pixel_hashes.add(digest)
        check(model.is_file(), f"model exists: {kind}/{asset_id}")
        json.loads(model.read_text())

for block_id in sorted(block_ids):
    state = ASSETS / "blockstates" / f"{block_id}.json"
    item_model = ASSETS / "models/item" / f"{block_id}.json"
    check(state.is_file(), f"blockstate exists: {block_id}")
    check(item_model.is_file(), f"block inventory model exists: {block_id}")
    json.loads(state.read_text()); json.loads(item_model.read_text())

gui = Image.open(ASSETS / "textures/gui/industrial_machine.png")
check(gui.size == (256, 256), "machine GUI atlas is 256x256")
check(gui.getpixel((0, 0))[3] == 255, "machine GUI window is opaque")
check(gui.getpixel((200, 200))[3] == 0, "unused GUI atlas area is transparent")
check(gui.getpixel((176, 0))[3] == 255, "energy overlay strip exists")
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

recipe_source = (JAVA / "MachineRecipe.java").read_text()
recipe_ids = re.findall(r'new MachineRecipe\("([^"]+)"', recipe_source)
expected_recipes = {
    "record_orbital_data", "record_lunar_data", "record_martian_data",
    "orbital_archive", "lunar_archive", "mars_authorization", "martian_autonomy",
    "precision_frame", "blank_data_cartridge", "control_processor",
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
print("Validated sprites, GUI atlas, models, IC2/FE energy contracts, ComputerCraft API, factory encounter, gates, and recipes.")
