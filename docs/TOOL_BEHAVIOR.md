# Tool Behavior

Industrial Civilization extends the existing tools without adding another coremod or startup dependency.

| Tool | Automatic area | Cost and limits |
|---|---|---|
| Stone-tier axe or better | Connected tree | One durability per log; only leaf-bearing trees; maximum 96 logs |
| IC2 Chainsaw | Connected tree | Normal IC2 EU operation cost per log; only leaf-bearing trees; maximum 96 logs |
| IC2 Mining Drill (metadata 0) | 3×3 plane perpendicular to the struck face | Normal IC2 EU operation cost per successfully harvested block |
| IC2 Diamond Drill (metadata 1) | 9×9 plane perpendicular to the struck face | Normal IC2 EU operation cost per successfully harvested block |

Sneaking disables all automatic area behavior for precision work. Normal Forge harvesting handles drops, tool wear, IC2 energy consumption, protection cancellations, and block-break telemetry. Area mining skips tile entities, unbreakable blocks, unloaded chunks, and blocks the equipped drill cannot mine.

Tree discovery follows diagonally connected wood within an eight-block horizontal radius. The leaf requirement and 96-log cap prevent ordinary timber structures or unbounded connected builds from being consumed accidentally.
