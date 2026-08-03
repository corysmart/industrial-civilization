# Item unification audit

The pack keeps one gameplay source for overlapping industrial materials while retaining disabled registry entries for old-save compatibility.

| Family | Canonical gameplay source | Disabled or hidden overlap |
|---|---|---|
| Copper, tin, bronze | IC2/pack ore dictionary | Techguns ingots, nuggets, and ore generation |
| Lead | Existing pack lead chain | Techguns generation and Galacticraft Venus lead ore-dictionary shortcut |
| Steel | Railcraft `ingotSteel`/`plateSteel` chain | Techguns steel and ICBM steel ingot/clump/plate recipes |
| Uranium | IC2 nuclear chain | Techguns uranium generation |
| Circuits | IC2 electronic and advanced circuits | ICBM basic/advanced/elite circuits |
| Wire and portable energy | IC2 cable and storage components | ICBM wire and battery recipes |
| Rubber/plastic | IC2 rubber, separate Industrial Foregoing plastic | Cross-registration removed |

`tools/audit_item_unification.py` verifies these policies. ICBM duplicate entries are hidden from HEI and uncraftable, but are not deleted from the registry because deletion would corrupt existing item IDs in saves.
