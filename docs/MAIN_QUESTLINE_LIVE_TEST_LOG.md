# Main Questline Live Test Log

## 2026-08-08 — Test Bed 1

- Some quests were already marked complete when Cory took over the client. These are expected completions caused by Codex performing the required gameplay actions in the same world, not evidence of quests completing spontaneously.
- Treat those pre-completed nodes as positive detection results only when the matching advancement timestamp and observed action agree. They do not count as a fresh negative-gate test.
- Chapters 2–4 were exercised through their intended main-line evidence. Chapter 5's Electric Fabricator and Programmable Assembler were genuinely crafted, and their machine recipes were run with real inputs.
- The Programmable Assembler was controlled through an adjacent ComputerCraft computer and produced five Control Processors. Its deliberate low-power stall and recovery granted `production_queue`, `multi_step_manufacturing`, `programmable_manufacturing`, and `programmable_capacity_access` from runtime telemetry.
- Continue the main-line run at Chapter 6 after the next client restart. If a future quest is already complete before its planned action, record it as contaminated test state and verify its advancement timestamp and telemetry source before deciding whether it is a defect.

## 2026-08-09 — Chapter 6 continuation

- Restart regression passed: the placed Programmable Assembler renders as a complete block and its live GUI uses the enlarged input/output slots without overlap at the 854×508 test window.
- `IC2 Nuclear Reactor` passed with fresh evidence. Raw ingredients were obtained in Creative, then a Reactor Chamber was genuinely crafted in a 3×3 crafting table. Better Questing detected the held result automatically as `Chamber Blocks 1/1 COMPLETE`; no checkbox, submit action, or direct grant of the quest item was used.
- The completed reactor quest unlocked `Sustained Nuclear Output` while `Reactor Telemetry`, `Remote Emergency Shutdown`, and `Monitored Nuclear Power` remained locked behind their declared prerequisites.
- `Sustained Nuclear Output` passed with fresh evidence. Redstone dust was obtained as the raw input, a Block of Redstone was genuinely crafted in the 3×3 crafting table, and Better Questing detected it automatically as `Block of Redstone 1/1 COMPLETE`.
- `Reactor Telemetry` was already satisfied when its prerequisite opened because the contaminated test inventory contained its ComputerCraft computer and comparator evidence alongside the newly crafted Reactor Chamber. Count this as a positive detection result only; its negative gate was not tested cleanly.
- `Remote Emergency Shutdown` passed with fresh evidence. One verified vanilla stick and one verified vanilla cobblestone were placed in the real crafting table, the lever output was taken normally, and Better Questing detected `Lever 1/1 COMPLETE` automatically. Two similarly shaped inventory items were first ruled out with `/ct hand` (`ic2:itemcable` and a Leather Works item), preventing a false recipe diagnosis.
- `Nuclear Containment` and `Monitored Nuclear Power` both accepted an MFFS Projector and completed, leaving the entire Chapter 6 canvas green. The projector was injected as Creative test evidence rather than crafted because searching for `projector` in the live item/recipe UI returned no discoverable result. Hovering the injected projector in the survival inventory and pressing `R` did open its two-page crafting recipe correctly, narrowing the defect to search/name discoverability rather than a missing recipe. This validates both quest telemetry paths but leaves the projector's real recipe execution unverified.
- The next clean live checkpoint is Chapter 7, `Orbital Age`. Before continuing, fix or explain why the MFFS Projector is absent from text search, then verify its displayed craft in isolation.
