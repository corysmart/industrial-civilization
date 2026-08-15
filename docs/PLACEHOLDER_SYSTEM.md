<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Placeholder Replacement Status

All eleven early test placeholders have been removed. Their stable milestone IDs were preserved while their fulfillment mechanisms were replaced with real runtime content. No `[TEST PLACEHOLDER]` item, two-item shortcut recipe, shared placeholder model, or placeholder enable toggle remains.

| Former placeholder | Stable milestone | Runtime object | Implemented behavior |
|---|---|---|---|
| `research_station` | `research_station` | `industrialcivilizationcore:research_station` | native IC2 EU energy sink; invisible Forge Energy compatibility adapter; four-slot inventory; environment-aware archive recipes; IC2-styled GUI; ComputerCraft peripheral |
| `orbital_experiment_module` | `orbital_experiment_module` | `industrialcivilizationcore:orbital_experiment_module` | powered environment detection; orbit/Moon/Mars tagged data; automation-compatible inventory |
| `orbital_research_archive` | `orbital_research_complete` | `industrialcivilizationcore:orbital_research_archive` | orbit-only Research Station output; persistent Moon-access gate |
| `lunar_engineering_archive` | `lunar_research_complete` | `industrialcivilizationcore:lunar_engineering_archive` | Moon-only Research Station output; requires orbital archive and lunar experiment data |
| `lunar_quantum_component` | `lunar_quantum_component` | `industrialcivilizationcore:lunar_quantum_component` | Moon-only Robotic Cell output; Mars gate prerequisite |
| `mars_mission_authorization` | `mars_mission_authorization` | `industrialcivilizationcore:mars_mission_authorization` | Earth Research Station synthesis; persistent Mars-access gate |
| `martian_autonomy_archive` | `martian_autonomy_complete` | `industrialcivilizationcore:martian_autonomy_archive` | Mars-only Research Station output; requires Martian data and programmable control |
| `ai_core` | `artificial_industrial_intelligence_core` | `industrialcivilizationcore:artificial_industrial_intelligence_core` | Robotic Cell synthesis; Martian archive and Lite Matter inputs; durable AE2 authorization catalyst |
| `electric_fabricator` | `electric_fabricator` | `industrialcivilizationcore:electric_fabricator` | 32 EU/t fixed recipes; 160-tick processing; sided automation |
| `programmable_assembler` | `programmable_assembler` | `industrialcivilizationcore:programmable_assembler` | 128 EU/t recipes; selectable programs; ComputerCraft queues |
| `robotic_manufacturing_cell` | `robotic_manufacturing_cell` | `industrialcivilizationcore:robotic_manufacturing_cell` | 512 EU/t high-tier synthesis; dimension-aware recipes; ComputerCraft queues |

The historical contract remains machine-readable in `progression/placeholder-registry.json` with status `replaced`, which prevents future work from accidentally reintroducing temporary fulfillment.
