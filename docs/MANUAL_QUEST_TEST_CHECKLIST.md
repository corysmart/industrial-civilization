<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Manual Quest Test Checklist

Do not use a valued world for the first database import. No runtime claims are made until this checklist passes.

- [ ] 1. Quest book opens with F6.
- [ ] 2. All 16 numbered chapters and all 5 independent side-path tabs appear.
- [ ] 3. Every future quest is visible for aspirational browsing, but locked quests cannot be opened or progressed.
- [ ] 4. Numbered chapter tabs contain no optional side-path objectives.
- [ ] 5. Early quests are reachable.
- [ ] 6. Placeholder recipes work when enabled.
- [ ] 7. Electrification chapter completes.
- [ ] 8. Automation chapter opens.
- [ ] 9. The Factions and Salvage path can start independently of the numbered chapters.
- [ ] 10. Resolving the criminal network can reveal and restore the abandoned factory.
- [ ] 11. Industrial Capacity Secured accepts either built Heavy Industry or the restored abandoned factory.
- [ ] 12. Programmable Capacity Secured accepts either built Programmable Manufacturing or the recovered factory control system.
- [ ] 13. Orbital Age follows Nuclear Age.
- [ ] 14. Moon remains locked before Orbital Research.
- [ ] 15. Moon unlocks after the Orbital Research Archive.
- [ ] 16. Lunar Research follows Lunar Settlement.
- [ ] 17. Quantum Technology remains locked before Lunar Research.
- [ ] 18. Quantum Technology unlocks after the Lunar Engineering Archive.
- [ ] 19. Mars remains locked before Quantum Technology.
- [ ] 20. Mars remains locked without Mars Mission Authorization.
- [ ] 21. Mars unlocks after Quantum Technology and authorization.
- [ ] 22. Existing Galacticraft Desh metadata 2 Mars Sample is recognized.
- [ ] 23. Existing Molecular Analyzer is recognized.
- [ ] 24. Martian Autonomy follows Mars Settlement.
- [ ] 25. AI Age remains locked before Martian Autonomy and Lite Matter Engineering.
- [ ] 26. Placeholder AI Core unlocks the supplied AE2 entry recipes only in testing mode.
- [ ] 27. AI Age is presented as the beginning of the endgame.
- [ ] 28. Post-AI branches are visible but do not block AI entry.
- [ ] 29. No circular or impossible dependencies are visible.

Check `logs/latest.log`, `logs/groovy.log`, and `crafttweaker.log` after the run. Record quest/task IDs and screenshots for any mismatch.
