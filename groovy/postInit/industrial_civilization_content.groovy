import buildcraft.lib.recipe.AssemblyRecipeRegistry

// Real Industrial Civilization content recipes. Machine outputs are handled by
// IndustrialCivilizationCore runtime logic and cannot be bypassed here.

crafting.addShaped('industrial_civilization:electric_fabricator',
    item('industrialcivilizationcore:electric_fabricator'), [
    [ore('ingotSteel'), item('minecraft:piston'), ore('ingotSteel')],
    [item('ic2:itemmisc:451'), item('ic2:blockmachinelv'), item('ic2:itemmisc:451')],
    [ore('itemRubber'), item('minecraft:crafting_table'), ore('itemRubber')]
])

crafting.addShaped('industrial_civilization:programmable_assembler',
    item('industrialcivilizationcore:programmable_assembler'), [
    [ore('ingotSteel'), item('computercraft:computer:16384'), ore('ingotSteel')],
    [item('ic2:itemmisc:452'), item('industrialcivilizationcore:electric_fabricator'), item('ic2:itemmisc:452')],
    [item('minecraft:piston'), ore('dustRedstone'), item('minecraft:piston')]
])

crafting.addShaped('industrial_civilization:robotic_manufacturing_cell',
    item('industrialcivilizationcore:robotic_manufacturing_cell'), [
    [item('ic2:itemmisc:452'), item('computercraft:computer:16384'), item('ic2:itemmisc:452')],
    [ore('ingotSteel'), item('industrialcivilizationcore:programmable_assembler'), ore('ingotSteel')],
    [item('minecraft:piston'), item('ic2:blockmachinemv'), item('minecraft:piston')]
])

// Phase-one quarry mobility preserves the physical BuildCraft Quarry. The
// controller moves it only after bedrock; players still extend power and item
// routing to each new lane until they build the later full utility deployment.
crafting.addShaped('industrial_civilization:mobile_quarry_controller',
    item('industrialcivilizationcore:mobile_quarry_controller'), [
    [ore('ingotSteel'), item('computercraft:computer:16384'), ore('ingotSteel')],
    [item('minecraft:piston'), item('ic2:blockmachinemv'), item('minecraft:piston')],
    [item('buildcraftcore:gear_diamond'), item('industrialcivilizationcore:control_processor'), item('buildcraftcore:gear_diamond')]
])

crafting.addShaped('industrial_civilization:research_station',
    item('industrialcivilizationcore:research_station'), [
    [item('minecraft:glass_pane'), item('computercraft:computer:16384'), item('minecraft:glass_pane')],
    [ore('ingotSteel'), item('ic2:blockmachinemv'), ore('ingotSteel')],
    [item('ic2:itemmisc:452'), item('industrialcivilizationcore:programmable_assembler'), item('ic2:itemmisc:452')]
])

crafting.addShaped('industrial_civilization:orbital_experiment_module',
    item('industrialcivilizationcore:orbital_experiment_module'), [
    [item('galacticraftcore:meteoric_iron_raw'), item('minecraft:glass_pane'), item('galacticraftcore:meteoric_iron_raw')],
    [ore('ingotSteel'), item('industrialcivilizationcore:research_station'), ore('ingotSteel')],
    [item('ic2:itemmisc:452'), item('industrialcivilizationcore:blank_data_cartridge'), item('ic2:itemmisc:452')]
])

crafting.addShaped('industrial_civilization:matter_replicator',
    item('industrialcivilizationcore:matter_replicator'), [
    [item('ic2:itemmisc:452'), item('industrialcivilizationcore:artificial_industrial_intelligence_core'), item('ic2:itemmisc:452')],
    [item('ic2:blockmachinehv'), item('industrialcivilizationcore:robotic_manufacturing_cell'), item('ic2:blockmachinehv')],
    [item('minecraft:obsidian'), item('industrialcivilizationcore:material_pattern_record'), item('minecraft:obsidian')]
])

crafting.addShaped('industrial_civilization:fusion_research_core',
    item('industrialcivilizationcore:fusion_research_core'), [
    [item('industrialcivilizationcore:lunar_quantum_component'), item('industrialcivilizationcore:artificial_industrial_intelligence_core'), item('industrialcivilizationcore:lunar_quantum_component')],
    [item('ic2:blockmachinehv'), item('industrialcivilizationcore:matter_replicator'), item('ic2:blockmachinehv')],
    [item('minecraft:obsidian'), item('industrialcivilizationcore:control_processor'), item('minecraft:obsidian')]
])

crafting.addShaped('industrial_civilization:interplanetary_cargo_controller',
    item('industrialcivilizationcore:interplanetary_cargo_controller'), [
    [item('galacticraftcore:meteoric_iron_raw'), item('computercraft:computer:16384'), item('galacticraftcore:meteoric_iron_raw')],
    [item('industrialcivilizationcore:control_processor'), item('industrialcivilizationcore:robotic_manufacturing_cell'), item('industrialcivilizationcore:control_processor')],
    [item('minecraft:ender_eye'), item('industrialcivilizationcore:recovered_factory_control_system'), item('minecraft:ender_eye')]
])

crafting.addShaped('industrial_civilization:civilization_service_interface',
    item('industrialcivilizationcore:civilization_service_interface'), [
    [ore('ingotSteel'), item('computercraft:computer:16384'), ore('ingotSteel')],
    [item('industrialcivilizationcore:control_processor'), item('industrialcivilizationcore:interplanetary_cargo_controller'), item('industrialcivilizationcore:control_processor')],
    [item('ic2:itemmisc:452'), item('industrialcivilizationcore:artificial_industrial_intelligence_core'), item('ic2:itemmisc:452')]
])

crafting.addShaped('industrial_civilization:orbital_megastructure_controller',
    item('industrialcivilizationcore:orbital_megastructure_controller'), [
    [item('minecraft:beacon'), item('industrialcivilizationcore:artificial_industrial_intelligence_core'), item('minecraft:beacon')],
    [item('ic2:blockmachinehv'), item('industrialcivilizationcore:interplanetary_cargo_controller'), item('ic2:blockmachinehv')],
    [item('minecraft:obsidian'), item('industrialcivilizationcore:control_processor'), item('minecraft:obsidian')]
])

crafting.addShaped('industrial_civilization:autonomous_colony_beacon',
    item('industrialcivilizationcore:autonomous_colony_beacon'), [
    [item('minecraft:beacon'), item('industrialcivilizationcore:artificial_industrial_intelligence_core'), item('minecraft:beacon')],
    [item('industrialcivilizationcore:control_processor'), item('industrialcivilizationcore:interplanetary_cargo_controller'), item('industrialcivilizationcore:control_processor')],
    [item('minecraft:diamond'), item('industrialcivilizationcore:robotic_manufacturing_cell'), item('minecraft:diamond')]
])

crafting.addShaped('industrial_civilization:environmental_solar_array',
    item('industrialcivilizationcore:environmental_solar_array'), [
    [item('minecraft:glass_pane'), item('ic2:itemmisc:452'), item('minecraft:glass_pane')],
    [item('ic2:itemmisc:451'), item('ic2:blockmachinemv'), item('ic2:itemmisc:451')],
    [ore('ingotSteel'), item('industrialcivilizationcore:control_processor'), ore('ingotSteel')]
])

crafting.addShaped('industrial_civilization:tracking_solar_array',
    item('industrialcivilizationcore:tracking_solar_array'), [
    [item('minecraft:observer'), item('industrialcivilizationcore:control_processor'), item('minecraft:observer')],
    [item('industrialcivilizationcore:environmental_solar_array'), item('industrialcivilizationcore:environmental_solar_array'), item('industrialcivilizationcore:environmental_solar_array')],
    [item('ic2:itemmisc:452'), item('computercraft:computer:16384'), item('ic2:itemmisc:452')]
])

// The core is a durable physical authorization key. These are the usable AE2
// foundation recipes; subsequent processors, terminals, storage components,
// buses, and crafting CPUs build from these outputs through the same key.
def aiCore = item('industrialcivilizationcore:artificial_industrial_intelligence_core')

// The End is disabled. Its supernatural drop is replaced globally (while
// retaining the vanilla registry ID for inherited recipe compatibility) by an
// AI-authorized, IC2-built phase component. The durable AI Core is returned by
// its container-item behavior.
crafting.removeByOutput(item('minecraft:ender_pearl'))
crafting.addShaped('industrial_civilization:technical_phase_pearl', item('minecraft:ender_pearl') * 2, [
    [item('industrialcivilizationcore:control_processor'), item('ic2:itemmisc:452'), item('industrialcivilizationcore:control_processor')],
    [item('minecraft:diamond'), aiCore, item('minecraft:diamond')],
    [item('ic2:itemmisc:452'), item('ic2:blockmachinehv'), item('ic2:itemmisc:452')]
])

// Additional Pipes registers its Item Teleport Pipe directly in BuildCraft's
// assembly-table registry. JEI hiding is not progression gating, and the stock
// chipset recipe is reachable before AI Age, so remove it by output rather than
// relying on its version-specific recipe key. The replacement requires the
// durable AI authorization core and the phase component unlocked in Chapter 15.
def teleportAssemblyKeys = AssemblyRecipeRegistry.REGISTRY.findAll { key, recipe ->
    recipe.outputPreviews.any { stack ->
        !stack.empty && stack.item?.registryName?.toString() == 'additionalpipes:pipe_items_teleport'
    }
}.keySet().toList()
teleportAssemblyKeys.each { key -> AssemblyRecipeRegistry.REGISTRY.remove(key) }

crafting.addShaped('industrial_civilization:ai_gated_item_teleport_pipe',
    item('additionalpipes:pipe_items_teleport') * 8, [
    [item('buildcraftsilicon:redstone_chipset:4'), item('minecraft:ender_pearl'), item('buildcraftsilicon:redstone_chipset:4')],
    [item('buildcrafttransport:pipe_diamond_item'), aiCore, item('buildcrafttransport:pipe_diamond_item')],
    [item('buildcraftsilicon:redstone_chipset:3'), item('industrialcivilizationcore:control_processor'), item('buildcraftsilicon:redstone_chipset:3')]
])

if (teleportAssemblyKeys.empty) {
    throw new IllegalStateException('Additional Pipes Item Teleport Pipe assembly recipe was not found')
}

def ae2Foundation = [
    'energy_acceptor': item('appliedenergistics2:energy_acceptor'),
    'controller': item('appliedenergistics2:controller'),
    'chest': item('appliedenergistics2:chest'),
    'drive': item('appliedenergistics2:drive'),
    'storage_cell_1k': item('appliedenergistics2:storage_cell_1k'),
    'crafting_unit': item('appliedenergistics2:crafting_unit'),
    'molecular_assembler': item('appliedenergistics2:molecular_assembler'),
    'interface': item('appliedenergistics2:interface'),
    'terminal': item('appliedenergistics2:part:380'),
    'storage_bus': item('appliedenergistics2:part:220'),
    'import_bus': item('appliedenergistics2:part:240'),
    'export_bus': item('appliedenergistics2:part:260')
]

// Capture the complete inherited output catalog before removing its recipes.
// Every craftable AE2 output is reconstructed behind the durable AI Core; the
// explicit foundation remains cheaper, while the rest consumes an Energy
// Acceptor to prevent a flat one-step bypass of the foundation tier.
def ae2Catalog = [:]
crafting.streamRecipes()
    .filter { recipe -> recipe.registryName?.toString()?.startsWith('appliedenergistics2:') }
    .each { recipe ->
        def stack = recipe.recipeOutput
        if (stack != null && !stack.empty && stack.item?.registryName != null) {
            def registry = stack.item.registryName.toString()
            def key = "${registry}:${stack.metadata}"
            ae2Catalog[key] = [registry: registry, metadata: stack.metadata, count: stack.count]
        }
    }
crafting.streamRecipes()
    .filter { recipe -> recipe.registryName?.toString()?.startsWith('appliedenergistics2:') }
    .removeAll()

ae2Foundation.each { id, output ->
    crafting.addShapeless("industrial_civilization:ai_gated_ae2_${id}", output, [
        aiCore, item('industrialcivilizationcore:control_processor'),
        item('minecraft:iron_ingot'), item('minecraft:redstone')
    ])
}

def reconstructed = 0
ae2Catalog.each { key, entry ->
    def safeId = "${entry.registry.replace(':', '_').replace('/', '_')}_${entry.metadata}"
    def output = item("${entry.registry}:${entry.metadata}") * Math.max(1, entry.count as int)
    crafting.addShapeless("industrial_civilization:ai_catalog_${safeId}", output, [
        aiCore, item('industrialcivilizationcore:control_processor'),
        item('appliedenergistics2:energy_acceptor'), item('ic2:itemmisc:452')
    ])
    reconstructed++
}

log.info("[industrial-civilization] Loaded real machine recipes, ${ae2Foundation.size()} foundation recipes, and ${reconstructed} AI-authorized AE2 catalog recipes")
