package com.industrialcivilization.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public final class TileIndustrialMachine extends TileEntity
        implements ITickable, ISidedInventory, IEnergySink, IPeripheral {
    public static final int OUTPUT_SLOT = 3;
    private static final int[] INPUTS = {0, 1, 2};
    private static final int[] OUTPUT = {3};
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private double energy;
    private int progress;
    private int completedOperations;
    private int queuedOperations = 1;
    private String selectedRecipe = "";
    private boolean addedToEnergyNet;
    private String cargoChannel = "";
    private java.util.UUID lastUser;
    private static final Set<TileIndustrialMachine> LOADED_CARGO_CONTROLLERS =
        Collections.newSetFromMap(new WeakHashMap<TileIndustrialMachine, Boolean>());

    private final IEnergyStorage forgeEnergy = new IEnergyStorage() {
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int accepted = (int) Math.min(amount, getCapacity() - energy);
            if (!simulate) { energy += accepted; markDirty(); }
            return accepted;
        }
        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return (int) energy; }
        @Override public int getMaxEnergyStored() { return getCapacity(); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    };

    public IndustrialMachineKind getKind() {
        if (world != null && world.getBlockState(pos).getBlock() instanceof BlockIndustrialMachine) {
            return ((BlockIndustrialMachine) world.getBlockState(pos).getBlock()).getKind();
        }
        return IndustrialMachineKind.ELECTRIC_FABRICATOR;
    }

    public int getCapacity() { return getKind().capacity; }
    public int getProgress() { return progress; }
    public int getDuration() { return getKind().duration; }
    public int getCompletedOperations() { return completedOperations; }
    public int getEnergyStored() { return (int) energy; }
    public void setLastUser(EntityPlayer player) { lastUser = player.getUniqueID(); markDirty(); }

    public String environment() {
        if (world == null || world.provider == null) return "earth";
        String name = world.provider.getDimensionType().getName().toLowerCase();
        if (name.contains("moon")) return "moon";
        if (name.contains("mars")) return "mars";
        if (name.contains("space") || name.contains("orbit")) return "orbit";
        return "earth";
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) return;
        if (getKind() == IndustrialMachineKind.CARGO_CONTROLLER && !cargoChannel.isEmpty()
                && world.getTotalWorldTime() % 100 == 0 && transferCargo()) return;
        MachineRecipe recipe = queuedOperations > 0 ? MachineRecipe.find(this, selectedRecipe) : null;
        int cost = getKind().voltage;
        if (recipe == null || energy < cost) {
            if (recipe == null && progress != 0) progress = 0;
            return;
        }
        energy -= cost;
        progress++;
        if (progress >= getDuration()) {
            recipe.complete(this);
            progress = 0;
            completedOperations++;
            queuedOperations = Math.max(0, queuedOperations - 1);
            if (getKind() == IndustrialMachineKind.ELECTRIC_FABRICATOR) queuedOperations = 1;
            awardOperation(recipe.id);
            markDirty();
        } else if ((progress & 15) == 0) {
            markDirty();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!world.isRemote && !addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
        if (!world.isRemote && getKind() == IndustrialMachineKind.CARGO_CONTROLLER) {
            LOADED_CARGO_CONTROLLERS.add(this);
        }
    }

    @Override
    public void invalidate() {
        unloadEnergyNet();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        unloadEnergyNet();
        super.onChunkUnload();
    }

    private void unloadEnergyNet() {
        LOADED_CARGO_CONTROLLERS.remove(this);
        if (world != null && !world.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) { return true; }
    @Override public double getDemandedEnergy() { return Math.max(0, getCapacity() - energy); }
    @Override public int getSinkTier() { return getKind().tier(); }
    @Override public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        double accepted = Math.min(amount, getDemandedEnergy());
        energy += accepted;
        return amount - accepted;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) forgeEnergy;
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setDouble("Energy", energy);
        compound.setInteger("Progress", progress);
        compound.setInteger("Completed", completedOperations);
        compound.setInteger("Queued", queuedOperations);
        compound.setString("SelectedRecipe", selectedRecipe);
        compound.setString("CargoChannel", cargoChannel);
        if (lastUser != null) compound.setUniqueId("LastUser", lastUser);
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) compound.setTag("Slot" + i, inventory.get(i).serializeNBT());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy = compound.getDouble("Energy");
        progress = compound.getInteger("Progress");
        completedOperations = compound.getInteger("Completed");
        queuedOperations = compound.getInteger("Queued");
        selectedRecipe = compound.getString("SelectedRecipe");
        cargoChannel = compound.getString("CargoChannel");
        lastUser = compound.hasUniqueId("LastUser") ? compound.getUniqueId("LastUser") : null;
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, compound.hasKey("Slot" + i, 10)
                ? new ItemStack(compound.getCompoundTag("Slot" + i)) : ItemStack.EMPTY);
        }
    }

    @Override public int getSizeInventory() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStackInSlot(int index) { return inventory.get(index); }
    @Override public ItemStack decrStackSize(int index, int count) {
        ItemStack result = net.minecraft.inventory.ItemStackHelper.getAndSplit(inventory, index, count);
        if (!result.isEmpty()) markDirty();
        return result;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        ItemStack result = net.minecraft.inventory.ItemStackHelper.getAndRemove(inventory, index);
        if (!result.isEmpty()) markDirty();
        return result;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) stack.setCount(getInventoryStackLimit());
        markDirty();
    }
    @Override public String getName() { return "tile.industrialcivilizationcore." + getKind().id + ".name"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentTranslation(getName()); }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos) <= 64.0;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return index != OUTPUT_SLOT; }
    @Override public int getField(int id) {
        if (id == 0) return progress;
        if (id == 1) return (int) energy;
        if (id == 2) return completedOperations;
        if (id == 3) return queuedOperations;
        return 0;
    }
    @Override public void setField(int id, int value) {
        if (id == 0) progress = value;
        else if (id == 1) energy = value;
        else if (id == 2) completedOperations = value;
        else if (id == 3) queuedOperations = value;
    }
    @Override public int getFieldCount() { return 4; }
    @Override public void clear() { inventory.clear(); }
    @Override public int[] getSlotsForFace(EnumFacing side) { return side == EnumFacing.DOWN ? OUTPUT : INPUTS; }
    @Override public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) { return index != OUTPUT_SLOT; }
    @Override public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) { return index == OUTPUT_SLOT; }

    @Override public String getType() { return "industrial_machine"; }
    @Override public String[] getMethodNames() {
        return new String[] {"getStatus", "getEnergy", "getCapacity", "getProgress",
            "getEnvironment", "listRecipes", "selectRecipe", "queue", "getCompleted",
            "setCargoChannel", "getCargoChannel", "transferCargo"};
    }
    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method,
            Object[] arguments) throws LuaException {
        switch (method) {
            case 0: return new Object[] {progress > 0 ? "running" : MachineRecipe.find(this, selectedRecipe) != null ? "ready" : "idle"};
            case 1: return new Object[] {(int) energy};
            case 2: return new Object[] {getCapacity()};
            case 3: return new Object[] {progress, getDuration()};
            case 4: return new Object[] {environment()};
            case 5: return new Object[] {Arrays.stream(MachineRecipe.all())
                .filter(r -> r.machine == getKind()).map(r -> r.id).toArray(String[]::new)};
            case 6:
                if (arguments.length < 1 || !(arguments[0] instanceof String)) throw new LuaException("Expected recipe ID");
                selectedRecipe = (String) arguments[0]; markDirty(); return new Object[] {true};
            case 7:
                int count = arguments.length > 0 && arguments[0] instanceof Double
                    ? Math.max(1, Math.min(64, ((Double) arguments[0]).intValue())) : 1;
                queuedOperations = count; markDirty(); return new Object[] {queuedOperations};
            case 8: return new Object[] {completedOperations};
            case 9:
                if (getKind() != IndustrialMachineKind.CARGO_CONTROLLER) throw new LuaException("Not a cargo controller");
                if (arguments.length < 1 || !(arguments[0] instanceof String)) throw new LuaException("Expected channel name");
                cargoChannel = ((String) arguments[0]).trim(); markDirty(); return new Object[] {cargoChannel};
            case 10: return new Object[] {cargoChannel};
            case 11: return new Object[] {transferCargo()};
            default: throw new LuaException("Unknown method");
        }
    }
    @Override public void attach(IComputerAccess computer) {}
    @Override public void detach(IComputerAccess computer) {}
    @Override public Object getTarget() { return this; }
    @Override public boolean equals(IPeripheral other) { return other == this; }

    private boolean transferCargo() {
        if (getKind() != IndustrialMachineKind.CARGO_CONTROLLER || cargoChannel.isEmpty()
                || energy < getKind().voltage || inventory.get(0).isEmpty()) return false;
        for (TileIndustrialMachine target : LOADED_CARGO_CONTROLLERS) {
            if (target == this || target.isInvalid() || !cargoChannel.equals(target.cargoChannel)
                    || target.world == world) continue;
            ItemStack destination = target.inventory.get(OUTPUT_SLOT);
            ItemStack source = inventory.get(0);
            if (!destination.isEmpty() && (!ItemStack.areItemsEqual(source, destination)
                    || destination.getCount() >= destination.getMaxStackSize())) continue;
            ItemStack moved = source.splitStack(1);
            if (destination.isEmpty()) target.inventory.set(OUTPUT_SLOT, moved);
            else destination.grow(1);
            energy -= getKind().voltage;
            completedOperations++;
            EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
            if (player != null) RuntimeAdvancements.grant(player, "cross_planetary_logistics");
            markDirty(); target.markDirty();
            return true;
        }
        return false;
    }

    private void awardOperation(String recipe) {
        EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
        if (player == null) return;
        if ("control_processor".equals(recipe)) {
            RuntimeAdvancements.grant(player, "production_queue");
            if (completedOperations >= 3) RuntimeAdvancements.grant(player, "multi_step_manufacturing");
            if (completedOperations >= 5) RuntimeAdvancements.grant(player, "programmable_manufacturing");
        } else if ("record_orbital_data".equals(recipe)) {
            RuntimeAdvancements.grant(player, "orbital_experiments");
            RuntimeAdvancements.grant(player, "orbital_operational_data");
        } else if ("record_lunar_data".equals(recipe)) {
            RuntimeAdvancements.grant(player, "lunar_science_program");
        } else if ("record_martian_data".equals(recipe)) {
            RuntimeAdvancements.grant(player, "martian_science_program");
        } else if ("lunar_quantum_component".equals(recipe)) {
            RuntimeAdvancements.grant(player, "lunar_precision_manufacturing");
        } else if ("martian_autonomy".equals(recipe)) {
            RuntimeAdvancements.grant(player, "autonomous_resource_response");
            RuntimeAdvancements.grant(player, "autonomous_power_response");
            RuntimeAdvancements.grant(player, "unattended_martian_production");
        } else if ("cargo_network".equals(recipe)) {
            RuntimeAdvancements.grant(player, "ai_factory_coordination");
        } else if ("civilization_scale_ai".equals(recipe)) {
            RuntimeAdvancements.grant(player, "continuous_civilization");
        }
    }
}
