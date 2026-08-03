import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;
import crafttweaker.oredict.IOreDict;
import crafttweaker.oredict.IOreDictEntry;

// Craftable (full) Akashic Tome
var startingTome = <akashictome:tome>.withTag(
	{"akashictome:data": {
		industrialforegoing: {id: "industrialforegoing:book_manual", Count: 1 as byte, tag: {"akashictome:definedMod": "industrialforegoing"}, Damage: 0 as short}, 
		buildcraftlib: {id: "buildcraftlib:guide", Count: 1 as byte, tag: {"akashictome:definedMod": "buildcraftlib"}, Damage: 0 as short}, 
		forestry: {id: "forestry:book_forester", Count: 1 as byte, tag: {"akashictome:definedMod": "forestry"}, Damage: 0 as short}, 
		modularforcefieldsystem: {id: "guideapi:minecraft-mffsguide", Count: 1 as byte, tag: {"akashictome:definedMod": "modularforcefieldsystem"}, Damage: 0 as short}, 
		logisticspipes: {id: "logisticspipes:guide_book", Count: 1 as byte, tag: {"akashictome:definedMod": "logisticspipes"}, Damage: 0 as short}}}); 
startingTome.addTooltip(format.aqua("Craftable with all mod guides included"));
mods.jei.JEI.addItem(startingTome);
recipes.addShapeless(startingTome, [<minecraft:book>, oreDict.bookshelf]);

// Galacticraft Pre-Launch Checklist reasonable recipe
recipes.removeShapeless(<galacticraftcore:prelaunch_checklist>);
recipes.addShapeless(<galacticraftcore:prelaunch_checklist>,
													[<galacticraftcore:canvas>, <minecraft:dye:6>]);

// Add a furnace recipe for Galacticraft's silicon & solar ores
furnace.addRecipe(<galacticraftcore:basic_item:2>, <galacticraftcore:basic_block_core:8>);
furnace.addRecipe(<galacticraftcore:basic_item:2>, <galacticraftplanets:venus:10>);
furnace.addRecipe(<galacticraftplanets:basic_item_venus:4>, <galacticraftplanets:venus:13>);

// Add Railcraft's Block of Coal Coke to ore dictionary
val oreDictBlockCoalCoke = <ore:blockCoalCoke>;
oreDictBlockCoalCoke.add(<railcraft:coke_block>);

// Add IC2C's Block of Charcoal to ore dictionary
val oreDictBlockCharcoal = <ore:blockCharcoal>;
oreDictBlockCharcoal.add(<ic2:blockmetal:12>);

// Add IC2C's Obsidian Dust to ore dictionary
val oreDictObsidianDust = <ore:dustObsidian>;
oreDictObsidianDust.add(<ic2:itemmisc:9>);

// Add vanilla's Bookshelf to ore dictionary
val oreDictBookshelf = <ore:bookshelf>;
oreDictBookshelf.add(<minecraft:bookshelf>);

// Remove duplicate, unnecessary or purposefully disabled recipes
recipes.removeByRecipeName("quark:hopper");
recipes.removeByRecipeName("minecraft:gold_ingot_from_block");
recipes.removeByRecipeName("minecraft:iron_ingot_from_block");
recipes.removeByRecipeName("minecraft:diamond");
recipes.removeByRecipeName("minecraft:emerald");
recipes.removeByRecipeName("minecraft:redstone");
recipes.removeByRecipeName("minecraft:coal");
recipes.removeByRecipeName("zettaindustries:coal_from_charcoal_block");
recipes.removeByRecipeName("zettaindustries:charcoal_block_from_coal");
recipes.removeByRecipeName("forestry:charcoal");
recipes.removeByRecipeName("forestry:charcoal_block");
recipes.removeByRecipeName("quark:coal");
recipes.removeByRecipeName("quark:charcoal_block");
recipes.removeByRecipeName("chisel:charcoal");
recipes.removeByRecipeName("railcraft:fuel_coke$1");
recipes.removeByRecipeName("projectred-exploration:blocks/barrel");
recipes.removeByRecipeName("ic2:shaped_item.charcoal_-1261499980");
recipes.removeByRecipeName("zettaindustries:sulfur_blaze");
recipes.removeByRecipeName("industrialforegoing:black_hole_controller_reworked_black_hole_controller_deprecated");
recipes.removeByRecipeName("railcraft:gear#1$1");
recipes.removeByRecipeName("railcraft:gear#4$1");
recipes.removeByRecipeName("railcraft:gear#3$2");
recipes.removeByRecipeName("railcraft:forestry_sturdy_machine$1");
recipes.removeByRecipeName("projectred-expansion:device/teleposer");
recipes.removeByRecipeName("projectred-expansion:items/jetpack");
recipes.removeByRecipeName("akashictome:tome");
recipes.removeByRecipeName("irontanks:diamond_emerald_tank");
recipes.removeByRecipeName("irontanks:diamond_emerald_upgrade");
recipes.removeByRecipeName("galacticraftcore:basic_item_5");
recipes.removeByRecipeName("hammercore:manual");
recipes.removeByRecipeName("hammercore:cableflux_recipescf.0");
recipes.removeByRecipeName("hammercore:cableflux_recipescf.1");
recipes.removeByRecipeName("hammercore:cableflux_recipescf.2");
recipes.removeByRecipeName("ic2:shaped_tile.pistonstickybase_1809078030");
recipes.removeByRecipeName("industrialforegoing:infinity_drill_0");
recipes.removeByRecipeName("minecraft:purple_shulker_box");

// ICBM strategic payloads cannot bypass the IC2 nuclear and AI stages. Their
// native crafting-table chains are disabled; controlled machine production is
// introduced only alongside the corresponding progression quests.
recipes.removeByRecipeName("icbmclassic:explosives/nuclear");
recipes.removeByRecipeName("icbmclassic:explosives/antimatter");
recipes.removeByRecipeName("icbmclassic:explosives/redmatter");
recipes.removeByRecipeName("icbmclassic:missile/ex/nuclear");
recipes.removeByRecipeName("icbmclassic:missile/ex/antimatter");
recipes.removeByRecipeName("icbmclassic:missile/ex/redmatter");
recipes.removeByRecipeName("icbmclassic:bombcarts/nuclear");
recipes.removeByRecipeName("icbmclassic:bombcarts/antimatter");
recipes.removeByRecipeName("icbmclassic:bombcarts/redmatter");

// Remove duplicate colored wool recipes due to Quark
recipes.removeByRecipeName("minecraft:orange_wool");
recipes.removeByRecipeName("minecraft:magenta_wool");
recipes.removeByRecipeName("minecraft:light_blue_wool");
recipes.removeByRecipeName("minecraft:yellow_wool");
recipes.removeByRecipeName("minecraft:lime_wool");
recipes.removeByRecipeName("minecraft:pink_wool");
recipes.removeByRecipeName("minecraft:gray_wool");
recipes.removeByRecipeName("minecraft:light_gray_wool");
recipes.removeByRecipeName("minecraft:cyan_wool");
recipes.removeByRecipeName("minecraft:purple_wool");
recipes.removeByRecipeName("minecraft:blue_wool");
recipes.removeByRecipeName("minecraft:brown_wool");
recipes.removeByRecipeName("minecraft:green_wool");
recipes.removeByRecipeName("minecraft:red_wool");
recipes.removeByRecipeName("minecraft:black_wool");

// Disable ProjectRed armor
recipes.removeByRecipeName("projectred-exploration:armor/ruby_helmet");
recipes.removeByRecipeName("projectred-exploration:armor/ruby_chestplate");
recipes.removeByRecipeName("projectred-exploration:armor/ruby_leggings");
recipes.removeByRecipeName("projectred-exploration:armor/ruby_boots");
recipes.removeByRecipeName("projectred-exploration:armor/sapphire_helmet");
recipes.removeByRecipeName("projectred-exploration:armor/sapphire_chestplate");
recipes.removeByRecipeName("projectred-exploration:armor/sapphire_leggings");
recipes.removeByRecipeName("projectred-exploration:armor/sapphire_boots");
recipes.removeByRecipeName("projectred-exploration:armor/peridot_helmet");
recipes.removeByRecipeName("projectred-exploration:armor/peridot_chestplate");
recipes.removeByRecipeName("projectred-exploration:armor/peridot_leggings");
recipes.removeByRecipeName("projectred-exploration:armor/peridot_boots");

// Remove Galacticraft "lead" from ore dictionary to highlight progression & avoid confusion
val oreDictIngotLead = <ore:ingotLead>;
val oreDictBlockLead = <ore:blockLead>;
val oreDictOreLead = <ore:oreLead>;
oreDictIngotLead.remove(<galacticraftplanets:basic_item_venus:1>);
oreDictBlockLead.remove(<galacticraftplanets:venus:12>);
oreDictOreLead.remove(<galacticraftplanets:venus:8>);

// Remove Industrial Foregoing's plastic from rubber ore dictionary to separate it from IC2C's rubber
val oreDictItemRubber = <ore:itemRubber>;
oreDictItemRubber.remove(<industrialforegoing:plastic>);

// Add tooltips for useful energy related items
<ic2:blockcompactedgenerator:6>.addTooltip("Converts EU to RF. Max output: " + format.green("256") + format.gray(" RF/t"));
<ic2:blockcompactedgenerator:7>.addTooltip("Converts EU to RF. Max output: " + format.green("1024") + format.gray(" RF/t"));
<ic2:blockcompactedgenerator:8>.addTooltip("Converts EU to RF. Max output: " + format.green("4096") + format.gray(" RF/t"));
<zettaindustries:batterycontroller>.addTooltip("Multiblock structure storing RF energy");

// Modify Galacticraft's wire recipes
recipes.remove(<galacticraftcore:aluminum_wire>);
recipes.addShaped(<galacticraftcore:aluminum_wire> * 6,
													[[<ic2:itemmisc:450>, <ic2:itemmisc:450>, <ic2:itemmisc:450>],
													[oreDict.ingotAluminum, oreDict.ingotAluminum, oreDict.ingotAluminum],
													[<ic2:itemmisc:450>, <ic2:itemmisc:450>, <ic2:itemmisc:450>]]);

recipes.remove(<galacticraftcore:aluminum_wire:1>);
recipes.addShapeless(<galacticraftcore:aluminum_wire:1>,
													[<galacticraftcore:aluminum_wire>, oreDict.ingotAluminum, <ic2:itemmisc:450>]);

// Modify Nature's Compass recipe
recipes.remove(<naturescompass:naturescompass>);
recipes.addShaped(<naturescompass:naturescompass>,
													[[oreDict.treeSapling, oreDict.logWood, oreDict.treeSapling],
													[oreDict.logWood, <ic2:itemmisc:263>, oreDict.logWood],
													[oreDict.treeSapling, oreDict.logWood, oreDict.treeSapling]]);

// Modify IC2C's Nuke recipe
recipes.remove(<ic2:blockexplosive:1>);
recipes.addShaped(<ic2:blockexplosive:1>,
										[[<minecraft:gunpowder>, <ic2:blockmetal:10>, <minecraft:gunpowder>],
										[<ic2:blockmetal:10>, <minecraft:gunpowder>, <ic2:blockmetal:10>],
										[<minecraft:gunpowder>, <ic2:blockmetal:10>, <minecraft:gunpowder>]]);

// Fix IC2C's wrench craze
recipes.remove(<ic2:precisionwrench>);
recipes.addShapedMirrored(<ic2:precisionwrench>.withTag({Lossless: 1 as byte}),
																					[[<ic2:itemtoolwrench>, null, null],
																					[null, <ic2:itemmisc:452>, null],
																					[null, null, <ic2:itembatcrystal>]]);
recipes.addShapedMirrored(<ic2:precisionwrench>.withTag({Lossless: 1 as byte}),
																					[[<ic2:itemtoolwrenchelectric>, null, null],
																					[null, <ic2:itemmisc:452>, null],
																					[null, null, <ic2:itembatcrystal>]]);
<ic2:precisionwrench>.removeTooltip("Lossless uses Left:");
<ic2:itemtoolwrench>.addTooltip("Not lossless");

// Fix Quark iron plate recipe conflict
recipes.remove(<quark:iron_plate>);
recipes.addShaped(<quark:iron_plate> * 24,
											[[<minecraft:iron_ingot>, <minecraft:iron_ingot>, <minecraft:iron_ingot>],
											[<minecraft:iron_ingot>, <minecraft:iron_bars>, <minecraft:iron_ingot>],
											[<minecraft:iron_ingot>, <minecraft:iron_ingot>, <minecraft:iron_ingot>]]);

// Fix MFFS recipes
recipes.remove(<modularforcefieldsystem:forcepower_crystal:0>);
recipes.addShaped(<modularforcefieldsystem:forcepower_crystal:101>.withTag({ForceEnergy: 0}),
																									[[<modularforcefieldsystem:forcicium>, <modularforcefieldsystem:forcicium>, <modularforcefieldsystem:forcicium>],
																									[<modularforcefieldsystem:forcicium>, oreDict.gemDiamond, <modularforcefieldsystem:forcicium>],
																									[<modularforcefieldsystem:forcicium>, <modularforcefieldsystem:forcicium>, <modularforcefieldsystem:forcicium>]]);

recipes.remove(<modularforcefieldsystem:forcicium_cell:0>);
recipes.addShaped(<modularforcefieldsystem:forcicium_cell:100>.withTag({Forceciumlevel: 0}),
																								[[<ic2:itemmisc:53>, <ic2:itemmisc:53>, <ic2:itemmisc:53>],
																								[<ic2:itemmisc:53>, <modularforcefieldsystem:forcepower_crystal:*>, <ic2:itemmisc:53>],
																								[<ic2:itemmisc:53>, <ic2:itemmisc:452>, <ic2:itemmisc:53>]]);

recipes.remove(<modularforcefieldsystem:projector_focus_matrix>);
recipes.addShaped(<modularforcefieldsystem:projector_focus_matrix> * 64,
																			[[<ic2:itemmisc:53>, <minecraft:glass>, <ic2:itemmisc:53>],
																			[<minecraft:glass>, oreDict.gemDiamond, <minecraft:glass>],
																			[<ic2:itemmisc:53>, <minecraft:glass>, <ic2:itemmisc:53>]]);

recipes.remove(<modularforcefieldsystem:control_system>);
recipes.addShaped(<modularforcefieldsystem:control_system>,
															[[<minecraft:ender_pearl>, <ic2:itemmisc:451>, <minecraft:ender_pearl>],
															[<ic2:itemmisc:53>, <ic2:blockmachinemv>, <ic2:itemmisc:53>],
															[<ic2:itemmisc:53>, <ic2:itemmisc:451>, <ic2:itemmisc:53>]]);

recipes.remove(<modularforcefieldsystem:security_storage>);
recipes.addShaped(<modularforcefieldsystem:security_storage>,
																[[<ic2:itemmisc:53>, <ic2:itemmisc:53>, <ic2:itemmisc:53>],
																[<ic2:itemmisc:53>, <ic2:itemmisc:451>, <ic2:itemmisc:53>],
																[<ic2:itemmisc:53>, <ic2:itemmisc:53>, <ic2:itemmisc:53>]]);

recipes.addShaped(<modularforcefieldsystem:module_diagonal_wall>,
																	[[<modularforcefieldsystem:projector_focus_matrix>, null, <modularforcefieldsystem:projector_focus_matrix>],
																											[null, <minecraft:obsidian>, null],
																	[<modularforcefieldsystem:projector_focus_matrix>, null, <modularforcefieldsystem:projector_focus_matrix>]]);

recipes.addShaped(<modularforcefieldsystem:module_sphere>,
																						[[null, <minecraft:obsidian>, null],
															[<minecraft:obsidian>, <modularforcefieldsystem:projector_focus_matrix>, <minecraft:obsidian>],
																						[null, <minecraft:obsidian>, null]]);

recipes.addShaped(<modularforcefieldsystem:module_cube>,
															[[<minecraft:obsidian>, null, <minecraft:obsidian>],
															[null, <modularforcefieldsystem:projector_focus_matrix>, null],
															[<minecraft:obsidian>, null, <minecraft:obsidian>]]);

recipes.addShapedMirrored(<modularforcefieldsystem:module_wall>,
																	[[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, null],
																	[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, null],
																	[<minecraft:obsidian>, <minecraft:obsidian>, null]]);

recipes.addShaped(<modularforcefieldsystem:module_deflector>,
																[[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>],
																[<modularforcefieldsystem:projector_focus_matrix>, <minecraft:obsidian>, <modularforcefieldsystem:projector_focus_matrix>],
																[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>]]);

recipes.addShaped(<modularforcefieldsystem:module_tube>,
															[[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>],
																														[null, <minecraft:obsidian>, null],
															[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>]]);

recipes.addShaped(<modularforcefieldsystem:module_containment>,
																	[[<minecraft:obsidian>, <minecraft:obsidian>, <minecraft:obsidian>],
																	[<minecraft:obsidian>, <modularforcefieldsystem:projector_focus_matrix>, <minecraft:obsidian>],
																	[<minecraft:obsidian>, <minecraft:obsidian>, <minecraft:obsidian>]]);

recipes.addShaped(<modularforcefieldsystem:module_advcube>,
															[[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>],
															[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:module_cube>, <modularforcefieldsystem:projector_focus_matrix>],
															[<modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>, <modularforcefieldsystem:projector_focus_matrix>]]);

// More expensive Steve's Carts 2 chunk loader
recipes.remove(<stevescarts:cartmodule:49>);
recipes.addShaped(<stevescarts:cartmodule:49>,
											[[null, <stevescarts:modulecomponents:45>, null],
											[<stevescarts:modulecomponents:16>, <stevescarts:modulecomponents:49>, <stevescarts:modulecomponents:16>],
											[<stevescarts:modulecomponents:49>, <stevescarts:modulecomponents:16>, <stevescarts:modulecomponents:49>]]);

// Add IC2 Classic rubber tree support to Industrial Foregoing extractor
mods.industrialforegoing.Extractor.add(<ic2:blockrubwood>, <liquid:latex> * 4);

// Fix ash brick stairs recipe
recipes.remove(<forestry:ash_stairs>);
recipes.addShapedMirrored(<forestry:ash_stairs> * 8,
													[[<forestry:ash_brick>, null, null],
													[<forestry:ash_brick>, <forestry:ash_brick>, null],
													[<forestry:ash_brick>, <forestry:ash_brick>, <forestry:ash_brick>]]);

// Progression naming
<ic2:itemsolarhelmets:1>.displayName = "Compacted Solar Helmet";

// Fix Railcraft's EU loader/unloader recipes						
recipes.remove(<railcraft:manipulator:6>);
recipes.addShaped(<railcraft:manipulator:6>,
											[[<ic2:itembatre:*>, <minecraft:hopper>, <ic2:itembatre:*>],
											[<ic2:itembatre:*>, <ic2:blockmachinelv>, <ic2:itembatre:*>],
											[<ic2:itembatre:*>, <railcraft:detector:7>, <ic2:itembatre:*>]]);

recipes.remove(<railcraft:manipulator:7>);
recipes.addShaped(<railcraft:manipulator:7>,
											[[<ic2:itembatre:*>, <railcraft:detector:7>, <ic2:itembatre:*>],
											[<ic2:itembatre:*>, <ic2:blockmachinelv>, <ic2:itembatre:*>],
											[<ic2:itembatre:*>, <minecraft:hopper>, <ic2:itembatre:*>]]);

// Fix naming
<railcraft:manipulator:6>.displayName = "IC2 EU Loader";
<railcraft:manipulator:7>.displayName = "IC2 EU Unloader";

// Add info for Giant Sequoia								
mods.jei.JEI.addDescription(<forestry:pollen_fertile>.withTag({IsAnalyzed: 0 as byte, Genome: {Chromosomes: [{UID1: "forestry.treeGigant", UID0: "forestry.treeGigant"}]}}), "Rumor has it that only the determined may find the pollen of this grand tree from specialized arborist villagers…");
mods.jei.JEI.addDescription(<forestry:sapling>.withTag({IsAnalyzed: 0 as byte, Genome: {Chromosomes: [{UID1: "forestry.treeGigant", UID0: "forestry.treeGigant"}]}}), "Rumor has it that only the determined may find the pollen of this grand tree from specialized arborist villagers…");

// Modify Grate recipe (avoid conflict)
recipes.remove(<quark:grate>);
recipes.addShaped(<quark:grate>,
								[[<minecraft:iron_bars>, <minecraft:iron_bars>]]);
										
// Modify Wireless Modem recipe (avoid conflict)
recipes.removeByRecipeName("computercraft:wired_modem");
recipes.addShaped(<computercraft:cable:1>,
										[[oreDict.stone, oreDict.stone, oreDict.stone],
										[oreDict.stone, <minecraft:redstone_torch>, oreDict.stone],
										[oreDict.stone, oreDict.stone, oreDict.stone]]);

// Modify Advanced Electric Jetpack recipe (nerf)
recipes.removeByRecipeName("gravisuit:shaped_item.advancedelectricjetpack_1513591940");
recipes.addShaped(<gravisuit:advancedelectricjetpack>,
										[[<ic2:itemmisc:256>, <ic2:itemarmorjetpackelectric>, <ic2:itemmisc:256>],
										[<gravisuit:engineboost>, <ic2:itemarmorlappack>, <gravisuit:engineboost>],
										[<ic2:itemcable:9>, <ic2:itemmisc:452>, <ic2:itemcable:9>]]);
										
// Modify Advanced Nuclear Jetpack recipe (nerf)
recipes.removeByRecipeName("gravisuit:shaped_item.advancednuclearjetpack_-801877924");
recipes.addShaped(<gravisuit:advancednuclearjetpack>,
										[[<ic2:itemmisc:256>, <ic2:itemarmorjetpacknuclear>, <ic2:itemmisc:256>],
										[<gravisuit:engineboost>, <ic2:itemarmorlappack>, <gravisuit:engineboost>],
										[<ic2:itemcable:9>, <ic2:itemmisc:452>, <ic2:itemcable:9>]]);

// Modify Compacted Jetpack recipe (nerf)
recipes.removeByRecipeName("ic2:shaped_item.itemarmorcombinedjetpack_1604687096");
recipes.addShaped(<ic2:itemarmorcombinedjetpack>,
										[[null, <ic2:ic2upgrades:1>, null],
										[<ic2:itemmisc:452>, <gravisuit:advancedelectricjetpack>, <ic2:itemmisc:452>],
										[null, <gravisuit:advancedlappack>, null]]);
										
// Modify Compacted Nuclear Jetpack recipe (nerf)
recipes.removeByRecipeName("ic2:shaped_item.itemarmornuclearcombindedjetpack_983003988");
recipes.addShaped(<ic2:itemarmornuclearcombindedjetpack>,
										[[null, <ic2:ic2upgrades:1>, null],
										[<ic2:itemmisc:452>, <gravisuit:advancednuclearjetpack>, <ic2:itemmisc:452>],
										[null, <gravisuit:advancedlappack>, null]]);

// Add tooltips for Kinesis Pipes
<buildcrafttransport:pipe_wood_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));
<buildcrafttransport:pipe_cobble_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));
<buildcrafttransport:pipe_stone_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));
<buildcrafttransport:pipe_quartz_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));
<buildcrafttransport:pipe_gold_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));
<buildcrafttransport:pipe_sandstone_power>.addTooltip(format.italic("Use Flux Cables for mod intercompatibility"));

// Add upgrade path for Advanced Solar Helmet
recipes.addShaped(<ic2:itemarmorquantumhelmet>,
												[[null, <advancedsolars:advancedsolarhelmet>, null],
												[<ic2:itemmisc:258>, <ic2:itembatlamacrystal>, <ic2:itemmisc:258>],
												[null, <ic2:blockutility:3>, null]]);
