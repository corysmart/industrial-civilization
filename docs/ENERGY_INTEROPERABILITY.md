# Energy Interoperability

Industrial Civilization is player-facing IC2 content. Every custom tooltip,
quest, GUI, status message, and ComputerCraft energy value uses EU. Forge Energy
exists only as an invisible compatibility adapter.

The canonical conversion is **1 EU = 8 FE**, matching IC2 Classic's `RFPerEU=8`.
AE2 uses `IC2=4.0` and `ForgeEnergy=0.5`, which preserves the same ratio, and
its client power display is explicitly set to EU.

| Gameplay system | Pack connection |
|---|---|
| IC2 Classic | Authoritative EU generation, storage, voltage, and distribution |
| Galacticraft | Direct IC2 input/output; competing energy APIs disabled |
| AE2 | Direct IC2 integration; hidden Forge Energy adapter remains available |
| Custom machines and Analyzer | Native IC2 sinks with tier-safe EU semantics |
| Custom solar arrays | Native IC2 sources |
| Techguns, Forestry, Industrial Foregoing, MFFS | Supplied through IC2 Electric Flux Generators and FE cables |
| BuildCraft | BuildCraft Fluxified places its machines on the compatible FE branch |

The IC2 Electric Flux Generators are intentionally the normal one-way bridge
from the EU backbone to the inherited FE ecosystem. A general FE-to-EU bridge
is not part of the intended progression.

Run `python3 tools/validate_energy_interop.py` after any energy/config change.
