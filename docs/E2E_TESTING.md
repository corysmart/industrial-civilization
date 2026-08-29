# Minecraft End-to-End Testing

## UI simulation without a Minecraft restart

The hot-reloading UI simulator is the fast feedback path for the branded main
menu, injected pause menu, every first-party screen, Better Questing home and
canvas, the Industrial Civilization advancements filter, and Galacticraft
space-map restrictions. It consumes repository Java data, localization, quest
JSON, Custom Main Menu configuration and textures, plus Minecraft 1.12.2's
installed bitmap font.

```sh
python3 tools/ui_simulator/server.py
open http://127.0.0.1:43127
python3 tools/ui_simulator/audit.py --screenshots
```

The 1,400-check audit covers ten screen families, five display sizes, GUI scales
1–4 and Auto, every custom machine, every faction and every quest line. It
rejects clipped panels, overlapping labels or buttons, undersized detail
columns, and quest nodes or backgrounds outside their legal canvas. Eleven
review PNGs plus six focused machine/menu comparisons are written under
`docs/ui-simulator/`. The server also discovers every first-party `Gui*` class
and fails the audit if any custom GUI is not mapped into the viewer.

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
3. The HeadlessMC runner stages the **actual assembled Astra pack** from the
   installed standalone Technic instance, creates a disposable flat world,
   inspects the branded menu and quest/advancement UI, joins a world, executes
   diagnostics, inspects HEI, and captures screenshots.
4. Mechanics that require spatial interaction—tree shapes, protected claims,
   sealed rooms, vehicles and rockets—remain real-world scenario tests driven
   by a small first-party test command/mod extension.

Implemented deterministic real-client scenarios include `workshop_adjacency`,
`earth_ecology`, and `mobile_quarry_relocation`. The quarry scenario places a
real BuildCraft Quarry beside the first-party controller in a shallow two-lane
rig carried by a real ProjectRed frame motor and frame harness. No spare Quarry
or teleport/tether block is present: the ProjectRed Block Breaker recovers the
one Quarry, hoppers deliver it to the carried Block Placer, and the controller
emits only directional redstone pulses. The scenario requires natural frame
construction, powered excavation to bedrock, sixteen individually verified
frame steps, reuse of that same Quarry item, moved output pipe/chest, second-frame
construction, and resumed excavation. A fixed fuel-burning Railcraft Standard
Worldspike then keeps the relocated lane loaded while the player is 512 blocks
away from a rig built outside vanilla spawn chunks. The rendered runner records
Minecraft's own framebuffer throughout this acceptance run. The
ecology scenario explicitly marks one zombie and skeleton
as deterministic conversion probes, then asserts that exactly one registered
robber and one registered militia patrol remain and that no source vanilla
hostiles do. This test-only marker bypasses natural density and outpost-distance
rules without changing survival spawning.

The current 0.7.0 review capture is
`artifacts/mobile-quarry-test-bed-0.7.0-no-teleport-candidate.mp4`. It shows the
real BuildCraft excavation, the visible ProjectRed harness moving sixteen
blocks, Block Placer redeployment, second-lane excavation, and the offscreen
Worldspike check. The parseable PASS marker is authoritative for item identity,
the absence of spare/teleport blocks, settled-frame coordinates, and the Forge
chunk ticket.

## Current boundary

The repository does not commit its third-party mod JARs, so public CI cannot
reconstruct the complete pack from Git alone. HeadlessMC stages the installed
`industrial-civilization-astra` Technic instance locally. It complements the
completed numbered-campaign run and manual scenario checklists; it does not
replace spatial, balance, side-path, or multiplayer acceptance.

Sources consulted: the official HeadlessMC documentation and the
`headlesshq/mc-runtime-test` project. Both are MIT-licensed tooling; neither is
bundled into the release pack.
