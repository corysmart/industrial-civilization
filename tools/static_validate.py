#!/usr/bin/env python3
"""Static-only acceptance checks. This script never imports or launches Minecraft."""
import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []
checks = []


def ok(condition, message):
    (checks if condition else errors).append(message)


def digest(path):
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


required_docs = [
    "BASELINE_AUDIT.md", "MAGIC_REMOVAL_AUDIT.md", "ADDED_MODS.md", "FIREARM_SELECTION.md",
    "FIREARM_PROGRESSION.md", "NPC_FRAMEWORK.md", "FACTIONS.md", "INTEGRATION_LAYER.md",
    "SENSOR_API.md", "PROGRESSION.md", "NUCLEAR_PROGRESSION.md", "MOON_PROGRESSION.md",
    "MARS_PROGRESSION.md", "LITE_MATTER_ENGINEERING.md", "AI_AGE_LOCK.md",
    "COMPATIBILITY_NOTES.md", "KNOWN_LIMITATIONS.md", "MANUAL_TEST_CHECKLIST.md", "HOT_RELOAD.md", "KEYBINDS.md", "NEXT_STEPS.md",
    "PROGRESSION_OVERVIEW.md", "PROGRESSION_GRAPH.md", "CRITICAL_PATH.md", "OPTIONAL_PATHS.md",
    "PACING_TARGETS.md", "PACING_PROFILES.md", "OPTIMIZATION_OPPORTUNITIES.md", "ANTI_GRIND_RULES.md",
    "PLACEHOLDER_SYSTEM.md", "QUEST_IMPLEMENTATION.md", "AUTOCRAFTING_PROGRESSION.md", "RESEARCH_PROGRESSION.md",
    "ORBITAL_STATION.md", "ORBITAL_RESEARCH.md", "LUNAR_PROGRAM.md", "LUNAR_RESEARCH.md",
    "QUANTUM_TECHNOLOGY.md", "MARS_PROGRAM.md", "MARTIAN_AUTONOMY.md", "POST_AI_ENDGAME.md",
    "TELEMETRY_SCHEMA.md", "MANUAL_QUEST_TEST_CHECKLIST.md",
]
for name in required_docs:
    ok((ROOT / "docs" / name).is_file(), f"required document {name}")
for name in ("baseline-mod-lock.json", "technical-baseline-mod-lock.json", "final-mod-lock.json"):
    path = ROOT / "manifest" / name
    try:
        json.loads(path.read_text())
        ok(True, f"valid manifest JSON {name}")
    except Exception as exc:
        ok(False, f"valid manifest JSON {name}: {exc}")

snapshot = ROOT / ".backups" / "pre-industrial-civilization"
ok((snapshot / "mods/ProjectE-1.12.2-PE1.4.8-14-technic.jar").is_file(), "rollback snapshot retains original ProjectE")

removed_paths = [
    ROOT / "mods/ProjectE-1.12.2-PE1.4.8-14-technic.jar",
    ROOT / "mods/ViewEMC-1.12.2-v8.jar",
    ROOT / "mods/ic2cuumatter-1.12.2-1.1.3.jar",
    ROOT / "config/ProjectE",
    ROOT / "cache/project-e-1.12.2-1.4.8-14-technic.zip",
    ROOT / "cache/view-emc-1.12.2-8.zip",
    ROOT / "cache/ic2cuumatter-1.12.2-1.1.3.zip",
    ROOT / "cache/z-tekkit2-configs-1.2.6.zip",
]
for path in removed_paths:
    ok(not path.exists(), f"removed active path {path.relative_to(ROOT)}")

for directory in (ROOT / "config", ROOT / "scripts", ROOT / "groovy", ROOT / "resources", ROOT / "examples"):
    if not directory.exists():
        continue
    for path in directory.rglob("*"):
        if path.is_file() and path.suffix.lower() in {".cfg", ".json", ".zs", ".lua", ".txt"}:
            try:
                content = path.read_text(errors="ignore").lower()
            except OSError:
                continue
            ok(not any(term in content for term in ("projecte", "viewemc", "ic2cuumatter")),
               f"no removed-system reference in {path.relative_to(ROOT)}")

for path in sorted((ROOT / "groovy").rglob("*.groovy")):
    source = path.read_text(errors="ignore")
    ok("getResourceDomain()" not in source and "getResourcePath()" not in source
       and ".resourceDomain" not in source and ".resourcePath" not in source,
       f"Groovy registry checks avoid unavailable MCP ResourceLocation methods in {path.relative_to(ROOT)}")

added_hashes = {
    "ICBM-classic-1.12.2-6.5.5.jar": "65533f69c3fe745385f51ff831c6c16adc8c963999093b44743961c8afe8d3d7",
    "appliedenergistics2-rv6-stable-7.jar": "e7f7fbf6caaf6206dbd958fb8f9185f874a405289e62ae3e97209b455a89fb1a",
    "techguns-1.12.2-2.0.2.0_pre3.2.jar": "154d3d794cfd74252f2cec979a6e72f5187bb9c21897ed4b42f45771a0e558f7",
    "CustomNPCs_1.12.2-(05Jul20).jar": "2759356b95ffb6a88c14997ff508dfe30f66abaa8e2d8029e18e02dcb57bc8cc",
    "BetterQuesting-3.5.329.jar": "f7803b91d54b98eb54b3e2f12143866e68a46e3f963d51997d1a1195b8469ba7",
    "StandardExpansion-3.4.173.jar": "405211aef15d442f857f92c4f755f051396283d6e6fc2de818f4f693091e89f9",
    "groovyscript-1.4.3.jar": "07617b7ce9170a857199bd61d730a0db3af2685cf83d31734a2d4b628fda7533",
}
for name, expected in added_hashes.items():
    path = ROOT / "mods" / name
    ok(path.is_file() and digest(path) == expected, f"pinned SHA-256 {name}")

# Verify the concrete classes and item models relied on by the integration layer.
# Galacticraft's Technic build contains both Core and Planets in one archive even
# though its legacy mcmod.info does not enumerate both logical mod IDs.
archive_requirements = {
    "Galacticraft-1.12.2-4.0.7-technic-ic2c.jar": (
        "micdoodle8/mods/galacticraft/core/GalacticraftCore.class",
        "micdoodle8/mods/galacticraft/planets/GalacticraftPlanets.class",
        "assets/galacticraftcore/models/item/meteoric_iron_raw.json",
        "assets/galacticraftplanets/models/item/ingot_desh.json",
        "assets/galacticraftplanets/models/item/rocket_t2.json",
    ),
    "CC-Tweaked-1.12.2-1.89.2.jar": (
        "dan200/computercraft/api/ComputerCraftAPI.class",
        "assets/computercraft/models/item/advanced_computer.json",
    ),
    "BetterQuesting-3.5.329.jar": ("betterquesting/core/BetterQuesting.class",),
    "HadEnoughItems_1.12.2-4.27.3.jar": ("mezz/jei/api/IModPlugin.class",),
    "techguns-1.12.2-2.0.2.0_pre3.2.jar": (
        "assets/techguns/models/item/pistol.json",
        "assets/techguns/models/item/combatshotgun.json",
        "assets/techguns/models/item/m4.json",
        "assets/techguns/models/item/pistolmagazine.json",
        "assets/techguns/models/item/shotgunrounds.json",
        "assets/techguns/models/item/assaultriflemagazine.json",
    ),
    "groovyscript-1.4.3.jar": (
        "com/cleanroommc/groovyscript/GroovyScript.class",
        "com/cleanroommc/groovyscript/core/GroovyScriptCore.class",
    ),
    "ICBM-classic-1.12.2-6.5.5.jar": (
        "icbm/classic/ICBMClassic.class",
        "assets/icbmclassic/recipes/launcher/base.json",
        "assets/icbmclassic/models/item/missiles/nuclear.json",
    ),
}
for archive, required_entries in archive_requirements.items():
    path = ROOT / "mods" / archive
    try:
        with zipfile.ZipFile(path) as zf:
            names = set(zf.namelist())
        for entry in required_entries:
            ok(entry in names, f"dependency/resource {archive}!/{entry}")
    except Exception as exc:
        ok(False, f"dependency/resource archive {archive}: {exc}")

jars = sorted((ROOT / "mods").rglob("*.jar"))
ok(len(jars) == 162, "expected active JAR count 162")
for path in jars:
    try:
        with zipfile.ZipFile(path) as zf:
            bad = zf.testzip()
        ok(bad is None, f"JAR integrity {path.relative_to(ROOT)}")
    except Exception as exc:
        ok(False, f"JAR integrity {path.relative_to(ROOT)}: {exc}")

core_build = ROOT / "development/IndustrialCivilizationCore/build/libs/IndustrialCivilizationCore-0.2.0.jar"
core_live = ROOT / "mods/IndustrialCivilizationCore-0.2.0.jar"
ok(core_build.is_file() and core_live.is_file() and digest(core_build) == digest(core_live), "custom build output equals live JAR")
core_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/IndustrialCivilizationCore.java").read_text()
ecology_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/PlanetaryEcologySystem.java").read_text()
faction_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/FactionSystem.java").read_text()
ok("public IndustrialCivilizationCore()" in core_source and "private IndustrialCivilizationCore()" not in core_source,
   "Forge-instantiable public @Mod constructor")
ok("EntitySkeleton.class" in ecology_source and "IndustrialMilitiaPatrol" in ecology_source,
   "Earth skeleton militia patrol replacement")
ok("isArmedWithGun" in ecology_source and "techguns.api.guns.IGenericGun" in ecology_source,
   "inventory-wide standard and exotic firearm detection")
ok("isExplosion()" in ecology_source and "militia_patrol_trap_kills" in ecology_source,
   "trap kills excluded from patrol blame")
ok("Math.max(-10" in faction_source and "militia_outposts_taken_down" in ecology_source,
   "patrol reputation floor and outpost hostility threshold")
ok('new Definition("civil_defense"' in faction_source
   and 'new Definition("territorial_militia"' in faction_source
   and "civil_defense_militia" not in faction_source,
   "Civil Defense and Territorial Militia are cleanly separated")
ok('Math.min(value, reputation(player, "riverside_works"))' in faction_source
   and 'Math.min(value, reputation(player, "survey_detachment_7"))' in faction_source,
   "Civil Defense hostility derives from city and honorable-factory standing")
ok('"territorial_militia", "trader", "armaments", "Militia Fence"' in
   (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/CivilizationWorldGenerator.java").read_text(),
   "militia presence tied to abandoned and criminal factories")
ok((ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/MilitiaOutpostRegistry.java").is_file(),
   "persistent militia outpost coordinate registry")
ok('"message.industrialcivilization.quest_guide"' in core_source and "PlayerLoggedInEvent" in core_source,
   "quest guide welcome message sent on login")
ok('key("key.betterquesting.quests", 41, KeyModifier.NONE, 64, KeyModifier.NONE)' in core_source,
   "Better Questing legacy default remapped to F6")
ok("KEY_MIGRATIONS" in core_source and "setKeyModifierAndCode" in core_source,
   "known inherited key conflicts migrated")
ok("oldCode == binding.getKeyCode()" in core_source and "oldModifier == binding.getKeyModifier()" in core_source,
   "explicit player key choices preserved")
lang = (ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/lang/en_us.lang").read_text()
ok("message.industrialcivilization.quest_guide=Industrial Civilization Guide: Press F6" in lang,
   "localized F6 quest guide welcome text")
ok("key_key.betterquesting.quests:64" in (ROOT / "options.txt").read_text(), "current profile quest key defaults to F6")
ok("key_key.betterquesting.quests:64:NONE" in (ROOT / "config/defaultoptions/keybindings.txt").read_text(),
   "new profiles default quest key to F6")
keybinding_lines = (ROOT / "config/defaultoptions/keybindings.txt").read_text().splitlines()
keybindings = {}
key_slots = {}
for line in keybinding_lines:
    if not line.startswith("key_"):
        continue
    name, code, modifier = line.rsplit(":", 2)
    keybindings[name] = (code, modifier)
    if code != "0":
        key_slots.setdefault((code, modifier), []).append(name)
duplicates = {slot: names for slot, names in key_slots.items() if len(names) > 1}
ok(not duplicates, f"no duplicate nonzero shipped keybinds: {duplicates}")
expected_keybindings = {
    "key_key.betterquesting.quests": ("64", "NONE"),
    "key_key.toggle_focus.desc": ("25", "CONTROL"),
    "key_key.jei.bookmark": ("23", "ALT"),
    "key_Quest Log": ("66", "NONE"),
    "key_invtweaks.key.sort": ("19", "ALT"),
    "key_techguns.key.forceReload": ("21", "ALT"),
    "key_Open Spaceship Inventory": ("23", "CONTROL"),
    "key_Boost Key": ("35", "ALT"),
    "key_key.control": ("34", "NONE"),
    "key_Mode Switch Key": ("68", "NONE"),
    "key_Hub Expand Key": ("24", "ALT"),
    "key_key.minimap.waypointhotkey": ("21", "CONTROL"),
    "key_waila.keybind.wailadisplay": ("26", "CONTROL"),
    "key_waila.keybind.liquid": ("27", "CONTROL"),
    "key_waila.keybind.recipe": ("39", "CONTROL"),
    "key_waila.keybind.wailaconfig": ("11", "CONTROL"),
    "key_key.music_player_previous": ("0", "NONE"),
    "key_key.music_player_toggle": ("0", "NONE"),
    "key_key.music_player_next": ("0", "NONE"),
    "key_key.music_player_gui": ("0", "NONE"),
    "key_keybind.universaltweaks.clear_toasts": ("0", "NONE"),
    "key_schematica.key.control": ("25", "ALT"),
    "key_RecipeSwitch": ("31", "ALT"),
}
for name, expected in expected_keybindings.items():
    ok(keybindings.get(name) == expected, f"curated keybind {name}={expected}")
ok(not any(line.startswith("key_pe.") for line in keybinding_lines), "removed ProjectE keybinds absent")
ok(not any(71 <= int(code) <= 83 for code, _modifier in keybindings.values() if code.lstrip("-").isdigit()),
   "no enabled shipped keybind requires a numpad")
ok(not any(modifier == "SHIFT" for code, modifier in keybindings.values() if code != "0"),
   "no enabled Shift-modified binding conflicts with Sneak")
ok(keybindings.get("key_ALT Key") == ("43", "NONE"), "IC2 modifier moved off Option")
with zipfile.ZipFile(core_live) as zf:
    names = set(zf.namelist())
    for expected in (
        "com/industrialcivilization/core/IndustrialCivilizationCore.class",
        "com/industrialcivilization/core/TileMolecularAnalyzer.class",
        "com/industrialcivilization/core/TileIndustrialMachine.class",
        "com/industrialcivilization/core/TileFactoryControlTerminal.class",
        "com/industrialcivilization/core/ProgressionNetwork.class",
        "com/industrialcivilization/core/SpaceSurvivalSystem.class",
        "mcmod.info",
        "assets/industrialcivilizationcore/blockstates/molecular_analyzer.json",
        "assets/industrialcivilizationcore/textures/gui/industrial_machine.png",
        "assets/industrialcivilizationcore/models/item/artificial_industrial_intelligence_core.json",
        "assets/industrialcivilizationcore/models/item/industrial_credit.json",
        "assets/industrialcivilizationcore/textures/items/industrial_credit.png",
        "assets/industrialcivilizationcore/models/item/technical_phase_pearl.json",
        "assets/industrialcivilizationcore/textures/items/technical_phase_pearl.png",
        "assets/industrialcivilizationcore/advancements/faction_contacts.json",
        "assets/industrialcivilizationcore/advancements/faction_membership.json",
        "assets/industrialcivilizationcore/advancements/faction_companion.json",
        "assets/industrialcivilizationcore/advancements/root.json",
        "assets/industrialcivilizationcore/advancements/civil_defense_contact.json",
        "assets/industrialcivilizationcore/advancements/territorial_militia_contact.json",
        "assets/industrialcivilizationcore/advancements/militia_outpost_takedown.json",
        "assets/industrialcivilizationcore/advancements/icbm_launch_control.json",
        "assets/industrialcivilizationcore/advancements/icbm_radar_defense.json",
        "assets/industrialcivilizationcore/advancements/icbm_conventional_missile.json",
    ):
        ok(expected in names, f"custom JAR resource {expected}")
    info = json.loads(zf.read("mcmod.info").decode())
    ok(info[0]["modid"] == "industrialcivilizationcore" and info[0]["mcversion"] == "1.12.2", "custom JAR metadata")

baseline_permissive_json = {
    "config/codechicken/supporters.json",
    "config/ic2/customMachineRecipes.json",
    "config/teamreborn/reborncore/selected_energy.json",
}
for path in sorted((ROOT / "config").rglob("*.json")) + sorted((ROOT / "development/IndustrialCivilizationCore/src/main/resources").rglob("*.json")):
    try:
        json.loads(path.read_text())
        ok(True, f"valid JSON {path.relative_to(ROOT)}")
    except Exception as exc:
        rel = str(path.relative_to(ROOT))
        original = snapshot / rel
        unchanged_permissive = rel in baseline_permissive_json and original.is_file() and digest(path) == digest(original)
        ok(unchanged_permissive, f"baseline permissive/empty JSON retained unchanged {rel}" if unchanged_permissive else f"valid JSON {rel}: {exc}")

quests = json.loads((ROOT / "config/betterquesting/DefaultQuests.json").read_text())
ok(quests.get("format:8") == "2.0.0", "Better Questing schema version")
ok(len(quests.get("questDatabase:9", {})) == 129, "129 Phase 2 capability milestones")
ok(len(quests.get("questLines:9", {})) == 23, "16 chapter and 7 independent side-path tabs")
names = [q["properties:10"]["betterquesting:10"]["name:8"] for q in quests["questDatabase:9"].values()]
ordered_gates = ["Orbital Research Archive", "Authorized Lunar Landing", "Lunar Engineering Archive",
                 "Quantum Technology Complete", "Mars Mission Authorization", "Authorized Tier 2 Mars Launch",
                 "Martian Autonomy Archive", "Lite Matter Engineering Complete", "Enter the AI Age",
                 "Applied Energistics Entry"]
ok(all(name in names for name in ordered_gates) and
   [names.index(name) for name in ordered_gates] == sorted(names.index(name) for name in ordered_gates),
   "Orbit-Moon-Quantum-Mars-AI-AE2 quest ordering")
progression_validation = subprocess.run(
    [sys.executable, str(ROOT / "tools/validate_progression.py")],
    cwd=str(ROOT), capture_output=True, text=True)
ok(progression_validation.returncode == 0,
   "canonical progression validator: " + progression_validation.stdout.strip().splitlines()[-1])
energy_validation = subprocess.run(
    [sys.executable, str(ROOT / "tools/validate_energy_interop.py")],
    cwd=str(ROOT), capture_output=True, text=True)
ok(energy_validation.returncode == 0,
   "energy interoperability validator: " + energy_validation.stdout.strip().splitlines()[-1])
quest_home = "industrialcivilizationcore:textures/gui/quest_home_v2.png"
quest_settings = quests["questSettings:10"]["betterquesting:10"]
ok(quest_settings.get("home_image:8") == quest_home, "pack-owned Better Questing home image configured")
ok(quest_settings.get("home_anchor_x:5") == 0.5, "Better Questing title uses centered horizontal anchor")
ok(quest_settings.get("home_offset_x:3") == -128, "Better Questing 256px title is centered on anchor")
ok(quest_settings.get("home_anchor_y:5") == 0.0 and quest_settings.get("home_offset_y:3") == 0,
   "Better Questing title uses top vertical anchor")
quest_home_file = ROOT / "resources/industrialcivilizationcore/textures/gui/quest_home_v2.png"
ok(quest_home_file.is_file(), "Better Questing home image exists")
if quest_home_file.is_file():
    png = quest_home_file.read_bytes()
    ok(png[:8] == b"\x89PNG\r\n\x1a\n", "Better Questing home image is PNG")
    width = int.from_bytes(png[16:20], "big") if len(png) >= 24 else 0
    height = int.from_bytes(png[20:24], "big") if len(png) >= 24 else 0
    ok((width, height) == (512, 512), "Better Questing home atlas is 512x512")
ok(quest_home in (ROOT / "tools/generate_objectives.py").read_text(), "quest generator preserves home image")
ok("QUEST_HOME_IMAGE" in core_source and "QUEST_HOME_OFFSET_X = -128" in core_source
   and "migrateQuestHomeImage" in core_source,
   "existing Better Questing worlds migrate to pack-owned home layout")
ok("GuiIngameMenu" in core_source and "button.id == 5" not in core_source
   and "PresetGUIs.HOME" not in core_source,
   "pause-menu vanilla Advancements button and screen are restored")
ok("button.id == 6" in core_source and "GuiFactionDirectory" in core_source
   and "gui.industrialcivilization.factions" in core_source,
   "pause-menu Statistics button opens the faction directory")
progression_network_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/ProgressionNetwork.java").read_text()
space_survival_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/SpaceSurvivalSystem.java").read_text()
analyzer_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/TileMolecularAnalyzer.java").read_text()
credits_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GuiIndustrialCredits.java").read_text()
village_suppression_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/VillageSuppressionHandler.java").read_text()
ok("GuiIndustrialCredits" in progression_network_source and "corysmart" in credits_source
   and "ai_credits_shown" in core_source
   and 'RuntimeAdvancements.grant(event.player, "ai_age_entry")' in core_source,
   "AI entry opens corysmart-branded one-time credits and leaves the post-AI world playable")
main_menu = json.loads((ROOT / "config/CustomMainMenu/mainmenu.json").read_text())
main_menu_text = (ROOT / "config/CustomMainMenu/mainmenu.json").read_text()
random_patches = (ROOT / "config/randompatches.cfg").read_text()
crash_info = (ROOT / "config/bloodymods/packcrashinfo.cfg").read_text()
ok("Industrial Civilization" in main_menu_text and "Tekkit 2" not in main_menu_text
   and "Tekkit-2" not in main_menu_text
   and main_menu.get("other", {}).get("background", {}).get("image")
       == "industrialcivilizationcore:textures/mainmenu/industrial_civilization_background.png",
   "main menu uses pack-owned Industrial Civilization branding and background")
ok("S:title=Industrial Civilization" in random_patches
   and "S:modpackName=Industrial Civilization" in crash_info
   and "S:modpackAuthor=corysmart" in crash_info,
   "window and crash-report branding identify Industrial Civilization and corysmart")
ok("GuiCelestialSelection" in core_source and "gui.possibleBodies = allowed" in core_source
   and "SpaceAccessRequest" in progression_network_source and "event.toDim == 1" in core_source
   and "Blocks.END_PORTAL_FRAME" in core_source and "SchematicRegistry.addUnlockedPage" in core_source
   and "tier2_schematic_unlocked" in core_source,
   "Galacticraft map selection and server transfers exclude locked/unsupported destinations and the End")
ok("suppressNaturalPhasePearls" in core_source and "removePrematurePhasePearls" in core_source
   and 'RuntimeAdvancements.grant(event.player, "technical_phase_pearl")' in core_source,
   "natural pearls are suppressed and only an AI-age craft completes the technical pearl")
ok('new SampleProfile("minecraft:iron_ingot", "Earth"' in analyzer_source
   and '"galacticraftcore:meteoric_iron_raw"' in analyzer_source
   and '"analysis_mars"' in analyzer_source and '"comparative_molecular_analysis"' in analyzer_source,
   "Analyzer requires real Earth, Moon, and Mars comparative samples")
ok("TileEntityOxygenSealer" in space_survival_source and "fullQuantumSuit" in space_survival_source
   and "radiation_exposure" in space_survival_source,
   "space radiation is protected by active sealed habitats or a full IC2 QuantumSuit")
ok("new MapGenVillage()" in village_suppression_source
   and "canSpawnStructureAtCoords" in village_suppression_source
   and "new MapGenBase()" not in village_suppression_source,
   "village suppression preserves ChunkGeneratorOverworld's required MapGenVillage type")
ok("PlayerContainerEvent.Close" in faction_source and "ContainerMerchant" in faction_source
   and '"completed IC Credit trade"' in faction_source,
   "faction trade contact requires a completed IC Credit transaction")

advancement_dir = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/advancements"
advancement_files = sorted(advancement_dir.glob("*.json"))
ok(len(advancement_files) == 130, "visible advancement tree covers all 129 quests plus its root")
advancements = {path.stem: json.loads(path.read_text()) for path in advancement_files}
ok("root" in advancements and "parent" not in advancements.get("root", {}),
   "Industrial Civilization advancement root exists")
for advancement_id, advancement in advancements.items():
    display = advancement.get("display", {})
    ok(bool(display) and display.get("hidden") is False,
       f"visible advancement display {advancement_id}")
    if advancement_id != "root":
        ok(str(advancement.get("parent", "")).startswith("industrialcivilizationcore:"),
           f"ordered in-pack advancement parent {advancement_id}")
for advancement_id in ("civil_defense_contact", "territorial_militia_contact", "militia_outpost_takedown",
                       "icbm_launch_control", "icbm_radar_defense", "icbm_conventional_missile",
                       "technical_phase_pearl"):
    ok(advancement_id in advancements, f"optional-path advancement {advancement_id}")
ok("new CreativeTabs(MODID)" in core_source
   and core_source.count(".setCreativeTab(CREATIVE_TAB)") >= 7,
   "all custom blocks and items are assigned to the Industrial Civilization creative tab")
with zipfile.ZipFile(ROOT / "mods/appliedenergistics2-rv6-stable-7.jar") as ae2_jar:
    ok("appeng/core/CreativeTab.class" in ae2_jar.namelist(),
       "Applied Energistics 2 supplies its gameplay creative tab")
jei_blacklist = (ROOT / "config/jei/itemBlacklist.cfg").read_text()
ok("appliedenergistics2:" not in jei_blacklist
   and "industrialcivilizationcore:" not in jei_blacklist,
   "AE2 and Industrial Civilization are not suppressed from HEI or Creative search")

ok(not (ROOT / "scripts/industrial_civilization.zs").exists(), "obsolete non-reloadable integration script removed")
faction_gui = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GuiFactionDirectory.java").read_text()
ok("visibleLineCount()" in faction_gui and "handleMouseInput()" in faction_gui
   and "Math.min(520" in faction_gui,
   "faction directory is responsive and scrolls long descriptions")
client_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/IndustrialCivilizationCore.java").read_text()
ok("showOnlyPackAdvancementTabs" in client_source
   and '"galacticraftcore".equals(domain)' in client_source,
   "Galacticraft advancement tabs are replaced by Industrial Civilization")
worldgen_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/CivilizationWorldGenerator.java").read_text()
ok("if (!roadChunk && structure == 0) return;" in worldgen_source,
   "empty civilization chunks avoid terrain height work")
jei_config = (ROOT / "config/jei/jei.cfg").read_text()
ok("S:tooltipSearchMode=ENABLED" in jei_config,
   "HEI retains full tooltip search despite its one-time startup indexing cost")
run_config = json.loads((ROOT / "groovy/runConfig.json").read_text())
ok(run_config["packId"] == "industrial_civilization", "stable GroovyScript pack ID")
ok(run_config["loaders"]["postInit"] == ["postInit/"], "reloadable postInit loader configured")
script = (ROOT / "groovy/postInit/industrial_civilization.groovy").read_text()
content_script = (ROOT / "groovy/postInit/industrial_civilization_content.groovy").read_text()
ok("startsWith('appliedenergistics2:')" in content_script and ".removeAll()" in content_script
   and "ae2Catalog" in content_script and "ai_catalog_" in content_script,
   "complete AE2 catalog is captured and reconstructed behind AI authorization")
ok("technical_phase_pearl" in content_script and "aiCore" in content_script
   and "crafting.removeByOutput(item('minecraft:ender_pearl'))" in content_script,
   "Technical Phase Pearl has one AI-authorized manufacturing source")
for item in ("techguns:pistol", "techguns:combatshotgun", "techguns:m4", "industrialcivilizationcore:molecular_analyzer"):
    ok(item in script, f"integration recipe reference {item}")
for item in ("techguns:itemshared:2", "techguns:itemshared:11", "techguns:itemshared:13", "computercraft:computer:16384"):
    ok(item in script, f"runtime-confirmed metadata recipe reference {item}")
for invalid_item in ("techguns:pistolmagazine", "techguns:shotgunrounds", "techguns:assaultriflemagazine", "computercraft:advanced_computer"):
    ok(invalid_item not in script, f"invalid standalone item reference removed {invalid_item}")
for item in (
    "ironchest:iron_chest:4", "ironchest:iron_gold_chest_upgrade",
    "ironchest:gold_diamond_chest_upgrade", "ironchest:copper_silver_chest_upgrade",
    "ironchest:silver_gold_chest_upgrade", "ironchest:diamond_crystal_chest_upgrade",
    "ironchest:diamond_obsidian_chest_upgrade",
):
    ok(item in script, f"Iron Chest ceiling removes/hides {item}")
ironchest_config = (ROOT / "config/ironchest.cfg").read_text()
ok('B:"Add Shulker Boxes to Creative Menu"=false' in ironchest_config, "higher-tier Iron Chest shulkers hidden")
ok('B:"Enable Shulker Box Recipes"=false' in ironchest_config, "higher-tier Iron Chest shulker recipes disabled")
ok("projecte" not in (ROOT / "scripts/tekkit2.zs").read_text().lower(), "no ProjectE in inherited CraftTweaker script")
ok("projecte" not in script.lower(), "no ProjectE in reloadable integration script")

ledger = json.loads((ROOT / "bin/extractedFiles.json").read_text())
ok(not any(any(token in entry.lower() for token in ("projecte", "viewemc", "ic2cuumatter")) for entry in ledger), "Technic extraction ledger repaired")

final_lock = json.loads((ROOT / "manifest/final-mod-lock.json").read_text())
ok(final_lock["mod_count_jars"] == 162, "final lock JAR count")
ok(not final_lock["duplicate_detected_mod_ids"], "no duplicate declared mod IDs")
locked = {record["file"]: record["sha256"] for record in final_lock["mods"]}
for path in jars:
    rel = str(path.relative_to(ROOT))
    ok(locked.get(rel) == digest(path), f"final lock hash {rel}")

print(f"STATIC VALIDATION: {len(checks)} checks passed")
if errors:
    print(f"STATIC VALIDATION FAILED: {len(errors)} error(s)")
    for error in errors:
        print(" - " + error)
    sys.exit(1)
print("No Minecraft, Forge, launcher, or server process was started.")
