// Railcraft's four signal circuits are custom IRecipe implementations. The
// pack-wide CraftTweaker ingredient replacement cannot rewrite those objects,
// so rebuild them explicitly with IC2 Sticky Resin before the final ecology
// audit runs. The shapes and circuit colour identities match Railcraft's
// native recipes; only the inaccessible Slimeball/Railcraft-resin union is
// replaced by the pack's canonical adhesive.
[0, 1, 2, 3].each { metadata ->
    crafting.removeByOutput(item("railcraft:circuit:${metadata}"))
}

def circuitRecipe = { String name, int metadata, int woolColour ->
    crafting.addShaped("industrial_civilization:${name}", item("railcraft:circuit:${metadata}"), [
        [null, item('minecraft:repeater'), item("minecraft:wool:${woolColour}")],
        [item('ic2:itemharz'), ore('plateGold'), item('minecraft:redstone')],
        [item("minecraft:wool:${woolColour}"), item('minecraft:redstone'), ore('gemLapis')]
    ])
}

circuitRecipe('railcraft_circuit_controller', 0, 14)
circuitRecipe('railcraft_circuit_receiver', 1, 13)
circuitRecipe('railcraft_circuit_signal', 2, 4)
circuitRecipe('railcraft_circuit_radio', 3, 11)
