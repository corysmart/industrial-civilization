<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Quest Implementation

Better Questing 3 reads `config/betterquesting/DefaultQuests.json`. Run `python3 tools/generate_objectives.py` after canonical edits; never hand-edit generated quests. The current projection has 21 quest lines (16 numbered chapters and 5 independent side paths) and 115 quests.

Every quest completes automatically. Native machine operations and dimension events use hidden advancement tasks; item, construction, and cross-mod capability objectives use non-consuming Standard Expansion retrieval tasks, including multi-item evidence sets from `progression/objective-detection.json`. There are no manual checkbox tasks. All quests use `ALWAYS` visibility and locked progress, so players can browse the whole civilization plan without completing locked objectives early.

Quest IDs are deterministic from chapter/side-path order and milestone order. Cross-line prerequisites use the same global numeric map. `pack_version` is 5. Import/update the default pack through Better Questing when a world retains the older database.

The integration mod persists research artifacts on each player and returns unauthorized arrivals to Earth. Moon entry requires the Orbital Research Archive. Mars entry requires the Lunar Quantum Component and Mars Mission Authorization. `config/industrialcivilization/runtime.cfg` provides an explicit creative-testing bypass.
