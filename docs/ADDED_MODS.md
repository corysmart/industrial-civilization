# Added Mods

| File | Purpose | Source file ID | Static compatibility |
|---|---|---:|---|
| `appliedenergistics2-rv6-stable-7.jar` | Future AI Age content, fully recipe-locked | 2747063 | Declares MC 1.12.2 |
| `techguns-1.12.2-2.0.2.0_pre3.2.jar` | Primary firearm framework | 2958103 | Declares MC 1.12.2 / Forge core plugin |
| `CustomNPCs_1.12.2-(05Jul20).jar` | Human NPC, faction, trader, dialogue authoring | 2996912 | Published for 1.12.2; metadata says 1.12 family |
| `BetterQuesting-3.5.329.jar` | Broad capability objectives | 2950248 | Declares MC 1.12.2 |
| `StandardExpansion-3.4.173.jar` | Checkbox task implementation | 2863771 | Requires Better Questing and JEI; both present |
| `groovyscript-1.4.3.jar` | In-game reloadable recipe/HEI integration | 7925117 | Java 8 bytecode; requires MixinBooter 10.6, already present |
| `!mod-director-launchwrapper-1.8.3.jar` | First-launch retrieval of author-hosted restricted dependencies | Modrinth `b8pITnja` | Minecraft 1.7.2–1.12.2; MIT; client/server LaunchWrapper bootstrap |
| `vehicle-mod-1.4.0-1.12.2.jar` | Onysd Vehicles driving/physics base and six curated vehicle chassis | Modrinth `LuvveCaI` | Minecraft 1.12.2; LGPL-2.1-or-later source available |
| `ICBM-classic-1.12.2-6.5.5.jar` | Strategic missiles, launch control, radar and defense hardware; duplicate parts hidden and recipes rebuilt around IC2 | CurseForge file `8177228` | Minecraft 1.12.2; the bundled asset license expressly permits inclusion in a modpack; preserve upstream credits and ship the official unmodified JAR |
| `obfuscate-0.4.2-1.12.2.jar` | Required vehicle animation/network library | 2916310 | Minecraft 1.12.2; GPL-3.0 |
| `IndustrialCivilizationCore-0.6.1.jar` | First-party progression, research, factory salvage, machine, gate, and UI integration | local source | Compiled against Forge 14.23.5.2860 / Java 8 |
| `connectedglass-1.1.8-forge-mc1.12.jar` | Crafted connecting, clear, and styled habitat glass and panes | CurseForge file `4771346` | Minecraft 1.12.2; last native-renderer release before the Fusion migration, intentionally pinned for compatibility with the pack's optimized legacy renderer; fetched from CurseForge by ModDirector |
| `_supermartijn642corelib-1.1.21-forge-mc1.12.jar` | Shared runtime dependency for Connected Glass | CurseForge file `7783295` | Minecraft 1.12.2; fetched from CurseForge by ModDirector |

All downloaded archives passed ZIP integrity checks before installation. Exact SHA-256 values and redistribution notes are in `manifest/final-mod-lock.json`. ICBM Classic 6.5.5 is cleared for modpack inclusion by its bundled license. Techguns remains All Rights Reserved and its upstream license forbids reuploading, so a public binary pack obtains it through an author-authorized download rather than embedding its JAR in a GitHub archive. Connected Glass explicitly permits modpack use; its official pre-Fusion 1.1.8 JAR and Core Lib dependency are pinned to their CurseForge files through ModDirector rather than rehosted.

Onysd Vehicles was selected because its actively maintained 1.12.2 source and LGPL license permit this first-party extension. Obfuscate is its required runtime dependency. Distribution must preserve both upstream notices and their source-license obligations.
