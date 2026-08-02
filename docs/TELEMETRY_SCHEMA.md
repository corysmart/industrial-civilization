<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Telemetry Schema

Telemetry is **not implemented**. `progression/telemetry-schema.json` prepares a future opt-in, aggregate-only implementation for pacing validation. It defines:

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

The goal is to compare real engineering behavior with 20/40/80 targets, not surveil players or enforce timers.
