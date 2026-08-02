# Hot Reload Workflow

GroovyScript 1.4.3 owns the custom, frequently edited integration layer in `groovy/postInit/industrial_civilization.groovy`. After the one startup needed to load GroovyScript, keep the game and disposable test world open while editing that file.

1. Save the Groovy file in both the working tree and the active Technic instance.
2. In the world chat, run `/gs reload --clean` (or use Command+R on macOS).
3. Confirm the reload succeeds, then reopen HEI or the crafting view and inspect the affected recipe/item.
4. If class caching behaves strangely, run `/gs deleteScriptCache`, followed by `/gs reload --clean`.
5. If reload reports an error, inspect `logs/groovy.log`; the prior successful recipe state should be treated as the baseline until the script is corrected.

Reloadable here: vanilla crafting additions/removals, AE2 crafting-table lockout, Iron Chest recipe/sidebar restrictions, HEI visibility, and the two custom tooltips.

Not reloadable here: Forge/JAR additions, custom Java block or tile-entity code, registry names, translations or packaged assets inside a JAR, most mod configuration, and anything executed before post-init. Those changes still require rebuilding/copying the relevant JAR and restarting Minecraft.
