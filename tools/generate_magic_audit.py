#!/usr/bin/env python3
"""Render the complete baseline classification table from the lock files."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
baseline = json.loads((ROOT / "manifest/baseline-mod-lock.json").read_text())
technical = json.loads((ROOT / "manifest/technical-baseline-mod-lock.json").read_text())

special = {
    "ProjectE-1.12.2-PE1.4.8-14-technic.jar": "Primary magical transmutation/EMC progression; removed.",
    "ViewEMC-1.12.2-v8.jar": "ProjectE-only EMC display integration; removed as isolated dependent.",
    "ic2cuumatter-1.12.2-1.1.3.jar": "Makes UU-Matter progression available before the AI Age; removed.",
    "AkashicTome-1.2-12.jar": "Retained after review: documentation aggregation only; ProjectE entry repaired.",
    "SnowRealMagic-1.12.2-0.7.4.jar": "Retained after review: physical/aesthetic snow behavior, no magic progression.",
    "fairylights-2.2.0-1.12.2.jar": "Retained after review: decorative lighting only, no magic progression.",
}

lines = [
    "# Magic Removal Audit",
    "",
    "The immutable baseline contained 155 active JARs. Every JAR was classified below. "
    "ProjectE, ViewEMC, and the IC2 Classic UU-Matter add-on were removed from `mods/` and the Technic cache; "
    "ProjectE configuration and CraftTweaker integrations were removed or repaired. No retained JAR declared a required dependency on these files.",
    "",
    "The rollback snapshot intentionally still contains the original files. Historical logs and baseline manifests may also mention them; neither is loaded by Forge.",
    "",
    "| Baseline JAR | Class | Decision |",
    "|---|---:|---|",
]
for record in baseline["mods"]:
    name = record["filename"]
    cls = record["classification"]
    if name in special:
        decision = special[name]
    elif cls == "LIBRARY":
        decision = "Required/support library retained with its technical dependents."
    elif cls == "REVIEW":
        decision = "Mixed naming or presentation reviewed; retained only because active functionality is decorative/technical."
    else:
        decision = "Technical, industrial, scientific, logistical, building, interface, atmosphere, or compatibility functionality retained."
    lines.append(f"| `{name}` | {cls} | {decision} |")

lines += [
    "",
    "## Removed functional references",
    "",
    "- Deleted the ProjectE guide entry and covalence-dust Akashic Tome recipe.",
    "- Deleted all Alchemical Bag recoloring integrations.",
    "- Deleted ProjectE EMC mappings and the complete `config/ProjectE/` tree.",
    "- Deleted the obsolete UU-Matter tooltip integration.",
    "- Removed the three corresponding download-cache archives so this local Technic instance cannot restore them from its cache.",
    "- Updated `bin/extractedFiles.json` separately to remove active extraction records for deleted files.",
    "",
    "## Deferred vanilla content",
    "",
    "Vanilla enchanting, potions, supernatural mobs, and the Nether remain unchanged as required. None is a required shortcut around the Moon/Mars material chain.",
]
(ROOT / "docs/MAGIC_REMOVAL_AUDIT.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
