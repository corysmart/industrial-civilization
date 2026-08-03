#!/usr/bin/env python3
"""Offline audit for the pack's IC2-first energy interoperability contract."""
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "development/IndustrialCivilizationCore/src/main/java/com/industrialcivilization/core"
RES = ROOT / "development/IndustrialCivilizationCore/src/main/resources"
checks = []
errors = []


def check(condition, message):
    (checks if condition else errors).append(message)


def number(path, key):
    text = path.read_text()
    match = re.search(rf"^[ \t]*[DI]:{re.escape(key)}=([0-9.]+)$", text, re.MULTILINE)
    return float(match.group(1)) if match else None


ic2 = ROOT / "config/ic2/ic2.cfg"
ae2 = ROOT / "config/AppliedEnergistics2/AppliedEnergistics2.cfg"
gc = (ROOT / "config/Galacticraft/energy.cfg").read_text()
fe_per_eu = number(ic2, "RFPerEU")
ae_fe = number(ae2, "ForgeEnergy")
ae_eu = number(ae2, "IC2")
check(fe_per_eu == 8, "IC2 Classic canonical conversion is 1 EU = 8 FE")
check(ae_fe == 0.5 and ae_eu == 4.0 and ae_eu / ae_fe == fe_per_eu,
      "AE2 IC2 and Forge Energy ratios agree with the canonical conversion")
check("S:PowerUnit=EU" in ae2.read_text(), "AE2 player-facing power display uses EU")
check('B:"Disable INPUT of IC2 energy"=false' in gc
      and 'B:"Disable OUTPUT of IC2 energy"=false' in gc,
      "Galacticraft IC2 input and output are enabled")
check('B:"Disable INPUT of Forge Energy to GC machines"=true' in gc,
      "Galacticraft exposes EU—not a competing FE UI—in this pack")

core = (JAVA / "IndustrialCivilizationCore.java").read_text()
machines = (JAVA / "TileIndustrialMachine.java").read_text()
solar = (JAVA / "TileEnvironmentalSolarArray.java").read_text()
analyzer = (JAVA / "TileMolecularAnalyzer.java").read_text()
check("FE_PER_EU = 8" in core, "custom content uses the canonical adapter ratio")
check("accepted / (double) IndustrialCivilizationCore.FE_PER_EU" in machines,
      "custom machine FE intake converts into internal EU")
check("amount / (double) IndustrialCivilizationCore.FE_PER_EU" in solar,
      "custom solar FE extraction debits the internal EU buffer correctly")
check("implements IPeripheral, IEnergySink" in analyzer
      and "ENERGY_PER_ANALYSIS_EU = 6250" in analyzer,
      "Molecular Analyzer is a native IC2 sink with a 6,250 EU operation")
check("EnergyEU" in analyzer and 'compound.getInteger("Energy") / (double)' in analyzer,
      "Molecular Analyzer migrates old FE-denominated saved energy")

jar_contracts = {
    "IC2Classic-1.12.2-1.5.11.jar": "ic2/core/block/base/tile/TileEntityRFProducer.class",
    "appliedenergistics2-rv6-stable-7.jar": "appeng/integration/modules/ic2/IC2PowerSinkAdapter.class",
    "techguns-1.12.2-2.0.2.0_pre3.2.jar": "techguns/tileentities/BasicPoweredTileEnt.class",
    "buildcraftfluxified-1.0.0.jar": "com/unicornora/buildcraftfluxified/mj/EnergyWrapperMJ.class",
    "CableFlux-1.12.2-12.2.3-technic2.jar": "tk/zeitheron/cableflux/pipes/impl/FEPipe.class",
    "MFFS-1.12.2-4.0.1.5-technic.jar": "com/nekokittygames/mffs/common/tileentity/TileEntityFEPoweredMachine.class",
}
for jar_name, class_name in jar_contracts.items():
    path = ROOT / "mods" / jar_name
    with zipfile.ZipFile(path) as archive:
        check(class_name in archive.namelist(), f"installed bridge contract: {jar_name}")

# Player-facing custom content must present itself as an IC2 expansion. FE remains
# implementation detail in source/config/developer documentation only.
player_files = [
    RES / "assets/industrialcivilizationcore/lang/en_us.lang",
    ROOT / "groovy/postInit/industrial_civilization.groovy",
    ROOT / "config/betterquesting/DefaultQuests.json",
]
for path in player_files:
    check(re.search(r"\bFE\b|Forge Energy|Redstone Flux", path.read_text()) is None,
          f"player-facing energy language is EU-only: {path.relative_to(ROOT)}")
gui = (JAVA / "GuiIndustrialMachine.java").read_text()
check('drawString("EU "' in gui, "custom machine GUI labels energy in EU")

if errors:
    print(f"ENERGY INTEROP FAILED: {len(errors)} of {len(errors) + len(checks)} checks")
    for error in errors:
        print(" - " + error)
    sys.exit(1)
print(f"ENERGY INTEROP: {len(checks)} checks passed")
print("Canonical display: EU. Hidden compatibility conversion: 1 EU = 8 FE.")
