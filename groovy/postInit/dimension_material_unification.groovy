// A few mods register recipes after CraftTweaker's replacement pass. Rebuild
// those late recipes here so the final Forge registry follows the same
// dimension-independent material policy as scripts/tekkit2.zs.

def colorfulLamp = item('computronics:colorful_lamp')
crafting.removeByOutput(colorfulLamp)
crafting.addShaped('industrial_civilization:colorful_lamp', colorfulLamp, [
    [ore('ingotIron'), ore('blockGlassColorless'), ore('ingotIron')],
    [ore('blockGlassColorless'), item('minecraft:redstone'), ore('blockGlassColorless')],
    [ore('ingotIron'), ore('blockGlassColorless'), ore('ingotIron')]
])

// Computronics' two largest tape tiers use the same chassis as their native
// recipes. Lunar Quantum Components replace the inaccessible Nether Stars.
def tapeChassis = item('computronics:parts:0')
def lunarQuantumComponent = item('industrialcivilizationcore:lunar_quantum_component')

crafting.removeByOutput(item('computronics:tape:4'))
crafting.addShaped('industrial_civilization:quantum_tape_diamond', item('computronics:tape:4'), [
    [null, ore('gemDiamond'), null],
    [ore('gemDiamond'), lunarQuantumComponent, ore('gemDiamond')],
    [null, tapeChassis, null]
])

crafting.removeByOutput(item('computronics:tape:8'))
crafting.addShaped('industrial_civilization:quantum_tape_dense', item('computronics:tape:8'), [
    [null, lunarQuantumComponent, null],
    [lunarQuantumComponent, lunarQuantumComponent, lunarQuantumComponent],
    [null, tapeChassis, null]
])

// A straight material substitution can make two otherwise unrelated recipes
// identical (for example, Purpur Stairs and Stone Brick Stairs). Give those
// affected outputs an explicit Earth-side recipe so CraftingManager cannot
// select the vanilla/modded lookalike first.
def certusCrystal = item('appliedenergistics2:material:0')
def purpleDye = item('minecraft:dye:5')

crafting.removeByOutput(item('minecraft:purpur_stairs'))
crafting.addShaped('industrial_civilization:earth_purpur_stairs', item('minecraft:purpur_stairs') * 4, [
    [item('minecraft:stonebrick'), null, purpleDye],
    [item('minecraft:stonebrick'), item('minecraft:stonebrick'), null],
    [item('minecraft:stonebrick'), item('minecraft:stonebrick'), item('minecraft:stonebrick')]
])

crafting.removeByOutput(item('minecraft:purpur_slab'))
crafting.addShaped('industrial_civilization:earth_purpur_slab', item('minecraft:purpur_slab') * 6, [
    [null, purpleDye, null],
    [item('minecraft:stonebrick'), item('minecraft:stonebrick'), item('minecraft:stonebrick')]
])

crafting.removeByOutput(item('minecraft:nether_brick'))
crafting.addShaped('industrial_civilization:earth_nether_brick', item('minecraft:nether_brick'), [
    [ore('ingotBrick'), ore('ingotBrick'), null],
    [ore('ingotBrick'), ore('dustCoal'), ore('ingotBrick')],
    [null, null, null]
])

crafting.removeByOutput(item('galacticraftcore:glowstone_torch'))
crafting.addShaped('industrial_civilization:earth_glowstone_torch', item('galacticraftcore:glowstone_torch') * 4, [
    [ore('blockGlassColorless'), item('minecraft:redstone'), ore('blockGlassColorless')],
    [null, ore('stickWood'), null],
    [null, certusCrystal, null]
])

crafting.removeByOutput(item('chiselsandbits:mirrorprint'))
crafting.addShapeless('industrial_civilization:earth_mirrorprint', item('chiselsandbits:mirrorprint'), [
    item('minecraft:water_bucket'), item('minecraft:paper'), certusCrystal
])

crafting.removeByOutput(item('techguns:neonlights:0'))
crafting.addShapeless('industrial_civilization:earth_neon_light', item('techguns:neonlights:0'), [
    item('techguns:lamp0'), certusCrystal, item('minecraft:redstone')
])

crafting.removeByOutput(item('quark:purpur_block_wall'))
crafting.addShapeless('industrial_civilization:earth_purpur_wall', item('quark:purpur_block_wall'), [
    item('quark:stonebrick_wall'), purpleDye
])

crafting.removeByOutput(item('quark:soul_sandstone'))
crafting.addShapeless('industrial_civilization:earth_soul_sandstone', item('quark:soul_sandstone'), [
    item('minecraft:sandstone'), ore('dustCoal')
])

crafting.addShapeless('industrial_civilization:earth_antiblock', item('chisel:antiblock'), [
    item('chisel:futura'), certusCrystal, purpleDye
])
