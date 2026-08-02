# Baseline Audit

## Instance identity

- Absolute root: `/Users/cory/Documents/tekkit-3`
- Launcher layout: Technic Launcher legacy instance (`bin/`, `cache/`, `mods/`, `config/`, `resources/`)
- Pack metadata before conversion: Tekkit 2 `1.2.6` in `bin/version`
- Minecraft: `1.12.2`
- Forge: `14.23.5.2860` from `bin/version.json`
- Configured Java: Java 8; launcher memory request 3072 MiB in `bin/runData`
- Immutable rollback snapshot: `.backups/pre-industrial-civilization/` (217 MiB; caches and logs intentionally excluded)

The directory passed the copied-instance check: it contains the Minecraft client JAR, Forge modpack JAR, Technic extraction ledger, 155 active mod JARs, 288 configuration files, a CraftTweaker script, launcher resources, resource packs, logs, and one existing save.

## Existing saves and logs

- `saves/New World/` existed before work and was not opened or used for validation.
- Existing `logs/latest.log`, compressed logs, and `crafttweaker.log` are historical baseline evidence only. They do not validate this build.
- No launcher, Forge client, or server was started.

## Relevant functionality found

The baseline already supplied IC2 Classic, Advanced Solars/advanced-machine functionality, GraviSuite, Energy Control, full BuildCraft, Additional Pipes, ProjectRed, Logistics Pipes, Railcraft, MFFS, Galacticraft, CC:Tweaked, Computronics, Plethora, Wireless Redstone, chunk loading, Reforged, Inventory Tweaks, VoxelMap, MAtmos/SpaceAmbient, and numerous 1.12.2 compatibility/performance patches.

The baseline did not supply AE2, a comprehensive firearm framework, a human faction authoring framework, Better Questing, or the requested final scientific machine.

## Suspicious and duplicate review

- No duplicate declared mod IDs were found.
- `ForgeMultipart-1.12.2-2.6.3.1-universal.jar` and `moresoundconfig-1.0.4.jar` contain nested JAR resources. Their declared technical dependents and baseline role justify retention; this is recorded, not treated as a duplicate installation.
- Nineteen baseline JARs lacked parseable `mcmod.info`; their filenames, hashes, manifests/core-plugin declarations, and local provenance are still locked.
- Performance/coremod files were retained because removing them without a runtime test would risk the higher-priority Tekkit systems.

## Authoritative inventory

`manifest/baseline-mod-lock.json` contains every baseline JAR filename, detected IDs, versions, declared dependencies, SHA-256, size, classification, core-plugin marker, configuration/resource hashes, and local provenance. It is generated from the immutable snapshot, not reconstructed from a published Tekkit manifest.
