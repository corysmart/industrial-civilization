package com.industrialcivilization.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class TileMolecularAnalyzer extends TileEntity implements IPeripheral, IEnergySink {
    public static final int CAPACITY_EU = 25000;
    public static final int ENERGY_PER_ANALYSIS_EU = 6250;
    private double energyEu;
    private int analysesCompleted;
    private boolean addedToEnergyNet;
    private final IEnergyStorage forgeEnergy = new IEnergyStorage() {
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int missingFe = (int) Math.floor((CAPACITY_EU - energyEu)
                * IndustrialCivilizationCore.FE_PER_EU);
            int accepted = Math.max(0, Math.min(amount, Math.min(missingFe, 2048)));
            if (!simulate && accepted > 0) {
                energyEu += accepted / (double) IndustrialCivilizationCore.FE_PER_EU;
                markDirty();
            }
            return accepted;
        }
        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return storedFe(); }
        @Override public int getMaxEnergyStored() {
            return CAPACITY_EU * IndustrialCivilizationCore.FE_PER_EU;
        }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    };

    public boolean analyze(EntityPlayer player, EnumHand hand) {
        ItemStack sample = player.getHeldItem(hand);
        if (!isMartianDesh(sample)) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.analyzer.invalid_sample"), true);
            return true;
        }
        if (energyEu < ENERGY_PER_ANALYSIS_EU) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.analyzer.no_power"), true);
            return true;
        }

        energyEu -= ENERGY_PER_ANALYSIS_EU;
        if (!player.capabilities.isCreativeMode) sample.shrink(1);
        ItemStack record = new ItemStack(IndustrialCivilizationCore.MATERIAL_PATTERN_RECORD);
        NBTTagCompound data = new NBTTagCompound();
        data.setString("Sample", "galacticraftplanets:item_basic_mars:2");
        data.setString("Origin", "Mars");
        data.setString("Classification", "Desh alloy / scientific pattern");
        data.setBoolean("Replicable", false);
        record.setTagCompound(data);
        if (!player.inventory.addItemStackToInventory(record)) {
            world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, record));
        }
        analysesCompleted++;
        markDirty();
        ProgressionState.record(player, "lite_matter_complete");
        RuntimeAdvancements.grant(player, "analyzer_power");
        RuntimeAdvancements.grant(player, "comparative_molecular_analysis");
        RuntimeAdvancements.grant(player, "lite_matter_complete");
        player.sendStatusMessage(new TextComponentTranslation(
            "message.industrialcivilization.analyzer.complete"), false);
        return true;
    }

    static boolean isMartianDesh(ItemStack stack) {
        return !stack.isEmpty()
            && stack.getItem().getRegistryName() != null
            && "galacticraftplanets:item_basic_mars".equals(
                stack.getItem().getRegistryName().toString())
            && stack.getMetadata() == 2;
    }

    private int storedFe() {
        return (int) Math.floor(energyEu * IndustrialCivilizationCore.FE_PER_EU);
    }

    @Override public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) { return true; }
    @Override public double getDemandedEnergy() { return Math.max(0, CAPACITY_EU - energyEu); }
    @Override public int getSinkTier() { return 3; }
    @Override public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        double accepted = Math.min(amount, getDemandedEnergy());
        energyEu += accepted;
        return amount - accepted;
    }

    @Override public void onLoad() {
        super.onLoad();
        if (!world.isRemote && !addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
    }

    @Override public void invalidate() { unloadEnergyNet(); super.invalidate(); }
    @Override public void onChunkUnload() { unloadEnergyNet(); super.onChunkUnload(); }
    private void unloadEnergyNet() {
        if (world != null && !world.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) forgeEnergy;
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setDouble("EnergyEU", energyEu);
        compound.setInteger("Energy", storedFe());
        compound.setInteger("AnalysesCompleted", analysesCompleted);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energyEu = compound.hasKey("EnergyEU", 99)
            ? compound.getDouble("EnergyEU")
            : compound.getInteger("Energy") / (double) IndustrialCivilizationCore.FE_PER_EU;
        energyEu = Math.max(0, Math.min(CAPACITY_EU, energyEu));
        analysesCompleted = compound.getInteger("AnalysesCompleted");
    }

    @Override public String getType() { return "molecular_analyzer"; }

    @Override
    public String[] getMethodNames() {
        return new String[] {"getStatus", "getProgress", "getStored", "getCapacity", "analyzeAvailable",
            "getOxygen", "getRadiation", "getPressure"};
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method,
            Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] {energyEu >= ENERGY_PER_ANALYSIS_EU ? "ready" : "charging"};
            case 1: return new Object[] {analysesCompleted};
            case 2: return new Object[] {energyEu};
            case 3: return new Object[] {CAPACITY_EU};
            case 4: return new Object[] {energyEu >= ENERGY_PER_ANALYSIS_EU};
            case 5: return new Object[] {isAirlessWorld() ? 0.0 : 21.0};
            case 6: return new Object[] {isAirlessWorld() ? 0.35 : 0.05};
            case 7: return new Object[] {isAirlessWorld() ? 0.0 : 101.3};
            default: throw new LuaException("Unknown method");
        }
    }

    private boolean isAirlessWorld() {
        if (world == null || world.provider == null) return false;
        String dimension = world.provider.getDimensionType().getName().toLowerCase();
        return dimension.contains("moon") || dimension.contains("mars") || dimension.contains("asteroid");
    }

    @Override public void attach(IComputerAccess computer) {}
    @Override public void detach(IComputerAccess computer) {}
    @Override public Object getTarget() { return this; }
    @Override public boolean equals(IPeripheral other) { return other == this; }
}
