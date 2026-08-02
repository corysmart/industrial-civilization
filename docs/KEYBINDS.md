# Curated macOS Keybinds

This layout targets a Mac keyboard without a numpad. Forge calls Command `CONTROL` internally and Option `ALT`; this document uses the Mac names.

The layout was derived from Forge's live `KeyBinding.conflicts` results, not only duplicate key codes. It avoids Shift-modified actions because Forge treats them as conflicts with Sneak, moves IC2's standalone `ALT Key` away from Option, and never modifies a key that has a conflicting plain in-world action.

| Action | Binding |
|---|---|
| Industrial Civilization / Better Questing guide | F6 |
| CustomNPC quest log | F8 |
| IC2 Classic mode switch | F10 |
| IC2 Classic armor modifier (`ALT Key`) | Backslash |
| IC2 Classic boost | Option+H |
| IC2 Classic hub expansion | Option+O |
| Galacticraft spaceship inventory | Command+I |
| HEI bookmark | Option+I |
| AE2 toggle search focus | Command+P |
| Schematica control | Option+P |
| Techguns reload | Option+Y |
| VoxelMap waypoint hotkey | Command+Y |
| Inventory Tweaks sort | Option+R |
| GroovyScript reload | Command+R |
| Recipe switch | Option+S |
| Waila display/liquid/recipe/config/usage | Command+[/]/;/0/' |
| Railcraft locomotive reverse/slower/mode/whistle | Option+[/]/;/' |
| Railcraft locomotive faster | Option+period |

CustomNPC scene controls, music-player shortcuts, Schematica's modified pick-block shortcut, GroovyScript's copy shortcut, and the clear-toast shortcut are unbound. They are niche or redundant and were responsible for conflicts with core controls; their corresponding GUIs or commands remain available.

The core mod migrates a binding only while it still matches a known inherited or previously shipped value, preserving later player choices.
