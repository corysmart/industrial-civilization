# Main Questline Live Test Log

## 2026-08-08 — Test Bed 1

- Some quests were already marked complete when Cory took over the client. These are expected completions caused by Codex performing the required gameplay actions in the same world, not evidence of quests completing spontaneously.
- Treat those pre-completed nodes as positive detection results only when the matching advancement timestamp and observed action agree. They do not count as a fresh negative-gate test.
- Chapters 2–4 were exercised through their intended main-line evidence. Chapter 5's Electric Fabricator and Programmable Assembler were genuinely crafted, and their machine recipes were run with real inputs.
- The Programmable Assembler was controlled through an adjacent ComputerCraft computer and produced five Control Processors. Its deliberate low-power stall and recovery granted `production_queue`, `multi_step_manufacturing`, `programmable_manufacturing`, and `programmable_capacity_access` from runtime telemetry.
- Continue the main-line run at Chapter 6 after the next client restart. If a future quest is already complete before its planned action, record it as contaminated test state and verify its advancement timestamp and telemetry source before deciding whether it is a defect.
