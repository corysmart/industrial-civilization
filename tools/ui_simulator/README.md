# Industrial Civilization UI Simulator

This browser simulator renders every first-party Industrial Civilization UI
without starting Minecraft, Forge, or Technic:

- the shared industrial machine container and HEI side panels;
- Factions & Settlements;
- terrain warmup;
- AI Age credits;
- Better Questing backgrounds, nodes, links, zoom, and edge clamping.

It reads the live repository sources: machine capacities, faction descriptions,
English localization, Better Questing JSON, GUI textures, quest backgrounds,
and Minecraft 1.12.2's installed bitmap font. Source and asset changes reload the
open simulator automatically within about one second.

Start the interactive simulator:

```sh
python3 tools/ui_simulator/server.py
open http://127.0.0.1:43127
```

Run the automated matrix (five display sizes, GUI scales 1–4 and Auto, every
machine, and every faction) and export review images:

```sh
python3 tools/ui_simulator/audit.py --screenshots
```

The command exits nonzero if text overlaps or a screen clips its logical
viewport. Review images are written to `docs/ui-simulator/`.
