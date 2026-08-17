#!/usr/bin/env python3
"""Fail when canonical materials or hostile-drop alternatives regress."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
techguns = (ROOT / "config/Techguns.cfg").read_text()
icbm = (ROOT / "groovy/postInit/industrial_icbm.groovy").read_text()
crafttweaker = (ROOT / "scripts/tekkit2.zs").read_text()
late_recipes = (ROOT / "groovy/postInit/dimension_material_unification.groovy").read_text()
ecology = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/PlanetaryEcologySystem.java").read_text()
rules = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GameplayRules.java").read_text()
agriculture = json.loads((ROOT / "progression/side-paths/03-industrial-agriculture.json").read_text())

errors = []
for option in ("addBronzeIngot", "addCopperIngot", "addCopperNugget", "addLeadIngot",
               "addLeadNugget", "addSteelIngot", "addSteelNugget", "addTinIngot",
               "doOreGenCopper", "doOreGenLead", "doOreGenTin", "doOreGenUranium"):
    if f"B:{option}=false" not in techguns:
        errors.append(f"Techguns duplicate provider enabled: {option}")
if "I:SpawnWeightBandit=0" not in techguns:
    errors.append("Techguns bandits overlap the staged Earth robber role")

for token in ("icbmclassic:circuit", "icbmclassic:battery", "icbmclassic:wire",
              "icbmclassic:ingot", "icbmclassic:plate"):
    if token not in icbm:
        errors.append(f"ICBM duplicate family not normalized: {token}")

for family in ("ingotLead", "itemRubber"):
    if family not in crafttweaker:
        errors.append(f"Inherited canonicalization missing: {family}")

recipe_contracts = {
    "pack-wide Slimeball replacement": "recipes.replaceAllOccurences(<minecraft:slime_ball>, <ic2:itemharz>, null)",
    "pack-wide Ghast Tear replacement": "recipes.replaceAllOccurences(<minecraft:ghast_tear>, <ic2:itemmisc:257>, null)",
    "pack-wide Nether Quartz replacement": "recipes.replaceAllOccurences(<minecraft:quartz>, <appliedenergistics2:material:0>, null)",
    "pack-wide Netherrack replacement": "recipes.replaceAllOccurences(<minecraft:netherrack>, <minecraft:cobblestone>, null)",
    "pack-wide Soul Sand replacement": "recipes.replaceAllOccurences(<minecraft:soul_sand>, <minecraft:sand>, null)",
    "pack-wide Nether Wart replacement": "recipes.replaceAllOccurences(<minecraft:nether_wart>, <ic2:itemmisc:159>, null)",
    "pack-wide Nether Brick replacement": "recipes.replaceAllOccurences(<minecraft:netherbrick>, <minecraft:brick>, null)",
    "pack-wide Glowstone replacement": "recipes.replaceAllOccurences(<minecraft:glowstone_dust>, <minecraft:redstone>, null)",
    "pack-wide Blaze Rod replacement": "recipes.replaceAllOccurences(<minecraft:blaze_rod>, <ic2:itemmisc:53>, null)",
    "pack-wide Blaze Powder replacement": "recipes.replaceAllOccurences(<minecraft:blaze_powder>, <ore:dustCoal>, null)",
    "pack-wide Magma Cream replacement": "recipes.replaceAllOccurences(<minecraft:magma_cream>, <ic2:itemharz>, null)",
    "pack-wide Nether Star replacement": "recipes.replaceAllOccurences(<minecraft:nether_star>, <industrialcivilizationcore:lunar_quantum_component>, null)",
    "pack-wide End Stone replacement": "recipes.replaceAllOccurences(<minecraft:end_stone>, <minecraft:obsidian>, null)",
    "pack-wide Chorus Fruit replacement": "recipes.replaceAllOccurences(<minecraft:chorus_fruit>, <minecraft:ender_pearl>, null)",
    "pack-wide Popped Chorus replacement": "recipes.replaceAllOccurences(<minecraft:chorus_fruit_popped>, <ic2:itemmisc:257>, null)",
    "pack-wide Purpur replacement": "recipes.replaceAllOccurences(<minecraft:purpur_block>, <minecraft:stonebrick>, null)",
    "pack-wide End Crystal replacement": "recipes.replaceAllOccurences(<minecraft:end_crystal>, <ic2:itembatcrystal>, null)",
    "canonical Lead output": 'recipes.addShapeless("industrial_civilization_lead", <minecraft:lead> * 2',
    "canonical Sticky Piston output": 'recipes.addShapeless("industrial_civilization_sticky_piston", <minecraft:sticky_piston>',
    "renewable industrial gunpowder": 'recipes.addShapeless("industrial_civilization_gunpowder", <minecraft:gunpowder> * 3',
}
for label, token in recipe_contracts.items():
    if token not in crafttweaker:
        errors.append(f"Missing recipe contract: {label}")
for token in ("computronics:colorful_lamp", "computronics:tape:4", "computronics:tape:8",
              "industrialcivilizationcore:lunar_quantum_component", "minecraft:redstone"):
    if token not in late_recipes:
        errors.append(f"Late dimension-material normalization missing: {token}")

if "supplementalLivestockBone" not in ecology or "new ItemStack(Items.BONE)" not in ecology:
    errors.append("Renewable livestock bone event is missing")
if "supplementalBoneDrop" not in rules or "roll == 0" not in rules:
    errors.append("Renewable bone probability rule is missing")

expected_agriculture = {
    "crop_engineering": "ic2:itemcrop",
    "breed_hemp": "ic2:itemmisc:159",
    "renewable_string": "minecraft:string",
    "controlled_livestock": "minecraft:lead",
}
actual_agriculture = {entry["id"]: entry.get("required_item")
                      for entry in agriculture.get("milestones", [])}
if actual_agriculture != expected_agriculture:
    errors.append(f"Industrial Agriculture evidence drifted: {actual_agriculture}")
for entry in agriculture.get("milestones", []):
    if entry.get("icon") != entry.get("required_item"):
        errors.append(f"Industrial Agriculture icon is not its real evidence item: {entry['id']}")

# Required quest data may not directly depend on inaccessible fantasy drops.
required_sources = list((ROOT / "progression/chapters").glob("*.json")) \
    + list((ROOT / "progression/side-paths").glob("*.json"))
for source in required_sources:
    data = json.loads(source.read_text())
    for entry in data.get("milestones", []):
        if entry.get("required_item") in {
            "minecraft:slime_ball", "minecraft:ghast_tear", "minecraft:quartz",
            "minecraft:netherrack", "minecraft:soul_sand", "minecraft:nether_wart",
            "minecraft:netherbrick", "minecraft:glowstone_dust", "minecraft:blaze_rod",
            "minecraft:blaze_powder", "minecraft:magma_cream", "minecraft:nether_star",
            "minecraft:end_stone", "minecraft:chorus_fruit", "minecraft:chorus_fruit_popped",
            "minecraft:purpur_block", "minecraft:purpur_pillar", "minecraft:shulker_shell",
            "minecraft:dragon_breath", "minecraft:dragon_egg", "minecraft:end_crystal",
        }:
            errors.append(f"Inaccessible required item in {source.name}: {entry['id']}")

if errors:
    raise SystemExit("ITEM UNIFICATION AUDIT FAILED\n- " + "\n- ".join(errors))
print("ITEM UNIFICATION AUDIT: canonical materials, hostile-drop alternatives, and agriculture evidence enforced")
