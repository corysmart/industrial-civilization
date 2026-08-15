# Industrial Civilization: Astra

Industrial Civilization: Astra is a progression-focused Forge 1.12.2 modpack
about growing a survival workshop into an interplanetary industrial
civilization. It began from the Tekkit 2 technical baseline, but it is now its
own source project, release archive, and hidden Technic pack.

The current pack release is **0.3.0**. Its complete 16-chapter numbered
campaign has passed live acceptance in `Test Bed 1`: all 111 numbered Better
Questing tasks completed, the final `continuous_civilization` milestone
persisted after save/reload, the one-time AI credits returned to the playable
world, and post-campaign sandbox play remained available. Independent faction
and vehicle side paths still require their own full acceptance pass.

## Current distribution status

- Technic pack: **Industrial Civilization: Astra** (`industrial-civilization-astra`)
- Pack version: **0.3.0**
- Minecraft: **1.12.2**, Forge **14.23.5.2860**, Java **8**
- GitHub release: `v0.3.0`
- Visibility: hidden internal alpha while redistribution review, multiplayer
  QA, and final balance work continue
- Installed Technic instance:
  `/Users/cory/Library/Application Support/technic/modpacks/industrial-civilization-astra`

The older `tekkit-2` directory was the pre-pack development installation. It
is retained only as legacy/migration context and is no longer the deployment
or testing target.

The release archive contains the full permitted pack payload. Techguns is not
rehosted; ModDirector retrieves the pinned official CurseForge file on first
launch and verifies its SHA-256 before Forge starts. See
`docs/TECHNIC_PLATFORM_PAGE.md`, `docs/ADDED_MODS.md`, and
`manifest/final-mod-lock.json` for packaging, dependency, and hash details.

## What is implemented

- 16 numbered chapters and 7 independent side paths across Earth, orbit, the
  Moon, Mars, Lite Matter Engineering, the AI Age, and post-AI civilization
- 129 automatic capability milestones with no manual checkbox objectives
- One Industrial Civilization advancement page with 324 connected visible
  advancements; obsolete, duplicate, magical, End-only, and fake-monster
  objectives are hidden, removed, or replaced
- IC2-centered EU power, manufacturing, monitored nuclear engineering,
  research, programmable automation, matter technology, and AE2 endgame
- Functional orbital, lunar, and Martian infrastructure gates backed by
  sustained telemetry and active Galacticraft Oxygen Detector proof
- Human Earth and space factions, settlements, cities, fabrication centers,
  militia, robbers/space pirates, vehicles, factories, trade, reputation, and
  equipment drops
- Continued sandbox play after the main campaign milestone

The authoritative progression is the machine-readable `progression/` tree.
The consolidated design and implementation reference is
`docs/GAME_DESIGN_DOCUMENT.md`; live campaign evidence is recorded in
`docs/MAIN_QUESTLINE_LIVE_TEST_LOG.md`.

## Repository scope

This repository tracks first-party source, configuration, quests, Groovy
integration, resources, documentation, validation tools, release metadata, and
locked mod manifests. It intentionally excludes Minecraft saves, player data,
logs, launcher caches, credentials, compiled output, and third-party binaries
that cannot be rehosted.

The Git working tree is `/Users/cory/Documents/tekkit-3`. Treat the installed
Technic instance as a generated deployment target, never as source authority.

## Generate and validate

Use the bundled Python environment when Pillow is not installed in the system
interpreter. The canonical generation and validation sequence is:

```sh
python3 tools/generate_ic2_assets.py
python3 tools/generate_runtime_advancements.py
python3 tools/generate_unified_advancements.py
python3 tools/generate_objectives.py
python3 tools/generate_progression_docs.py
python3 tools/validate_runtime_content.py
python3 tools/validate_unified_advancements.py
python3 tools/validate_energy_interop.py
python3 tools/validate_progression.py
python3 tools/static_validate.py
python3 tools/e2e/preflight.py
```

At the current 0.3.0 state these checks report 801 runtime-content checks,
324 connected visible advancements, 22 energy-interoperability checks, 3,540
progression checks, 1,834 static checks, and 8 HeadlessMC preflight checks.
Offline checks complement—not replace—the live evidence and scenario lists in
`docs/E2E_TESTING.md`, `docs/MAIN_QUESTLINE_TEST_PLAN.md`, and
`docs/MANUAL_TEST_CHECKLIST.md`.

## UI development

Custom screens and quest layouts can be previewed without restarting
Minecraft. The hot-reloading simulator uses the real pack data, textures, and
Minecraft bitmap font:

```sh
python3 tools/ui_simulator/server.py
open http://127.0.0.1:43127
python3 tools/ui_simulator/audit.py --screenshots
```

See `tools/ui_simulator/README.md` for the display-size and GUI-scale audit.
