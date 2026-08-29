# Industrial Civilization UI Simulator

This browser simulator renders every first-party Industrial Civilization UI
without starting Minecraft, Forge, or Technic:

- the branded main menu and the injected pause-menu layout;
- the shared industrial machine container and HEI side panels, including every
  machine plus original, Cargo Policy, congestion, service-operating, and
  service-suspended comparison states;
- Factions & Settlements;
- terrain warmup;
- AI Age credits;
- Better Questing home art, backgrounds, nodes, links, zoom, and edge clamping;
- the filtered Industrial Civilization advancements screen; and
- the Galacticraft space-map restrictions.

It reads the live repository sources: machine capacities, faction descriptions,
English localization, Better Questing JSON, GUI textures, quest backgrounds,
Custom Main Menu configuration, and Minecraft 1.12.2's installed bitmap font.
Source and asset changes reload the open simulator automatically within about
one second. Changes to the simulator server itself require restarting the small
Python server; production GUI and asset changes do not.

Start the interactive simulator:

```sh
python3 tools/ui_simulator/server.py
open http://127.0.0.1:43127
```

Run the automated matrix (five display sizes, GUI scales 1–4 and Auto, all ten
screen families, every machine, all four 0.9.0 operations states, and every
faction) and export review images:

```sh
python3 tools/ui_simulator/audit.py --screenshots
```

The command exits nonzero if text overlaps or a screen clips its logical
viewport. Review images are written to `docs/ui-simulator/`.
