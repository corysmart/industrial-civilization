<!-- Generated from progression/*.json by tools/generate_progression_docs.py. -->

# Placeholder System

Every missing runtime system has a visibly named `[TEST PLACEHOLDER]` item with a stable registry ID, explanatory tooltip, final target, and replacement contract. Recipes live only in `groovy/postInit/industrial_civilization_placeholders.groovy` and are controlled by `config/industrialcivilization/placeholders.cfg`:

```properties
enableTestingPlaceholders=true
```

Set it to `false` to remove every temporary recipe and AI-gated AE2 recipe without deleting the canonical graph. Research artifacts use Paper + Redstone; machine artifacts use Iron Ingot + Redstone.

| ID | Quest milestone | Test item | Final target | Recipe |
|---|---|---|---|---|
| `research_station` | `research_station` | `industrialcivilizationcore:placeholder_research_station` | `industrial_civilization:research_station` | minecraft:paper + minecraft:redstone |
| `orbital_experiment_module` | `orbital_experiment_module` | `industrialcivilizationcore:placeholder_orbital_experiment_module` | `industrial_civilization:orbital_experiment_module` | minecraft:paper + minecraft:redstone |
| `orbital_research_archive` | `orbital_research_complete` | `industrialcivilizationcore:placeholder_orbital_research_archive` | `industrial_civilization:orbital_research_archive` | minecraft:paper + minecraft:redstone |
| `lunar_engineering_archive` | `lunar_research_complete` | `industrialcivilizationcore:placeholder_lunar_engineering_archive` | `industrial_civilization:lunar_engineering_archive` | minecraft:paper + minecraft:redstone |
| `lunar_quantum_component` | `lunar_quantum_component` | `industrialcivilizationcore:placeholder_lunar_quantum_component` | `industrial_civilization:lunar_quantum_component` | minecraft:paper + minecraft:redstone |
| `mars_mission_authorization` | `mars_mission_authorization` | `industrialcivilizationcore:placeholder_mars_mission_authorization` | `industrial_civilization:mars_mission_authorization` | minecraft:paper + minecraft:redstone |
| `martian_autonomy_archive` | `martian_autonomy_complete` | `industrialcivilizationcore:placeholder_martian_autonomy_archive` | `industrial_civilization:martian_autonomy_archive` | minecraft:paper + minecraft:redstone |
| `ai_core` | `artificial_industrial_intelligence_core` | `industrialcivilizationcore:placeholder_ai_core` | `industrial_civilization:artificial_industrial_intelligence_core` | minecraft:iron_ingot + minecraft:redstone |
| `electric_fabricator` | `electric_fabricator` | `industrialcivilizationcore:placeholder_electric_fabricator` | `industrial_civilization:electric_fabricator` | minecraft:iron_ingot + minecraft:redstone |
| `programmable_assembler` | `programmable_assembler` | `industrialcivilizationcore:placeholder_programmable_assembler` | `industrial_civilization:programmable_assembler` | minecraft:iron_ingot + minecraft:redstone |
| `robotic_manufacturing_cell` | `robotic_manufacturing_cell` | `industrialcivilizationcore:placeholder_robotic_manufacturing_cell` | `industrial_civilization:robotic_manufacturing_cell` | minecraft:iron_ingot + minecraft:redstone |

When a final implementation arrives, keep `milestone_id`, replace only `placeholder_item`/temporary validation with the runtime condition, and mark `replacement_status` accordingly.
