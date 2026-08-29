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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
        "getMfsuPacketsThisTick", "getOperationPeakMfsuPackets",
        "getFacilityStatus", "setFacilityIdentity", "getPowerStatus",
        "configureReserve", "getReserveStatus", "configureEmergencyReserve",
        "setPolicyEnabled", "setManualOverride", "getPolicyAction", "getPolicyBlocker",
        "requestFreight", "getManifest", "registerNoncriticalLoad", "getLoadState",
        "configureServiceProgram", "getServiceProgramStatus", "commissionServiceProgram",
        "getRouteStatus"
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
    private final IndustrialPolicyState policy = new IndustrialPolicyState();
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
    public boolean hasLocalOperationsStatus() {
        return getKind() == IndustrialMachineKind.CARGO_CONTROLLER
            || getKind() == IndustrialMachineKind.SERVICE_INTERFACE;
    }
    public String getLocalOperationsStatus() {
        if (getKind() == IndustrialMachineKind.CARGO_CONTROLLER) {
            String facility = policy.facilityName.isEmpty() ? "unnamed facility" : policy.facilityName;
            String route = cargoChannel.isEmpty() ? "unregistered route" : "route " + cargoChannel;
            String manifest = "none".equals(policy.manifestStatus) ? "no manifest"
                : "manifest " + policy.manifestStatus + " " + policy.manifestDelivered
                    + "/" + policy.manifestRequested;
            String cause = policy.blocker.isEmpty() ? policy.manifestFailure : policy.blocker;
            return facility + " | " + route + " | " + manifest
                + (cause.isEmpty() ? "" : " | " + cause);
        }
        if (getKind() == IndustrialMachineKind.SERVICE_INTERFACE) {
            String program = serviceProgramDisplayName();
            String cause = policy.serviceBlocker.isEmpty() ? "operational" : policy.serviceBlocker;
            return program + " | " + policy.servicePhase + " " + policy.commissioningTicks
                + "/200 | " + cause;
        }
        return "";
    }
    public String getOperationsGuiTitle() {
        if (!hasLocalOperationsStatus() || "unnamed facility".equals(policy.facilityName))
            return getDisplayName().getUnformattedText();
        return policy.facilityName;
    }
    public String[] getOperationsGuiLines() {
        if (getKind() == IndustrialMachineKind.CARGO_CONTROLLER) {
            long age = policy.manifestLastTransferTick <= 0L || world == null ? -1L
                : Math.max(0L, (world.getTotalWorldTime() - policy.manifestLastTransferTick) / 20L);
            String reserve = policy.reserveItem.isEmpty() ? "Reserve not configured"
                : "Reserve " + policy.observedReserve + "/" + policy.minimumReserve
                    + ">" + policy.preferredReserve + " P" + policy.priority;
            String manifest = "none".equals(policy.manifestStatus) ? "No manifest"
                : "M " + policy.manifestDelivered + "/" + policy.manifestRequested
                    + " " + policy.manifestStatus;
            String route = (cargoChannel.isEmpty() ? "No route" : cargoChannel)
                + " " + policy.routePeers + "p | " + manifest
                + (age < 0L ? "" : " | " + age + "s");
            String cause = policy.blocker.isEmpty() ? policy.manifestFailure : policy.blocker;
            return new String[] {reserve, route,
                cause.isEmpty() ? "Action " + policy.action : "Blocked " + cause};
        }
        if (getKind() == IndustrialMachineKind.SERVICE_INTERFACE) {
            String program = serviceProgramDisplayName();
            String progress = policy.serviceCommissioned ? policy.servicePhase
                : policy.servicePhase + " " + policy.commissioningTicks + "/200";
            String cause = policy.serviceBlocker.isEmpty() ? "Operational" : policy.serviceBlocker;
            return new String[] {program, "Service " + progress, cause};
        }
        return new String[0];
    }
    private String serviceProgramDisplayName() {
        if ("earth_machine_service".equals(policy.serviceProgram)) return "Earth Machine Service";
        if ("mars_spares".equals(policy.serviceProgram)) return "Martian Spares Service";
        return policy.serviceProgram.isEmpty() ? "No program" : policy.serviceProgram;
    }
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
        if (getKind() == IndustrialMachineKind.CARGO_CONTROLLER
                && world.getTotalWorldTime() % 20 == 0) runPolicyTick();
        if (getKind() == IndustrialMachineKind.SERVICE_INTERFACE) updateServiceProgram();
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
        compound.setTag("IndustrialPolicy", policy.write());
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
        if (compound.hasKey("IndustrialPolicy", 10))
            policy.read(compound.getCompoundTag("IndustrialPolicy"));
        lastUser = compound.hasUniqueId("LastUser") ? compound.getUniqueId("LastUser") : null;
        for (int i = 0; i < inventory.size(); i++) {
            inventory.set(i, compound.hasKey("Slot" + i, 10)
                ? new ItemStack(compound.getCompoundTag("Slot" + i)) : ItemStack.EMPTY);
        }
        energy = Math.max(0D, energy);
        pendingOperationEU = Math.max(0D, pendingOperationEU);
    }

    @Override public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override public void onDataPacket(NetworkManager network, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    private void syncPolicyState() {
        markDirty();
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
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
                cargoChannel = ((String) arguments[0]).trim(); syncPolicyState(); return new Object[] {cargoChannel};
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
            case 22: return new Object[] {policy.facilityName, policy.facilityRole,
                environment(), pos.getX(), pos.getY(), pos.getZ()};
            case 23:
                requireControllerOrService();
                policy.facilityName = stringArgument(arguments, 0, "facility name");
                policy.facilityRole = stringArgument(arguments, 1, "facility role");
                syncPolicyState(); return new Object[] {true};
            case 24: return new Object[] {getEnergyStored(), getCapacity(),
                policy.emergencyThreshold(getCapacity()), policy.noncriticalLoadShed,
                getAcceptedEUThisTick()};
            case 25:
                requireCargoController();
                policy.configureReserve(stringArgument(arguments, 0, "resource ID"),
                    intArgument(arguments, 1, "minimum reserve", 0, 4096),
                    intArgument(arguments, 2, "preferred reserve", 0, 4096),
                    intArgument(arguments, 3, "priority", 0, 9));
                syncPolicyState(); return new Object[] {true};
            case 26: return new Object[] {policy.reserveItem, observedReserve(),
                policy.minimumReserve, policy.preferredReserve, policy.priority};
            case 27:
                requireCargoController();
                policy.configureEmergency(intArgument(arguments, 0, "EU reserve", 0, getCapacity()),
                    intArgument(arguments, 1, "reserve percent", 0, 100));
                syncPolicyState(); return new Object[] {policy.emergencyThreshold(getCapacity())};
            case 28:
                requireCargoController(); policy.enabled = booleanArgument(arguments, 0, "enabled");
                if (!policy.enabled) { policy.action = "idle"; policy.blocker = "policy disabled";
                    policy.noncriticalLoadShed = false; }
                syncPolicyState(); notifyNeighbors(); return new Object[] {policy.enabled};
            case 29:
                requireCargoController(); policy.manualOverride = booleanArgument(arguments, 0, "manual override");
                if (policy.manualOverride) { policy.action = "manual control";
                    policy.blocker = "automatic actions disabled by operator"; policy.noncriticalLoadShed = false; }
                syncPolicyState(); notifyNeighbors(); return new Object[] {policy.manualOverride};
            case 30: return new Object[] {policy.action};
            case 31: return new Object[] {policy.blocker};
            case 32:
                requireCargoController();
                String destination = stringArgument(arguments, 0, "destination facility");
                String item = stringArgument(arguments, 1, "resource ID");
                int amount = intArgument(arguments, 2, "item count", 1, 4096);
                policy.requestManifest(destination, item, amount, world.getTotalWorldTime());
                syncPolicyState(); return new Object[] {true};
            case 33: return manifestStatus();
            case 34:
                requireCargoController();
                String sideName = stringArgument(arguments, 0, "redstone side").toLowerCase();
                if (EnumFacing.byName(sideName) == null) throw new LuaException("Unknown side: " + sideName);
                policy.loadSide = sideName; policy.noncriticalLoadRegistered = true;
                syncPolicyState(); notifyNeighbors(); return new Object[] {true};
            case 35: return new Object[] {policy.noncriticalLoadRegistered,
                policy.noncriticalLoadShed, policy.loadSide};
            case 36:
                requireServiceInterface(); configureServiceProgram(
                    stringArgument(arguments, 0, "service program"));
                return new Object[] {policy.serviceProgram, policy.servicePhase};
            case 37: return new Object[] {policy.serviceProgram, policy.servicePhase,
                policy.commissioningTicks, 200, policy.serviceCommissioned, policy.serviceBlocker};
            case 38:
                requireServiceInterface();
                if (!"constructed".equals(policy.servicePhase))
                    throw new LuaException("Construction bill has not been accepted");
                policy.servicePhase = "commissioning"; policy.serviceBlocker = "sustained EU and AI authorization required";
                syncPolicyState(); return new Object[] {true};
            case 39: return routeStatus();
            default: throw new LuaException("Unknown method");
        }
    }
    @Override public void attach(IComputerAccess computer) {}
    @Override public void detach(IComputerAccess computer) {}
    @Override public Object getTarget() { return this; }
    @Override public boolean equals(IPeripheral other) { return other == this; }

    public int policyRedstonePower(EnumFacing side) {
        return getKind() == IndustrialMachineKind.CARGO_CONTROLLER
            && policy.noncriticalLoadRegistered && policy.noncriticalLoadShed
            && policy.loadSide.equals(side.getName()) ? 15 : 0;
    }

    boolean isCommissionedService(String program) {
        return getKind() == IndustrialMachineKind.SERVICE_INTERFACE
            && policy.serviceCommissioned && program.equals(policy.serviceProgram)
            && energy >= getKind().voltage && inventory.get(2).getItem() == IndustrialCivilizationCore.AI_CORE;
    }

    boolean consumeServiceEU(int amount) {
        if (amount <= 0 || energy < amount) return false;
        energy -= amount; markDirty(); return true;
    }

    private void requireCargoController() throws LuaException {
        if (getKind() != IndustrialMachineKind.CARGO_CONTROLLER)
            throw new LuaException("Not an Interplanetary Cargo Controller");
    }

    private void requireServiceInterface() throws LuaException {
        if (getKind() != IndustrialMachineKind.SERVICE_INTERFACE)
            throw new LuaException("Not a Civilization Service Interface");
    }

    private void requireControllerOrService() throws LuaException {
        if (getKind() != IndustrialMachineKind.CARGO_CONTROLLER
                && getKind() != IndustrialMachineKind.SERVICE_INTERFACE)
            throw new LuaException("Facility identity is available on cargo and service controllers");
    }

    private static String stringArgument(Object[] arguments, int index, String label) throws LuaException {
        if (arguments.length <= index || !(arguments[index] instanceof String))
            throw new LuaException("Expected " + label);
        String value = ((String) arguments[index]).trim();
        if (value.isEmpty()) throw new LuaException(label + " may not be empty");
        return value;
    }

    private static int intArgument(Object[] arguments, int index, String label, int min, int max)
            throws LuaException {
        if (arguments.length <= index || !(arguments[index] instanceof Double))
            throw new LuaException("Expected numeric " + label);
        return Math.max(min, Math.min(max, ((Double) arguments[index]).intValue()));
    }

    private static boolean booleanArgument(Object[] arguments, int index, String label)
            throws LuaException {
        if (arguments.length <= index || !(arguments[index] instanceof Boolean))
            throw new LuaException("Expected boolean " + label);
        return (Boolean) arguments[index];
    }

    private void notifyNeighbors() {
        if (world != null) world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    private Object[] manifestStatus() {
        return new Object[] {policy.manifestStatus, policy.facilityName,
            policy.manifestDestination, policy.manifestItem, policy.manifestRequested,
            policy.manifestDelivered, policy.manifestCreatedTick,
            policy.manifestLastTransferTick, policy.manifestFailure};
    }

    private Object[] routeStatus() {
        int peers = routePeerCount();
        String state = cargoChannel.isEmpty() ? "unregistered"
            : peers == 0 ? "no loaded peer" : "available";
        return new Object[] {cargoChannel, state, peers, 1, 100,
            policy.manifestLastTransferTick, policy.manifestFailure};
    }

    private int routePeerCount() {
        int peers = 0;
        for (TileIndustrialMachine controller : LOADED_CARGO_CONTROLLERS)
            if (controller != this && !controller.isInvalid()
                    && !cargoChannel.isEmpty() && cargoChannel.equals(controller.cargoChannel)) peers++;
        return peers;
    }

    private TileIndustrialMachine higherPriorityShortage() {
        if (cargoChannel.isEmpty() || policy.reserveItem.isEmpty()) return null;
        TileIndustrialMachine result = null;
        for (TileIndustrialMachine candidate : LOADED_CARGO_CONTROLLERS) {
            if (candidate == this || candidate.isInvalid()
                    || !cargoChannel.equals(candidate.cargoChannel)
                    || !candidate.policy.outranks(policy)
                    || !candidate.policy.belowMinimum(candidate.observedReserve())) continue;
            if (result == null || candidate.policy.priority > result.policy.priority) result = candidate;
        }
        return result;
    }

    private int observedReserve() {
        return observedResource(policy.reserveItem);
    }

    private int observedResource(String resource) {
        if (resource == null || resource.isEmpty()) return 0;
        int count = countResource(this, resource);
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity tile = world == null ? null : world.getTileEntity(pos.offset(facing));
            if (tile instanceof IInventory) count += countResource((IInventory) tile, resource);
        }
        return count;
    }

    private static int countResource(IInventory inventory, String resource) {
        int count = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem().getRegistryName() != null
                    && resource.equals(stack.getItem().getRegistryName().toString())) count += stack.getCount();
        }
        return count;
    }

    private void runPolicyTick() {
        int reserve = observedReserve();
        policy.observedReserve = reserve;
        policy.routePeers = routePeerCount();
        int emergency = policy.emergencyThreshold(getCapacity());
        boolean threatened = emergency > 0 && energy < emergency;
        boolean shouldShed = policy.enabled && !policy.manualOverride
            && policy.noncriticalLoadRegistered && threatened;
        if (shouldShed != policy.noncriticalLoadShed) {
            policy.noncriticalLoadShed = shouldShed;
            if (shouldShed) policy.loadShedObserved = true;
            notifyNeighbors(); syncPolicyState();
        }
        if (!policy.enabled) { policy.action = "idle"; policy.blocker = "policy disabled";
            syncPolicyState(); return; }
        if (policy.manualOverride) { policy.action = "manual control";
            policy.blocker = "automatic actions disabled by operator"; syncPolicyState(); return; }
        if (threatened) {
            policy.action = policy.noncriticalLoadShed ? "noncritical load shed" : "critical reserve alert";
            policy.blocker = "stored EU below protected emergency reserve";
        }
        if (!policy.belowMinimum(reserve)) {
            if (policy.recovered(reserve) && !threatened) {
                policy.action = "reserve satisfied"; policy.blocker = "";
                if (policy.shortageObserved && (policy.productionRequested || policy.freightRequested)) {
                    EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
                    if (player != null) {
                        RuntimeAdvancements.grant(player, "predictive_production");
                        RuntimeAdvancements.grant(player, "ai_industrial_policy");
                    }
                }
            }
            syncPolicyState();
            return;
        }
        policy.shortageObserved = true;
        TileIndustrialMachine higherPriority = higherPriorityShortage();
        if (higherPriority != null) {
            policy.action = "deferred to higher priority";
            policy.blocker = "higher-priority facility " + higherPriority.policy.facilityName
                + " is below minimum reserve";
            syncPolicyState();
            return;
        }
        if (!policy.canAct(world.getTotalWorldTime())) { syncPolicyState(); return; }
        if (requestProduction()) {
            policy.productionRequested = true;
            policy.acted(world.getTotalWorldTime(), "production requested",
                "waiting for connected manufacturing to complete");
            syncPolicyState(); return;
        }
        if (requestFreightFromPeer()) {
            policy.freightRequested = true;
            policy.acted(world.getTotalWorldTime(), "freight requested",
                "waiting for registered route throughput");
            syncPolicyState(); return;
        }
        policy.acted(world.getTotalWorldTime(), "shortage unresolved",
            cargoChannel.isEmpty() ? "no production recipe and no registered freight route"
                : "local production unavailable and source minimum reserves protected");
        syncPolicyState();
    }

    private boolean requestProduction() {
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity candidate = world.getTileEntity(pos.offset(facing));
            if (!(candidate instanceof TileIndustrialMachine)) continue;
            TileIndustrialMachine machine = (TileIndustrialMachine) candidate;
            for (MachineRecipe recipe : MachineRecipe.all()) {
                if (recipe.machine != machine.getKind() || recipe.output.isEmpty()
                        || recipe.output.getItem().getRegistryName() == null
                        || !policy.reserveItem.equals(recipe.output.getItem().getRegistryName().toString())) continue;
                machine.selectedRecipe = recipe.id;
                machine.queuedOperations = Math.min(64,
                    Math.max(1, policy.preferredReserve - observedReserve()));
                machine.markDirty();
                return true;
            }
        }
        return false;
    }

    private boolean requestFreightFromPeer() {
        if (cargoChannel.isEmpty()) return false;
        int needed = Math.max(1, policy.preferredReserve - observedReserve());
        for (TileIndustrialMachine source : LOADED_CARGO_CONTROLLERS) {
            if (source == this || source.isInvalid() || !cargoChannel.equals(source.cargoChannel)) continue;
            ItemStack stack = source.inventory.get(0);
            if (stack.isEmpty() || stack.getItem().getRegistryName() == null
                    || !policy.reserveItem.equals(stack.getItem().getRegistryName().toString())) continue;
            int protectedMinimum = policy.reserveItem.equals(source.policy.reserveItem)
                ? source.policy.minimumReserve : 0;
            if (source.observedResource(policy.reserveItem) <= protectedMinimum) continue;
            source.policy.requestManifest(policy.facilityName, policy.reserveItem, needed,
                world.getTotalWorldTime());
            source.syncPolicyState();
            return source.transferCargo();
        }
        return false;
    }

    private void configureServiceProgram(String requested) throws LuaException {
        String normalized = requested.toLowerCase(java.util.Locale.ROOT);
        String expected = "mars".equals(environment()) ? "mars_spares" : "earth_machine_service";
        if (!expected.equals(normalized))
            throw new LuaException("This environment supports " + expected);
        policy.serviceProgram = normalized;
        policy.servicePhase = "construction";
        policy.serviceCommissioned = false;
        policy.commissioningTicks = 0;
        policy.serviceBlocker = "insert the three-slot construction bill";
        syncPolicyState();
    }

    private void updateServiceProgram() {
        if (policy.serviceProgram.isEmpty()) return;
        if ("construction".equals(policy.servicePhase) && consumeServiceBill()) {
            policy.servicePhase = "constructed";
            policy.serviceBlocker = "start commissioning from ComputerCraft";
            syncPolicyState();
        }
        if ("commissioning".equals(policy.servicePhase)) {
            String blocker = commissioningBlocker();
            if (!blocker.isEmpty()) {
                policy.serviceBlocker = blocker;
                policy.commissioningTicks = Math.max(0, policy.commissioningTicks - 1);
            } else if (energy >= getKind().voltage) {
                energy -= getKind().voltage;
                policy.commissioningTicks++;
                policy.serviceBlocker = "";
                if (policy.commissioningTicks >= 200) commissionServiceProgram();
            } else {
                policy.serviceBlocker = "insufficient sustained EU";
                policy.commissioningTicks = Math.max(0, policy.commissioningTicks - 1);
            }
            if ((world.getTotalWorldTime() & 15) == 0) syncPolicyState();
        }
        if (policy.serviceCommissioned) {
            refreshOperatingServiceState();
            if ("mars_spares".equals(policy.serviceProgram)
                    && "operating".equals(policy.servicePhase)
                    && world.getTotalWorldTime() % 1200 == 0) produceMartianSpares();
        }
    }

    private boolean consumeServiceBill() {
        ItemStack first = inventory.get(0), second = inventory.get(1), third = inventory.get(2);
        boolean earth = "earth_machine_service".equals(policy.serviceProgram);
        if (!matches(first, IndustrialCivilizationCore.PRECISION_FRAME, earth ? 16 : 12)
                || !matches(second, IndustrialCivilizationCore.CONTROL_PROCESSOR, 8)) return false;
        String thirdId = earth ? "minecraft:iron_ingot" : "galacticraftcore:meteoric_iron_raw";
        if (third.isEmpty() || third.getItem().getRegistryName() == null
                || !thirdId.equals(third.getItem().getRegistryName().toString())
                || third.getCount() < (earth ? 32 : 16)) return false;
        first.shrink(earth ? 16 : 12); second.shrink(8); third.shrink(earth ? 32 : 16);
        return true;
    }

    private static boolean matches(ItemStack stack, net.minecraft.item.Item item, int count) {
        return !stack.isEmpty() && stack.getItem() == item && stack.getCount() >= count;
    }

    private String commissioningBlocker() {
        if (inventory.get(2).getItem() != IndustrialCivilizationCore.AI_CORE)
            return "AI Core authorization missing from input slot three";
        TileIndustrialMachine cargo = null;
        for (EnumFacing facing : EnumFacing.values())
            if (world.getTileEntity(pos.offset(facing)) instanceof TileIndustrialMachine
                    && ((TileIndustrialMachine) world.getTileEntity(pos.offset(facing))).getKind()
                        == IndustrialMachineKind.CARGO_CONTROLLER)
                cargo = (TileIndustrialMachine) world.getTileEntity(pos.offset(facing));
        if (cargo == null) return "no adjacent Cargo Controller";
        if (cargo.cargoChannel.isEmpty()) return "adjacent Cargo Controller route is unregistered";
        if (cargo.routePeerCount() == 0) return "adjacent cargo route has no loaded peer";
        if ("earth_machine_service".equals(policy.serviceProgram)
                && !SettlementEconomySystem.hasTierThreeSettlement(world, pos, 64D))
            return "no tier-three settlement within service range";
        return "";
    }

    private void refreshOperatingServiceState() {
        String blocker = operatingServiceBlocker();
        String phase = blocker.isEmpty() ? "operating" : "suspended";
        if (!phase.equals(policy.servicePhase) || !blocker.equals(policy.serviceBlocker)) {
            policy.servicePhase = phase;
            policy.serviceBlocker = blocker;
            syncPolicyState();
        }
    }

    private String operatingServiceBlocker() {
        if (inventory.get(2).getItem() != IndustrialCivilizationCore.AI_CORE)
            return "AI Core authorization removed";
        if ("earth_machine_service".equals(policy.serviceProgram)) {
            if (energy < getKind().voltage) return "operating EU reserve below 512 EU";
            return "";
        }
        if (energy < 4096) return "fewer than 4,096 EU available for next spares cycle";
        ItemStack iron = inventory.get(0), redstone = inventory.get(1), output = inventory.get(OUTPUT_SLOT);
        if (iron.getItem() != net.minecraft.init.Items.IRON_INGOT || iron.getCount() < 2)
            return "local iron reserve below two ingots";
        if (redstone.getItem() != net.minecraft.init.Items.REDSTONE || redstone.getCount() < 1)
            return "local redstone reserve empty";
        if (!output.isEmpty() && (output.getItem() != IndustrialCivilizationCore.PRECISION_FRAME
                || output.getCount() >= output.getMaxStackSize()))
            return "Precision Frame output congested";
        return "";
    }

    private void commissionServiceProgram() {
        policy.serviceCommissioned = true;
        policy.servicePhase = "operating";
        policy.serviceBlocker = "";
        buildServicePad();
        if ("earth_machine_service".equals(policy.serviceProgram))
            SettlementEconomySystem.commissionMachineService(world, pos, 64D);
        EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
        if (player != null) RuntimeAdvancements.grant(player, "autonomous_colony_expansion");
        if (player != null) RuntimeAdvancements.grant(player,
            "earth_machine_service".equals(policy.serviceProgram)
                ? "earth_civilization_service" : "martian_service_program");
        syncPolicyState();
    }

    private void buildServicePad() {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos floor = pos.add(x, -1, z);
            world.setBlockState(floor, IndustrialCivilizationCore.INDUSTRIAL_FLOOR.getDefaultState(), 3);
        }
        for (int x : new int[] {-2, 2}) for (int z : new int[] {-2, 2})
            for (int y = 0; y <= 2; y++)
                if (world.isAirBlock(pos.add(x, y, z))) world.setBlockState(pos.add(x, y, z),
                    IndustrialCivilizationCore.STEEL_FRAME.getDefaultState(), 3);
    }

    private void produceMartianSpares() {
        if (!isCommissionedService("mars_spares") || energy < 4096) return;
        ItemStack iron = inventory.get(0), redstone = inventory.get(1), output = inventory.get(OUTPUT_SLOT);
        if (iron.getItem() != net.minecraft.init.Items.IRON_INGOT || iron.getCount() < 2
                || redstone.getItem() != net.minecraft.init.Items.REDSTONE || redstone.getCount() < 1) return;
        if (!output.isEmpty() && (output.getItem() != IndustrialCivilizationCore.PRECISION_FRAME
                || output.getCount() >= output.getMaxStackSize())) return;
        iron.shrink(2); redstone.shrink(1); energy -= 4096;
        if (output.isEmpty()) inventory.set(OUTPUT_SLOT, new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME));
        else output.grow(1);
        completedOperations++; syncPolicyState();
    }

    private boolean transferCargo() {
        if (!policy.manifestDestination.isEmpty() && "delivered".equals(policy.manifestStatus))
            return false;
        if (getKind() != IndustrialMachineKind.CARGO_CONTROLLER || cargoChannel.isEmpty()
                || energy < getKind().voltage || inventory.get(0).isEmpty()) {
            policy.failManifest(cargoChannel.isEmpty() ? "route channel not registered"
                : energy < getKind().voltage ? "insufficient controller EU"
                : "source inventory empty");
            syncPolicyState();
            return false;
        }
        ItemStack sourceStack = inventory.get(0);
        String sourceId = sourceStack.getItem().getRegistryName() == null ? ""
            : sourceStack.getItem().getRegistryName().toString();
        if (!policy.manifestItem.isEmpty() && !policy.manifestItem.equals(sourceId)) {
            policy.failManifest("source item does not match active manifest");
            syncPolicyState();
            return false;
        }
        int protectedMinimum = sourceId.equals(policy.reserveItem) ? policy.minimumReserve : 0;
        if (!nationManaged && observedResource(sourceId) <= protectedMinimum) {
            policy.failManifest("source minimum reserve protected");
            syncPolicyState();
            return false;
        }
        String failure = "no loaded destination on registered channel";
        for (TileIndustrialMachine target : LOADED_CARGO_CONTROLLERS) {
            if (target == this || target.isInvalid() || !cargoChannel.equals(target.cargoChannel)
                    || (target.world == world && !(nationManaged && target.nationManaged))) continue;
            if (!policy.manifestDestination.isEmpty()
                    && !policy.manifestDestination.equals(target.policy.facilityName)) continue;
            ItemStack destination = target.inventory.get(OUTPUT_SLOT);
            ItemStack source = inventory.get(0);
            if (!destination.isEmpty() && (!ItemStack.areItemsEqual(source, destination)
                    || destination.getCount() >= destination.getMaxStackSize())) {
                failure = "destination output congested";
                continue;
            }
            ItemStack moved = source.splitStack(1);
            if (destination.isEmpty()) target.inventory.set(OUTPUT_SLOT, moved);
            else destination.grow(1);
            energy -= getKind().voltage;
            completedOperations++;
            if (!"none".equals(policy.manifestStatus))
                policy.recordDelivery(world.getTotalWorldTime());
            target.policy.manifestLastTransferTick = world.getTotalWorldTime();
            target.policy.manifestFailure = "";
            EntityPlayerMP player = RuntimeAdvancements.playerFor(this, lastUser);
            if (player != null) {
                RuntimeAdvancements.grant(player, "cross_planetary_logistics");
                if (!policy.manifestDestination.isEmpty())
                    RuntimeAdvancements.grant(player, "ai_factory_coordination");
                if (!policy.manifestDestination.isEmpty())
                    RuntimeAdvancements.grant(player, "observable_industrial_network");
            }
            EntityPlayer nearest = world.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D, 64D, false);
            if (nationManaged && target.nationManaged && nearest != null)
                RuntimeAdvancements.grant(nearest, "nation_trade_network");
            policy.routePeers = routePeerCount();
            target.policy.routePeers = target.routePeerCount();
            syncPolicyState(); target.syncPolicyState();
            return true;
        }
        policy.failManifest(failure);
        syncPolicyState();
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

    void setEnergyForTest(double value) {
        energy = Math.max(0D, Math.min(getCapacity(), value));
        markDirty();
    }

    void configureFacilityForTest(String name, String role) {
        policy.facilityName = name; policy.facilityRole = role; markDirty();
    }

    void configurePolicyForTest(String resource, int minimum, int preferred,
            int emergency, String shedSide) {
        configurePolicyForTest(resource, minimum, preferred, emergency, shedSide, 5);
    }

    void configurePolicyForTest(String resource, int minimum, int preferred,
            int emergency, String shedSide, int priority) {
        policy.configureReserve(resource, minimum, preferred, priority);
        policy.configureEmergency(emergency, 0);
        policy.enabled = true;
        policy.noncriticalLoadRegistered = true;
        policy.loadSide = shedSide;
        markDirty();
    }

    void requestManifestForTest(String destination, String resource, int count) {
        policy.requestManifest(destination, resource, count,
            world == null ? 0L : world.getTotalWorldTime());
        markDirty();
    }

    void runPolicyForTest() { runPolicyTick(); }
    String[] policySnapshotForTest() {
        return new String[] {policy.action, policy.blocker, policy.manifestStatus,
            policy.manifestFailure, Integer.toString(policy.manifestDelivered),
            Boolean.toString(policy.noncriticalLoadShed), policy.servicePhase,
            policy.serviceBlocker};
    }

    boolean policyRoundTripForTest() {
        NBTTagCompound tag = writeToNBT(new NBTTagCompound());
        TileIndustrialMachine restored = new TileIndustrialMachine();
        restored.readFromNBT(tag);
        return policy.facilityName.equals(restored.policy.facilityName)
            && policy.facilityRole.equals(restored.policy.facilityRole)
            && policy.reserveItem.equals(restored.policy.reserveItem)
            && policy.minimumReserve == restored.policy.minimumReserve
            && policy.preferredReserve == restored.policy.preferredReserve
            && policy.enabled == restored.policy.enabled
            && policy.manualOverride == restored.policy.manualOverride
            && policy.noncriticalLoadShed == restored.policy.noncriticalLoadShed
            && policy.manifestStatus.equals(restored.policy.manifestStatus)
            && policy.manifestDelivered == restored.policy.manifestDelivered
            && policy.serviceProgram.equals(restored.policy.serviceProgram)
            && policy.servicePhase.equals(restored.policy.servicePhase)
            && policy.serviceCommissioned == restored.policy.serviceCommissioned;
    }

    boolean configureServiceForTest(String program) {
        try { configureServiceProgram(program); return true; }
        catch (LuaException exception) { return false; }
    }

    boolean startCommissioningForTest() {
        if (!"constructed".equals(policy.servicePhase)) return false;
        policy.servicePhase = "commissioning"; markDirty(); return true;
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
