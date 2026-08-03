# Industrial Civilization

Source-controlled overlay for the Industrial Civilization vertical slice built
on the Technic Tekkit 2 Minecraft 1.12.2 pack.

This repository tracks the first-party mod source, configuration, quests,
Groovy integration, resources, documentation, validation tools, and locked mod
manifests. It intentionally excludes Minecraft saves, player data, logs,
launcher caches, credentials, compiled output, and third-party mod binaries.

The local development tree is `/Users/cory/Documents/tekkit-3`. The installed
Technic test instance is `/Users/cory/Library/Application Support/technic/modpacks/tekkit-2`
and should be treated as a deployment target rather than the Git working tree.

See `docs/ADDED_MODS.md` for added dependencies, `manifest/final-mod-lock.json`
for exact hashes, and `docs/MANUAL_QUEST_TEST_CHECKLIST.md` for Phase 2 verification.

The authoritative Phase 2 progression is the machine-readable `progression/`
tree. Regenerate its Better Questing projection and documentation with:

```sh
python3 tools/generate_ic2_assets.py
python3 tools/generate_runtime_advancements.py
python3 tools/generate_objectives.py
python3 tools/generate_progression_docs.py
python3 tools/validate_runtime_content.py
python3 tools/validate_energy_interop.py
python3 tools/validate_progression.py
python3 tools/static_validate.py
```

The asset generator derives every custom registry object's original 16×16 sprite,
block model, and the shared IC2-styled machine GUI from
`progression/runtime-content.json`. The runtime-content harness checks those
assets and the Java/Groovy integration without starting Minecraft, Forge,
Technic, or a server.
