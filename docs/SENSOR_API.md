# ComputerCraft Sensor API

## Existing adapters retained

Static class/config inspection confirms Plethora has IC2 integration for energy storage and reactors, Forge Energy integration, and Forge fluid-handler integration. Relevant calls include:

- `getEUStored()`, `getEUCapacity()`, `getEUOutput()`, `getOfferedEnergy()`, `getDemandedEnergy()`
- `getReactorCore()` returning reactor metadata fields `heat`, `maxHeat`, `euOutput`, `active`, and `fluidCooled`
- `getEnergyStored()`, `getEnergyCapacity()`
- `getTanks()`, `pushFluid()`, `pullFluid()`

Computronics' Railcraft relay sensor is retained and enabled. Galacticraft's own telemetry block, oxygen detector, and sealed-habitat systems remain available.

## New Analyzer peripheral

Place a wired modem adjacent to the Molecular Analyzer. Its type is `molecular_analyzer` and it exposes:

```lua
getStatus()           -- "charging" or "ready"
getProgress()         -- completed analysis count
getStored()           -- FE stored
getCapacity()         -- 200000
analyzeAvailable()    -- true at >= 50000 FE
getOxygen()           -- ambient percent approximation
getRadiation()        -- ambient normalized approximation
getPressure()         -- ambient kPa approximation
```

The environmental values distinguish Earthlike from Galacticraft Moon/Mars/asteroid dimension providers. They do not claim a room is sealed. For habitat safety, combine them with the Galacticraft Oxygen Detector's redstone signal.

Examples in `examples/computercraft/` cover factory status, reactor monitoring, SCRAM output, rocket fuel/telemetry monitoring, and habitat environment display. Peripheral method availability varies by the specific adjacent block; the examples fail safely when an optional method is absent.
