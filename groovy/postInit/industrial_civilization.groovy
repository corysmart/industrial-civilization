// Industrial Civilization vertical-slice integration.
// This postInit script is reloadable in-game with: /gs reload --clean

import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraft.client.Minecraft

// AI Age hard lock. AE2 stays installed for future compatibility, but none of
// its ordinary crafting-table recipes are currently craftable.
crafting.streamRecipes()
    .filter { recipe -> recipe.registryName?.toString()?.startsWith('appliedenergistics2:') }
    .removeAll()

// Intended storage progression stops at Iron Chests. Higher registered
// variants remain internal for save compatibility but are uncraftable and
// absent from the HEI ingredient sidebar.
def lockedChests = [
    item('ironchest:iron_chest:1'), // Gold
    item('ironchest:iron_chest:2'), // Diamond
    item('ironchest:iron_chest:4'), // Silver
    item('ironchest:iron_chest:5'), // Crystal
    item('ironchest:iron_chest:6')  // Obsidian
]

// Preserve only Wood -> Copper, Wood -> Iron, and Copper -> Iron upgrades.
def lockedChestUpgrades = [
    item('ironchest:iron_gold_chest_upgrade'),
    item('ironchest:gold_diamond_chest_upgrade'),
    item('ironchest:copper_silver_chest_upgrade'),
    item('ironchest:silver_gold_chest_upgrade'),
    item('ironchest:diamond_crystal_chest_upgrade'),
    item('ironchest:diamond_obsidian_chest_upgrade')
]

mods.jei.ingredient.removeAndHide(lockedChests)
mods.jei.ingredient.removeAndHide(lockedChestUpgrades)

// Representative industrial firearm routes. Techguns' machines and remaining
// catalogue stay intact.
crafting.removeByOutput(item('techguns:pistol'))
crafting.addShaped('industrial_civilization:pistol', item('techguns:pistol'), [
    [ore('ingotSteel'), ore('ingotSteel'), ore('ingotCopper')],
    [null, item('ic2:itemmisc:451'), ore('itemRubber')],
    [null, ore('ingotSteel'), null]
])

crafting.removeByOutput(item('techguns:combatshotgun'))
crafting.addShaped('industrial_civilization:combat_shotgun', item('techguns:combatshotgun'), [
    [ore('ingotSteel'), ore('ingotSteel'), ore('ingotSteel')],
    [ore('ingotIron'), item('ic2:itemmisc:451'), ore('itemRubber')],
    [null, ore('plankWood'), ore('plankWood')]
])

crafting.removeByOutput(item('techguns:m4'))
crafting.addShaped('industrial_civilization:m4', item('techguns:m4'), [
    [ore('ingotSteel'), item('ic2:itemmisc:452'), ore('ingotAluminum')],
    [ore('ingotSteel'), ore('ingotCopper'), ore('itemRubber')],
    [null, ore('ingotSteel'), item('minecraft:glass_pane')]
])

// Techguns ammunition variants share techguns:itemshared.
// Metadata: shotgun rounds=2, pistol magazine=11, assault-rifle magazine=13.
crafting.addShaped('industrial_civilization:pistol_magazine', item('techguns:itemshared:11'), [
    [ore('ingotCopper'), item('minecraft:gunpowder'), ore('ingotCopper')],
    [ore('ingotSteel'), item('minecraft:gunpowder'), ore('ingotSteel')]
])

crafting.addShaped('industrial_civilization:shotgun_rounds', item('techguns:itemshared:2') * 8, [
    [item('minecraft:paper'), item('minecraft:gunpowder'), ore('nuggetIron')],
    [item('minecraft:paper'), item('minecraft:gunpowder'), ore('nuggetIron')]
])

crafting.addShaped('industrial_civilization:assault_rifle_magazine', item('techguns:itemshared:13'), [
    [ore('ingotCopper'), item('minecraft:gunpowder'), ore('ingotCopper')],
    [ore('ingotSteel'), item('ic2:itemmisc:451'), ore('ingotSteel')]
])

// Lite Matter Engineering: Earth industry + Moon meteorite + Mars Desh.
crafting.addShaped('industrial_civilization:molecular_analyzer', item('industrialcivilizationcore:molecular_analyzer'), [
    [item('galacticraftcore:meteoric_iron_raw'), item('computercraft:computer:16384'), item('galacticraftcore:meteoric_iron_raw')],
    [ore('ingotSteel'), item('ic2:blockmachinemv'), ore('ingotSteel')],
    [item('ic2:itemmisc:452'), item('galacticraftplanets:item_basic_mars:2'), item('ic2:itemmisc:452')]
])

// Keep the two slice tooltips in the same reloadable layer.
event_manager.listen { ItemTooltipEvent event ->
    if (event.itemStack in item('industrialcivilizationcore:molecular_analyzer')) {
        event.toolTip << 'Consumes 6,250 EU per Martian Desh analysis'
    }
    if (event.itemStack in item('industrialcivilizationcore:material_pattern_record')) {
        event.toolTip << 'Lite Matter input for Artificial Industrial Intelligence Core synthesis'
    }
}

// Development diagnostic: use Forge's live conflict contexts rather than
// guessing from options.txt. Results are written to logs/groovy.log on reload.
if (FMLCommonHandler.instance().getSide().isClient()) {
    def activeBindings = Minecraft.getMinecraft().gameSettings.keyBindings.findAll {
        it.keyCode != 0
    }
    def conflictCount = 0
    for (int left = 0; left < activeBindings.size(); left++) {
        for (int right = left + 1; right < activeBindings.size(); right++) {
            def first = activeBindings[left]
            def second = activeBindings[right]
            if (first.conflicts(second) || first.hasKeyCodeModifierConflict(second)) {
                conflictCount++
                log.info("[key-conflict] ${first.keyDescription} (${first.keyModifier}+${first.keyCode}, ${first.keyConflictContext}) <-> ${second.keyDescription} (${second.keyModifier}+${second.keyCode}, ${second.keyConflictContext})")
            }
        }
    }
    log.info("[key-conflict-summary] ${conflictCount} live Forge conflict pair(s)")
}
