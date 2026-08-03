<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Known Limitations

- Static validation cannot prove Better Questing GUI rendering, retrieval-task runtime behavior, or existing-world import behavior.
- Space gates are enforced after Galacticraft transfers the player; unauthorized players briefly enter the destination before being returned to Earth because Galacticraft's selection GUI has no stable public cancellation hook in this pack version.
- Cross-mod systems without stable runtime events use tangible, non-consuming inventory evidence. This detects real ownership but cannot always prove that a large structure remains assembled after completion.
- Solar multipliers, Martian dust derating, and cross-dimensional cargo channels are implemented. Galacticraft remains responsible for oxygen/pressure and its existing environmental survival behavior; richer radiation and habitat-integrity simulation remains future work.
- The real Analyzer currently accepts only Martian Desh metadata 2, consumes 6,250 EU, and does not yet perform Earth/lunar comparative research.
- AI-authorized AE2 covers a curated foundation set; original AE2 recipes stay removed. A complete balanced recipe reconstruction for every AE2 part remains future balance work.
- Matter, fusion, logistics, megastructure, colony, and civilization-scale AI machines implement concrete capstone proofs; their broad physical build-scale and balance still require full playthrough tuning.
- Quest updates may require importing Better Questing defaults in existing worlds. Back up world quest data first.
- Distance-based primitive settlements, abandoned factories, militia outposts, operational factories, and cities generate only in new chunks. Use a fresh world to verify the intended geography; existing village chunks are not deleted.
- Merchant contact is credited when a player opens a merchant while holding IC Credits because Forge 1.12.2 exposes no reliable universal post-transaction event across inherited merchant implementations. The offers themselves still consume real IC Credits.
- Generated settlements are compact, deterministic gameplay prototypes. Building scale, structure variety, spawn frequency, pathfinding, and economy prices require measured playtesting.
