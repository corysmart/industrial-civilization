package com.industrialcivilization.core;

import buildcraft.builders.tile.TileQuarry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Sequencer for a real ProjectRed frame carriage. This tile never moves,
 * removes, places, powers, or chunk-loads another block. Instead it exposes
 * three directional redstone outputs and confirms every movement pulse by
 * observing that ProjectRed physically relocated this tile by one block.
 *
 * <p>Relative outputs: left = break, rear = frame movement, bottom = deploy.
 * The BuildCraft Quarry being controlled is immediately in front.</p>
 */
public final class TileMobileQuarryController extends TileEntity implements ITickable {
    static final int DEVICE_PULSE_TICKS = 4;
    static final int DEVICE_RETRY_TICKS = 20;
    static final int RECOVERY_TIMEOUT_TICKS = 400;
    // The pulse must end before the ProjectRed moving-row animation settles.
    // Otherwise this tile lands still in MOVE_HIGH and the rebuilt redstone
    // bus can immediately trigger a second, uncommanded frame step.
    static final int MOVE_PULSE_TICKS = 4;
    static final int MOVE_TIMEOUT_TICKS = 40;
    static final int MAX_MOVE_RETRIES = 4;

    enum Phase {
        DISARMED,
        MONITORING,
        BREAK_HIGH,
        BREAK_WAIT,
        RECOVERY_WAIT,
        MOVE_HIGH,
        MOVE_WAIT,
        MOVE_SETTLE,
        DEPLOY_HIGH,
        DEPLOY_WAIT,
        FAULT
    }

    private EnumFacing facing = EnumFacing.NORTH;
    private UUID ownerId;
    private boolean armed;
    private int relocations;
    private String status = "disarmed";
    private Phase phase = Phase.DISARMED;
    private int phaseTicks;
    private int movementSteps;
    private int movementRetries;
    private BlockPos pulseOrigin;
    private boolean relocationQuestPending;

    public void setFacing(EnumFacing value) {
        facing = value != null && value.getAxis().isHorizontal() ? value : EnumFacing.NORTH;
        markDirty();
    }

    public EnumFacing getFacing() { return facing; }
    public boolean isArmed() { return armed; }
    public int getRelocations() { return relocations; }
    public int getMovementSteps() { return movementSteps; }
    public String getStatus() { return status; }
    String getPhaseName() { return phase.name().toLowerCase(java.util.Locale.ROOT); }
    public BlockPos quarryPos() { return pos.offset(facing); }

    public int redstoneLevel(EnumFacing side) {
        if (!armed || side == null) return 0;
        if (phase == Phase.BREAK_HIGH
                && side == MobileQuarryRules.powerQuerySide(facing, MobileQuarryRules.Output.BREAK)) return 15;
        if (phase == Phase.MOVE_HIGH
                && side == MobileQuarryRules.powerQuerySide(facing, MobileQuarryRules.Output.MOVE)) return 15;
        if (phase == Phase.DEPLOY_HIGH
                && side == MobileQuarryRules.powerQuerySide(facing, MobileQuarryRules.Output.DEPLOY)) return 15;
        return 0;
    }

    public void interact(EntityPlayer player, boolean disarm) {
        if (disarm) {
            armed = false;
            setPhase(Phase.DISARMED, "disarmed");
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.mobile_quarry.disarmed"), false);
            return;
        }
        ownerId = player.getUniqueID();
        armed = true;
        movementSteps = 0;
        movementRetries = 0;
        pulseOrigin = null;
        setPhase(Phase.MONITORING, hasQuarry() ? "monitoring" : "missing_quarry");
        player.sendStatusMessage(statusMessage(), false);
    }

    private TextComponentTranslation statusMessage() {
        if ("monitoring".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.monitoring",
            facing.getName(), MobileQuarryRules.LANE_STEP, relocations);
        if ("missing_quarry".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.missing", facing.getName());
        if ("breaking".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.breaking");
        if ("recovering".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.recovering");
        if ("moving".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.moving",
            movementSteps, MobileQuarryRules.LANE_STEP);
        if ("deploying".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.deploying");
        if ("movement_stalled".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.stalled");
        if ("movement_invalid".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.invalid_move");
        if ("recovery_missing".equals(status)) return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.recovery_missing");
        return new TextComponentTranslation(
            "message.industrialcivilization.mobile_quarry.disarmed");
    }

    @Override
    public void update() {
        if (world == null || world.isRemote || !armed) return;
        if (relocationQuestPending && phaseTicks % 20 == 0) awardRelocationQuest();
        phaseTicks++;
        switch (phase) {
            case MONITORING:
                monitorQuarry();
                break;
            case BREAK_HIGH:
                if (phaseTicks >= DEVICE_PULSE_TICKS) setPhase(Phase.BREAK_WAIT, "breaking");
                break;
            case BREAK_WAIT:
                if (!hasQuarry()) setPhase(Phase.RECOVERY_WAIT, "recovering");
                else if (phaseTicks >= DEVICE_RETRY_TICKS) setPhase(Phase.BREAK_HIGH, "breaking");
                break;
            case RECOVERY_WAIT:
                if (hasRecoveredQuarry()) beginMovement();
                else if (phaseTicks >= RECOVERY_TIMEOUT_TICKS) {
                    setPhase(Phase.FAULT, "recovery_missing");
                }
                break;
            case MOVE_HIGH:
                if (phaseTicks >= MOVE_PULSE_TICKS) setPhase(Phase.MOVE_WAIT, "moving");
                break;
            case MOVE_WAIT:
                observeMovement();
                break;
            case MOVE_SETTLE:
                if (phaseTicks >= DEVICE_PULSE_TICKS) {
                    setPhase(movementSteps >= MobileQuarryRules.LANE_STEP
                        ? Phase.DEPLOY_HIGH : Phase.MOVE_HIGH,
                        movementSteps >= MobileQuarryRules.LANE_STEP ? "deploying" : "moving");
                }
                break;
            case DEPLOY_HIGH:
                if (phaseTicks >= DEVICE_PULSE_TICKS) setPhase(Phase.DEPLOY_WAIT, "deploying");
                break;
            case DEPLOY_WAIT:
                if (hasQuarry()) finishRelocation();
                else if (phaseTicks >= DEVICE_RETRY_TICKS) setPhase(Phase.DEPLOY_HIGH, "deploying");
                break;
            default:
                break;
        }
    }

    private void monitorQuarry() {
        TileEntity quarry = world.getTileEntity(quarryPos());
        if (!(quarry instanceof TileQuarry)) {
            setStatus("missing_quarry");
            return;
        }
        setStatus("monitoring");
        NBTTagCompound state = quarry.writeToNBT(new NBTTagCompound());
        if (MobileQuarryRules.isBuildCraftQuarryComplete(state)) {
            setPhase(Phase.BREAK_HIGH, "breaking");
        }
    }

    private void beginMovement() {
        movementSteps = 0;
        movementRetries = 0;
        pulseOrigin = pos;
        setPhase(Phase.MOVE_HIGH, "moving");
    }

    private void observeMovement() {
        if (pulseOrigin == null) {
            pulseOrigin = pos;
            markDirty();
        }
        if (!pos.equals(pulseOrigin)) {
            if (!MobileQuarryRules.isOneStepForward(pulseOrigin, pos, facing)) {
                setPhase(Phase.FAULT, "movement_invalid");
                return;
            }
            movementSteps++;
            movementRetries = 0;
            pulseOrigin = pos;
            // Keep the movement output low briefly between steps. Besides
            // producing a clean edge for ProjectRed, this gives clients one
            // settled frame before the next moving-row animation begins.
            setPhase(Phase.MOVE_SETTLE, "moving");
            return;
        }
        if (phaseTicks >= MOVE_TIMEOUT_TICKS) {
            movementRetries++;
            if (movementRetries >= MAX_MOVE_RETRIES) {
                setPhase(Phase.FAULT, "movement_stalled");
            } else {
                setPhase(Phase.MOVE_HIGH, "moving");
            }
        }
    }

    private void finishRelocation() {
        relocations++;
        movementSteps = 0;
        movementRetries = 0;
        pulseOrigin = null;
        relocationQuestPending = true;
        if (IndustrialCivilizationCore.TEST_BRIDGE_ENABLED) {
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|QUEST_TRIGGER|mobile_quarry_relocation|owner={}|pending={}"
                + "|controller={}|relocations={}", ownerId,
                relocationQuestPending, pos, relocations);
        }
        awardRelocationQuest();
        setPhase(Phase.MONITORING, "monitoring");
    }

    private void awardRelocationQuest() {
        if (!relocationQuestPending || world == null
                || world.getMinecraftServer() == null) return;
        if (ownerId == null) {
            if (IndustrialCivilizationCore.TEST_BRIDGE_ENABLED) {
                IndustrialCivilizationCore.LOGGER.error(
                    "IC_TEST|QUEST_AWARD|mobile_quarry_relocation|completed=false"
                    + "|reason=missing_owner|controller={}", pos);
            }
            return;
        }
        EntityPlayerMP owner = null;
        // Forge automation can register a FakePlayer with the owner's UUID.
        // PlayerList#getPlayerByUUID may return that entry first, and Forge
        // deliberately rejects advancement grants to FakePlayers. Select the
        // matching connected player explicitly.
        for (EntityPlayerMP candidate
                : world.getMinecraftServer().getPlayerList().getPlayers()) {
            if (ownerId.equals(candidate.getUniqueID()) && !(candidate instanceof FakePlayer)) {
                owner = candidate;
                break;
            }
        }
        if (owner == null) {
            if (IndustrialCivilizationCore.TEST_BRIDGE_ENABLED) {
                IndustrialCivilizationCore.LOGGER.error(
                    "IC_TEST|QUEST_AWARD|mobile_quarry_relocation|owner={}"
                    + "|completed=false|reason=owner_offline|controller={}", ownerId, pos);
            }
            return;
        }
        relocationQuestPending = !RuntimeAdvancements.grant(owner,
            "mobile_quarry_relocation",
            "same_quarry_redeployed_after_physical_frame_relocation");
        if (!relocationQuestPending) {
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|QUEST_AWARD|mobile_quarry_relocation|owner={}|completed=true",
                ownerId);
        }
        markDirty();
    }

    private boolean hasQuarry() {
        return world != null && world.getTileEntity(quarryPos()) instanceof TileQuarry;
    }

    private boolean hasRecoveredQuarry() {
        if (world == null) return false;
        TileEntity deployer = world.getTileEntity(quarryPos().down());
        if (!(deployer instanceof IInventory)) return false;
        net.minecraft.block.Block quarryBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("buildcraftbuilders", "quarry"));
        if (quarryBlock == null) return false;
        Item quarryItem = Item.getItemFromBlock(quarryBlock);
        IInventory inventory = (IInventory) deployer;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == quarryItem) return true;
        }
        return false;
    }

    private void setPhase(Phase value, String newStatus) {
        Phase previous = phase;
        phase = value;
        phaseTicks = 0;
        setStatus(newStatus);
        if (IndustrialCivilizationCore.TEST_BRIDGE_ENABLED && previous != value) {
            IndustrialCivilizationCore.LOGGER.info(
                "IC_TEST|CONTROLLER|pos={}|from={}|to={}|status={}|steps={}"
                + "|retries={}|relocations={}", pos,
                previous.name().toLowerCase(java.util.Locale.ROOT),
                value.name().toLowerCase(java.util.Locale.ROOT), status,
                movementSteps, movementRetries, relocations);
        }
        if (world != null && !world.isRemote) {
            world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
            // A directional strong-power output can energize an opaque relay
            // block. Notify around each relay as levers do so its downstream
            // machine observes both rising and falling edges.
            for (MobileQuarryRules.Output output : MobileQuarryRules.Output.values()) {
                world.notifyNeighborsOfStateChange(
                    pos.offset(MobileQuarryRules.outputSide(facing, output)),
                    getBlockType(), false);
            }
        }
        markDirty();
    }

    private void setStatus(String value) {
        if (!value.equals(status)) {
            status = value;
            markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("facing", facing.getName());
        compound.setBoolean("armed", armed);
        compound.setInteger("relocations", relocations);
        compound.setString("status", status);
        compound.setString("phase", phase.name());
        compound.setInteger("phaseTicks", phaseTicks);
        compound.setInteger("movementSteps", movementSteps);
        compound.setInteger("movementRetries", movementRetries);
        if (pulseOrigin != null) compound.setLong("pulseOrigin", pulseOrigin.toLong());
        if (ownerId != null) compound.setString("ownerId", ownerId.toString());
        compound.setBoolean("relocationQuestPending", relocationQuestPending);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        EnumFacing saved = EnumFacing.byName(compound.getString("facing"));
        facing = saved != null && saved.getAxis().isHorizontal() ? saved : EnumFacing.NORTH;
        armed = compound.getBoolean("armed");
        relocations = Math.max(0, compound.getInteger("relocations"));
        status = compound.hasKey("status", 8) ? compound.getString("status") : "disarmed";
        try {
            phase = compound.hasKey("phase", 8)
                ? Phase.valueOf(compound.getString("phase")) : Phase.DISARMED;
        } catch (IllegalArgumentException ignored) {
            phase = Phase.DISARMED;
            armed = false;
            status = "disarmed";
        }
        phaseTicks = Math.max(0, compound.getInteger("phaseTicks"));
        movementSteps = Math.max(0, compound.getInteger("movementSteps"));
        movementRetries = Math.max(0, compound.getInteger("movementRetries"));
        pulseOrigin = compound.hasKey("pulseOrigin", 4)
            ? BlockPos.fromLong(compound.getLong("pulseOrigin")) : null;
        ownerId = null;
        if (compound.hasKey("ownerId", 8)) {
            try {
                ownerId = UUID.fromString(compound.getString("ownerId"));
            } catch (IllegalArgumentException ignored) {
                ownerId = null;
            }
        } else if (compound.hasUniqueId("owner")) {
            ownerId = compound.getUniqueId("owner");
        }
        relocationQuestPending = compound.getBoolean("relocationQuestPending");
    }
}
