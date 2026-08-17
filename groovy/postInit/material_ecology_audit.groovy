// Runtime recipe audit for Industrial Civilization's human-centered ecology.
// Keep this script reloadable with /gs reload --clean: it inspects the final
// Forge crafting registry after CraftTweaker and every mod have registered.

def auditedInputs = [
    'slime_ball': item('minecraft:slime_ball'),
    'ghast_tear': item('minecraft:ghast_tear'),
    'bone': item('minecraft:bone'),
    'string': item('minecraft:string'),
    'spider_eye': item('minecraft:spider_eye'),
    'gunpowder': item('minecraft:gunpowder'),
    'ender_pearl': item('minecraft:ender_pearl'),
    'blaze_rod': item('minecraft:blaze_rod'),
    'blaze_powder': item('minecraft:blaze_powder'),
    'magma_cream': item('minecraft:magma_cream'),
    'nether_star': item('minecraft:nether_star'),
    'nether_quartz': item('minecraft:quartz'),
    'netherrack': item('minecraft:netherrack'),
    'soul_sand': item('minecraft:soul_sand'),
    'nether_wart': item('minecraft:nether_wart'),
    'nether_brick': item('minecraft:netherbrick'),
    'glowstone_dust': item('minecraft:glowstone_dust'),
    'end_stone': item('minecraft:end_stone'),
    'chorus_fruit': item('minecraft:chorus_fruit'),
    'popped_chorus_fruit': item('minecraft:chorus_fruit_popped'),
    'purpur_block': item('minecraft:purpur_block'),
    'purpur_pillar': item('minecraft:purpur_pillar'),
    'shulker_shell': item('minecraft:shulker_shell'),
    'dragon_breath': item('minecraft:dragon_breath'),
    'dragon_egg': item('minecraft:dragon_egg'),
    'elytra': item('minecraft:elytra'),
    'end_crystal': item('minecraft:end_crystal')
]

def matches = auditedInputs.collectEntries { key, stack -> [(key): []] }
crafting.streamRecipes().each { recipe ->
    recipe.ingredients.each { ingredient ->
        ingredient.matchingStacks.each { candidate ->
            auditedInputs.each { key, stack ->
                if (candidate.item == stack.item && candidate.metadata == stack.metadata) {
                    matches[key] << "${recipe.registryName} -> ${recipe.recipeOutput}"
                }
            }
        }
    }
}

matches.each { key, recipes ->
    def unique = recipes.unique().sort()
    log.info("[material-ecology-audit] ${key}: ${unique.size()} recipe(s)")
    unique.each { recipe -> log.info("[material-ecology-audit] ${key} | ${recipe}") }
}

// Elytra recipes are dyeing/duplication operations that already require an
// Elytra and therefore cannot gate an unrelated machine. Engineered Ender
// Pearls are intentionally available as Technical Phase Pearls. Every other
// dimension-exclusive vanilla input must be absent from the final registry.
def forbiddenResiduals = [
    'slime_ball', 'ghast_tear',
    'blaze_rod', 'blaze_powder', 'magma_cream', 'nether_star',
    'nether_quartz', 'netherrack', 'soul_sand', 'nether_wart',
    'nether_brick', 'glowstone_dust',
    'end_stone', 'chorus_fruit', 'popped_chorus_fruit', 'purpur_block',
    'purpur_pillar', 'shulker_shell', 'dragon_breath', 'dragon_egg',
    'end_crystal'
].collectMany { key ->
    matches[key].unique().collect { recipe -> "${key}: ${recipe}" }
}
if (!forbiddenResiduals.empty) {
    throw new IllegalStateException('Forbidden inaccessible crafting inputs remain after unification: '
        + forbiddenResiduals.join('; '))
}
