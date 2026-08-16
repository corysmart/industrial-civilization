<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Quest Implementation

Better Questing 3 reads `config/betterquesting/DefaultQuests.json`. Run `python3 tools/generate_objectives.py` after canonical edits; never hand-edit generated quests. The current projection has 24 quest lines (16 numbered chapters and 8 independent side paths) and 134 quests.

Every quest completes automatically. Native machine operations and dimension events use advancement-backed tasks; item, construction, and cross-mod capability objectives use non-consuming Standard Expansion retrieval tasks, including multi-item evidence sets from `progression/objective-detection.json`. There are no manual checkbox tasks. All quests use `ALWAYS` visibility and locked progress, so players can browse the whole civilization plan without completing locked objectives early.

The generator also emits one visible vanilla advancement per quest plus an Industrial Civilization root. Critical milestones form the intended chapter-order spine, while optional milestones branch from their real prerequisites. Pause > Advancements opens this vanilla tree; F6 remains the detailed Better Questing story, tutorial, and controls interface.

Quest IDs are deterministic from chapter/side-path order and milestone order. Cross-line prerequisites use the same global numeric map. `pack_version` is 15. Every generated description contains story, mission, automatic proof, and contextual Mac/no-numpad controls. Quest pictures use real required/evidence objects, and each line uses an era-specific pack-owned background. Each tab opens at the artwork center and expands through rotated circular or elliptical geometry. Import/update the default pack through Better Questing when a world retains the older database.

The integration mod persists research artifacts on each player and returns unauthorized arrivals to Earth. Moon entry requires the Orbital Research Archive. Mars entry requires the Lunar Quantum Component and Mars Mission Authorization. `config/industrialcivilization/runtime.cfg` provides an explicit creative-testing bypass.
