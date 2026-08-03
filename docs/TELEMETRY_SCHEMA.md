<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Telemetry Schema

The integration mod now persists a minimal local-only telemetry foundation: active ticks, manual crafts, manually broken blocks, dimension transfers, synchronized artifacts, per-machine completed operations, stored energy, current progress, cargo transfers, and solar generation. Players can inspect their personal counters with `/ic_status`; machines expose operational counters through ComputerCraft. Nothing is transmitted off the computer.

`progression/telemetry-schema.json` defines the complete future pacing dataset:

- `milestone_completion_time`: `map<string,seconds>`
- `chapter_time`: `map<string,seconds>`
- `manual_crafts`: `integer`
- `autocrafting_operations`: `integer`
- `blocks_mined_manually`: `integer`
- `blocks_mined_automatically`: `integer`
- `machine_utilization`: `map<string,ratio>`
- `machine_idle_time`: `map<string,seconds>`
- `eu_generated`: `number`
- `eu_consumed`: `number`
- `reactor_efficiency`: `number`
- `launches`: `integer`
- `cargo_transported`: `number`
- `moon_resources_imported`: `number`
- `moon_resources_produced`: `number`
- `mars_resources_imported`: `number`
- `mars_resources_produced`: `number`
- `computercraft_programs_used`: `integer`
- `ae_unlock_time`: `seconds`

Fields not yet attributable reliably across inherited mods—automatic mining, all EU network generation/consumption, reactor efficiency, and imported/locally produced resource totals—remain schema-only. The goal is to compare real engineering behavior with 20/40/80 targets, not surveil players or enforce timers.
