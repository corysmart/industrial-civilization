#!/usr/bin/env python3
"""Generate reproducible mod locks from the immutable snapshot and live instance."""
import hashlib
import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SNAPSHOT = ROOT / ".backups" / "pre-industrial-civilization"
OUT = ROOT / "manifest"

REMOVED = {
    "ProjectE-1.12.2-PE1.4.8-14-technic.jar": "REMOVE",
    "ViewEMC-1.12.2-v8.jar": "REMOVE",
    "ic2cuumatter-1.12.2-1.1.3.jar": "REMOVE",
}
ADDED = {
    "appliedenergistics2-rv6-stable-7.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2/files/2747063",
        "redistribution": "Custom license; local assembled instance only. Re-check permission before publication.",
    },
    "techguns-1.12.2-2.0.2.0_pre3.2.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/techguns/files/2958103",
        "redistribution": "All Rights Reserved; local assembled instance only. Do not redistribute without permission.",
    },
    "CustomNPCs_1.12.2-(05Jul20).jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/custom-npcs/files/2996912",
        "redistribution": "CC BY-NC 3.0; attribution and noncommercial terms apply.",
    },
    "BetterQuesting-3.5.329.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/better-questing/files/2950248",
        "redistribution": "All Rights Reserved; CurseForge page permits modpack use subject to Minecraft EULA.",
    },
    "StandardExpansion-3.4.173.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/better-questing-standard-expansion/files/2863771",
        "redistribution": "All Rights Reserved; local assembled instance only. Re-check permission before publication.",
    },
    "groovyscript-1.4.3.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/groovyscript/files/7925117",
        "redistribution": "LGPL-3.0; source and license terms are linked from the upstream project.",
    },
    "vehicle-mod-1.4.0-1.12.2.jar": {
        "source": "https://modrinth.com/mod/onysd-vehicles/version/LuvveCaI",
        "redistribution": "LGPL-2.1-or-later; preserve notices and make source for modifications available under the same terms.",
    },
    "obfuscate-0.4.2-1.12.2.jar": {
        "source": "https://www.curseforge.com/minecraft/mc-mods/obfuscate/files/2916310",
        "redistribution": "GPL-3.0 dependency; preserve license and corresponding-source obligations when distributing.",
    },
    "IndustrialCivilizationCore-0.2.0.jar": {
        "source": "development/IndustrialCivilizationCore",
        "redistribution": "First-party pack integration source included in this instance.",
    },
}
LIBRARY_HINTS = (
    "lib", "core", "api", "forgelin", "mixinbooter", "censoredasm", "renderlib",
    "autoreglib", "guide-api", "mrtjpcore", "reborncore", "ichunutil", "wanion",
    "mcmultipart", "sledgehammer", "codechickenlib", "gunpowderlib", "ctm-mc",
)
REVIEW_HINTS = ("akashictome", "snowrealmagic", "fairylights")


def sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def parse_info(raw):
    try:
        data = json.loads(raw.decode("utf-8-sig"))
    except Exception:
        return []
    if isinstance(data, dict) and "modList" in data:
        data = data["modList"]
    if isinstance(data, dict):
        data = [data]
    return data if isinstance(data, list) else []


def classify(name):
    low = name.lower()
    if name in REMOVED:
        return REMOVED[name]
    if name in ADDED:
        return "KEEP"
    if any(h in low for h in REVIEW_HINTS):
        return "REVIEW"
    if any(h in low for h in LIBRARY_HINTS):
        return "LIBRARY"
    return "KEEP"


def inspect_jar(path, base, classification=None):
    infos, manifest = [], ""
    suspicious = []
    try:
        with zipfile.ZipFile(path) as zf:
            names = zf.namelist()
            candidates = [n for n in names if n == "mcmod.info" or n.endswith("/mcmod.info")]
            for candidate in candidates:
                infos.extend(parse_info(zf.read(candidate)))
            if "META-INF/MANIFEST.MF" in names:
                manifest = zf.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
            if any(n.endswith(".jar") for n in names):
                suspicious.append("contains nested JAR")
    except zipfile.BadZipFile:
        suspicious.append("invalid ZIP/JAR")
    mods = []
    deps = []
    for info in infos:
        if not isinstance(info, dict):
            continue
        entry_deps = []
        for key in ("requiredMods", "dependencies", "dependants"):
            value = info.get(key, [])
            if isinstance(value, str):
                value = [value]
            entry_deps.extend(value or [])
        deps.extend(str(v) for v in entry_deps)
        mods.append({
            "id": info.get("modid") or info.get("modId"),
            "name": info.get("name"),
            "version": info.get("version"),
            "minecraft": info.get("mcversion"),
            "dependencies": sorted(set(str(v) for v in entry_deps)),
        })
    core_plugin = re.search(r"^FMLCorePlugin:\s*(.+)$", manifest, re.M)
    record = {
        "file": str(path.relative_to(base)),
        "filename": path.name,
        "size": path.stat().st_size,
        "sha256": sha256(path),
        "classification": classification or classify(path.name),
        "mods": mods,
        "declared_dependencies": sorted(set(deps)),
        "core_plugin": core_plugin.group(1).strip() if core_plugin else None,
        "suspicious": suspicious,
    }
    source = ADDED.get(path.name)
    if source:
        record.update(source)
    else:
        record["source"] = "Inherited from audited local Tekkit 2 v1.2.6 instance"
        record["redistribution"] = "Inherited third-party binary; verify author terms before any export."
    return record


def config_hashes(base):
    rows = []
    for directory in ("config", "scripts", "groovy", "resources", "resourcepacks"):
        root = base / directory
        if not root.exists():
            continue
        for path in sorted(p for p in root.rglob("*") if p.is_file()):
            rows.append({"file": str(path.relative_to(base)), "sha256": sha256(path), "size": path.stat().st_size})
    return rows


def jar_paths(base):
    return sorted((base / "mods").rglob("*.jar"), key=lambda p: str(p).lower())


def write_lock(name, base, mode):
    paths = jar_paths(base)
    if mode == "technical":
        paths = [p for p in paths if p.name not in REMOVED and p.name not in ADDED]
    records = [inspect_jar(p, base) for p in paths]
    known_ids = {}
    duplicates = []
    for record in records:
        for mod in record["mods"]:
            modid = mod.get("id")
            if not modid:
                continue
            if modid in known_ids:
                duplicates.append({"mod_id": modid, "files": [known_ids[modid], record["file"]]})
            else:
                known_ids[modid] = record["file"]
    payload = {
        "schema": 1,
        "pack": "Industrial Civilization" if mode == "final" else "Tekkit 2 baseline",
        "instance_root": str(ROOT),
        "source_tree": str(base),
        "minecraft": "1.12.2",
        "forge": "14.23.5.2860",
        "java": "8",
        "mode": mode,
        "mod_count_jars": len(records),
        "mods": records,
        "duplicate_detected_mod_ids": duplicates,
        "configuration_hashes": config_hashes(base),
    }
    OUT.mkdir(exist_ok=True)
    (OUT / name).write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


write_lock("baseline-mod-lock.json", SNAPSHOT, "baseline")
write_lock("technical-baseline-mod-lock.json", SNAPSHOT, "technical")
write_lock("final-mod-lock.json", ROOT, "final")
