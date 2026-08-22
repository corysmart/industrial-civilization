<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Manual Quest Test Checklist

Use this checklist for release regressions and dedicated side-path acceptance. The numbered campaign's completed acceptance evidence and subsequent 0.6.2 regression results are recorded in `MAIN_QUESTLINE_LIVE_TEST_LOG.md`; rerun affected checks after progression or runtime changes. Do not use a valued world for a quest-database migration.

- [ ] 1. Quest book opens with F6.
- [ ] 2. All 16 numbered chapters and all 10 independent side-path tabs appear.
- [ ] 3. Every objective completes automatically from item evidence or a hooked runtime event; no manual checkbox task appears.
- [ ] 4. Every future quest is visible for aspirational browsing, but locked quests cannot be opened or progressed.
- [ ] 5. Numbered chapter tabs contain no optional side-path objectives.
- [ ] 6. Early quests are reachable.
- [ ] 7. All former placeholder registry objects are absent from HEI and every replacement has a distinct sprite.
- [ ] 8. The Electric Fabricator processes a fixed recipe using IC2 EU.
- [ ] 9. The Programmable Assembler accepts ComputerCraft recipe selection and queues.
- [ ] 10. The Robotic Manufacturing Cell enforces Moon-only Quantum-component synthesis.
- [ ] 11. Electrification chapter completes.
- [ ] 12. Automation chapter opens.
- [ ] 13. The Factions and Salvage path can start independently of the numbered chapters.
- [ ] 14. Pause > Advancements opens the vanilla screen and its Industrial Civilization tab contains the ordered root plus one visible advancement for every quest.
- [ ] 15. Pause > Factions & Settlements displays all six factions, reputation, attitude, membership eligibility, settlement types, and products.
- [ ] 16. Civil Defense contact, Territorial Militia contact, and a registered militia-outpost takedown complete their separate optional objectives.
- [ ] 17. The Strategic Defense path detects ICBM launch control, radar defense, and a conventional missile without unlocking prohibited strategic-payload shortcuts.
- [ ] 18. Every village and settlement merchant accepts IC Credits rather than emeralds.
- [ ] 19. Sneak-right-click joins an eligible faction, while reputation 60 plus eight IC Credits can recruit a friendly companion.
- [ ] 20. Hostile faction members attack, guards defend their settlement, and companions follow and defend their owner.
- [ ] 21. A fresh world has only three primitive settlements near spawn and increasingly industrial structures in the documented distance bands.
- [ ] 22. Resolving the criminal network can reveal and restore the abandoned factory.
- [ ] 23. Industrial Capacity Secured accepts either built Heavy Industry or the restored abandoned factory.
- [ ] 24. Programmable Capacity Secured accepts either built Programmable Manufacturing or the recovered factory control system.
- [ ] 25. Orbital Age follows Nuclear Age.
- [ ] 26. Moon remains locked before Orbital Research.
- [ ] 27. Moon unlocks after the Orbital Research Archive.
- [ ] 28. Lunar Research follows Lunar Settlement.
- [ ] 29. Quantum Technology remains locked before Lunar Research.
- [ ] 30. Quantum Technology unlocks after the Lunar Engineering Archive.
- [ ] 31. Mars remains locked before Quantum Technology.
- [ ] 32. Mars remains locked without Mars Mission Authorization.
- [ ] 33. Mars unlocks after Quantum Technology and authorization.
- [ ] 34. Existing Galacticraft Desh metadata 2 Mars Sample is recognized.
- [ ] 35. Existing Molecular Analyzer is recognized.
- [ ] 36. Martian Autonomy follows Mars Settlement.
- [ ] 37. AI Age remains locked before Martian Autonomy and Lite Matter Engineering.
- [ ] 38. The real AI Core is synthesized from Martian Autonomy, Lite Matter, and programmable manufacturing outputs and acts as a durable AE2 recipe catalyst.
- [ ] 39. Unlocking the AI Age opens the one-time scrolling credits screen without ending the world.
- [ ] 40. The Technical Phase Pearl has no pre-AI quest or recipe dependency and becomes craftable only with the durable AI Core.
- [ ] 41. The Galacticraft destination list permits orbit, gated Moon, and gated Mars only; unsupported bodies and the End cannot be entered, and Mars Mission Authorization unlocks the Tier 2 NASA Workbench page without a lunar dungeon.
- [ ] 42. Earth Iron, lunar Meteoric Iron, and Martian Desh all produce origin-tagged Analyzer records before comparative research completes.
- [ ] 43. An unshielded player in orbit, on the Moon, or on Mars takes radiation damage unless protected by an active sealed habitat or full QuantumSuit.
- [ ] 44. Completing an actual IC Credit buy or sell—not merely opening a merchant—records faction trade contact.
- [ ] 45. AI Age is presented as the beginning of the endgame.
- [ ] 46. Post-AI branches are visible but do not block AI entry.
- [ ] 47. No circular or impossible dependencies are visible.
- [ ] 48. Orbital, lunar, and Mars habitat quests trigger only after a nearby Galacticraft Oxygen Detector becomes active from breathable air; oxygen affecting the player or a running sealer alone is insufficient.
- [ ] 49. Functional off-world bases require two continuous minutes of stable habitat samples plus their placed/operating infrastructure.
- [ ] 50. A primitive settlement absorbs nearby stockpile items, pays an exact material bill, and constructs each physical upgrade without a random roll.
- [ ] 51. Already-generated Mars chunks receive deterministic civilization processing after an AI-age player loads them.
- [ ] 52. Apollo 11, 12, 14, 15, 16, and 17 markers show mission, landing date, coordinates, flag and heritage designation.
- [ ] 53. Faction villagers, militia patrols, and robbers use their Industrial Civilization faction skins.
- [ ] 54. World warmup remains visible at least 15 seconds and releases by 30 seconds.
- [ ] 55. Stone axe/chainsaw trees and 3x3/9x9 drills process at most 12 extra blocks per tick while preserving protection, drops, enchantments and per-block tool payment.
- [ ] 56. Radiation correctly follows players in vehicles and other moving entities as their AABB enters or leaves breathable air.

Check `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log` after the run. Record quest/task IDs and screenshots for any mismatch.
