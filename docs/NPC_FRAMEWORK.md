# NPC and Faction Framework

IndustrialCivilizationCore owns the gameplay-critical NPC system. It uses stable vanilla villager rendering, navigation, merchant screens, and persistence while adding faction identity, settlement roles, IC Credit offers, reputation, membership, conditional hostility, guards, and recruitable companions. Custom NPCs remains available for authored story scenes, but no clone database or manual NPC Wand setup is required for the procedural civilization system.

Five factions are defined in `config/industrialcivilization/faction-system.json`: the Frontier Cooperative, Riverside Works Consortium, Civil Defense Militia, Survey Detachment 7, and hostile Ashline Raiders. Each records its settlement types, product specialties, starting attitude, reputation effects, and playstyle-based membership rules. Player state is persistent and per faction.

Normal right-click opens an IC Credit merchant offer. Sneak-right-click requests faction membership when the displayed criteria and 35 reputation are satisfied. At 60 reputation, a faction member holding eight IC Credits can recruit a companion; sneak-right-click dismisses an owned companion. Friendly attacks reduce reputation, civilian kills are remembered, raider kills improve legitimate-faction standing, guarded property damage is recorded, and industrial, research, crafting, and trade behavior can improve appropriate relationships. Hostile faction members pursue and attack; settlement guards also defend against monsters; companions follow and defend their owner.

The pause menu's former Statistics button is `Factions & Settlements`. Its directory shows encountered status, reputation, current attitude, membership eligibility, settlement types, products, and the interaction rules. This is the player-facing reference; an external wiki is not required.

New-world geography is deterministic from the world seed. Three small primitive settlements appear at roughly 240, 520, and 800 blocks from spawn. Abandoned factories begin beyond 900 blocks, militia outposts beyond 1,400, guarded operational specialty factories beyond 2,200, and industrial cities beyond 3,000. New vanilla village generation is suppressed, but existing village chunks are preserved. Any surviving vanilla villagers receive IC Credit-only offers when loaded.

The generated buildings and economy are intentionally compact first playable implementations. A fresh disposable world is required to verify distribution, prices, navigation, combat, membership, and companion behavior before scale and frequency are tuned.
