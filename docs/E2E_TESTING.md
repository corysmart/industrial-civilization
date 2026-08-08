# Minecraft End-to-End Testing

Minecraft 1.12.2 predates Mojang's GameTest framework. There is therefore no
drop-in simulator that can faithfully emulate this 162-mod Forge client.

The selected real-runtime path is **HeadlessMC + HMC-Specifics**. Its current
documentation explicitly supports Forge 1.12.2 and can inspect GUIs, rendered
text, inventory slots, tooltips, chat, keys and screenshots. MC-Runtime-Test
can launch a 1.12.2 Forge client and wait for a world/chunks, but its richer
GameTest execution applies to newer Minecraft versions.

## Test layers

1. `./gradlew test` runs deterministic Java rules without Minecraft.
2. `python3 tools/static_validate.py` and the progression/runtime validators
   reject broken content graphs, registrations and source-level contracts.
3. A future HeadlessMC runner will launch the **actual assembled pack**, create
   a disposable flat world, inspect the branded menu, press F6, inspect quest
   text, open advancements, join a world, execute `/ic_status`, inspect HEI and
   capture screenshots.
4. Mechanics that require spatial interaction—tree shapes, protected claims,
   sealed rooms, vehicles and rockets—remain real-world scenario tests driven
   by a small first-party test command/mod extension.

Implemented deterministic real-client scenarios are `workshop_adjacency` and
`earth_ecology`. The ecology scenario explicitly marks one zombie and skeleton
as deterministic conversion probes, then asserts that exactly one registered
robber and one registered militia patrol remain and that no source vanilla
hostiles do. This test-only marker bypasses natural density and outpost-distance
rules without changing survival spawning.

## Current boundary

The repository does not redistribute its third-party mod JARs, so a public CI
runner cannot reconstruct the pack legally from Git alone. The first HeadlessMC
runner must stage the already-installed private Technic instance locally. It is
not yet a replacement for the manual playthrough checklist, but it is the
viable route to automated UI and launch smoke tests.

Sources consulted: the official HeadlessMC documentation and the
`headlesshq/mc-runtime-test` project. Both are MIT-licensed tooling; neither is
bundled into the release pack.
