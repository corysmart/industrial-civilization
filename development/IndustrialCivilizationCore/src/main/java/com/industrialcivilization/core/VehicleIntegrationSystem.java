package com.industrialcivilization.core;

import com.mrcrayfish.vehicle.entity.vehicle.EntityMiniBus;
import com.mrcrayfish.vehicle.entity.EntityPoweredVehicle;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Adds the top-tier mobile-industry role to Onysd's real Mini Bus vehicle. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class VehicleIntegrationSystem {
    private static final ResourceLocation MOBILE_INDUSTRY = new ResourceLocation(
        IndustrialCivilizationCore.MODID, "mobile_industry_vehicle");
    private static final String CONDITION = "IndustrialVehicleCondition";

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityMiniBus) event.addCapability(MOBILE_INDUSTRY, new Provider());
    }

    @SubscribeEvent
    public static void joined(net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPoweredVehicle
                && !event.getEntity().getEntityData().hasKey(CONDITION, 3)) {
            event.getEntity().getEntityData().setInteger(CONDITION, MarketEconomy.NEW_CONDITION);
        }
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPoweredVehicle)
            recordOwnedDeployment((EntityPoweredVehicle) event.getEntity());
    }

    /** Wear is mileage based, sampled once per second to keep the pack inexpensive. */
    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || event.world.getTotalWorldTime() % 20L != 0L) return;
        for (Entity entity : event.world.loadedEntityList) {
            if (!(entity instanceof EntityPoweredVehicle)) continue;
            EntityPoweredVehicle vehicle = (EntityPoweredVehicle) entity;
            NBTTagCompound tag = vehicle.getEntityData();
            int condition = tag.hasKey(CONDITION, 3) ? tag.getInteger(CONDITION) : MarketEconomy.NEW_CONDITION;
            if (vehicle.isMoving()) {
                condition = Math.max(0, condition - (vehicle.isBoosting() ? 3 : 1));
                tag.setInteger(CONDITION, condition);
                tag.setBoolean(MarketEconomy.USED, true);
            }
            if (condition <= 0) vehicle.setSpeed(0.0F);
            else if (condition < 2000) vehicle.speedMultiplier = Math.min(vehicle.speedMultiplier, 0.45F);
            updateVehicleLifecycle(vehicle);
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof EntityPoweredVehicle)) return;
        EntityPoweredVehicle powered = (EntityPoweredVehicle) event.getTarget();
        Item machine = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2:blockmachinelv"));
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (machine != null && held.getItem() == machine) {
            cancel(event);
            if (!event.getWorld().isRemote) {
                if (powered.isMoving()) {
                    event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation(
                        "message.industrialcivilization.service_vehicle.park"), false);
                } else {
                    powered.getEntityData().setInteger(CONDITION, MarketEconomy.NEW_CONDITION);
                    if (!event.getEntityPlayer().capabilities.isCreativeMode) held.shrink(1);
                    event.getEntityPlayer().sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                        "Vehicle restored with one IC2 machine block."), false);
                }
            }
            return;
        }
        if (!(event.getTarget() instanceof EntityMiniBus)) return;
        EntityMiniBus vehicle = (EntityMiniBus) event.getTarget();
        if (vehicle.motionX * vehicle.motionX + vehicle.motionZ * vehicle.motionZ > 0.0025D) {
            if (!event.getWorld().isRemote) event.getEntityPlayer().sendStatusMessage(
                new TextComponentTranslation("message.industrialcivilization.service_vehicle.park"), false);
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();
        if (!event.getWorld().isRemote && vehicle.hasCapability(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)
                && FluidUtil.interactWithFluidHandler(player, hand, vehicle.getCapability(
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))) {
            cancel(event);
            return;
        }
        if (!player.isSneaking()) return;
        cancel(event);
        if (!event.getWorld().isRemote) openServiceInterface(player, vehicle, hand);
    }

    static boolean interactForTest(EntityPlayer player, EntityMiniBus vehicle, EnumHand hand) {
        if (!player.isSneaking() || vehicle.motionX * vehicle.motionX
                + vehicle.motionZ * vehicle.motionZ > 0.0025D) return false;
        return openServiceInterface(player, vehicle, hand);
    }

    private static boolean openServiceInterface(EntityPlayer player, EntityMiniBus vehicle,
            EnumHand hand) {
        boolean crafting = player.getHeldItem(hand).getItem()
            == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.CRAFTING_TABLE);
        ProgressionState.record(player, crafting
            ? "industrial_service_carrier_crafting_opened"
            : "industrial_service_carrier_storage_opened");
        player.openGui(IndustrialCivilizationCore.INSTANCE,
            crafting ? IndustrialCivilizationCore.GUI_VEHICLE_CRAFTING
                : IndustrialCivilizationCore.GUI_VEHICLE_STORAGE,
            player.world, (int) vehicle.posX, (int) vehicle.posY, (int) vehicle.posZ);
        checkServiceCarrier(player);
        return true;
    }

    private static void cancel(PlayerInteractEvent.EntityInteract event) {
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @Nullable
    public static EntityMiniBus findServiceVehicle(World world, BlockPos pos) {
        if (world == null) return null;
        List<EntityMiniBus> vehicles = world.getEntitiesWithinAABB(EntityMiniBus.class,
            new AxisAlignedBB(pos).grow(4), vehicle -> vehicle.isEntityAlive()
                && vehicle.motionX * vehicle.motionX + vehicle.motionZ * vehicle.motionZ <= 0.0025D);
        return vehicles.isEmpty() ? null : vehicles.get(0);
    }

    @Nullable
    static EntityPlayerMP owner(EntityPoweredVehicle vehicle) {
        if (vehicle.world == null || vehicle.world.getMinecraftServer() == null) return null;
        for (EntityPlayer player : vehicle.world.playerEntities) {
            if (player instanceof EntityPlayerMP && vehicle.isOwner(player))
                return (EntityPlayerMP) player;
        }
        return null;
    }

    private static void recordOwnedDeployment(EntityPoweredVehicle vehicle) {
        EntityPlayerMP player = owner(vehicle);
        if (player == null || !ProgressionState.has(player, "regional_vehicle_manufactured")) return;
        ProgressionState.record(player, "regional_vehicle_deployed");
        if (vehicle instanceof EntityMiniBus
                && ProgressionState.has(player, "industrial_service_carrier_manufactured"))
            ProgressionState.record(player, "industrial_service_carrier_deployed");
    }

    private static void updateVehicleLifecycle(EntityPoweredVehicle vehicle) {
        EntityPlayerMP player = owner(vehicle);
        if (player == null || !ProgressionState.has(player, "regional_vehicle_deployed")) return;
        if (vehicle.getCurrentFuel() > 0F)
            ProgressionState.record(player, "regional_vehicle_fueled");
        NBTTagCompound tag = vehicle.getEntityData();
        if (vehicle.isMoving()) {
            tag.setBoolean("IndustrialVehicleWasDriven", true);
            ProgressionState.record(player, "regional_vehicle_driven");
        } else if (tag.getBoolean("IndustrialVehicleWasDriven")) {
            ProgressionState.record(player, "regional_vehicle_braked");
        }
        if (ProgressionState.has(player, "regional_vehicle_manufactured")
                && ProgressionState.has(player, "regional_vehicle_deployed")
                && ProgressionState.has(player, "regional_vehicle_fueled")
                && ProgressionState.has(player, "regional_vehicle_driven")
                && ProgressionState.has(player, "regional_vehicle_braked"))
            RuntimeAdvancements.grant(player, "regional_mobility");
    }

    static void updateVehicleForTest(EntityPoweredVehicle vehicle) {
        recordOwnedDeployment(vehicle);
        updateVehicleLifecycle(vehicle);
    }

    static void recordDockTransfer(EntityMiniBus vehicle, boolean fluid) {
        EntityPlayerMP player = owner(vehicle);
        if (player == null) return;
        ProgressionState.record(player, fluid
            ? "industrial_service_carrier_fluid_transfer"
            : "industrial_service_carrier_item_transfer");
        checkServiceCarrier(player);
    }

    private static void checkServiceCarrier(EntityPlayer player) {
        String[] evidence = {"industrial_service_carrier_manufactured",
            "industrial_service_carrier_deployed", "industrial_service_carrier_storage_opened",
            "industrial_service_carrier_crafting_opened",
            "industrial_service_carrier_item_transfer",
            "industrial_service_carrier_fluid_transfer"};
        for (String milestone : evidence) if (!ProgressionState.has(player, milestone)) return;
        RuntimeAdvancements.grant(player, "industrial_service_carrier");
    }

    public static final class Provider implements ICapabilitySerializable<NBTTagCompound> {
        final ItemStackHandler items = new ItemStackHandler(54);
        final FluidTank tank = new FluidTank(64000);

        @Override public boolean hasCapability(Capability<?> capability, @Nullable net.minecraft.util.EnumFacing side) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        }

        @Override @SuppressWarnings("unchecked")
        public <T> T getCapability(Capability<T> capability, @Nullable net.minecraft.util.EnumFacing side) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) items;
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) tank;
            return null;
        }

        @Override public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Items", items.serializeNBT());
            NBTTagCompound fluid = new NBTTagCompound();
            tank.writeToNBT(fluid);
            tag.setTag("Tank", fluid);
            return tag;
        }

        @Override public void deserializeNBT(NBTTagCompound tag) {
            items.deserializeNBT(tag.getCompoundTag("Items"));
            tank.readFromNBT(tag.getCompoundTag("Tank"));
        }
    }

    private VehicleIntegrationSystem() {}
}
