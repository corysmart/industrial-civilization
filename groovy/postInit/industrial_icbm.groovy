// ICBM Classic is an IC2 expansion in this pack. Reload with /gs reload --clean.
// Its native duplicate components remain registered for save compatibility but
// are neither craftable nor visible; all functional recipes consume IC2 parts.

import net.minecraftforge.event.entity.player.ItemTooltipEvent

def duplicateParts = [
    item('icbmclassic:circuit:0'), item('icbmclassic:circuit:1'), item('icbmclassic:circuit:2'),
    item('icbmclassic:battery'), item('icbmclassic:wire:0'), item('icbmclassic:wire:1'),
    item('icbmclassic:ingot'), item('icbmclassic:clump'), item('icbmclassic:plate')
]
// removeByOutput reports an error when a registered compatibility item has no
// native recipe (ICBM ingots and plates in this build). Filter the registry so
// zero matching recipes is the intended, silent result.
duplicateParts.each { part ->
    crafting.streamRecipes()
        .filter { recipe ->
            def output = recipe.recipeOutput
            output != null && !output.empty && output.item == part.item
                && output.metadata == part.metadata
        }
        .removeAll()
}
mods.jei.ingredient.removeAndHide(duplicateParts)

// Launch control hardware is deliberately MV/HV IC2 infrastructure. ICBM's
// machines receive converted IC2 EU through the pack's installed bridge.
[
    item('icbmclassic:launcherbase'), item('icbmclassic:launcherscreen'),
    item('icbmclassic:radarstation'), item('icbmclassic:cruiselauncher')
].each { output -> crafting.removeByOutput(output) }

crafting.addShaped('industrial_civilization:icbm_launcher_base', item('icbmclassic:launcherbase'), [
    [ore('plateSteel'), item('ic2:itemmisc:452'), ore('plateSteel')],
    [ore('plateSteel'), item('ic2:blockmachinemv'), ore('plateSteel')],
    [ore('ingotSteel'), item('industrialcivilizationcore:control_processor'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:icbm_launcher_screen', item('icbmclassic:launcherscreen'), [
    [item('ic2:itemmisc:452'), item('minecraft:glass'), item('ic2:itemmisc:452')],
    [item('ic2:itemcable:0'), item('computercraft:computer:16384'), item('ic2:itemcable:0')],
    [ore('plateSteel'), item('industrialcivilizationcore:control_processor'), ore('plateSteel')]
])
crafting.addShaped('industrial_civilization:icbm_radar', item('icbmclassic:radarstation'), [
    [ore('plateSteel'), item('ic2:itemmisc:452'), ore('plateSteel')],
    [item('icbmclassic:launcherscreen'), item('ic2:blockmachinehv'), item('icbmclassic:launcherscreen')],
    [ore('ingotSteel'), item('industrialcivilizationcore:control_processor'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:icbm_cruise_launcher', item('icbmclassic:cruiselauncher'), [
    [item('icbmclassic:launcherbase'), item('industrialcivilizationcore:control_processor'), ore('plateSteel')],
    [ore('plateSteel'), item('ic2:blockmachinehv'), ore('plateSteel')],
    [item('ic2:itemmisc:452'), item('icbmclassic:launcherscreen'), item('ic2:itemmisc:452')]
])

event_manager.listen { ItemTooltipEvent event ->
    def id = event.itemStack?.item?.registryName
    // Groovy executes against the obfuscated runtime, where MCP-named
    // ResourceLocation accessors are not guaranteed to exist. Its
    // string representation is stable on both development and live clients.
    if (id != null && id.toString().startsWith('icbmclassic:')) {
        event.toolTip << 'IC2 EU infrastructure required (8 converted power units per EU)'
        event.toolTip << 'Industrial Civilization: military/nuclear progression rules apply'
    }
}
