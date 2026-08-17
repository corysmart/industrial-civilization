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
