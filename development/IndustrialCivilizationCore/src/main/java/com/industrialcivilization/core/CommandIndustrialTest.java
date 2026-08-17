package com.industrialcivilization.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Development-only deterministic scenarios and parseable runtime snapshots. */
public final class CommandIndustrialTest extends CommandBase {
    private static final String PREFIX = "IC_TEST|";

    @Override public String getName() { return "ic_test"; }
    @Override public String getUsage(ICommandSender sender) {
        return "/ic_test snapshot [radius] | scenario <workshop_adjacency|earth_ecology|release_recipes> | assert <workshop_adjacency|earth_ecology>";
    }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (args.length == 0) throw new WrongUsageException(getUsage(sender));
        if ("snapshot".equals(args[0])) {
            int radius = args.length > 1 ? parseInt(args[1], 4, 64) : 24;
            emit(player, "SNAPSHOT|" + snapshot(player, radius));
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "workshop_adjacency".equals(args[1])) {
            BlockPos base = createWorkshopScenario(player);
            int[] state = WorkshopSystem.inspect(player.world, base.add(4, 0, 0), 24);
            boolean pass = state[0] == 2 && state[1] == 1 && state[2] == 1;
            emit(player, (pass ? "PASS" : "FAIL") + "|workshop_adjacency|controllers="
                + state[0] + "|pairs=" + state[1] + "|connections=" + state[2]
                + "|base=" + coordinates(base));
            return;
        }
        if (args.length == 2 && "assert".equals(args[0]) && "workshop_adjacency".equals(args[1])) {
            int[] state = WorkshopSystem.inspect(player.world, player.getPosition(), 64);
            boolean pass = state[0] >= 2 && state[1] >= 1 && state[2] == state[1];
            emit(player, (pass ? "PASS" : "FAIL") + "|workshop_adjacency|controllers="
                + state[0] + "|pairs=" + state[1] + "|connections=" + state[2]);
            return;
        }
        if (args.length == 2 && ("scenario".equals(args[0]) || "assert".equals(args[0]))
                && "earth_ecology".equals(args[1])) {
            EcologyResult result = runEarthEcologyScenario(server, player);
            emit(player, (result.pass ? "PASS" : "FAIL") + "|earth_ecology|robbers="
                + result.robbers + "|patrols=" + result.patrols + "|vanilla_zombies="
                + result.vanillaZombies + "|vanilla_skeletons=" + result.vanillaSkeletons
                + "|other_vanilla_hostiles=" + result.otherVanillaHostiles);
            return;
        }
        if (args.length == 2 && "scenario".equals(args[0]) && "release_recipes".equals(args[1])) {
            RecipeResult result = runReleaseRecipeScenario(player);
            emit(player, (result.failures.isEmpty() ? "PASS" : "FAIL")
                + "|release_recipes|explicit=" + result.explicitPassed + "/" + result.explicitTotal
                + "|modified=" + result.modifiedPassed + "/" + result.modifiedTotal
                + "|forbidden_inputs=" + result.forbiddenInputs
                + (result.failures.isEmpty() ? "" : "|failures=" + String.join(",", result.failures)));
            return;
        }
        throw new WrongUsageException(getUsage(sender));
    }

    private static RecipeResult runReleaseRecipeScenario(EntityPlayerMP player) {
        String[][] explicit = {
            {"lv_plant_sower", "industrialforegoing:crop_sower", "1"},
            {"lv_plant_gatherer", "industrialforegoing:crop_recolector", "1"},
            {"lv_resourceful_furnace", "industrialforegoing:resourceful_furnace", "1"},
            {"lv_plant_interactor", "industrialforegoing:plant_interactor", "1"},
            {"lv_plant_fertilizer", "industrialforegoing:crop_enrich_material_injector", "1"},
            {"lv_animal_breeder", "industrialforegoing:animal_stock_increaser", "1"},
            {"lv_animal_growth", "industrialforegoing:animal_growth_increaser", "1"},
            {"lv_animal_separator", "industrialforegoing:animal_independence_selector", "1"},
            {"lv_animal_harvester", "industrialforegoing:animal_resource_harvester", "1"},
            {"lv_sewage_collector", "industrialforegoing:animal_byproduct_recolector", "1"},
            {"lv_sewage_composter", "industrialforegoing:sewage_composter_solidifier", "1"},
            {"lv_water_resource_collector", "industrialforegoing:water_resources_collector", "1"},
            {"industrial_civilization_gunpowder", "minecraft:gunpowder", "3"},
            {"colorful_lamp", "computronics:colorful_lamp", "1"},
            {"quantum_tape_diamond", "computronics:tape", "1"},
            {"quantum_tape_dense", "computronics:tape", "1"},
            {"earth_purpur_stairs", "minecraft:purpur_stairs", "4"},
            {"earth_purpur_slab", "minecraft:purpur_slab", "6"},
            {"earth_nether_brick", "minecraft:nether_brick", "1"},
            {"earth_glowstone_torch", "galacticraftcore:glowstone_torch", "4"},
            {"earth_mirrorprint", "chiselsandbits:mirrorprint", "1"},
            {"earth_neon_light", "techguns:neonlights", "1"},
            {"earth_purpur_wall", "quark:purpur_block_wall", "1"},
            {"earth_soul_sandstone", "quark:soul_sandstone", "1"},
            {"earth_antiblock", "chisel:antiblock", "1"}
        };
        RecipeResult result = new RecipeResult(explicit.length);
        for (String[] target : explicit) {
            IRecipe recipe = findRecipe(target[0], target[1]);
            if (recipe == null) {
                result.failures.add(target[0] + ":missing");
                continue;
            }
            String failure = craftFailure(recipe, player.world, target[1], Integer.parseInt(target[2]));
            if (failure == null) result.explicitPassed++;
            else result.failures.add(target[0] + ":" + failure);
        }
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            ResourceLocation name = recipe.getRegistryName();
            if (name == null || !"crafttweaker".equals(name.getResourceDomain())
                    || !name.getResourcePath().endsWith("_modified")) continue;
            if (isSupersededModifiedRecipe(name.toString())) continue;
            ItemStack expected = recipe.getRecipeOutput();
            String expectedId = expected.isEmpty() || expected.getItem().getRegistryName() == null
                ? "" : expected.getItem().getRegistryName().toString();
            result.modifiedTotal++;
            String failure = craftFailure(recipe, player.world, expectedId, expected.getCount());
            if (failure == null) result.modifiedPassed++;
            else if (result.failures.size() < 12) result.failures.add(name + ":" + failure);
        }
        result.forbiddenInputs = forbiddenInputCount();
        if (result.forbiddenInputs != 0) result.failures.add("forbidden_inputs:" + result.forbiddenInputs);
        return result;
    }

    private static boolean isSupersededModifiedRecipe(String recipeId) {
        return Arrays.asList(
            "crafttweaker:minecraft_purpur_stairs_modified",
            "crafttweaker:minecraft_purpur_slab_modified",
            "crafttweaker:minecraft_nether_brick_modified",
            "crafttweaker:galacticraftcore_glowstone_torch_modified",
            "crafttweaker:chiselsandbits_mirrorprint_modified",
            "crafttweaker:icbmclassic_parts/circuit.elite_modified",
            "crafttweaker:techguns_neonlights_0_modified",
            "crafttweaker:quark_purpur_block_wall_modified",
            "crafttweaker:quark_soul_sandstone_modified",
            "crafttweaker:chisel_antiblock_modified"
        ).contains(recipeId);
    }

    private static IRecipe findRecipe(String token, String outputId) {
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            ResourceLocation name = recipe.getRegistryName();
            ItemStack output = recipe.getRecipeOutput();
            ResourceLocation outputName = output.isEmpty() ? null : output.getItem().getRegistryName();
            if (name != null && name.toString().contains(token)
                    && outputName != null && outputId.equals(outputName.toString())) return recipe;
        }
        return null;
    }

    private static String craftFailure(IRecipe recipe, World world, String expectedId, int expectedCount) {
        final Container container = new Container() {
            @Override public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) { return true; }
        };
        InventoryCrafting grid = new InventoryCrafting(container, 3, 3);
        List<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : 3;
        if (width < 1 || width > 3 || ingredients.size() > 9) return "unsupported_grid";
        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient ingredient = ingredients.get(index);
            if (ingredient == Ingredient.EMPTY) continue;
            ItemStack[] candidates = ingredient.getMatchingStacks();
            if (candidates.length == 0) return "empty_ingredient_" + index;
            int slot = recipe instanceof IShapedRecipe
                ? (index / width) * 3 + index % width : index;
            grid.setInventorySlotContents(slot, candidates[0].copy());
        }
        IRecipe matched = CraftingManager.findMatchingRecipe(grid, world);
        if (matched == null) return "no_match";
        ItemStack output = matched.getCraftingResult(grid);
        ResourceLocation outputName = output.isEmpty() ? null : output.getItem().getRegistryName();
        if (outputName == null || !expectedId.equals(outputName.toString()))
            return "wrong_output_" + (outputName == null ? "empty" : outputName);
        if (output.getCount() != expectedCount) return "wrong_count_" + output.getCount();
        return null;
    }

    private static int forbiddenInputCount() {
        List<String> forbidden = Arrays.asList(
            "minecraft:slime_ball", "minecraft:ghast_tear", "minecraft:blaze_rod",
            "minecraft:blaze_powder", "minecraft:magma_cream", "minecraft:nether_star",
            "minecraft:quartz", "minecraft:netherrack", "minecraft:soul_sand",
            "minecraft:nether_wart", "minecraft:netherbrick", "minecraft:glowstone_dust",
            "minecraft:end_stone", "minecraft:chorus_fruit", "minecraft:chorus_fruit_popped",
            "minecraft:purpur_block", "minecraft:purpur_pillar", "minecraft:shulker_shell",
            "minecraft:dragon_breath", "minecraft:dragon_egg", "minecraft:end_crystal");
        int count = 0;
        for (IRecipe recipe : ForgeRegistries.RECIPES) for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack candidate : ingredient.getMatchingStacks()) {
                ResourceLocation name = candidate.getItem().getRegistryName();
                if (name != null && forbidden.contains(name.toString())) count++;
            }
        }
        return count;
    }

    private static final class RecipeResult {
        final int explicitTotal;
        final List<String> failures = new ArrayList<>();
        int explicitPassed;
        int modifiedTotal;
        int modifiedPassed;
        int forbiddenInputs;
        RecipeResult(int explicitTotal) { this.explicitTotal = explicitTotal; }
    }

    private static EcologyResult runEarthEcologyScenario(MinecraftServer server, EntityPlayerMP player) {
        // Test the Earth-only replacement rules in the Overworld even when a
        // seeded test-bed player was last saved on the Moon or Mars.
        World world = server.getWorld(0);
        BlockPos anchor = player.dimension == 0 ? player.getPosition() : world.getSpawnPoint();
        BlockPos base = anchor.add(0, 2, 10);
        AxisAlignedBB box = new AxisAlignedBB(base.add(-3, -2, -3), base.add(8, 4, 3));
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, box, candidate ->
                candidate instanceof EntityRobber || candidate instanceof EntityMilitiaPatrol)) {
            entity.setDead();
        }
        EntityZombie zombie = new EntityZombie(world);
        zombie.getEntityData().setBoolean(PlanetaryEcologySystem.FORCE_ROBBER_REPLACEMENT, true);
        EntitySkeleton skeleton = new EntitySkeleton(world);
        skeleton.getEntityData().setBoolean(PlanetaryEcologySystem.FORCE_PATROL_REPLACEMENT, true);
        EntityCreeper creeper = new EntityCreeper(world);
        EntitySpider spider = new EntitySpider(world);
        EntityEnderman enderman = new EntityEnderman(world);
        Entity[] sources = {zombie, skeleton, creeper, spider, enderman};
        for (int index = 0; index < sources.length; index++) {
            sources[index].setPosition(base.getX() + index * 1.5D, base.getY(), base.getZ());
            world.spawnEntity(sources[index]);
        }
        int robbers = world.getEntitiesWithinAABB(EntityRobber.class, box).size();
        int patrols = world.getEntitiesWithinAABB(EntityMilitiaPatrol.class, box).size();
        int vanillaZombies = world.getEntitiesWithinAABB(EntityZombie.class, box).size();
        int vanillaSkeletons = world.getEntitiesWithinAABB(EntitySkeleton.class, box).size();
        int otherVanillaHostiles = world.getEntitiesWithinAABB(EntityCreeper.class, box).size()
            + world.getEntitiesWithinAABB(EntitySpider.class, box).size()
            + world.getEntitiesWithinAABB(EntityEnderman.class, box).size();
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, box, candidate ->
                candidate instanceof EntityRobber || candidate instanceof EntityMilitiaPatrol)) {
            entity.setDead();
        }
        return new EcologyResult(robbers, patrols, vanillaZombies, vanillaSkeletons,
            otherVanillaHostiles);
    }

    private static final class EcologyResult {
        final int robbers;
        final int patrols;
        final int vanillaZombies;
        final int vanillaSkeletons;
        final int otherVanillaHostiles;
        final boolean pass;

        EcologyResult(int robbers, int patrols, int vanillaZombies, int vanillaSkeletons,
                int otherVanillaHostiles) {
            this.robbers = robbers;
            this.patrols = patrols;
            this.vanillaZombies = vanillaZombies;
            this.vanillaSkeletons = vanillaSkeletons;
            this.otherVanillaHostiles = otherVanillaHostiles;
            this.pass = robbers == 1 && patrols == 1 && vanillaZombies == 0
                && vanillaSkeletons == 0 && otherVanillaHostiles == 0;
        }
    }

    private static BlockPos createWorkshopScenario(EntityPlayerMP player) {
        World world = player.world;
        BlockPos base = player.getPosition().add(-4, 0, 8);
        // Deliberately destructive only inside the documented disposable scenario volume.
        for (int x = -4; x <= 13; x++) for (int z = -3; z <= 3; z++) {
            world.setBlockState(base.add(x, -2, z), Blocks.STONE.getDefaultState(), 2);
            world.setBlockToAir(base.add(x, -1, z));
            for (int y = 0; y <= 4; y++) world.setBlockToAir(base.add(x, y, z));
        }
        BlockPos car = base;
        BlockPos gun = base.add(9, 0, 0);
        world.setBlockState(car, IndustrialCivilizationCore.CAR_WORKSHOP.getDefaultState(), 3);
        setFacing(world, car, EnumFacing.NORTH);
        WorkshopSystem.deploy(world, car, IndustrialMachineKind.CAR_WORKSHOP, EnumFacing.NORTH);
        world.setBlockState(gun, IndustrialCivilizationCore.GUN_FACTORY.getDefaultState(), 3);
        setFacing(world, gun, EnumFacing.NORTH);
        WorkshopSystem.deploy(world, gun, IndustrialMachineKind.GUN_FACTORY, EnumFacing.NORTH);
        WorkshopSystem.connectAdjacent(world, gun);
        return base;
    }

    private static void setFacing(World world, BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileIndustrialMachine) ((TileIndustrialMachine) tile).setWorkshopFacing(facing);
    }

    private static JsonObject snapshot(EntityPlayerMP player, int radius) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        root.addProperty("player", player.getName());
        root.addProperty("dimension", player.dimension);
        root.addProperty("dimension_type", player.world.provider.getDimensionType().getName());
        root.addProperty("position", coordinates(player.getPosition()));
        root.addProperty("radius", radius);
        root.addProperty("habitat_detector_active", SpaceSurvivalSystem.protectedByHabitat(player));
        root.addProperty("functional_stable_samples",
            ProgressionState.counter(player, environment(player) + "_functional_stable_samples"));
        root.addProperty("ai_age", ProgressionState.has(player, "ai_age"));
        root.addProperty("active_ticks", ProgressionState.counter(player, "active_ticks"));
        int[] workshops = WorkshopSystem.inspect(player.world, player.getPosition(), radius);
        root.addProperty("workshop_controllers", workshops[0]);
        root.addProperty("workshop_pairs", workshops[1]);
        root.addProperty("workshop_connections", workshops[2]);
        JsonArray machines = new JsonArray();
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (!(tile instanceof TileIndustrialMachine)
                    || tile.getPos().distanceSq(player.getPosition()) > radius * radius) continue;
            TileIndustrialMachine machine = (TileIndustrialMachine) tile;
            JsonObject entry = new JsonObject();
            entry.addProperty("kind", machine.getKind().id);
            entry.addProperty("position", coordinates(tile.getPos()));
            entry.addProperty("energy_eu", machine.getEnergyStored());
            entry.addProperty("progress", machine.getProgress());
            entry.addProperty("completed", machine.getCompletedOperations());
            entry.addProperty("rusted", machine.isRusted());
            entry.addProperty("facing", machine.getWorkshopFacing().getName());
            machines.add(entry);
        }
        root.add("machines", machines);
        return root;
    }

    private static String coordinates(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String environment(EntityPlayerMP player) {
        String name = player.world.provider.getDimensionType().getName().toLowerCase(java.util.Locale.ROOT);
        if (name.contains("moon")) return "lunar";
        if (name.contains("mars")) return "martian";
        if (name.contains("orbit") || name.contains("space station")) return "orbit";
        return "earth";
    }

    private static void emit(EntityPlayerMP player, String value) {
        String line = PREFIX + value;
        IndustrialCivilizationCore.LOGGER.info(line);
        player.sendMessage(new TextComponentString(line));
    }

    @Override
    public java.util.List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "snapshot", "scenario", "assert");
        if (args.length == 2 && ("scenario".equals(args[0]) || "assert".equals(args[0])))
            return getListOfStringsMatchingLastWord(args, "workshop_adjacency", "earth_ecology", "release_recipes");
        return Arrays.asList();
    }
}
