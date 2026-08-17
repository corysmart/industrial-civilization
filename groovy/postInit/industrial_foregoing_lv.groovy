// Peaceful Industrial Foregoing agriculture is an IC2 LV companion in this
// pack. Remove every inherited plastic/Tesla-frame variant, then rebuild the
// farm machines from parts available with the first IC2 electrical workshop.
def lvFarmMachines = [
    item('industrialforegoing:crop_sower'),
    item('industrialforegoing:crop_recolector'),
    item('industrialforegoing:resourceful_furnace'),
    item('industrialforegoing:plant_interactor'),
    item('industrialforegoing:crop_enrich_material_injector'),
    item('industrialforegoing:animal_stock_increaser'),
    item('industrialforegoing:animal_growth_increaser'),
    item('industrialforegoing:animal_independence_selector'),
    item('industrialforegoing:animal_resource_harvester'),
    item('industrialforegoing:animal_byproduct_recolector'),
    item('industrialforegoing:sewage_composter_solidifier'),
    item('industrialforegoing:water_resources_collector')
]
lvFarmMachines.each { crafting.removeByOutput(it) }

crafting.addShaped('industrial_civilization:lv_plant_sower', item('industrialforegoing:crop_sower'), [
    [ore('plateIron'), item('minecraft:flower_pot'), ore('plateIron')],
    [item('minecraft:piston'), item('ic2:blockmachinelv'), item('minecraft:piston')],
    [ore('itemRubber'), item('ic2:itemmisc:451'), ore('itemRubber')]
])
crafting.addShaped('industrial_civilization:lv_plant_gatherer', item('industrialforegoing:crop_recolector'), [
    [item('minecraft:iron_axe'), item('minecraft:iron_hoe'), item('minecraft:shears')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_resourceful_furnace', item('industrialforegoing:resourceful_furnace'), [
    [ore('plateIron'), item('minecraft:furnace'), ore('plateIron')],
    [item('minecraft:bucket'), item('ic2:blockmachinelv'), item('minecraft:bucket')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_plant_interactor', item('industrialforegoing:plant_interactor'), [
    [item('minecraft:iron_hoe'), ore('itemRubber'), item('minecraft:iron_hoe')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_plant_fertilizer', item('industrialforegoing:crop_enrich_material_injector'), [
    [item('minecraft:glass_bottle'), item('minecraft:dye:15'), item('minecraft:glass_bottle')],
    [ore('itemRubber'), item('ic2:blockmachinelv'), ore('itemRubber')],
    [ore('plateIron'), item('ic2:itemmisc:451'), ore('plateIron')]
])
crafting.addShaped('industrial_civilization:lv_animal_breeder', item('industrialforegoing:animal_stock_increaser'), [
    [item('minecraft:wheat'), item('minecraft:golden_carrot'), item('minecraft:wheat')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [ore('itemRubber'), item('ic2:itemmisc:451'), ore('itemRubber')]
])
crafting.addShaped('industrial_civilization:lv_animal_growth', item('industrialforegoing:animal_growth_increaser'), [
    [item('minecraft:wheat'), item('minecraft:apple'), item('minecraft:wheat')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_animal_separator', item('industrialforegoing:animal_independence_selector'), [
    [item('minecraft:iron_bars'), item('minecraft:lead'), item('minecraft:iron_bars')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_animal_harvester', item('industrialforegoing:animal_resource_harvester'), [
    [item('minecraft:shears'), item('minecraft:bucket'), item('minecraft:shears')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [ore('itemRubber'), item('ic2:itemmisc:451'), ore('itemRubber')]
])
crafting.addShaped('industrial_civilization:lv_sewage_collector', item('industrialforegoing:animal_byproduct_recolector'), [
    [item('minecraft:brick'), item('minecraft:bucket'), item('minecraft:brick')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [ore('itemRubber'), item('ic2:itemmisc:451'), ore('itemRubber')]
])
crafting.addShaped('industrial_civilization:lv_sewage_composter', item('industrialforegoing:sewage_composter_solidifier'), [
    [item('minecraft:brick'), item('minecraft:furnace'), item('minecraft:brick')],
    [item('minecraft:piston'), item('ic2:blockmachinelv'), item('minecraft:piston')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
crafting.addShaped('industrial_civilization:lv_water_resource_collector', item('industrialforegoing:water_resources_collector'), [
    [item('minecraft:fishing_rod'), item('minecraft:bucket'), item('minecraft:fishing_rod')],
    [ore('plateIron'), item('ic2:blockmachinelv'), ore('plateIron')],
    [item('ic2:itemcable'), item('ic2:itemmisc:451'), item('ic2:itemcable')]
])
