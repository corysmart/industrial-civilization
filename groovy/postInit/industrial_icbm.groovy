// ICBM Classic is an IC2 expansion in this pack. Reload with /gs reload --clean.
// Its native duplicate components remain registered for save compatibility but
// are neither craftable nor visible; all functional recipes consume IC2 parts.

import net.minecraftforge.event.entity.player.ItemTooltipEvent

def duplicateParts = [
    item('icbmclassic:circuit:0'), item('icbmclassic:circuit:1'), item('icbmclassic:circuit:2'),
    item('icbmclassic:battery'), item('icbmclassic:wire:0'), item('icbmclassic:wire:1'),
    item('icbmclassic:ingot'), item('icbmclassic:clump'), item('icbmclassic:plate')
]
duplicateParts.each { part -> crafting.removeByOutput(part) }
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
    if (id != null && id.getResourceDomain() == 'icbmclassic') {
        event.toolTip << 'IC2 EU infrastructure required (8 converted power units per EU)'
        event.toolTip << 'Industrial Civilization: military/nuclear progression rules apply'
    }
}
