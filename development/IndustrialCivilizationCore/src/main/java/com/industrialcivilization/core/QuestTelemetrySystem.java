package com.industrialcivilization.core;

import java.util.Locale;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Authoritative, local-only quest evidence for capabilities that inventory
 * retrieval cannot prove. It observes sustained, placed and operating systems;
 * it never accepts a checkbox, chat command, or a merely held controller.
 */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class QuestTelemetrySystem {
    private static final int SAMPLE_TICKS = 100;
    private static final int REQUIRED_SAMPLES = 24; // two minutes of stable operation
    private static final double RANGE_SQ = 32.0D * 32.0D;

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % SAMPLE_TICKS != 0) return;

        evaluateRouteTransitions(player);
        String environment = environment(player);
        if ("earth".equals(environment)) return;

        Evidence evidence = observeNearbyInfrastructure(player);
        boolean habitat = SpaceSurvivalSystem.protectedByHabitat(player);
        if (habitat) {
            RuntimeAdvancements.grant(player, environment + "_habitat", "sealed_habitat_scan");
            ProgressionState.increment(player, environment + "_habitat_stable_samples", 1);
        }
        if (evidence.communications && habitat && "orbit".equals(environment))
            RuntimeAdvancements.grant(player, "orbital_communications", "placed_telemetry_scan");
        if (evidence.automatedMiner && habitat)
            RuntimeAdvancements.grant(player, environment + "_mining", "placed_automation_scan");
        if (evidence.operatingManufacturing && habitat)
            RuntimeAdvancements.grant(player, environment + "_manufacturing", "completed_local_operation");

        if ("orbit".equals(environment)) {
            RuntimeAdvancements.grant(player, "tier1_orbital_launch", "orbital_dimension_arrival");
            if (habitat && evidence.communications && evidence.researchStation
                    && evidence.experimentModule && evidence.power
                    && stable(player, environment)) {
                RuntimeAdvancements.grant(player, "functional_orbital_station", "sustained_station_telemetry");
            }
        } else if ("lunar".equals(environment)) {
            if (habitat && evidence.communications && evidence.automatedMiner
                    && evidence.operatingManufacturing && evidence.power
                    && stable(player, environment)) {
                RuntimeAdvancements.grant(player, "functional_lunar_base", "sustained_lunar_base_telemetry");
            }
        } else if ("martian".equals(environment)) {
            if (habitat && evidence.communications && evidence.automatedMiner
                    && evidence.operatingManufacturing && evidence.power
                    && hasDesh(player) && stable(player, environment)) {
                RuntimeAdvancements.grant(player, "functional_martian_base", "sustained_mars_base_telemetry");
            }
        }
    }

    private static void evaluateRouteTransitions(EntityPlayer player) {
        if (RuntimeAdvancements.completed(player, "heavy_industry")
                || ProgressionState.has(player, "abandoned_factory_operational")) {
            RuntimeAdvancements.grant(player, "industrial_capacity_access", "validated_route_transition");
        }
        if (RuntimeAdvancements.completed(player, "programmable_manufacturing")
                || ProgressionState.has(player, "recovered_factory_control_system")) {
            RuntimeAdvancements.grant(player, "programmable_capacity_access", "validated_route_transition");
        }
    }

    private static boolean stable(EntityPlayer player, String environment) {
        return ProgressionState.counter(player, environment + "_habitat_stable_samples") >= REQUIRED_SAMPLES;
    }

    private static Evidence observeNearbyInfrastructure(EntityPlayer player) {
        Evidence result = new Evidence();
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (tile == null || tile.isInvalid() || tile.getPos().distanceSq(player.getPosition()) > RANGE_SQ) continue;
            String blockId = registryName(tile);
            String className = tile.getClass().getName().toLowerCase(Locale.ROOT);
            if (tile instanceof TileEnvironmentalSolarArray) {
                result.power |= ((TileEnvironmentalSolarArray) tile).getEnergyStored() > 0;
            }
            if (tile instanceof TileIndustrialMachine) {
                TileIndustrialMachine machine = (TileIndustrialMachine) tile;
                result.researchStation |= machine.getKind() == IndustrialMachineKind.RESEARCH_STATION;
                result.experimentModule |= machine.getKind() == IndustrialMachineKind.EXPERIMENT_MODULE;
                result.operatingManufacturing |= machine.getCompletedOperations() > 0
                    && (machine.getKind() == IndustrialMachineKind.ELECTRIC_FABRICATOR
                        || machine.getKind() == IndustrialMachineKind.PROGRAMMABLE_ASSEMBLER
                        || machine.getKind() == IndustrialMachineKind.ROBOTIC_CELL);
            }
            result.communications |= className.contains("computer") || className.contains("telemetry")
                || blockId.contains("computer") || blockId.contains("telemetry");
            result.automatedMiner |= className.contains("quarry") || className.contains("miner")
                || blockId.contains("quarry") || blockId.contains("miner");
            result.power |= className.contains("electricblock") || className.contains("energystorage")
                || blockId.contains("blockelectric");
        }
        return result;
    }

    private static String registryName(TileEntity tile) {
        if (tile.getWorld() == null) return "";
        ResourceLocation id = tile.getWorld().getBlockState(tile.getPos()).getBlock().getRegistryName();
        return id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
    }

    private static boolean hasDesh(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) {
            ResourceLocation id = stack.isEmpty() ? null : stack.getItem().getRegistryName();
            if (id != null && "galacticraftplanets:item_basic_mars".equals(id.toString())
                    && stack.getMetadata() == 2) return true;
        }
        return false;
    }

    private static String environment(EntityPlayer player) {
        String name = player.world.provider.getDimensionType().getName().toLowerCase(Locale.ROOT);
        if (name.contains("moon")) return "lunar";
        if (name.contains("mars")) return "martian";
        if (name.contains("orbit") || name.contains("space station")) return "orbit";
        return "earth";
    }

    private static final class Evidence {
        boolean communications;
        boolean automatedMiner;
        boolean operatingManufacturing;
        boolean researchStation;
        boolean experimentModule;
        boolean power;
    }

    private QuestTelemetrySystem() {}
}
