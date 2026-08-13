#!/usr/bin/env python3
"""Port every sensible displayed foreign advancement into the IC campaign tab.

Original advancements remain loaded without a display so their native triggers,
recipes, commands, and save compatibility survive. Mirrored entries retain the
same criteria and requirements while their parent chains are anchored to the
era in which the content becomes reachable in this pack.
"""
from pathlib import Path
import json
import re
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets"
VANILLA = Path.home() / "Library/Application Support/technic/cache/minecraft_1.12.2.jar"
SOURCES = [VANILLA] + sorted((ROOT / "mods").glob("*.jar"))
OWN = "industrialcivilizationcore"

ANCHORS = {
    "minecraft": "industrialcivilizationcore:root",
    "railcraft": "industrialcivilizationcore:early_autocrafting",
    "buildcraftcore": "industrialcivilizationcore:early_autocrafting",
    "buildcraftbuilders": "industrialcivilizationcore:early_autocrafting",
    "buildcraftenergy": "industrialcivilizationcore:early_autocrafting",
    "buildcraftfactory": "industrialcivilizationcore:early_autocrafting",
    "buildcraftsilicon": "industrialcivilizationcore:early_autocrafting",
    "buildcrafttransport": "industrialcivilizationcore:early_autocrafting",
    "ic2": "industrialcivilizationcore:first_ic2_generator",
    "galacticraftcore": "industrialcivilizationcore:monitored_nuclear_power",
    "appliedenergistics2": "industrialcivilizationcore:ai_age_entry",
}

# Content made impossible or redundant by deliberate pack design.
EXCLUDED = {
    "minecraft:adventure/kill_a_mob",       # no vanilla monster ecology
    "minecraft:adventure/kill_all_mobs",
    "minecraft:adventure/sniper_duel",
    "minecraft:story/cure_zombie_villager",
    "minecraft:story/follow_ender_eye",
    "minecraft:story/enter_the_end",
    "minecraft:story/enchant_item",
    # The pack has no native monster-food ecology and no End chorus fruit, so
    # the vanilla all-food challenge cannot be completed honestly.
    "minecraft:husbandry/balanced_diet",
    "ic2:basic/killcreeperchainsaw",
    "ic2:basic/killdragonmininglaser",
    "ic2:basic/killwitherwithnuke",
    "ic2:basic/diefromownnuke",
    "ic2:basic/starvewithqhelmet",
    "galacticraftcore:galacticraft/basic_solar",    # replaced by Environmental Solar Array
    "galacticraftcore:galacticraft/advanced_solar", # replaced by Tracking Solar Array
    "galacticraftcore:galacticraft/moon_dungeon",   # dungeons intentionally removed
    "galacticraftcore:galacticraft/key_t1",
    "galacticraftcore:galacticraft/moon_buggy_schematic",
    "galacticraftcore:galacticraft/moon_buggy",
    "minecraft:nether/all_effects",
    "minecraft:nether/all_potions",
    "minecraft:nether/brew_potion",
    "minecraft:nether/get_wither_skull",
    "minecraft:nether/obtain_blaze_rod",
    "minecraft:nether/return_to_sender",
    "minecraft:nether/summon_wither",
    "minecraft:nether/uneasy_alliance",
    # Railcraft's magic module remains installable for compatibility, but raw
    # Firestone is not part of the technological campaign.
    "railcraft:tracks/firestone",
}
EXCLUDED_PREFIXES = ("minecraft:end/",)
EXCLUDED_LOWER = {identifier.lower() for identifier in EXCLUDED}


def normalize_parent(parent, namespace):
    if not parent:
        return ""
    return parent if ":" in parent else f"{namespace}:{parent}"


def port_path(identifier):
    namespace, path = identifier.split(":", 1)
    safe = re.sub(r"[^a-z0-9_./-]", "_", path.lower())
    return f"ported/{namespace}/{safe}"


def excluded(identifier):
    lower = identifier.lower()
    return lower in EXCLUDED_LOWER or any(lower.startswith(prefix) for prefix in EXCLUDED_PREFIXES)


def load_displayed():
    found = {}
    for jar in SOURCES:
        if not jar.is_file():
            continue
        try:
            archive = zipfile.ZipFile(jar)
        except zipfile.BadZipFile:
            continue
        for name in archive.namelist():
            if not (name.startswith("assets/") and "/advancements/" in name
                    and name.endswith(".json")):
                continue
            namespace = name.split("/")[1]
            if namespace == OWN or namespace not in ANCHORS:
                continue
            try:
                data = json.loads(archive.read(name))
            except (ValueError, KeyError):
                continue
            if "display" not in data:
                continue
            path = name.split("/advancements/", 1)[1][:-5]
            identifier = f"{namespace}:{path}"
            # A first-party override can replace a native definition but must
            # not be treated as an additional foreign source.
            found.setdefault(identifier, data)
    return found


def apply_pack_replacements(displayed):
    replacements = {
        "minecraft:adventure/totem_of_undying": {
            "display": {
                "icon": {"item": "industrialcivilizationcore:emergency_continuity_core"},
                "title": {"text": "Postmortal Continuity"},
                "description": {"text": "Survive a lethal event with an AI Emergency Continuity Core"},
                "frame": "goal",
            },
            "parent": "minecraft:adventure/root",
            "criteria": {"ai_continuity": {"trigger": "minecraft:impossible"}},
        },
        "ic2:basic/terraformEndCultivation": {
            "display": {
                "icon": {"item": "ic2:itemtfbpbase", "data": 20},
                "title": {"text": "Martian Paradise"},
                "description": {"text": "Insert a Cultivation TFBP into an IC2 Terraformer on Mars"},
                "frame": "challenge",
            },
            "parent": "ic2:basic/buildterraformer",
            "criteria": {"mars_cultivation": {"trigger": "minecraft:impossible"}},
        },
    }
    for identifier, replacement in replacements.items():
        if identifier in displayed:
            displayed[identifier] = replacement


def nearest_parent(identifier, data, displayed, included):
    namespace = identifier.split(":", 1)[0]
    parent = normalize_parent(data.get("parent", ""), namespace)
    visited = set()
    while parent and parent not in visited:
        visited.add(parent)
        if parent in included:
            return f"{OWN}:{port_path(parent)}"
        ancestor = displayed.get(parent)
        if ancestor is None:
            break
        parent = normalize_parent(ancestor.get("parent", ""), parent.split(":", 1)[0])
    return ANCHORS[namespace]


def main():
    displayed = load_displayed()
    apply_pack_replacements(displayed)
    included = {identifier for identifier in displayed if not excluded(identifier)}

    # Regenerate only the managed mirror directory and foreign display stubs.
    mirror_root = OUT / OWN / "advancements/ported"
    if mirror_root.exists():
        shutil.rmtree(mirror_root)

    for identifier, data in sorted(displayed.items()):
        namespace, path = identifier.split(":", 1)

        # Hide every original page while retaining its full nonvisual contract.
        hidden = dict(data)
        hidden.pop("display", None)
        hidden_path = OUT / namespace / "advancements" / f"{path}.json"
        hidden_path.parent.mkdir(parents=True, exist_ok=True)
        hidden_path.write_text(json.dumps(hidden, indent=2) + "\n")

        if identifier not in included:
            continue
        mirror = dict(data)
        mirror["parent"] = nearest_parent(identifier, data, displayed, included)
        # A visible mirror is awarded by UnifiedAdvancementSystem only after
        # both the native source and its campaign parent are complete. Native
        # criteria here would bypass that era ordering.
        mirror["criteria"] = {"ported_source": {"trigger": "minecraft:impossible"}}
        mirror.pop("requirements", None)
        # Foreign reward functions are intentionally not executed twice.
        mirror.pop("rewards", None)
        target = OUT / OWN / "advancements" / f"{port_path(identifier)}.json"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(mirror, indent=2) + "\n")

    manifest = {
        "schema": 1,
        "displayed_foreign": len(displayed),
        "ported": len(included),
        "excluded": sorted(set(displayed) - included),
        "anchors": ANCHORS,
    }
    manifest_path = ROOT / "progression/unified-advancements.json"
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n")
    java_map = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/ported_advancements.json"
    java_map.write_text(json.dumps({
        identifier: f"{OWN}:{port_path(identifier)}" for identifier in sorted(included)
    }, indent=2) + "\n")
    print(f"UNIFIED ADVANCEMENTS: {len(included)} ported; "
          f"{len(displayed) - len(included)} removed/replaced; {len(displayed)} foreign displays hidden")


if __name__ == "__main__":
    main()
