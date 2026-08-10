package com.industrialcivilization.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class TileEnvironmentalSolarArray extends TileEntity
        implements ITickable, IEnergySource, IPeripheral {
    private static final int CAPACITY = 200000;
    private double energy;
    private boolean loaded;
    private long generatedTotal;
    private long lunarDarkTicks;
    private java.util.UUID lastUser;

    private final IEnergyStorage forgeEnergy = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int availableFe = (int) Math.floor(energy * IndustrialCivilizationCore.FE_PER_EU);
            int outputLimit = 128 * IndustrialCivilizationCore.FE_PER_EU;
            int amount = Math.max(0, Math.min(maxExtract, Math.min(availableFe, outputLimit)));
            if (!simulate && amount > 0) {
                energy -= amount / (double) IndustrialCivilizationCore.FE_PER_EU;
                markDirty();
            }
            return amount;
        }
        @Override public int getEnergyStored() {
            return (int) Math.floor(energy * IndustrialCivilizationCore.FE_PER_EU);
        }
        @Override public int getMaxEnergyStored() {
            return CAPACITY * IndustrialCivilizationCore.FE_PER_EU;
        }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    };

    @Override
    public void update() {
        if (world == null || world.isRemote || !world.canSeeSky(pos.up())) return;
        if ("moon".equals(environment()) && !world.isDaytime()) {
            lunarDarkTicks++;
            if (lunarDarkTicks >= 12000 && lastUser != null) {
                EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(lastUser);
                if (player != null) RuntimeAdvancements.grant(player, "lunar_darkness_mastery");
            }
        }
        boolean stellarLight = "orbit".equals(environment()) || world.isDaytime();
        if (!stellarLight) return;
        int generated = Math.min(getGenerationRate(), (int) (CAPACITY - energy));
        energy += generated;
        generatedTotal += generated;
        if (GameplayRules.solarMilestoneReady(generatedTotal, lastUser != null,
                hasConnectedLoad())) {
            EntityPlayerMP player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(lastUser);
            if (player != null) {
                if ("orbit".equals(environment())) RuntimeAdvancements.grant(player,
                    world.getBlockState(pos).getBlock() == IndustrialCivilizationCore.TRACKING_SOLAR_ARRAY
                        ? "orbital_tracking_array" : "orbital_solar_industry");
                else if ("moon".equals(environment())) RuntimeAdvancements.grant(player, "lunar_power");
                else if ("mars".equals(environment())) RuntimeAdvancements.grant(player, "martian_power");
            }
        }
        if (world.getTotalWorldTime() % 20 == 0) markDirty();
    }

    private boolean hasConnectedLoad() {
        for (EnumFacing side : EnumFacing.values()) {
            TileEntity adjacent = world.getTileEntity(pos.offset(side));
            if (adjacent instanceof IEnergyAcceptor
                    && ((IEnergyAcceptor) adjacent).acceptsEnergyFrom(this, side.getOpposite())) {
                return true;
            }
            if (adjacent != null && adjacent.hasCapability(
                    CapabilityEnergy.ENERGY, side.getOpposite())) {
                IEnergyStorage storage = adjacent.getCapability(
                    CapabilityEnergy.ENERGY, side.getOpposite());
                if (storage != null && storage.canReceive()) return true;
            }
        }
        return false;
    }

    public String environment() {
        if (world == null || world.provider == null) return "earth";
        String name = world.provider.getDimensionType().getName().toLowerCase();
        if (name.contains("moon")) return "moon";
        if (name.contains("mars")) return "mars";
        if (name.contains("space") || name.contains("orbit")) return "orbit";
        return "earth";
    }

    public int getGenerationRate() {
        String environment = environment();
        boolean tracking = world != null && world.getBlockState(pos).getBlock() instanceof BlockEnvironmentalSolarArray
            && ((BlockEnvironmentalSolarArray) world.getBlockState(pos).getBlock()).isTracking();
        if ("orbit".equals(environment)) return tracking ? 192 : 96;
        if ("moon".equals(environment)) return 32;
        if ("mars".equals(environment)) {
            // Deterministic dust cycle: periodic derating makes storage useful.
            return world != null && (world.getTotalWorldTime() / 24000L) % 8 == 7 ? 4 : 16;
        }
        return tracking ? 12 : 8;
    }

    public int getEnergyStored() { return (int) energy; }
    public void setLastUser(EntityPlayer player) { lastUser = player.getUniqueID(); markDirty(); }
    @Override public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) { return true; }
    @Override public double getOfferedEnergy() { return Math.min(128, energy); }
    @Override public void drawEnergy(double amount) { energy = Math.max(0, energy - amount); }
    @Override public int getSourceTier() { return 2; }

    @Override public void onLoad() {
        super.onLoad();
        if (!world.isRemote && !loaded) { MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this)); loaded = true; }
    }
    @Override public void invalidate() { unload(); super.invalidate(); }
    @Override public void onChunkUnload() { unload(); super.onChunkUnload(); }
    private void unload() {
        if (world != null && !world.isRemote && loaded) { MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this)); loaded = false; }
    }

    @Override public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }
    @Override @SuppressWarnings("unchecked") public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY ? (T) forgeEnergy : super.getCapability(capability, facing);
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setDouble("Energy", energy); tag.setLong("GeneratedTotal", generatedTotal);
        tag.setLong("LunarDarkTicks", lunarDarkTicks);
        if (lastUser != null) tag.setUniqueId("LastUser", lastUser); return tag;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); energy = tag.getDouble("Energy"); generatedTotal = tag.getLong("GeneratedTotal");
        lunarDarkTicks = tag.getLong("LunarDarkTicks");
        lastUser = tag.hasUniqueId("LastUser") ? tag.getUniqueId("LastUser") : null;
    }

    @Override public String getType() { return "environmental_solar_array"; }
    @Override public String[] getMethodNames() { return new String[] {"getGeneration", "getStored", "getEnvironment", "getGeneratedTotal"}; }
    @Override public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] args) throws LuaException {
        switch (method) {
            case 0: return new Object[] {getGenerationRate()};
            case 1: return new Object[] {(int) energy, CAPACITY};
            case 2: return new Object[] {environment()};
            case 3: return new Object[] {generatedTotal};
            default: throw new LuaException("Unknown method");
        }
    }
    @Override public void attach(IComputerAccess computer) {}
    @Override public void detach(IComputerAccess computer) {}
    @Override public Object getTarget() { return this; }
    @Override public boolean equals(IPeripheral other) { return other == this; }
}
