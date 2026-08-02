# IndustrialCivilizationCore

Source: `development/IndustrialCivilizationCore/`  
Runtime JAR: `mods/IndustrialCivilizationCore-0.1.0.jar`

Responsibilities implemented:

- Pack identity/version metadata.
- Molecular Analyzer block, energy buffer, Desh validation, pattern-record item, and completion flag.
- ComputerCraft peripheral for analyzer status, energy, progress, and environmental readings.
- Moon/Mars dimension telemetry stored on the player; a Mars visit without a recorded Moon visit emits a sequence-violation message and log entry.
- Persistent Lite Matter completion telemetry in player NBT.

The reloadable GroovyScript file `groovy/postInit/industrial_civilization.groovy` implements the parts that do not require Java: AE2 lockout, chest restrictions, representative firearm recipes, the cross-mod Analyzer recipe, and slice tooltips. Run `/gs reload --clean` after editing it. Galacticraft's normal Tier 1 → Moon dungeon schematic → Tier 2 route remains authoritative; the integration layer validates the observed order instead of rewriting rocket mechanics.

Build command (does not launch Minecraft):

```text
JAVA_HOME=<Java-8-JDK> GRADLE_USER_HOME=<project-cache> ./gradlew clean build
```

The successful build used Java 8 and ForgeGradle against Forge `14.23.5.2860`. The final JAR was reobfuscated and ZIP-validated.
