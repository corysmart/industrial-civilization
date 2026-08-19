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
    "TELEMETRY_SCHEMA.md", "MANUAL_QUEST_TEST_CHECKLIST.md", "ITEM_UNIFICATION_AUDIT.md",
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
pack_version = json.loads((ROOT / "manifest/pack-version.json").read_text())
technic_version = json.loads((ROOT / "bin/version").read_text())
groovy_pack = json.loads((ROOT / "groovy/runConfig.json").read_text())
main_menu = json.loads((ROOT / "config/CustomMainMenu/mainmenu.json").read_text())
ok(pack_version == {
       "industrial_civilization_version": "0.6.2",
       "technic_base_version": "1.2.6",
       "core_version": "0.6.2",
   } and technic_version["version"] == pack_version["technic_base_version"],
   "private pack release is versioned without inventing a nonexistent Technic Solder build")
release_version = pack_version["industrial_civilization_version"]
ok(groovy_pack["version"] == release_version,
   "GroovyScript in-client pack version matches the Industrial Civilization release")
ok(main_menu["labels"]["industrialcivilization"]["text"]
   == f"Industrial Civilization v{release_version}",
   "Custom Main Menu in-client version label matches the Industrial Civilization release")

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
ok(len(jars) == 163, "expected active JAR count 163")
for path in jars:
    try:
        with zipfile.ZipFile(path) as zf:
            bad = zf.testzip()
        ok(bad is None, f"JAR integrity {path.relative_to(ROOT)}")
    except Exception as exc:
        ok(False, f"JAR integrity {path.relative_to(ROOT)}: {exc}")

core_build = ROOT / "development/IndustrialCivilizationCore/build/libs/IndustrialCivilizationCore-0.6.2.jar"
core_live = ROOT / "mods/IndustrialCivilizationCore-0.6.2.jar"
ok(core_build.is_file() and core_live.is_file() and digest(core_build) == digest(core_live), "custom build output equals live JAR")
core_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/IndustrialCivilizationCore.java").read_text()
ok("openQuestGuideAtFirstChapter" in core_source
   and "QuestLineDatabase.INSTANCE.getValue(0)" in core_source,
   "Better Questing home opens with the first chapter selected instead of a black canvas")
ok("clampQuestBackgroundZoom" in core_source
   and "QUEST_CANVAS" in core_source
   and "GameplayRules.questMinimumZoom" in core_source
   and "GameplayRules.questBoundedScroll" in core_source
   and "NativeProps.BG_SIZE" in core_source,
   "Better Questing zoom and panning stop at the quest backdrop edges")
solar_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/TileEnvironmentalSolarArray.java").read_text()
ok("GameplayRules.nextLunarDarkTicks" in solar_source
   and 'RuntimeAdvancements.completed(player, "lunar_science_program")' in solar_source
   and "SpaceSurvivalSystem.protectedByHabitat(player)" in solar_source,
   "lunar darkness evidence requires Lunar Science and an active detector habitat")
ecology_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/PlanetaryEcologySystem.java").read_text()
robber_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/EntityRobber.java").read_text()
ok("BASE_MOVEMENT_SPEED = 0.20D" in robber_source
   and "EntityAIAttackMelee(this, 0.95D, false)" in robber_source,
   "robbers use a deliberately slower, escapable pursuit speed")
ok("MarketEconomy.carriesRobberLoot(player)" in ecology_source
   and "robberTargetsPlayer" in ecology_source
   and "EntityAINearestAttackableTarget" not in robber_source,
   "robbers proactively attack only players carrying technical or valuable loot")
patrol_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/EntityMilitiaPatrol.java").read_text()
render_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/ClientRenderRegistration.java").read_text()
faction_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/FactionSystem.java").read_text()
ok("public IndustrialCivilizationCore()" in core_source and "private IndustrialCivilizationCore()" not in core_source,
   "Forge-instantiable public @Mod constructor")
ok("GameplayRules.aiAgeReady" in core_source
   and 'ProgressionState.has(event.player, "martian_autonomy_archive")' in core_source,
   "AI entry consumes the canonical Martian Autonomy Archive record")
ok("instanceof EntitySkeleton" in ecology_source and "EntityMilitiaPatrol" in ecology_source,
   "Earth skeleton militia patrol replacement")
ok('militiaPatrolRadius", "ecology", 128' in core_source
   and 'militiaPatrolLocalCap", "ecology", 6' in core_source
   and "MilitiaOutpostRegistry.nearby" in ecology_source
   and "militiaPatrolSpawnAllowed" in ecology_source
   and "FORCE_PATROL_REPLACEMENT" in ecology_source
   and 'setBoolean("PersistenceRequired", false)' in patrol_source,
   "militia patrols are bounded to registered outposts with a local cap and deterministic test bypass")
ok("extends EntityMob" in robber_source and "extends EntityMob" in patrol_source
   and "ENTITY_PLAYER_HURT" in robber_source and "ENTITY_PLAYER_DEATH" in robber_source
   and "ENTITY_PLAYER_HURT" in patrol_source and "ENTITY_PLAYER_DEATH" in patrol_source
   and 'texture("ashline_raiders")' in render_source,
   "replacement mobs use independent human bodies, opaque skins, and non-monster audio")
ok('robberSpawnPercent", "ecology", 25' in core_source
   and 'robberLocalCap", "ecology", 4' in core_source
   and "robberSpawnAllowed" in ecology_source
   and "enablePersistence()" not in ecology_source.split("private static void configureRobber", 1)[1].split("@SubscribeEvent", 1)[0]
   and "nextInt(8)" in ecology_source,
   "Robber ecology uses a 25% conversion rate, local cap, natural despawning, and reduced squads")
ok("removeVanillaHostileSpawnEggs" in core_source
   and "IMob.class.isAssignableFrom(entry.getEntityClass())" in core_source
   and "EntityList.ENTITY_EGGS.keySet().removeIf" in core_source,
   "Creative hides vanilla hostile spawn eggs in favor of Industrial identities")
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
ok(len(quests.get("questDatabase:9", {})) == 144, "144 campaign capability milestones")
ok(len(quests.get("questLines:9", {})) == 26, "16 chapter and 10 independent side-path tabs")
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
ok(quest_settings.get("home_anchor_y:5") == 0.5 and quest_settings.get("home_offset_y:3") == -64,
   "Better Questing title uses centered vertical anchor")
quest_home_file = ROOT / "resources/industrialcivilizationcore/textures/gui/quest_home_v2.png"
ok(quest_home_file.is_file(), "Better Questing home image exists")
if quest_home_file.is_file():
    png = quest_home_file.read_bytes()
    ok(png[:8] == b"\x89PNG\r\n\x1a\n", "Better Questing home image is PNG")
    width = int.from_bytes(png[16:20], "big") if len(png) >= 24 else 0
    height = int.from_bytes(png[20:24], "big") if len(png) >= 24 else 0
    ok((width, height) == (512, 512), "Better Questing home atlas is 512x512")
ok(quest_home in (ROOT / "tools/generate_objectives.py").read_text(), "quest generator preserves home image")
ok("QUEST_HOME_IMAGE" in core_source and "QUEST_HOME_ANCHOR_Y = 0.5F" in core_source
   and "QUEST_HOME_OFFSET_X = -128" in core_source and "QUEST_HOME_OFFSET_Y = -64" in core_source
   and "migrateQuestHomeImage" in core_source,
   "existing Better Questing worlds migrate to pack-owned home layout")
ok("resizeAndCenterQuestHomeTitle" in core_source and "questHomeTitleWidth" in core_source,
   "Better Questing title grows responsively and remains centered")
ok("GuiIngameMenu" in core_source and "if (button.id == 5) advancements = button;" in core_source
   and "PresetGUIs.HOME" not in core_source,
   "pause-menu vanilla Advancements button and screen are restored")
ok("button.id == 6" in core_source and "GuiFactionDirectory" in core_source
   and "gui.industrialcivilization.factions" in core_source,
   "pause-menu Statistics button opens the faction directory")
progression_network_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/ProgressionNetwork.java").read_text()
space_survival_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/SpaceSurvivalSystem.java").read_text()
quest_telemetry_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/QuestTelemetrySystem.java").read_text()
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
ok(not main_menu.get("images") and "drawResponsiveMainMenuTitle" not in core_source,
   "main menu intentionally leaves the background unobstructed by a title card")
ok("slice" not in main_menu_text.lower(),
   "main menu contains no internal development-slice terminology")
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
ok('"galacticraftcore:oxygen_detector"' in
       (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GameplayRules.java").read_text()
   and "activeOxygenDetector" in space_survival_source
   and "getMetaFromState" in space_survival_source
   and "OxygenUtil.isAABBInBreathableAirBlock" not in space_survival_source
   and "fullQuantumSuit" in space_survival_source and "radiation_exposure" in space_survival_source,
   "space habitat protection requires an active Galacticraft Oxygen Detector or a full IC2 QuantumSuit")
ok("GameplayRules.habitatMilestone(environment)" in quest_telemetry_source
   and '"orbit".equals(environment) ? "orbital_habitat"' in
       (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GameplayRules.java").read_text()
   and 'environment + "_habitat_stable_samples"' in quest_telemetry_source,
   "orbital habitat grants its canonical advancement while preserving stable-sample counters")
ok('environment + "_functional_stable_samples"' in quest_telemetry_source
   and "nextFunctionalStableSamples" in quest_telemetry_source
   and "resetFunctionalStability" in quest_telemetry_source
   and 'functional_stable_samples' in
       (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/CommandIndustrialTest.java").read_text(),
   "functional off-world gates require a resettable continuous full-infrastructure sample streak")
ok("new MapGenVillage()" in village_suppression_source
   and "canSpawnStructureAtCoords" in village_suppression_source
   and "new MapGenBase()" not in village_suppression_source,
   "village suppression preserves ChunkGeneratorOverworld's required MapGenVillage type")
ok("PlayerContainerEvent.Close" in faction_source and "ContainerMerchant" in faction_source
   and '"completed IC Credit trade"' in faction_source,
   "faction trade contact requires a completed IC Credit transaction")

advancement_dir = ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/advancements"
advancement_files = sorted(advancement_dir.glob("*.json"))
ok(len(advancement_files) == 145, "visible advancement tree covers all 144 quests plus its root")
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
ok(advancements["mars_readiness_trial"]["display"]["icon"]["item"] == "minecraft:fireworks",
   "Mars Readiness Trial uses the Minecraft 1.12 firework item ID")
ok(advancements["martian_science_program"]["display"]["icon"]["item"] == "minecraft:sand",
   "Martian Science Program uses the Minecraft 1.12 sand item ID")
ok("def quest_icon_stack(ms):" in (ROOT / "tools/generate_objectives.py").read_text()
   and 'item_id if damage == 32767 else icon' in (ROOT / "tools/generate_objectives.py").read_text(),
   "Better Questing wildcard retrieval evidence never renders as metadata 32767")
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
   and "Math.min(520" in faction_gui and "width - 16" in faction_gui
   and "panelWidth - 12" in faction_gui,
   "faction directory is responsive and scrolls long descriptions")
client_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/IndustrialCivilizationCore.java").read_text()
ok("showOnlyPackAdvancementTabs" in client_source
   and "ADVANCEMENT_TAB_INSTANCE_PAGE.setInt(tab, -1)" in client_source,
   "all foreign advancement tabs are replaced by Industrial Civilization")
worldgen_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/CivilizationWorldGenerator.java").read_text()
ok("if (!roadChunk && structure == 0) return;" in worldgen_source,
   "empty civilization chunks avoid terrain height work")
terrain_gui = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GuiTerrainWarmup.java").read_text()
ok("doesGuiPauseGame()" in terrain_gui and "return false" in terrain_gui
   and "MINIMUM_WARMUP_MS = 15000L" in terrain_gui and "TIMEOUT_MS = 30000L" in terrain_gui
   and "Math.min(4, mc.gameSettings.renderDistanceChunks)" in terrain_gui
   and "drawWrappedCentered" in terrain_gui and "width - 32" in terrain_gui,
   "terrain warmup waits for a bounded playable area and post-join initialization without trapping the player")
credits_gui = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/GuiIndustrialCredits.java").read_text()
ok("listFormattedStringToWidth" in credits_gui and "width - 24" in credits_gui,
   "AI credits wrap long lines and bound their Done button to the viewport")
main_menu_scale = (ROOT / "config/teamreborn/mainmenuscale/config.cfg").read_text()
ok("I:GUI_SCALE=2" in main_menu_scale and "I:HIGH_RES_SCALE=2" in main_menu_scale,
   "branded main menu uses a small-window-safe GUI scale")
icbm_script = (ROOT / "groovy/postInit/industrial_icbm.groovy").read_text()
ok("crafting.streamRecipes()" in icbm_script and "output.item == part.item" in icbm_script
   and "duplicateParts.each { part -> crafting.removeByOutput(part) }" not in icbm_script,
   "ICBM duplicate recipe cleanup silently accepts registered parts with no native recipe")
ok("holdTerrainLoadingScreen" in client_source and "terrainWarmupWorld != minecraft.world" in client_source,
   "terrain warmup runs after every world or dimension transition")
jei_config = (ROOT / "config/jei/jei.cfg").read_text()
ok("S:tooltipSearchMode=ENABLED" in jei_config,
   "HEI retains full tooltip search despite its one-time startup indexing cost")
run_config = json.loads((ROOT / "groovy/runConfig.json").read_text())
ok(run_config["packId"] == "industrial_civilization", "stable GroovyScript pack ID")
ok(run_config["loaders"]["postInit"] == ["postInit/"], "reloadable postInit loader configured")
script = (ROOT / "groovy/postInit/industrial_civilization.groovy").read_text()
content_script = (ROOT / "groovy/postInit/industrial_civilization_content.groovy").read_text()
ok("mods.jei.ingredient.add(mffsProgressionMachines)" in script
   and "modularforcefieldsystem:projector" in script
   and "modularforcefieldsystem:capacitor" in script,
   "progression-critical MFFS machines are restored to HEI name and mod searches")
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
crafttweaker = (ROOT / "scripts/tekkit2.zs").read_text()
ok("projecte" not in crafttweaker.lower(), "no ProjectE in inherited CraftTweaker script")
ok("recipes.replaceAllOccurences(<minecraft:slime_ball>, <ic2:itemharz>, null)" in crafttweaker
   and 'recipes.addShapeless("industrial_civilization_lead"' in crafttweaker
   and 'recipes.addShapeless("industrial_civilization_sticky_piston"' in crafttweaker,
   "Sticky Resin replaces generic Slimeball adhesive in Leads and Sticky Pistons")
ok("recipes.replaceAllOccurences(<minecraft:ghast_tear>, <ic2:itemmisc:257>, null)" in crafttweaker,
   "Ghast Tear crafting dependencies use IC2 Advanced Alloy")
dimension_replacements = {
    "minecraft:quartz": "appliedenergistics2:material:0",
    "minecraft:netherrack": "minecraft:cobblestone",
    "minecraft:soul_sand": "minecraft:sand",
    "minecraft:nether_wart": "ic2:itemmisc:159",
    "minecraft:netherbrick": "minecraft:brick",
    "minecraft:glowstone_dust": "minecraft:redstone",
    "minecraft:blaze_rod": "ic2:itemmisc:53",
    "minecraft:blaze_powder": "ore:dustCoal",
    "minecraft:magma_cream": "ic2:itemharz",
    "minecraft:nether_star": "industrialcivilizationcore:lunar_quantum_component",
    "minecraft:end_stone": "minecraft:obsidian",
    "minecraft:chorus_fruit": "minecraft:ender_pearl",
    "minecraft:chorus_fruit_popped": "ic2:itemmisc:257",
    "minecraft:purpur_block": "minecraft:stonebrick",
    "minecraft:end_crystal": "ic2:itembatcrystal",
}
for legacy, replacement in dimension_replacements.items():
    ok(f"recipes.replaceAllOccurences(<{legacy}>, <{replacement}>, null)" in crafttweaker,
       f"dimension-locked crafting input {legacy} uses {replacement}")
late_dimension_recipes = (ROOT / "groovy/postInit/dimension_material_unification.groovy").read_text()
ok("computronics:colorful_lamp" in late_dimension_recipes
   and "computronics:tape:4" in late_dimension_recipes
   and "computronics:tape:8" in late_dimension_recipes
   and "industrialcivilizationcore:lunar_quantum_component" in late_dimension_recipes
   and "minecraft:redstone" in late_dimension_recipes,
   "late Computronics recipes use Overworld and lunar technical materials")
ok('recipes.removeByRecipeName("quark:purple_shulker_box")' in crafttweaker
   and "recipes.remove(<industrialforegoing:wither_builder>)" in crafttweaker,
   "End-only shulker storage and ecology-incompatible Wither Builder recipes are disabled")
ok('recipes.addShapeless("industrial_civilization_gunpowder"' in crafttweaker
   and "<ore:dustCoal>" in crafttweaker,
   "gunpowder has a survival industrial route without Creepers")
if_lv = (ROOT / "groovy/postInit/industrial_foregoing_lv.groovy").read_text()
for machine in (
    "crop_sower", "crop_recolector", "resourceful_furnace", "plant_interactor",
    "crop_enrich_material_injector", "animal_stock_increaser", "animal_growth_increaser",
    "animal_independence_selector", "animal_resource_harvester",
    "animal_byproduct_recolector", "sewage_composter_solidifier",
    "water_resources_collector",
):
    ok(f"industrialforegoing:{machine}" in if_lv, f"Industrial Foregoing LV farm recipe: {machine}")
ok("industrialforegoing:plastic" not in if_lv and "teslacorelib:machine_case" not in if_lv
   and "ic2:blockmachinelv" in if_lv and "ic2:itemmisc:451" in if_lv,
   "peaceful Industrial Foregoing recipes use early IC2 LV components")
ok('addExternalSale(offers, "ic2:itemharz", 0, 2, 4' in faction_source
   and "Items.SLIME_BALL" not in faction_source,
   "electronics markets sell canonical IC2 Sticky Resin instead of Slimeballs")
material_unification = (ROOT / "groovy/postInit/industrial_material_unification.groovy").read_text()
ok('crafting.removeByOutput(item("railcraft:circuit:${metadata}"))' in material_unification
   and material_unification.count("circuitRecipe('") == 4
   and "item('ic2:itemharz')" in material_unification
   and "minecraft:slime_ball" not in material_unification,
   "Railcraft custom circuit recipes use canonical IC2 Sticky Resin before the live audit")
material_audit = (ROOT / "groovy/postInit/material_ecology_audit.groovy").read_text()
ok("crafting.streamRecipes()" in material_audit and "material-ecology-audit" in material_audit
   and "slime_ball" in material_audit and "ghast_tear" in material_audit,
   "live final-registry material ecology audit is installed")
ok("supplementalLivestockBone" in ecology_source
   and "GameplayRules.supplementalBoneDrop" in ecology_source
   and "new ItemStack(Items.BONE)" in ecology_source,
   "adult player-killed livestock provide rare supplemental bones")
for metadata in (0, 4):
    ok(f'recipes.removeByRecipeName("galacticraftcore:solar_{metadata}")' in crafttweaker,
       f"duplicate Galacticraft solar recipe {metadata} is disabled")
    ok(f'mods.jei.JEI.hide(<galacticraftcore:solar:{metadata}>)' in crafttweaker,
       f"duplicate Galacticraft solar stack {metadata} is hidden from HEI")
solar_block_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/BlockEnvironmentalSolarArray.java").read_text()
ok("EnumBlockRenderType getRenderType" in solar_block_source
   and "return EnumBlockRenderType.MODEL;" in solar_block_source,
   "container-backed solar arrays opt into modeled block rendering")
solar_tile_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/TileEnvironmentalSolarArray.java").read_text()
ok("GameplayRules.solarMilestoneReady(generatedTotal, lastUser != null" in solar_tile_source
   and "hasConnectedLoad()" in solar_tile_source
   and "acceptsEnergyFrom(this, side.getOpposite())" in solar_tile_source
   and "storage.canReceive()" in solar_tile_source,
   "solar milestones require an attributed 10,000 EU plus a connected IC2 or FE load")
ic2_paradise = json.loads((ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/ic2/advancements/basic/terraformEndCultivation.json").read_text())
ic2_override_lang = (ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/ic2/lang/en_us.lang").read_text()
ok(ic2_paradise["criteria"] == {"mars_cultivation": {"trigger": "minecraft:impossible"}}
   and "awardMarsCultivationParadise" in core_source
   and '"basic/terraformendcultivation"' in core_source
   and "GameplayRules.marsCultivationAchievement" in core_source,
   "IC2 Endgame Paradise is reassigned from the disabled End to a Mars Cultivation TFBP insertion")
ok("achievement.terraformEndCultivation=Martian Paradise" in ic2_override_lang
   and "IC2 Terraformer on Mars" in ic2_override_lang,
   "IC2 paradise achievement title and description reference Mars")
continuity_source = json.loads((ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/minecraft/advancements/adventure/totem_of_undying.json").read_text())
continuity_advancement = json.loads((ROOT / "development/IndustrialCivilizationCore/src/main/resources/assets/industrialcivilizationcore/advancements/ported/minecraft/adventure/totem_of_undying.json").read_text())
ecology_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/PlanetaryEcologySystem.java").read_text()
worldgen_source = (ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core/CivilizationWorldGenerator.java").read_text()
ok("display" not in continuity_source
   and continuity_advancement["display"]["icon"]["item"] == "industrialcivilizationcore:emergency_continuity_core"
   and continuity_source["criteria"] == {"ai_continuity": {"trigger": "minecraft:impossible"}}
   and continuity_advancement["criteria"] == {"ported_source": {"trigger": "minecraft:impossible"}}
   and "emergencyContinuity" in core_source
   and '"adventure/totem_of_undying"' in core_source
   and "Items.TOTEM_OF_UNDYING.setCreativeTab(null)" in core_source
   and "drop.getItem().getItem() == Items.TOTEM_OF_UNDYING" in core_source
   and "mods.jei.ingredient.removeAndHide([vanillaTotem])" in script,
   "AI Emergency Continuity Core wholly replaces the removed Totem and rewrites Postmortal")
ok("ADVANCEMENT_TAB_INSTANCE_PAGE.setInt(tab, -1)" in core_source
   and "return MODID.equals(advancement.getId().getResourceDomain())" in core_source,
   "Advancements GUI preserves the full graph while moving foreign roots off the rendered page")
ok("ADVANCEMENT_TAB_INSTANCE_PAGE.setInt(tab, 0)" in core_source
   and "SELECTED_ADVANCEMENT_TAB.set(screen, packTab)" in core_source
   and "ADVANCEMENT_TAB_PAGE.setInt(null, 0)" in core_source
   and "ADVANCEMENT_TAB_SCROLL_X.setInt(packTab, 103)" in core_source
   and "ADVANCEMENT_TAB_SCROLL_Y.setInt(packTab, 43)" in core_source
   and "ADVANCEMENT_TAB_CENTERED.setBoolean(packTab, true)" in core_source
   and "button.id == 101 || button.id == 102" in core_source,
   "Unified advancement screen selects its sole tab and opens at the campaign root")
ok("EntitySpacePirate" in ecology_source and "EntitySpaceMilitia" in ecology_source
   and "EntitySpaceCitizen" in ecology_source
   and "event.getEntity() instanceof IMob" in ecology_source
   and 'domain.startsWith("galacticraft")' not in ecology_source,
   "Moon and Mars reject monster identities and use breathable human astronaut entities")
ok("spacePirateUsesNanoSuit" in ecology_source and 'equipArmor(pirate, nano ? "nano" : "astronaut")' in ecology_source
   and 'equipArmor(militia, "quantum")' in ecology_source
   and "setDropChance(slot, 2.0F)" in ecology_source
   and "contextualHumanSalvage" in ecology_source,
   "space gear tiers use 20-percent NanoSuit pirates, QuantumSuit security, guaranteed kit, and rare salvage")
ok("buildOperationalFactory" in worldgen_source and "buildIndustrialCity" in worldgen_source
   and "buildMilitiaOutpost" in worldgen_source and "buildPrimitiveSettlement" in worldgen_source
   and "AbandonedFactoryWorldGenerator.buildShell" in worldgen_source,
   "Moon and Mars generate cities, settlements, militia, fabrication centers, and abandoned or pirate sites")
ok("projecte" not in script.lower(), "no ProjectE in reloadable integration script")

ledger = json.loads((ROOT / "bin/extractedFiles.json").read_text())
ok(not any(any(token in entry.lower() for token in ("projecte", "viewemc", "ic2cuumatter")) for entry in ledger), "Technic extraction ledger repaired")

final_lock = json.loads((ROOT / "manifest/final-mod-lock.json").read_text())
ok(final_lock["mod_count_jars"] == 163, "final lock JAR count")
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
