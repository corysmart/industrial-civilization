# Nuclear Progression

IC2 Classic reactor mechanics and danger are unchanged. The player mines uranium, builds the normal reactor/chambers and safe component layout, connects meaningful EU load, and uses Energy Control/Plethora plus ComputerCraft for visibility.

The supplied monitor reads Plethora reactor heat/core metadata when attached through a compatible peripheral setup. `reactor_scram.lua` asserts an external redstone line at 70% maximum heat. Wire that line through ProjectRed/Wireless Redstone to a reactor control circuit whose asserted state disables the reactor. Test the fail-safe logic with the reactor empty before inserting fuel.

Nuclear output is an accelerator for ore processing, fuel refinement, oxygen equipment, compressors, and launch preparation; no reactor mechanic or fuel duration was modified.
