<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Verification Boundaries

No intentionally missing gameplay system remains in the current design specification. The remaining boundaries concern evidence and tuning rather than unavailable mechanics:

- Static validation cannot prove Better Questing/Galacticraft GUI rendering or every third-party runtime event; the disposable-world checklist is the acceptance test.
- Cross-mod quests without a stable event use tangible, non-consuming item evidence. This proves acquisition but not that an inherited multiblock remains assembled forever after completion.
- Matter, fusion, logistics, megastructure, colony, vehicles, factions, settlements, and civilization-scale AI are functional; measured playthrough data is still required for final numerical balance.
- Procedural structures generate in new chunks, which is normal Minecraft world-generation behavior. Use a fresh test world for geography acceptance.
- Public release licensing, authorized dependency delivery, and packaging remain distribution work and do not limit private-test gameplay.
