#!/usr/bin/env python3
"""Create an isolated HeadlessMC game directory backed by the live private pack."""
from __future__ import annotations
import os
import shutil
import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = Path(os.environ.get("IC_TECHNIC_PACK",
    "/Users/cory/Library/Application Support/technic/modpacks/tekkit-2")).resolve()
TARGET = (ROOT / ".headlessmc/game").resolve()
CACHE_ROOT = (ROOT / ".headlessmc/pack-cache").resolve()

# These alter low-level audio/render classes that HeadlessMC intentionally replaces.
# They remain installed and tested by normal Technic; only the headless shadow excludes them.
HEADLESS_EXCLUSIONS = {
    "EntityCulling-1.12.2-6.5.0.jar",
    "Loading-Progress-Bar-v1.0-mc[1.8-1.12.2].jar",
    "Nothirium-1.12.2-0.4.7-beta.jar",
    "RenderLib-1.12.2-1.4.5.jar",
    "modernsplash-1.12.2-1.3.1.jar",
    "moresoundconfig-1.0.4.jar",
    "naughthirium-2.3.0.jar",
    "particleculling-1.12.2-v1.4.3.jar",
}

def reset_target() -> None:
    expected = (ROOT / ".headlessmc/game").resolve()
    if TARGET != expected or ROOT not in TARGET.parents:
        raise RuntimeError(f"refusing to reset unexpected path: {TARGET}")
    if TARGET.exists(): shutil.rmtree(TARGET)
    TARGET.mkdir(parents=True)

def copy_if_present(name: str) -> None:
    source = SOURCE / name
    target = TARGET / name
    if source.is_dir(): shutil.copytree(source, target, symlinks=True)
    elif source.is_file(): shutil.copy2(source, target)

def configure_scenario(scenario: str) -> None:
    runtime = TARGET / "config/industrialcivilization/runtime.cfg"
    if not runtime.is_file(): raise SystemExit(f"runtime config missing: {runtime}")
    text = runtime.read_text()
    if "S:autoScenario=" not in text:
        raise SystemExit("runtime config does not expose testing.autoScenario")
    lines = [f"    S:autoScenario={scenario}" if line.strip().startswith("S:autoScenario=") else line
             for line in text.splitlines()]
    runtime.write_text("\n".join(lines) + "\n")

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--scenario", default="", help="dev-only scenario to run after joining a disposable world")
    args = parser.parse_args()
    if not (SOURCE / "mods").is_dir(): raise SystemExit(f"pack missing: {SOURCE}")
    reset_target()
    CACHE_ROOT.mkdir(parents=True, exist_ok=True)
    for name in ("config", "groovy", "scripts", "customnpcs", "options.txt"):
        copy_if_present(name)
    for name in ("resources", "resourcepacks"):
        source = SOURCE / name
        if source.exists(): (TARGET / name).symlink_to(source, target_is_directory=True)
    # Preserve expensive HEI/VintageFix indexes between disposable-world runs.
    for name in ("cache", "vintagefix"):
        seed = CACHE_ROOT / name
        source = SOURCE / name
        if not seed.exists() and source.is_dir(): shutil.copytree(source, seed, symlinks=True)
        if seed.exists(): (TARGET / name).symlink_to(seed, target_is_directory=True)
    mods = TARGET / "mods"
    mods.mkdir()
    staged = 0
    for source in sorted((SOURCE / "mods").glob("*.jar")):
        if source.name in HEADLESS_EXCLUSIONS: continue
        (mods / source.name).symlink_to(source)
        staged += 1
    if args.scenario: configure_scenario(args.scenario)
    for name in ("logs", "crash-reports", "saves", "screenshots"):
        (TARGET / name).mkdir()
    print(f"HEADLESS PACK: {staged} mod JARs staged; scenario={args.scenario or 'none'}; excluded {sorted(HEADLESS_EXCLUSIONS)}")
    print(TARGET)

if __name__ == "__main__": main()
