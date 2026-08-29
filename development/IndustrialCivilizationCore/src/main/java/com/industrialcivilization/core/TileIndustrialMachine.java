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
    static final String[] PERIPHERAL_METHODS = {
        "getStatus", "getEnergy", "getCapacity", "getProgress", "getEnvironment",
        "listRecipes", "selectRecipe", "queue", "getCompleted", "setCargoChannel",
        "getCargoChannel", "transferCargo", "getEnergyStored", "getInputTier",
        "getAcceptedEUThisTick", "getBaselineEUPerTick", "getEffectiveSpeedMultiplier",
        "getWorkCompleted", "getWorkRequired", "getEstimatedTicksRemaining",
        "getMfsuPacketsThisTick", "getOperationPeakMfsuPackets"
    };
    private static final int[] INPUTS = {0, 1, 2};
    private static final int[] OUTPUT = {3};
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private double energy;
    private int progress;
    private double workCompletedEU;
    private double pendingOperationEU;
    private double acceptedSinceLastUpdateEU;
    private double lastAcceptedEU;
    private double acceptedMfsuEUSinceLastUpdate;
    private int lastAcceptedMfsuPackets;
    private int operationPeakMfsuPackets;
    private int elapsedOperationTicks;
    private String activeRecipeId = "";
    private boolean legacyWorkAwaitingRecipe;
    private int legacyProgressTicks;
    private int completedOperations;
    private int queuedOperations = 1;
    private String selectedRecipe = "";
    private boolean addedToEnergyNet;
    private String cargoChannel = "";
    private java.util.UUID lastUser;
    private boolean rusted;
    private boolean nationManaged;
    private String nationProduct = "";
    private EnumFacing workshopFacing = EnumFacing.NORTH;
    private static final Set<TileIndustrialMachine> LOADED_CARGO_CONTROLLERS =
        Collections.newSetFromMap(new WeakHashMap<TileIndustrialMachine, Boolean>());

    private final IEnergyStorage forgeEnergy = new IEnergyStorage() {
        @Override public int receiveEnergy(int amount, boolean simulate) {
            double offeredEU = amount / (double) IndustrialCivilizationCore.FE_PER_EU;
            double acceptedEU = acceptEnergyEU(Math.min(offeredEU, getKind().voltage), simulate);
            return Math.max(0, Math.min(amount,
                (int) Math.floor(acceptedEU * IndustrialCivilizationCore.FE_PER_EU)));
        }
        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() {
            return (int) Math.floor(energy * IndustrialCivilizationCore.FE_PER_EU);
        }
        @Override public int getMaxEnergyStored() {
            return getCapacity() * IndustrialCivilizationCore.FE_PER_EU;
        }
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
    public long getWorkRequiredEU() { return getKind().totalWorkEU(); }
    public long getWorkCompletedEU() { return (long) Math.floor(workCompletedEU); }
    public int getMinimumTicks() { return getKind().minimumTicks; }
    public int getElapsedOperationTicks() { return elapsedOperationTicks; }
    public int getAcceptedEUThisTick() { return (int) Math.floor(lastAcceptedEU); }
    public int getMfsuPacketsThisTick() { return lastAcceptedMfsuPackets; }
    public int getOperationPeakMfsuPackets() { return operationPeakMfsuPackets; }
    public int getBaselineEUPerTick() { return getKind().voltage; }
    public double getEffectiveSpeedMultiplier() {
        if (getBaselineEUPerTick() <= 0) return 0D;
        double effective = IndustrialCivilizationCore.NATIVE_IC2_POWER_SCALING
            ? Math.max(getBaselineEUPerTick(), lastAcceptedEU) : getBaselineEUPerTick();
        if (!IndustrialCivilizationCore.ALLOW_MULTI_PACKET_THROUGHPUT)
            effective = Math.min(effective, getBaselineEUPerTick());
        return effective / getBaselineEUPerTick();
    }
    public int getEstimatedTicksRemaining() {
        return NativeIc2PowerModel.estimateTicksRemaining(workCompletedEU, getWorkRequiredEU(),
            elapsedOperationTicks, getMinimumTicks(), getBaselineEUPerTick(), lastAcceptedEU,
            IndustrialCivilizationCore.NATIVE_IC2_POWER_SCALING,
            IndustrialCivilizationCore.ALLOW_MULTI_PACKET_THROUGHPUT);
    }
    public int getCompletedOperations() { return completedOperations; }
    public int getEnergyStored() { return (int) energy; }
    public void setLastUser(EntityPlayer player) { lastUser = player.getUniqueID(); markDirty(); }
    public boolean isRusted() { return rusted; }
    public EnumFacing getWorkshopFacing() { return workshopFacing; }
    public void setWorkshopFacing(EnumFacing facing) {
        workshopFacing = facing != null && facing.getAxis().isHorizontal() ? facing : EnumFacing.NORTH;
        markDirty();
    }
    public boolean repairRust() {
        if (!rusted) return false;
        rusted = false;
        markDirty();
        return true;
    }
    public void seedNationExchange(String channel, String product) {
        nationManaged = true;
        cargoChannel = channel;
        nationProduct = product;
        energy = getCapacity();
        markDirty();
    }

    boolean transferNationCargoForTest() {
        if (!nationManaged) return false;
        restockNationProduct();
        return transferCargo();
    }

    boolean isNationManagedForTest() { return nationManaged; }

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
        // NBT is read before the tile necessarily has a world/block state. Defer the
        // capacity clamp until now so high-capacity machines are not truncated using
        // the fallback machine kind during chunk loading.
        energy = Math.max(0D, Math.min(getCapacity(), energy));
        lastAcceptedEU = acceptedSinceLastUpdateEU;
        acceptedSinceLastUpdateEU = 0D;
        lastAcceptedMfsuPackets = NativeIc2PowerModel.mfsuPacketEquivalents(
            acceptedMfsuEUSinceLastUpdate);
        acceptedMfsuEUSinceLastUpdate = 0D;
        if (isWeatherSensitive() && world.getTotalWorldTime() % 20 == 0
                && world.isRainingAt(pos.up()) && world.canSeeSky(pos.up())) {
            rusted = true;
            resetOperation(true);
            markDirty();
        }
        if (rusted) return;
        if (getKind() == IndustrialMachineKind.CAR_WORKSHOP && !world.canSeeSky(pos.up())
                && (energy > 0D || lastAcceptedEU > 0D)) {
            EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
            if (player != null && ProgressionState.has(player, "car_workshop_structure_deployed"))
                RuntimeAdvancements.grant(player, "car_workshop_deployed");
        }
        if (nationManaged && world.getTotalWorldTime() % 1200 == 0) restockNationProduct();
        if (getKind() == IndustrialMachineKind.CARGO_CONTROLLER && !cargoChannel.isEmpty()
                && world.getTotalWorldTime() % 100 == 0 && transferCargo()) return;
        MachineRecipe recipe = queuedOperations > 0 ? MachineRecipe.find(this, selectedRecipe) : null;
        if (recipe == null) {
            if (progress != 0 || workCompletedEU > 0D || pendingOperationEU > 0D)
                resetOperation(true);
            return;
        }
        if (!recipe.id.equals(activeRecipeId)) {
            if (legacyWorkAwaitingRecipe && legacyProgressTicks > 0) {
                workCompletedEU = NativeIc2PowerModel.migrateLegacyProgress(
                    legacyProgressTicks, getDuration(), getWorkRequiredEU());
                elapsedOperationTicks = legacyProgressTicks;
                legacyProgressTicks = 0;
            }
            if (legacyWorkAwaitingRecipe && activeRecipeId.isEmpty()) {
                activeRecipeId = recipe.id;
                legacyWorkAwaitingRecipe = false;
            } else {
                resetOperation(true);
                activeRecipeId = recipe.id;
            }
        }
        operationPeakMfsuPackets = Math.max(operationPeakMfsuPackets,
            lastAcceptedMfsuPackets);

        workCompletedEU = Math.max(0D, Math.min(getWorkRequiredEU(), workCompletedEU));
        pendingOperationEU = Math.max(0D, Math.min(
            getWorkRequiredEU() - workCompletedEU, pendingOperationEU));

        double remaining = Math.max(0D, getWorkRequiredEU() - workCompletedEU);
        double usable = NativeIc2PowerModel.usableWorkEU(energy, pendingOperationEU,
            lastAcceptedEU, getBaselineEUPerTick(), remaining,
            IndustrialCivilizationCore.NATIVE_IC2_POWER_SCALING,
            IndustrialCivilizationCore.ALLOW_MULTI_PACKET_THROUGHPUT);
        if (usable > 0D) {
            double directUsed = Math.min(pendingOperationEU, usable);
            pendingOperationEU -= directUsed;
            energy = Math.max(0D, energy - (usable - directUsed));
            workCompletedEU = Math.min(getWorkRequiredEU(), workCompletedEU + usable);
        }
        if (usable > 0D || workCompletedEU >= getWorkRequiredEU()) elapsedOperationTicks++;
        progress = (int) Math.min(getDuration(), Math.floor(
            workCompletedEU * getDuration() / (double) getWorkRequiredEU()));

        if (workCompletedEU >= getWorkRequiredEU()
                && elapsedOperationTicks >= getMinimumTicks()) {
            recipe.complete(this);
            completedOperations++;
            queuedOperations = Math.max(0, queuedOperations - 1);
            if (getKind() == IndustrialMachineKind.ELECTRIC_FABRICATOR) queuedOperations = 1;
            awardOperation(recipe.id);
            resetOperation(false);
            markDirty();
        } else if (usable > 0D && (elapsedOperationTicks & 15) == 0) {
            markDirty();
        }
    }

    private void resetOperation(boolean recoverPending) {
        if (recoverPending && pendingOperationEU > 0D) {
            energy = Math.min(getCapacity(), energy + pendingOperationEU);
        }
        progress = 0;
        workCompletedEU = 0D;
        pendingOperationEU = 0D;
        elapsedOperationTicks = 0;
        activeRecipeId = "";
        legacyWorkAwaitingRecipe = false;
        legacyProgressTicks = 0;
        operationPeakMfsuPackets = 0;
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
    @Override public double getDemandedEnergy() {
        double demand = Math.max(0D, getCapacity() - energy);
        if (canAcceptDirectOperationEnergy()) {
            demand += Math.max(0D, getWorkRequiredEU() - workCompletedEU - pendingOperationEU);
        }
        return demand;
    }
    @Override public int getSinkTier() { return getKind().tier(); }
    @Override public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
        // IC2 Classic validates each delivered packet against getSinkTier() before
        // invoking this method. Do not combine calls or reinterpret their voltage.
        double accepted = acceptEnergyEU(amount, false);
        if (accepted > 0D && canAcceptDirectOperationEnergy()
                && NativeIc2PowerModel.isMfsuClassVoltage(voltage)) {
            // IC2 Classic may split one source packet across several cable paths
            // and invoke the sink more than once. Count the accepted 512-EU
            // equivalents, not callbacks, or a meshed cable bus inflates the bank.
            acceptedMfsuEUSinceLastUpdate += accepted;
        }
        return amount - accepted;
    }

    private boolean canAcceptDirectOperationEnergy() {
        return IndustrialCivilizationCore.NATIVE_IC2_POWER_SCALING
            && IndustrialCivilizationCore.ALLOW_MULTI_PACKET_THROUGHPUT
            && !rusted && queuedOperations > 0
            && MachineRecipe.find(this, selectedRecipe) != null;
    }

    private double acceptEnergyEU(double offeredEU, boolean simulate) {
        if (offeredEU <= 0D) return 0D;
        double bufferRoom = Math.max(0D, getCapacity() - energy);
        double directRoom = canAcceptDirectOperationEnergy()
            ? Math.max(0D, getWorkRequiredEU() - workCompletedEU - pendingOperationEU) : 0D;
        double accepted = Math.min(offeredEU, bufferRoom + directRoom);
        if (!simulate && accepted > 0D) {
            double buffered = Math.min(bufferRoom, accepted);
            energy += buffered;
            pendingOperationEU += accepted - buffered;
            acceptedSinceLastUpdateEU += accepted;
            markDirty();
        }
        return accepted;
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
        compound.setDouble("WorkCompletedEU", workCompletedEU);
        compound.setDouble("PendingOperationEU", pendingOperationEU);
        compound.setInteger("ElapsedOperationTicks", elapsedOperationTicks);
        compound.setString("ActiveRecipe", activeRecipeId);
        compound.setInteger("OperationPeakMfsuPackets", operationPeakMfsuPackets);
        compound.setInteger("Completed", completedOperations);
        compound.setInteger("Queued", queuedOperations);
        compound.setString("SelectedRecipe", selectedRecipe);
        compound.setString("CargoChannel", cargoChannel);
        compound.setBoolean("Rusted", rusted);
        compound.setBoolean("NationManaged", nationManaged);
        compound.setString("NationProduct", nationProduct);
        compound.setInteger("WorkshopFacing", workshopFacing.getHorizontalIndex());
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
        if (compound.hasKey("WorkCompletedEU", 99)) {
            workCompletedEU = Math.max(0D, compound.getDouble("WorkCompletedEU"));
            pendingOperationEU = Math.max(0D, compound.getDouble("PendingOperationEU"));
            elapsedOperationTicks = Math.max(0, compound.getInteger("ElapsedOperationTicks"));
            activeRecipeId = compound.getString("ActiveRecipe");
            operationPeakMfsuPackets = Math.max(0,
                compound.getInteger("OperationPeakMfsuPackets"));
        } else {
            legacyProgressTicks = Math.max(0, progress);
            legacyWorkAwaitingRecipe = legacyProgressTicks > 0;
        }
        completedOperations = compound.getInteger("Completed");
        queuedOperations = compound.getInteger("Queued");
        selectedRecipe = compound.getString("SelectedRecipe");
        cargoChannel = compound.getString("CargoChannel");
        rusted = compound.getBoolean("Rusted");
        nationManaged = compound.getBoolean("NationManaged");
        nationProduct = compound.getString("NationProduct");
        workshopFacing = compound.hasKey("WorkshopFacing", 3)
            ? EnumFacing.getHorizontal(compound.getInteger("WorkshopFacing")) : EnumFacing.NORTH;
        lastUser = compound.hasUniqueId("LastUser") ? compound.getUniqueId("LastUser") : null;
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, compound.hasKey("Slot" + i, 10)
                ? new ItemStack(compound.getCompoundTag("Slot" + i)) : ItemStack.EMPTY);
        }
        energy = Math.max(0D, energy);
        pendingOperationEU = Math.max(0D, pendingOperationEU);
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
        if (id == 1) return lowWord((long) energy);
        if (id == 2) return completedOperations;
        if (id == 3) return queuedOperations;
        if (id == 4) return highWord((long) energy);
        if (id == 5) return lowWord(getWorkCompletedEU());
        if (id == 6) return highWord(getWorkCompletedEU());
        if (id == 7) return lowWord(getAcceptedEUThisTick());
        if (id == 8) return highWord(getAcceptedEUThisTick());
        if (id == 9) return elapsedOperationTicks;
        return 0;
    }
    @Override public void setField(int id, int value) {
        if (id == 0) progress = value;
        else if (id == 1) energy = combineWords(value, highWord((long) energy));
        else if (id == 2) completedOperations = value;
        else if (id == 3) queuedOperations = value;
        else if (id == 4) energy = combineWords(lowWord((long) energy), value);
        else if (id == 5) workCompletedEU = combineWords(value, highWord(getWorkCompletedEU()));
        else if (id == 6) workCompletedEU = combineWords(lowWord(getWorkCompletedEU()), value);
        else if (id == 7) lastAcceptedEU = combineWords(value, highWord(getAcceptedEUThisTick()));
        else if (id == 8) lastAcceptedEU = combineWords(lowWord(getAcceptedEUThisTick()), value);
        else if (id == 9) elapsedOperationTicks = value & 0xFFFF;
    }
    @Override public int getFieldCount() { return 10; }
    private static int lowWord(long value) { return (int) (value & 0xFFFFL); }
    private static int highWord(long value) { return (int) ((value >>> 16) & 0xFFFFL); }
    private static long combineWords(int low, int high) {
        return ((long) high & 0xFFFFL) << 16 | ((long) low & 0xFFFFL);
    }
    @Override public void clear() { inventory.clear(); }
    @Override public int[] getSlotsForFace(EnumFacing side) { return side == EnumFacing.DOWN ? OUTPUT : INPUTS; }
    @Override public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) { return index != OUTPUT_SLOT; }
    @Override public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) { return index == OUTPUT_SLOT; }

    @Override public String getType() { return "industrial_machine"; }
    @Override public String[] getMethodNames() {
        return PERIPHERAL_METHODS.clone();
    }
    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method,
            Object[] arguments) throws LuaException {
        switch (method) {
            case 0: return new Object[] {rusted ? "rusted - repair at a Repair Bench"
                : progress > 0 ? "running" : MachineRecipe.find(this, selectedRecipe) != null ? "ready" : "idle"};
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
            case 12: return new Object[] {getEnergyStored()};
            case 13: return new Object[] {getKind().tier()};
            case 14: return new Object[] {getAcceptedEUThisTick()};
            case 15: return new Object[] {getBaselineEUPerTick()};
            case 16: return new Object[] {getEffectiveSpeedMultiplier()};
            case 17: return new Object[] {getWorkCompletedEU()};
            case 18: return new Object[] {getWorkRequiredEU()};
            case 19: return new Object[] {getEstimatedTicksRemaining()};
            case 20: return new Object[] {getMfsuPacketsThisTick()};
            case 21: return new Object[] {getOperationPeakMfsuPackets()};
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
                    || (target.world == world && !(nationManaged && target.nationManaged))) continue;
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
            EntityPlayer nearest = world.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 64D, false);
            if (nationManaged && target.nationManaged && nearest != null)
                RuntimeAdvancements.grant(nearest, "nation_trade_network");
            markDirty(); target.markDirty();
            return true;
        }
        return false;
    }

    private boolean isWeatherSensitive() {
        return getKind() == IndustrialMachineKind.CAR_WORKSHOP
            || getKind() == IndustrialMachineKind.GUN_FACTORY;
    }

    private void restockNationProduct() {
        if (!inventory.get(0).isEmpty() || nationProduct.isEmpty()) return;
        net.minecraft.item.Item item = net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS
            .getValue(new net.minecraft.util.ResourceLocation(nationProduct));
        if (item != null) inventory.set(0, new ItemStack(item));
    }

    private void awardOperation(String recipe) {
        EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
        if (player == null) return;
        awardMfsuBankProgress(player);
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
        } else if ("city_compact".equals(recipe) || "frontier_off_roader".equals(recipe)
                || "passenger_carrier".equals(recipe) || "agricultural_tractor".equals(recipe)
                || "utility_cart".equals(recipe) || "scout_atv".equals(recipe)) {
            ProgressionState.record(player, "regional_vehicle_manufactured");
            if ("passenger_carrier".equals(recipe))
                ProgressionState.record(player, "industrial_service_carrier_manufactured");
        } else if ("combat_shotgun".equals(recipe) || "automatic_rifle".equals(recipe)) {
            if (ProgressionState.has(player, "gun_factory_structure_deployed")
                    && !world.canSeeSky(pos.up()) && (energy > 0D || lastAcceptedEU > 0D))
                RuntimeAdvancements.grant(player, "advanced_armament_factory");
        }
    }

    void selectRecipeForTest(String recipe) {
        selectedRecipe = recipe;
        queuedOperations = 1;
        markDirty();
    }

    private void awardMfsuBankProgress(EntityPlayerMP player) {
        if (operationPeakMfsuPackets >= 1)
            RuntimeAdvancements.grant(player, "mfsu_bank_baseline");
        if (operationPeakMfsuPackets >= 4)
            RuntimeAdvancements.grant(player, "mfsu_bank_quad");
        if (operationPeakMfsuPackets >= 10)
            RuntimeAdvancements.grant(player, "mfsu_bank_ten");
        if (operationPeakMfsuPackets >= 50)
            RuntimeAdvancements.grant(player, "mfsu_bank_fifty");
        if (NativeIc2PowerModel.qualifiesBlinkManufacturing(operationPeakMfsuPackets,
                elapsedOperationTicks, getMinimumTicks())) {
            RuntimeAdvancements.grant(player, "blink_manufacturing");
        }
    }
}
