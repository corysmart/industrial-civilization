# Native IC2 Power Scaling

Industrial Civilization machines use IC2 Classic 1.5.11 as their authoritative
energy system. Operations are expressed as total EU work, not as an unchangeable
timer. Supplying more independently legal IC2 packets therefore increases
factory throughput without changing recipes, progression IDs, or total baseline
energy cost.

## Installed EnergyNet behavior

The implementation was checked against the installed
`IC2Classic-1.12.2-1.5.11.jar`, not inferred from modern IC2 documentation.

- `IEnergySink` exposes `getDemandedEnergy()`, `getSinkTier()`, and
  `injectEnergy(side, amount, voltage)`.
- `EnergyNetLocal.emitEnergyFrom` evaluates a delivered path share against the
  sink's maximum input derived from `getSinkTier()` before it invokes
  `injectEnergy`. In normal mode, an over-tier delivery calls IC2 Classic's
  native tile-explosion path.
- Each source emission and path delivery invokes the sink independently. The
  sink returns the unaccepted remainder. Industrial Civilization never merges
  several calls into one fictitious higher-voltage packet.
- The `voltage` argument passed to `injectEnergy` is the source-tier power, while
  the installed network's destructive comparison is performed on the delivered
  packet amount before the call.
- The installed MFSU is tier 3, stores 10,000,000 EU, offers 512 EU when it has
  at least 512 EU, and reports a 512-EU maximum send. Four independently
  connected MFSUs can therefore make four legal deliveries; they are not one
  2,048-EU packet.

Static bytecode inspection proves the API and installed control flow. It does
not replace the controlled live EnergyNet experiment at the end of this file.

## Work model

Every processing-machine kind derives its default total work from its previous
baseline:

```text
totalWorkEU = baselineEUPerTick × legacyDurationTicks
```

At each machine update, energy accepted since the preceding update is added
across independent injection calls. Usable work is limited by available stored
or direct-operation EU and the remaining work:

```text
effectiveEUPerTick = max(baselineEUPerTick, acceptedEUThisTick)
workCompletedEU += min(effectiveEUPerTick, availableEU, remainingWorkEU)
```

The baseline draw remains available from the finite internal buffer, so a
charged machine continues at its historical speed during an input interruption.
When an active operation's legacy buffer is full, additional legal energy can
be accepted into a bounded, per-operation direct-work bucket. That bucket is
capped by the current operation's remaining work and is consumed before stored
EU. It prevents a full legacy buffer from silently limiting a 4-, 10-, or
50-MFSU bank while avoiding infinite internal storage.

The compatibility Forge Energy capability remains hidden, uses 8 FE = 1 EU,
and limits each receive call to the machine's normal packet voltage. It does not
change the player-facing IC2 rules.

## Machine classification

| Machine | Baseline | Total work | Minimum elapsed time | Classification |
|---|---:|---:|---:|---|
| Electric Fabricator | 32 EU/t × 160 t | 5,120 EU | none | Energy-limited manufacturing |
| Programmable Assembler | 128 EU/t × 240 t | 30,720 EU | none | Energy-limited manufacturing |
| Car Workshop | 128 EU/t × 600 t | 76,800 EU | none | Energy-limited manufacturing |
| Gun Factory | 512 EU/t × 800 t | 409,600 EU | none | Energy-limited manufacturing |
| Robotic Manufacturing Cell | 512 EU/t × 320 t | 163,840 EU | none | Energy-limited manufacturing |
| Research Station | 32 EU/t × 600 t | 19,200 EU | none | Energy-limited computation/archive synthesis; observation occurs in the Experiment Module |
| Orbital Experiment Module | 32 EU/t × 600 t | 19,200 EU | 600 t | Scientific observation; extra EU removes energy delay but cannot shorten the observation |
| Matter Replicator | 2,048 EU/t × 2,000 t | 4,096,000 EU | none | Energy-limited matter engineering |
| Fusion Research Core | 8,192 EU/t × 4,000 t | 32,768,000 EU | 600 t | Energy-limited containment work plus 30-second stabilization |
| Interplanetary Cargo Controller recipe | 512 EU/t × 600 t | 307,200 EU | none | Energy-limited network-key synthesis; periodic cargo transfer cadence is unchanged |
| Orbital Megastructure Controller | 8,192 EU/t × 2,000 t | 16,384,000 EU | 400 t | Powered construction coordination plus 20-second physical floor |
| Autonomous Colony Beacon | 2,048 EU/t × 1,200 t | 2,457,600 EU | 400 t | Powered certification plus 20-second operational floor |

The Molecular Analyzer remains a manual total-cost transaction: one valid
analysis consumes 6,250 EU immediately from its 25,000-EU buffer. It had no
processing timer to convert. Habitat uptime, oxygen stability, autonomous
production, shortage/power recovery, faction interactions, travel, cargo
cadence, and other mastery telemetry are unchanged and cannot be bypassed with
EU.

## Persistence and compatibility

Existing registry IDs, inventories, recipes, queues, quest/advancement IDs, and
the original ComputerCraft methods are preserved. New saves store
`WorkCompletedEU`, `PendingOperationEU`, `ElapsedOperationTicks`, and
`ActiveRecipe` while continuing to write the legacy proportional `Progress`
field. A legacy save that only has `Progress` is migrated on the first server
tick after the actual block kind and matching recipe are known:

```text
workCompletedEU = legacyProgress / legacyDuration × totalWorkEU
```

Changing or removing the matching recipe resets its partial work as before.
Pending direct EU is recovered into the finite normal buffer when possible.

## Player and ComputerCraft telemetry

The GUI remains EU-only and displays stored EU, accepted EU/t, baseline-relative
speed, completed/required work, and estimated time remaining. The existing
peripheral method order is unchanged. The following methods are appended:

```lua
getEnergyStored()
getInputTier()
getAcceptedEUThisTick()
getBaselineEUPerTick()
getEffectiveSpeedMultiplier()
getWorkCompleted()
getWorkRequired()
getEstimatedTicksRemaining()
```

## Configuration

`config/industrialcivilization/runtime.cfg` defaults to:

```text
nativeIc2PowerScaling=true
allowMultiPacketThroughput=true
```

Disabling native scaling restores baseline-rate work consumption. Disabling
multi-packet throughput keeps the work model and caps effective operation rate
at the historical baseline. Energy-limited machines have no arbitrary aggregate
cap by default.

## Intentional MFSU and orbital strategy

Parallel MFSU banks are a supported expert-engineering strategy. One, four,
ten, and fifty legal 512-EU deliveries can respectively supply 512, 2,048,
5,120, and 25,600 EU of aggregate work per tick to a compatible sink. Cable,
transformer, packet-tier, storage, and machine-tier rules still apply.

Orbit receives no special manufacturing multiplier. Its advantage emerges from
high-output solar arrays charging large MFSU banks, which can then discharge
legal packets into much faster energy-limited industry.

## Live acceptance — 2026-08-15

The installed 194-mod client and the existing `Test Bed 1` world were used for
controlled tests on a disposable Mars platform. These checks exercised actual
IC2 Classic storage blocks and the real first-party recipes rather than granting
finished outputs.

- One MFSU delivered 512 EU/t to a Robotic Manufacturing Cell. Its GUI showed
  `Input 512 EU/t`, `1.0x`, and a roughly 15-second remaining time before the
  genuine AI Core recipe completed.
- Four independently oriented MFSUs delivered 2,048 aggregate EU/t to a Gun
  Factory. Its GUI showed `Input 2k EU/t`, `4.0x`, and a roughly 10-second
  completion time for the genuine automatic-rifle recipe. No false
  overvoltage occurred.
- A tier-1 Electric Fabricator connected directly to one real 512-EU MFSU was
  destroyed by IC2 and left an explosion crater. This proves one genuinely
  illegal packet still takes the native destructive path.
- Four legal 32-EU BatBox deliveries completed genuine Electric Fabricator
  precision-frame operations. A Programmable Assembler connected to legal MFE
  sources consumed its genuine three-input recipe and produced an Industrial
  Control Processor.
- Four legal BatBox deliveries supplied an Orbital Experiment Module running
  `record_martian_data`. Energy work was satisfied early, but the operation
  remained active until the configured 600-tick scientific observation floor.
- A Robotic Manufacturing Cell was unloaded at `WorkCompletedEU=25472`,
  `Progress=49`, and `ElapsedOperationTicks=50`. The same exact values were
  present immediately after reloading `Test Bed 1`; after the real source was
  refilled, the operation resumed, consumed its inputs, placed the AI Core in
  output slot 3, and incremented `Completed` to 1.

The deterministic harness additionally covers 10- and 50-source aggregation,
buffer limits, baseline/2x/4x timing, overvoltage distinction, NBT migration,
and ComputerCraft return values.

## Live 10/50-bank acceptance — 2026-08-16

The optional Chapter 11 `MFSU Burst Power` side path was exercised in the same
client and world with a loop-free glass-fibre trunk and fifty physical IC2 MFSUs.
The Robotic Manufacturing Cell began each measurement with an empty internal
buffer and the three genuine AI-core ingredients already installed; reconnecting
the real cable started the operation.

- With ten MFSUs facing the trunk and forty facing away, the one-, four-, and
  ten-bank milestones completed. The fifty-bank and eight-tick blink milestones
  remained incomplete.
- After all fifty MFSUs were oriented toward the trunk and the chunks were
  reloaded to rebuild EnergyNet, the fifty-bank and blink milestones both
  completed. This is the live proof that the actual EnergyNet provides the
  intended approximately seven-tick AI-core burst.
- A looped two-dimensional cable mesh is not a valid bank topology. It causes
  IC2 Classic to split a packet over many routes, multiplying callback count and
  route loss. Detection therefore aggregates EU from 512-EU source-voltage
  deliveries instead of counting callbacks.
- Source-packet equivalents use nearest-packet conversion. This tolerates small
  legal cable losses on fifty real 512-EU source packets while 49 complete
  packets remain below the fifty-bank threshold.

An orbital burst factory and a live Matter Replicator burst remain useful future
stress tests, but the physical 10/50-MFSU acceptance is complete.

## Manual live-test handoff

Use a disposable copy of `Test Bed 1` and record the GUI/peripheral telemetry.

1. Run one known recipe in an Electric Fabricator at 32 EU/t and confirm about 160 ticks and 5,120 EU.
2. Repeat the same recipe at 2× and 4× legal aggregate input; confirm about 80 and 40 ticks.
3. Run a Robotic Manufacturing Cell recipe with one 512-EU MFSU delivery; confirm about 320 ticks.
4. Repeat with 4, 10, and, if practical, 50 independently connected MFSUs; target about 80, 32, and 7 ticks.
5. Confirm accepted EU/t reports approximately 512, 2,048, 5,120, and 25,600 without a false overvoltage event.
6. On a disposable machine, deliver one genuinely over-tier packet and confirm IC2 Classic's native rejection/explosion behavior.
7. Repeat a Programmable Assembler operation and verify its existing recipe selection, queue, and ComputerCraft programs still work.
8. Run an Orbital Experiment Module with extreme legal power and verify it still requires 600 elapsed powered/observation ticks.
9. Run a Matter Replicator with baseline and burst infrastructure and confirm total EU remains 4,096,000 while duration changes.
10. Save and reload each of a manufacturing and time-limited operation at partial progress; verify proportional work and elapsed time resume.
11. Repeat one burst test in orbit using enhanced solar generation feeding storage; confirm acceleration emerges from accepted EU rather than a dimension multiplier.
12. Verify habitat, oxygen-detector, shortage recovery, power recovery, cargo, and sustained-operation milestones still require their original real telemetry.
