# Vehicles, Nations, and Large Workshops

## Vehicle foundation

The pack uses Onysd Vehicles 1.4.0 for real 1.12.2 driving physics, fuel, mounting, seat behavior, and vehicle entities. IndustrialCivilizationCore extends it rather than imitating the complete vehicle stack. Onysd Vehicles is LGPL-2.1-or-later; its required Obfuscate 0.4.2 library is GPL-3.0. Preserve both licenses and corresponding-source obligations before distributing a pack binary.

The generic Onysd Workstation recipe is removed. The IC2-powered Car Workshop is the only curated manufacturing route for these six roles:

| Program | Chassis | Purpose |
|---|---|---|
| City Compact | Smart Car | Low-footprint paved-road transport |
| Frontier Off-Roader | Off Roader | Rough-road exploration |
| Passenger Carrier | Mini Bus | Group transport and top-tier mobile industry |
| Agricultural Tractor | Tractor | Farm and settlement utility |
| Utility Cart | Golf Cart | Short-range factory/campus movement |
| Scout ATV | ATV | Fast solo reconnaissance |

The Passenger Carrier receives persistent 54-slot item storage and a 64,000 mB fluid tank. While parked, sneak-right-click opens cargo; holding a Crafting Table instead opens mobile crafting. Fluid containers interact directly. A Vehicle Service Dock within four blocks exposes the vehicle item/fluid capabilities to BuildCraft automation.

## Nation infrastructure

New-world civilization bands progress from three primitive settlements near spawn to abandoned factories, militia outposts, operational specialty factories, and cities. A three-wide regional road grid begins as dirt and changes to paved double slabs in the industrial band. Selected structures install IC2 solar generation, cable spines, wall-level outlet positions, and BuildCraft transport holders.

Each city receives a nation-managed cargo controller on `earth_nation_exchange`. Its deterministic specialty is iron, redstone, coal, paper, or bread. Loaded city controllers restock slowly and exchange one item at a time. Neutral component trades are deliberately more favorable than emergency IC Credit crafting; friendly and trusted reputation adjust prices by only one or two units.

## Large workshop rules

Placing a Car Workshop or Gun Factory deploys a 9×7 equipment footprint with floor, frame, lights, and work surfaces. The player supplies the building and roof. If rain reaches the controller, it rusts and production stops. Right-clicking a Repair Bench with one IC2 Machine Block repairs the nearest rusted workshop within 12 blocks.

The Programmable Assembler produces the pistol. The Gun Factory produces the combat shotgun and M4 automatic rifle. A complete balance pass for every remaining Techguns weapon and internal Techguns production machine is still required before public release; the current implementation gates the pack's explicit progression weapons, not the entire upstream catalog.

## Controls

- W/A/S/D: drive; H: horn; C: cycle seats.
- Sneak-right-click a parked Passenger Carrier: cargo.
- Sneak-right-click while holding a Crafting Table: mobile crafting.
- Right-click with a fluid container: fill or drain the carrier tank.
- Park within four blocks of a Vehicle Service Dock: expose item/fluid capability to connected pipes.
