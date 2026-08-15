<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Telemetry Schema

The integration mod persists local-only telemetry: first milestone completion time and evidence source, active ticks, manual crafts, manually broken blocks, dimension transfers, synchronized artifacts, per-machine completed operations, stored energy, current progress, cargo transfers, solar generation, and sustained off-world habitat samples. Players can inspect personal counters with `/ic_status`; machines expose operational counters through ComputerCraft. Nothing is transmitted off the computer.

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

Complex orbital, lunar and Mars base quests now require sustained runtime evidence rather than inventory ownership: a nearby active Galacticraft Oxygen Detector, communications, operating local manufacturing, automated mining, local power, and dimension-specific requirements. Player oxygen exposure or a running sealer alone is not habitat quest evidence. Fields not yet attributable reliably across inherited mods—automatic mining totals, all EU network generation/consumption, reactor efficiency, chapter rollups and complete imported/locally produced resource totals—remain schema-only. The goal is pacing analysis and reliable quest completion, not surveillance or fixed timers.
