# Added Mods

| File | Purpose | Source file ID | Static compatibility |
|---|---|---:|---|
| `appliedenergistics2-rv6-stable-7.jar` | Future AI Age content, fully recipe-locked | 2747063 | Declares MC 1.12.2 |
| `techguns-1.12.2-2.0.2.0_pre3.2.jar` | Primary firearm framework | 2958103 | Declares MC 1.12.2 / Forge core plugin |
| `CustomNPCs_1.12.2-(05Jul20).jar` | Human NPC, faction, trader, dialogue authoring | 2996912 | Published for 1.12.2; metadata says 1.12 family |
| `BetterQuesting-3.5.329.jar` | Broad capability objectives | 2950248 | Declares MC 1.12.2 |
| `StandardExpansion-3.4.173.jar` | Checkbox task implementation | 2863771 | Requires Better Questing and JEI; both present |
| `groovyscript-1.4.3.jar` | In-game reloadable recipe/HEI integration | 7925117 | Java 8 bytecode; requires MixinBooter 10.6, already present |
| `IndustrialCivilizationCore-0.2.0.jar` | First-party progression, research, factory salvage, machine, gate, and UI integration | local source | Compiled against Forge 14.23.5.2860 / Java 8 |

All downloaded archives passed ZIP integrity checks before installation. Exact SHA-256 values and redistribution notes are in `manifest/final-mod-lock.json`. Techguns is All Rights Reserved and the other third-party terms vary: this assembled local instance must not be published as a binary modpack without a fresh permission review.

No dependency mod was added unnecessarily: AE2 has no required external library here; Techguns includes its core plugin; Custom NPCs is standalone; Better Questing Standard Expansion uses the installed base and existing JEI-compatible HEI environment; GroovyScript uses the inherited `!mixinbooter-10.6.jar`.
