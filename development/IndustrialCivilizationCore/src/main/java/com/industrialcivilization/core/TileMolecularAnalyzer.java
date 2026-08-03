package com.industrialcivilization.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public final class TileMolecularAnalyzer extends TileEntity implements IPeripheral {
    public static final int CAPACITY = 200000;
    public static final int ENERGY_PER_ANALYSIS = 50000;
    private final EnergyStorage energy = new EnergyStorage(CAPACITY, 2048, 0);
    private int analysesCompleted;

    public boolean analyze(EntityPlayer player, EnumHand hand) {
        ItemStack sample = player.getHeldItem(hand);
        if (!isMartianDesh(sample)) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.analyzer.invalid_sample"), true);
            return true;
        }
        if (energy.getEnergyStored() < ENERGY_PER_ANALYSIS) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.analyzer.no_power"), true);
            return true;
        }

        energy.extractEnergy(ENERGY_PER_ANALYSIS, false);
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

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) energy;
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Energy", energy.getEnergyStored());
        compound.setInteger("AnalysesCompleted", analysesCompleted);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy.receiveEnergy(compound.getInteger("Energy"), false);
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
            case 0: return new Object[] {energy.getEnergyStored() >= ENERGY_PER_ANALYSIS ? "ready" : "charging"};
            case 1: return new Object[] {analysesCompleted};
            case 2: return new Object[] {energy.getEnergyStored()};
            case 3: return new Object[] {CAPACITY};
            case 4: return new Object[] {energy.getEnergyStored() >= ENERGY_PER_ANALYSIS};
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
