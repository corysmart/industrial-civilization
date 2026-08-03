package com.industrialcivilization.core;

import java.util.Arrays;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Deterministic runtime recipes shared by machines and the offline harness. */
public final class MachineRecipe {
    public final String id;
    public final IndustrialMachineKind machine;
    public final ItemStack[] inputs;
    public final ItemStack output;
    public final String environment;

    private MachineRecipe(String id, IndustrialMachineKind machine, String environment,
            ItemStack output, ItemStack... inputs) {
        this.id = id;
        this.machine = machine;
        this.environment = environment;
        this.output = output;
        this.inputs = inputs;
    }

    public boolean matches(TileIndustrialMachine tile) {
        if (tile.getKind() != machine || !environmentMatches(tile.environment())) return false;
        for (int index = 0; index < inputs.length; index++) {
            if (!matchesStack(inputs[index], tile.getStackInSlot(index))) return false;
        }
        ItemStack existing = tile.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT);
        return existing.isEmpty() || (ItemStack.areItemsEqual(existing, output)
            && ItemStack.areItemStackTagsEqual(existing, output)
            && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    public void complete(TileIndustrialMachine tile) {
        for (int index = 0; index < inputs.length; index++) {
            if (inputs[index].getItem() != IndustrialCivilizationCore.AI_CORE) {
                tile.decrStackSize(index, inputs[index].getCount());
            }
        }
        ItemStack produced = output.copy();
        if (produced.getItem() == IndustrialCivilizationCore.RESEARCH_DATA) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Environment", tile.environment());
            produced.setTagCompound(tag);
        }
        ItemStack existing = tile.getStackInSlot(TileIndustrialMachine.OUTPUT_SLOT);
        if (existing.isEmpty()) tile.setInventorySlotContents(TileIndustrialMachine.OUTPUT_SLOT, produced);
        else existing.grow(produced.getCount());
    }

    private boolean environmentMatches(String actual) {
        return environment == null || environment.equals(actual);
    }

    private static boolean matchesStack(ItemStack expected, ItemStack actual) {
        if (actual.isEmpty() || !ItemStack.areItemsEqual(expected, actual)
                || actual.getCount() < expected.getCount()) return false;
        if (expected.getItem() == IndustrialCivilizationCore.RESEARCH_DATA
                && expected.hasTagCompound()) {
            return actual.hasTagCompound() && expected.getTagCompound().getString("Environment")
                .equals(actual.getTagCompound().getString("Environment"));
        }
        return true;
    }

    private static ItemStack data(String environment) {
        ItemStack stack = new ItemStack(IndustrialCivilizationCore.RESEARCH_DATA);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Environment", environment);
        stack.setTagCompound(tag);
        return stack;
    }

    private static ItemStack external(String id, int metadata) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, 1, metadata);
    }

    private static ItemStack external(String id, int metadata, int count) {
        ItemStack stack = external(id, metadata);
        if (!stack.isEmpty()) stack.setCount(count);
        return stack;
    }

    private static ItemStack vehicleCrate(String vehicleId) {
        ItemStack stack = external("vehicle:vehicle_crate", 0);
        if (stack.isEmpty()) return stack;
        NBTTagCompound blockEntity = new NBTTagCompound();
        blockEntity.setString("vehicle", "vehicle:" + vehicleId);
        blockEntity.setInteger("color", 0x3F6672);
        blockEntity.setInteger("engineTier", 2);
        blockEntity.setInteger("wheelType", 0);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("BlockEntityTag", blockEntity);
        stack.setTagCompound(tag);
        return stack;
    }

    public static MachineRecipe[] all() {
        return new MachineRecipe[] {
            new MachineRecipe("record_orbital_data", IndustrialMachineKind.EXPERIMENT_MODULE,
                "orbit", new ItemStack(IndustrialCivilizationCore.RESEARCH_DATA),
                new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE)),
            new MachineRecipe("record_lunar_data", IndustrialMachineKind.EXPERIMENT_MODULE,
                "moon", new ItemStack(IndustrialCivilizationCore.RESEARCH_DATA),
                new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE)),
            new MachineRecipe("record_martian_data", IndustrialMachineKind.EXPERIMENT_MODULE,
                "mars", new ItemStack(IndustrialCivilizationCore.RESEARCH_DATA),
                new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE)),
            new MachineRecipe("orbital_archive", IndustrialMachineKind.RESEARCH_STATION,
                "orbit", new ItemStack(IndustrialCivilizationCore.ORBITAL_RESEARCH_ARCHIVE),
                data("orbit")),
            new MachineRecipe("lunar_archive", IndustrialMachineKind.RESEARCH_STATION,
                "moon", new ItemStack(IndustrialCivilizationCore.LUNAR_ENGINEERING_ARCHIVE),
                data("moon"), new ItemStack(IndustrialCivilizationCore.ORBITAL_RESEARCH_ARCHIVE)),
            new MachineRecipe("mars_authorization", IndustrialMachineKind.RESEARCH_STATION,
                "earth", new ItemStack(IndustrialCivilizationCore.MARS_MISSION_AUTHORIZATION),
                new ItemStack(IndustrialCivilizationCore.LUNAR_QUANTUM_COMPONENT),
                new ItemStack(IndustrialCivilizationCore.LUNAR_ENGINEERING_ARCHIVE)),
            new MachineRecipe("martian_autonomy", IndustrialMachineKind.RESEARCH_STATION,
                "mars", new ItemStack(IndustrialCivilizationCore.MARTIAN_AUTONOMY_ARCHIVE),
                data("mars"), new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR)),
            new MachineRecipe("precision_frame", IndustrialMachineKind.ELECTRIC_FABRICATOR,
                null, new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME),
                external("minecraft:iron_ingot", 0), external("minecraft:redstone", 0)),
            new MachineRecipe("blank_data_cartridge", IndustrialMachineKind.ELECTRIC_FABRICATOR,
                null, new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE, 2),
                external("minecraft:paper", 0), external("minecraft:redstone", 0)),
            new MachineRecipe("control_processor", IndustrialMachineKind.PROGRAMMABLE_ASSEMBLER,
                null, new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME),
                new ItemStack(IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE),
                external("minecraft:redstone", 0)),
            new MachineRecipe("printed_pistol", IndustrialMachineKind.PROGRAMMABLE_ASSEMBLER,
                null, external("techguns:pistol", 0),
                new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME),
                external("ic2:itemmisc", 451), external("minecraft:iron_ingot", 0, 3)),
            new MachineRecipe("city_compact", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("smart_car"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 2),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 8)),
            new MachineRecipe("frontier_off_roader", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("off_roader"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 3),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 12)),
            new MachineRecipe("passenger_carrier", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("mini_bus"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 4),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR, 2), external("minecraft:iron_ingot", 0, 16)),
            new MachineRecipe("agricultural_tractor", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("tractor"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 3),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 14)),
            new MachineRecipe("utility_cart", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("golf_cart"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 2),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 6)),
            new MachineRecipe("scout_atv", IndustrialMachineKind.CAR_WORKSHOP,
                null, vehicleCrate("atv"), new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 2),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 7)),
            new MachineRecipe("combat_shotgun", IndustrialMachineKind.GUN_FACTORY,
                null, external("techguns:combatshotgun", 0),
                new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 2),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR), external("minecraft:iron_ingot", 0, 8)),
            new MachineRecipe("automatic_rifle", IndustrialMachineKind.GUN_FACTORY,
                null, external("techguns:m4", 0),
                new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME, 3),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR, 2), external("ic2:itemmisc", 452)),
            new MachineRecipe("lunar_quantum_component", IndustrialMachineKind.ROBOTIC_CELL,
                "moon", new ItemStack(IndustrialCivilizationCore.LUNAR_QUANTUM_COMPONENT),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.LUNAR_ENGINEERING_ARCHIVE),
                external("galacticraftcore:meteoric_iron_raw", 0)),
            new MachineRecipe("ai_core", IndustrialMachineKind.ROBOTIC_CELL,
                null, new ItemStack(IndustrialCivilizationCore.AI_CORE),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.MARTIAN_AUTONOMY_ARCHIVE),
                new ItemStack(IndustrialCivilizationCore.MATERIAL_PATTERN_RECORD)),
            new MachineRecipe("uu_matter", IndustrialMachineKind.MATTER_REPLICATOR,
                null, new ItemStack(IndustrialCivilizationCore.UU_MATTER_CAPSULE),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.MATERIAL_PATTERN_RECORD),
                external("minecraft:glowstone_dust", 0)),
            new MachineRecipe("controlled_replication", IndustrialMachineKind.MATTER_REPLICATOR,
                null, new ItemStack(IndustrialCivilizationCore.REPLICATION_RECORD),
                new ItemStack(IndustrialCivilizationCore.UU_MATTER_CAPSULE),
                new ItemStack(IndustrialCivilizationCore.MATERIAL_PATTERN_RECORD),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR)),
            new MachineRecipe("contained_antimatter", IndustrialMachineKind.FUSION_RESEARCH_CORE,
                "orbit", new ItemStack(IndustrialCivilizationCore.ANTIMATTER_CAPSULE),
                new ItemStack(IndustrialCivilizationCore.UU_MATTER_CAPSULE),
                new ItemStack(IndustrialCivilizationCore.AI_CORE),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR)),
            new MachineRecipe("cargo_network", IndustrialMachineKind.CARGO_CONTROLLER,
                null, new ItemStack(IndustrialCivilizationCore.CARGO_NETWORK_KEY),
                new ItemStack(IndustrialCivilizationCore.RECOVERED_FACTORY_CONTROL_SYSTEM),
                new ItemStack(IndustrialCivilizationCore.AI_CORE),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR)),
            new MachineRecipe("orbital_megastructure", IndustrialMachineKind.MEGASTRUCTURE_CONTROLLER,
                "orbit", new ItemStack(IndustrialCivilizationCore.MEGASTRUCTURE_CONTROL_RECORD),
                new ItemStack(IndustrialCivilizationCore.ANTIMATTER_CAPSULE),
                new ItemStack(IndustrialCivilizationCore.CARGO_NETWORK_KEY),
                new ItemStack(IndustrialCivilizationCore.AI_CORE)),
            new MachineRecipe("lunar_colony_charter", IndustrialMachineKind.COLONY_BEACON,
                "moon", new ItemStack(IndustrialCivilizationCore.AUTONOMOUS_COLONY_CHARTER),
                new ItemStack(IndustrialCivilizationCore.CARGO_NETWORK_KEY),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.AI_CORE)),
            new MachineRecipe("martian_colony_charter", IndustrialMachineKind.COLONY_BEACON,
                "mars", new ItemStack(IndustrialCivilizationCore.AUTONOMOUS_COLONY_CHARTER),
                new ItemStack(IndustrialCivilizationCore.CARGO_NETWORK_KEY),
                new ItemStack(IndustrialCivilizationCore.CONTROL_PROCESSOR),
                new ItemStack(IndustrialCivilizationCore.AI_CORE)),
            new MachineRecipe("civilization_scale_ai", IndustrialMachineKind.ROBOTIC_CELL,
                null, new ItemStack(IndustrialCivilizationCore.CIVILIZATION_SCALE_AI_CORE),
                new ItemStack(IndustrialCivilizationCore.MEGASTRUCTURE_CONTROL_RECORD),
                new ItemStack(IndustrialCivilizationCore.AUTONOMOUS_COLONY_CHARTER),
                new ItemStack(IndustrialCivilizationCore.REPLICATION_RECORD))
        };
    }

    public static MachineRecipe find(TileIndustrialMachine tile, String selected) {
        return Arrays.stream(all())
            .filter(recipe -> selected == null || selected.isEmpty() || recipe.id.equals(selected))
            .filter(recipe -> recipe.matches(tile)).findFirst().orElse(null);
    }
}
