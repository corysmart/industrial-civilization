// Phase 2 placeholder recipes. Canonical definitions live in
// progression/placeholder-registry.json. Keep this entire file removable.

def placeholderConfig = new File('config/industrialcivilization/placeholders.cfg')
def placeholdersEnabled = placeholderConfig.exists() && placeholderConfig.readLines().any {
    it.trim().equalsIgnoreCase('enableTestingPlaceholders=true')
}

if (!placeholdersEnabled) {
    log.info('[industrial-civilization] Testing placeholder recipes are disabled')
    return
}

def researchPlaceholders = [
    'placeholder_research_station',
    'placeholder_orbital_experiment_module',
    'placeholder_orbital_research_archive',
    'placeholder_lunar_engineering_archive',
    'placeholder_lunar_quantum_component',
    'placeholder_mars_mission_authorization',
    'placeholder_martian_autonomy_archive'
]

def machinePlaceholders = [
    'placeholder_ai_core',
    'placeholder_electric_fabricator',
    'placeholder_programmable_assembler',
    'placeholder_robotic_manufacturing_cell'
]

researchPlaceholders.each { id ->
    crafting.addShapeless("industrial_civilization:${id}", item("industrialcivilizationcore:${id}"), [
        item('minecraft:paper'), item('minecraft:redstone')
    ])
}

machinePlaceholders.each { id ->
    crafting.addShapeless("industrial_civilization:${id}", item("industrialcivilizationcore:${id}"), [
        item('minecraft:iron_ingot'), item('minecraft:redstone')
    ])
}

// The main postInit script removes every original AE2 crafting recipe. These
// deliberately small testing recipes exist only behind the central toggle and
// every output requires the post-AI placeholder core as a physical ingredient.
def aiCore = item('industrialcivilizationcore:placeholder_ai_core')
def ae2Entry = [
    'energy_acceptor': item('appliedenergistics2:energy_acceptor'),
    'controller': item('appliedenergistics2:controller'),
    'chest': item('appliedenergistics2:chest'),
    'drive': item('appliedenergistics2:drive'),
    'storage_cell_1k': item('appliedenergistics2:storage_cell_1k'),
    'crafting_unit': item('appliedenergistics2:crafting_unit'),
    'molecular_assembler': item('appliedenergistics2:molecular_assembler')
]

ae2Entry.each { id, output ->
    crafting.addShapeless("industrial_civilization:ai_gated_ae2_${id}", output, [
        aiCore, item('minecraft:iron_ingot'), item('minecraft:redstone')
    ])
}

log.info("[industrial-civilization] Loaded ${researchPlaceholders.size() + machinePlaceholders.size()} placeholder and ${ae2Entry.size()} AI-gated AE2 testing recipes")
