# Added Mods

| File | Purpose | Source file ID | Static compatibility |
|---|---|---:|---|
| `appliedenergistics2-rv6-stable-7.jar` | Future AI Age content, fully recipe-locked | 2747063 | Declares MC 1.12.2 |
| `techguns-1.12.2-2.0.2.0_pre3.2.jar` | Primary firearm framework | 2958103 | Declares MC 1.12.2 / Forge core plugin |
| `CustomNPCs_1.12.2-(05Jul20).jar` | Human NPC, faction, trader, dialogue authoring | 2996912 | Published for 1.12.2; metadata says 1.12 family |
| `BetterQuesting-3.5.329.jar` | Broad capability objectives | 2950248 | Declares MC 1.12.2 |
| `StandardExpansion-3.4.173.jar` | Checkbox task implementation | 2863771 | Requires Better Questing and JEI; both present |
| `groovyscript-1.4.3.jar` | In-game reloadable recipe/HEI integration | 7925117 | Java 8 bytecode; requires MixinBooter 10.6, already present |
| `vehicle-mod-1.4.0-1.12.2.jar` | Onysd Vehicles driving/physics base and six curated vehicle chassis | Modrinth `LuvveCaI` | Minecraft 1.12.2; LGPL-2.1-or-later source available |
| `ICBM-classic-1.12.2-6.5.5.jar` | Strategic missiles, launch control, radar and defense hardware; duplicate parts hidden and recipes rebuilt around IC2 | CurseForge file `8177228` | Minecraft 1.12.2; All Rights Reserved; installed only for this private test environment |
| `obfuscate-0.4.2-1.12.2.jar` | Required vehicle animation/network library | 2916310 | Minecraft 1.12.2; GPL-3.0 |
| `IndustrialCivilizationCore-0.2.0.jar` | First-party progression, research, factory salvage, machine, gate, and UI integration | local source | Compiled against Forge 14.23.5.2860 / Java 8 |

All downloaded archives passed ZIP integrity checks before installation. Exact SHA-256 values and redistribution notes are in `manifest/final-mod-lock.json`. Techguns is All Rights Reserved and the other third-party terms vary: this assembled local instance must not be published as a binary modpack without a fresh permission review.

Onysd Vehicles was selected because its actively maintained 1.12.2 source and LGPL license permit this first-party extension. Obfuscate is its required runtime dependency. Distribution must preserve both upstream notices and their source-license obligations.
