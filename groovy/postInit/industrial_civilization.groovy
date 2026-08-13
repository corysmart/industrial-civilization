// Industrial Civilization vertical-slice integration.
// This postInit script is reloadable in-game with: /gs reload --clean

import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraft.client.Minecraft

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

// MFFS registers its two progression-critical machine outputs and recipes, but
// does not contribute those ItemBlocks to HEI's searchable ingredient list.
// Re-add only the machines used by Industrial Civilization progression; this
// keeps internal MFFS cards/debug tools hidden while making name and @mod
// searches lead players to the real recipes.
def mffsProgressionMachines = [
    item('modularforcefieldsystem:projector'),
    item('modularforcefieldsystem:capacitor')
]
mods.jei.ingredient.add(mffsProgressionMachines)

// The magical Totem is replaced by a Robotic Manufacturing Cell product.
// Removing it here covers recipe viewers and any inherited recipe source;
// the core mod also suppresses drops and creative exposure.
def vanillaTotem = item('minecraft:totem_of_undying')
crafting.removeByOutput(vanillaTotem)
mods.jei.ingredient.removeAndHide([vanillaTotem])

// IC Credits can be minted only as an expensive emergency measure. Selling
// ordinary components at neutral settlement prices is intentionally cheaper.
crafting.addShaped('industrial_civilization:industrial_credit', item('industrialcivilizationcore:industrial_credit'), [
    [ore('ingotCopper'), item('minecraft:redstone'), ore('ingotCopper')],
    [ore('ingotIron'), ore('ingotGold'), ore('ingotIron')],
    [null, item('ic2:itemmisc:451'), null]
])

// Onysd's generic Workstation is disabled: the six curated road vehicles are
// produced as powered programs in the covered, weather-sensitive Car Workshop.
crafting.removeByOutput(item('vehicle:workstation'))

crafting.addShaped('industrial_civilization:car_workshop', item('industrialcivilizationcore:car_workshop'), [
    [ore('ingotSteel'), item('ic2:itemmisc:452'), ore('ingotSteel')],
    [item('minecraft:piston'), item('industrialcivilizationcore:programmable_assembler'), item('minecraft:piston')],
    [ore('ingotSteel'), item('ic2:blockmachinemv'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:gun_factory', item('industrialcivilizationcore:gun_factory'), [
    [ore('ingotSteel'), item('ic2:itemmisc:452'), ore('ingotSteel')],
    [item('minecraft:dispenser'), item('industrialcivilizationcore:programmable_assembler'), item('minecraft:dispenser')],
    [ore('ingotSteel'), item('ic2:blockmachinemv'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:repair_bench', item('industrialcivilizationcore:repair_bench'), [
    [ore('ingotSteel'), item('minecraft:anvil'), ore('ingotSteel')],
    [ore('ingotIron'), item('ic2:itemmisc:451'), ore('ingotIron')]
])
crafting.addShaped('industrial_civilization:vehicle_service_dock', item('industrialcivilizationcore:vehicle_service_dock'), [
    [item('buildcrafttransport:pipe_diamond_item'), item('industrialcivilizationcore:control_processor'), item('vehicle:fluid_pipe')],
    [ore('ingotSteel'), item('ic2:blockmachinelv'), ore('ingotSteel')]
])

// Every block shown on the workshop architecture board is a normal,
// independently craftable building block. Auto-deployment uses these exact
// registry objects; players may instead build or decorate with them directly.
crafting.addShaped('industrial_civilization:workshop_steel_frame', item('industrialcivilizationcore:steel_frame') * 4, [
    [ore('ingotSteel'), null, ore('ingotSteel')], [ore('ingotSteel'), ore('ingotSteel'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:workshop_steel_casing', item('industrialcivilizationcore:steel_casing') * 4, [
    [ore('ingotSteel'), ore('plateIron'), ore('ingotSteel')], [ore('plateIron'), null, ore('plateIron')], [ore('ingotSteel'), ore('plateIron'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:workshop_machine_casing', item('industrialcivilizationcore:machine_casing') * 2, [
    [ore('plateIron'), item('ic2:itemmisc:451'), ore('plateIron')], [ore('ingotSteel'), item('ic2:blockmachinelv'), ore('ingotSteel')]
])
crafting.addShaped('industrial_civilization:workshop_reinforced_plate', item('industrialcivilizationcore:reinforced_plate') * 6, [
    [ore('plateIron'), ore('ingotSteel'), ore('plateIron')], [ore('plateIron'), ore('ingotSteel'), ore('plateIron')]
])
crafting.addShaped('industrial_civilization:workshop_grated_plate', item('industrialcivilizationcore:grated_plate') * 6, [
    [item('minecraft:iron_bars'), ore('plateIron'), item('minecraft:iron_bars')], [ore('plateIron'), item('minecraft:iron_bars'), ore('plateIron')]
])
crafting.addShaped('industrial_civilization:workshop_industrial_floor', item('industrialcivilizationcore:industrial_floor') * 8, [
    [item('minecraft:stone_slab'), item('industrialcivilizationcore:reinforced_plate'), item('minecraft:stone_slab')], [item('minecraft:stone_slab'), item('industrialcivilizationcore:reinforced_plate'), item('minecraft:stone_slab')]
])
crafting.addShaped('industrial_civilization:workshop_hazard_stripe', item('industrialcivilizationcore:hazard_stripe') * 8, [
    [item('industrialcivilizationcore:industrial_floor'), item('minecraft:dye:11')], [item('minecraft:dye'), item('industrialcivilizationcore:industrial_floor')]
])
crafting.addShaped('industrial_civilization:workshop_cable_block', item('industrialcivilizationcore:workshop_cable_block') * 4, [
    [ore('plateIron'), item('ic2:itemcable'), ore('plateIron')], [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:workshop_cable_cover', item('industrialcivilizationcore:workshop_cable_cover') * 6, [
    [ore('plateIron'), item('industrialcivilizationcore:workshop_cable_block'), ore('plateIron')]
])
crafting.addShaped('industrial_civilization:workshop_glass_panel', item('industrialcivilizationcore:reinforced_glass_panel') * 6, [
    [ore('ingotSteel'), item('minecraft:glass_pane'), ore('ingotSteel')], [item('minecraft:glass_pane'), item('minecraft:glass_pane'), item('minecraft:glass_pane')]
])
crafting.addShaped('industrial_civilization:workshop_tool_wall', item('industrialcivilizationcore:tool_wall') * 2, [
    [item('minecraft:shears'), item('minecraft:iron_pickaxe'), item('ic2:itemtoolwrench')], [ore('plateIron'), item('industrialcivilizationcore:steel_casing'), ore('plateIron')]
])
crafting.addShaped('industrial_civilization:workshop_drawer_cabinet', item('industrialcivilizationcore:drawer_cabinet') * 2, [
    [item('minecraft:chest'), ore('plateIron'), item('minecraft:chest')], [ore('ingotSteel'), item('industrialcivilizationcore:steel_casing'), ore('ingotSteel')]
])

// Firearms above simple black-powder weapons are machine products. The pistol
// is printed by the Programmable Assembler; shotgun and M4 require the Gun Factory.
crafting.removeByOutput(item('techguns:pistol'))
crafting.removeByOutput(item('techguns:combatshotgun'))
crafting.removeByOutput(item('techguns:m4'))

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
