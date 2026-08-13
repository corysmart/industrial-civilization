# Unified Industrial Civilization Advancements

Industrial Civilization is the pack's only visible advancement page. The build
ports the displayed Minecraft, IC2, Galacticraft, Applied Energistics 2,
Railcraft, and BuildCraft advancement lines beneath era-appropriate Industrial
Civilization milestones.

The original advancement identifiers are retained as display-less compatibility
entries. Their native triggers, saved progress, recipe grants, and commands keep
working. A server-side synchronizer mirrors completed originals into their
visible `industrialcivilizationcore:ported/...` counterparts on login, every
five seconds, and immediately after an Industrial Civilization runtime award.
The visible mirror uses an internal criterion and is granted only after its
selected campaign parent is complete, so native content cannot appear out of
order. This also migrates existing worlds without editing player data.

The generator preserves each usable native parent chain, then connects its
earliest reachable node to an Industrial Civilization era:

- Minecraft survival begins at the campaign root.
- Railcraft and BuildCraft begin after Early Autocrafting.
- IC2 begins after the First IC2 Generator.
- Galacticraft begins after Monitored Nuclear Power.
- AE2 begins at AI Age Entry.

Advancements are removed when the pack deliberately makes their condition
invalid: the disabled End, magical enchanting/potion/boss tasks, native-monster
kills, Galacticraft's duplicate solar panels, removed Moon dungeon/buggy content,
and Railcraft Firestone. Vanilla Balanced Diet is also removed because it
requires hostile-monster food and End-only chorus fruit. Postmortal is replaced
by the AI Emergency Continuity Core, and IC2 Endgame Paradise is replaced by a
Mars Cultivation Terraformer objective.

Regenerate and verify with:

```bash
python3 tools/generate_unified_advancements.py
python3 tools/validate_unified_advancements.py
```
