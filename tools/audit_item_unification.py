#!/usr/bin/env python3
"""Fail when known overlapping material families escape the canonical pack policy."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
techguns = (ROOT / "config/Techguns.cfg").read_text()
icbm = (ROOT / "groovy/postInit/industrial_icbm.groovy").read_text()
crafttweaker = (ROOT / "scripts/tekkit2.zs").read_text()

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

if errors:
    raise SystemExit("ITEM UNIFICATION AUDIT FAILED\n- " + "\n- ".join(errors))
print("ITEM UNIFICATION AUDIT: canonical metals, circuits, wire, batteries, and rubber enforced")
