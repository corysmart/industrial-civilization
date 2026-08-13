# IndustrialCivilizationCore

Source: `development/IndustrialCivilizationCore/`  
Runtime JAR: `mods/IndustrialCivilizationCore-0.3.0.jar`

Responsibilities implemented:

- Pack identity/version metadata.
- Molecular Analyzer block, energy buffer, Desh validation, pattern-record item, and completion flag.
- ComputerCraft peripheral for analyzer status, energy, progress, and environmental readings.
- Moon/Mars dimension telemetry stored on the player; a Mars visit without a recorded Moon visit emits a sequence-violation message and log entry.
- Persistent Lite Matter completion telemetry in player NBT.
- Native IC2 EU-powered research and manufacturing machines with sided inventories, progress, GUI state, and ComputerCraft peripherals. Forge Energy support is intentionally invisible compatibility plumbing.
- Environment-tagged orbital, lunar, and Martian research data and real archive synthesis.
- Persistent Moon and Mars research gates with a documented testing bypass.
- Rare abandoned-factory world structures, criminal defense encounters, staged repair, and alternate industrial-capacity outputs.
- Real AI Core synthesis and durable AE2 recipe authorization.
- Original IC2-adjacent block, item, and GUI pixel art plus an offline asset review harness.

The reloadable GroovyScript files implement chest restrictions, firearms, Analyzer construction, complete AI-gated AE2 catalog recipes, the Technical Phase Pearl, and tooltips. Run `/gs reload --clean` after editing them. Java owns the Galacticraft route: Tier 1 reaches orbit/Moon, Orbital Research gates Moon selection, and Mars Mission Authorization unlocks the Tier 2 NASA Workbench page because lunar dungeons are intentionally absent.

Build command (does not launch Minecraft):

```text
JAVA_HOME=<Java-8-JDK> GRADLE_USER_HOME=<project-cache> ./gradlew clean build
```

The successful build used Java 8 and ForgeGradle against Forge `14.23.5.2860`. The final JAR was reobfuscated and ZIP-validated.
