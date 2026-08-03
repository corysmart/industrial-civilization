package com.industrialcivilization.core;

import com.mrcrayfish.vehicle.entity.vehicle.EntityMiniBus;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

/** Adds the top-tier mobile-industry role to Onysd's real Mini Bus vehicle. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class VehicleIntegrationSystem {
    private static final ResourceLocation MOBILE_INDUSTRY = new ResourceLocation(
        IndustrialCivilizationCore.MODID, "mobile_industry_vehicle");

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityMiniBus) event.addCapability(MOBILE_INDUSTRY, new Provider());
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
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
        if (!event.getWorld().isRemote) {
            boolean crafting = player.getHeldItem(hand).getItem()
                == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.CRAFTING_TABLE);
            player.openGui(IndustrialCivilizationCore.INSTANCE,
                crafting ? IndustrialCivilizationCore.GUI_VEHICLE_CRAFTING
                    : IndustrialCivilizationCore.GUI_VEHICLE_STORAGE,
                event.getWorld(), (int) vehicle.posX, (int) vehicle.posY, (int) vehicle.posZ);
        }
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
