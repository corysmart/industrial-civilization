# Final Manual Test Checklist

## Startup

1. In Technic Launcher, select the customized Tekkit 2 instance at `/Users/cory/Library/Application Support/technic/modpacks/tekkit-2`.
2. Confirm Java 8 and allocate at least 4 GiB if available (the old metadata requests 3 GiB; Techguns, Custom NPCs, AE2, and Better Questing increase load).
3. Launch once and wait at the title screen. Do not open the pre-existing `New World` save.
4. Confirm the menu label reads `Industrial Civilization v0.1.0` and inspect Loaded Mods for 159 JAR files / the expected mod IDs, including GroovyScript 1.4.3.
5. Open a disposable world, run `/gs reload --clean`, and confirm chat reports a successful reload with no errors. Review `logs/groovy.log` if it does not.
6. Confirm the welcome chat identifies **F6** as the Industrial Civilization Guide key. Press F6 and verify the ten-quest tutorial/reference line opens without a key conflict.
7. Open Options → Controls and confirm the Conflicts section is empty. On macOS, spot-check F6 (guide), Option+Y (Techguns reload), Command+R (GroovyScript reload), Option+R (inventory sort), and Command+[/]/;/0/' (Waila).

## Disposable world

- Create a new disposable survival world.
- Open the quest UI and confirm all ten chapters appear in order.
- Confirm ProjectE/EMC searches return no active items and AE2 recipes are absent.
- Confirm Gold, Diamond, Crystal, and Obsidian Chest recipes are absent; Copper/Iron remain.
- Confirm pistol, combat shotgun, M4, and ammunition recipes resolve without missing ingredients.
- Place/energize Techguns Ammo Press and one representative gun; verify reload/fire behavior in a safe test area.
- Use the NPC Wand to instantiate the three faction blueprints; verify neutral trade, hostile ranged combat, warning/trespass hostility, and persistence after reload.
- Verify IC2 energy values and reactor metadata through Plethora; test SCRAM with no fuel first.
- Verify Galacticraft Tier 1 selection cannot directly choose Mars and that Moon dungeon progression supplies the Tier 2 path.
- Give test-only Moon meteoric iron and Mars Desh only after independently validating the travel order; craft the Analyzer, charge it to 50,000 FE, analyze a second Desh ingot, and inspect the pattern record.
- Attach a wired modem to the Analyzer and call all documented methods.

If startup fails, return `logs/latest.log`, the newest file in `crash-reports/`, and the visible Technic error.
