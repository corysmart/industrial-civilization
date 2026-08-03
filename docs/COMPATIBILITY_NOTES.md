# Compatibility Notes

- Priority systems were left intact: IC2 Classic, BuildCraft, Galacticraft, ProjectRed, Railcraft, ComputerCraft, Tekkit patches, and MFFS.
- Existing ore-dictionary and Galacticraft wire compatibility in `scripts/tekkit2.zs` remains.
- Techguns duplicate common ingots/ores and structures are disabled; titanium remains because the baseline has no confirmed equivalent supply.
- Techguns machines require power and can use the pack's RF path; CableFlux/buildcraftfluxified already bridge the established energy ecosystem.
- The procedural faction system uses vanilla villager navigation, merchant UI, and melee equipment for stable 1.12.2 behavior. Custom NPCs remains available for optional authored scenes; direct Custom NPCs ↔ Techguns inventory/animation behavior is not a dependency of faction progression.
- Plethora already supplies IC2 energy/reactor and generic fluid/energy adapters; the integration mod does not register competing wrappers for them.
- Better Questing 3.5.329 and Standard Expansion 3.4.173 are the matching 1.12.2 generation.
- The main-menu label identifies Industrial Civilization while retaining the existing Technic/Tekkit resources and launcher format.
