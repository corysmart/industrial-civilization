#!/usr/bin/env python3
"""Run natural structure/road acceptance across deterministic fresh-world seeds."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GAME = ROOT / ".headlessmc/game"
OUT = ROOT / "artifacts/worldgen-multiseed"
SEEDS = (8675309, 42, 104729, -2147483648, 987654321)


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    failures: list[int] = []
    reload_saved = False
    for seed in SEEDS:
        env = os.environ.copy()
        env["IC_E2E_WORLD_SEED"] = str(seed)
        print(f"MULTISEED: starting seed {seed}", flush=True)
        result = subprocess.run(
            [sys.executable, str(ROOT / "tools/e2e/run_rendered_scenario.py"),
             "worldgen_natural_road_review"], cwd=ROOT, env=env)
        seed_dir = OUT / f"seed-{seed}"
        seed_dir.mkdir(parents=True, exist_ok=True)
        log = ROOT / ".headlessmc/direct-rendered.log"
        if log.is_file(): shutil.copy2(log, seed_dir / "rendered.log")
        screenshots = GAME / "screenshots/worldgen-natural-review"
        if screenshots.is_dir():
            shutil.copytree(screenshots, seed_dir / "screenshots", dirs_exist_ok=True)
        world = GAME / "saves/ic-e2e-worldgen_natural_road_review"
        if result.returncode == 0 and world.is_dir() and not reload_saved:
            shutil.copytree(world, OUT / "reload-seed-world", dirs_exist_ok=True)
            (OUT / "reload-seed.txt").write_text(str(seed) + "\n", encoding="utf-8")
            reload_saved = True
        if result.returncode != 0: failures.append(seed)
    print(f"MULTISEED: seeds={len(SEEDS)} failures={failures}", flush=True)
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
