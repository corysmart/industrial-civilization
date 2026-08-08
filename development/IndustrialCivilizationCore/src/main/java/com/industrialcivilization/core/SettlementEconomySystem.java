package com.industrialcivilization.core;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Deterministic, material-backed settlement production and construction. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class SettlementEconomySystem extends WorldSavedData {
    private static final String DATA_NAME = "industrial_civilization_settlements_v1";
    private static final long CYCLE_TICKS = 1200L;
    private final Map<String, Settlement> settlements = new HashMap<>();

    public SettlementEconomySystem() { super(DATA_NAME); }
    public SettlementEconomySystem(String name) { super(name); }

    public static void register(World world, BlockPos origin, String kind, String specialty, int tier) {
        if (world.isRemote) return;
        SettlementEconomySystem data = get(world);
        String id = id(origin);
        if (!data.settlements.containsKey(id)) {
            Settlement settlement = new Settlement(origin, kind, specialty, tier);
            settlement.food = tier == 0 ? 24 : 64;
            settlement.wood = tier == 0 ? 24 : 48;
            settlement.stone = tier * 48;
            settlement.iron = tier * 12;
            settlement.lastCycle = world.getTotalWorldTime();
            data.settlements.put(id, settlement);
            data.markDirty();
        }
    }

    /** Completed IC Credit trades add real circulation to the nearest settlement ledger. */
    public static void recordTrade(EntityPlayer player, int creditDelta) {
        if (player.world.isRemote) return;
        Settlement nearest = get(player.world).nearest(player.getPosition(), 64.0D);
        if (nearest == null) return;
        nearest.credits += Math.max(1, Math.abs(creditDelta));
        get(player.world).markDirty();
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || event.world.getTotalWorldTime() % CYCLE_TICKS != 0) return;
        SettlementEconomySystem data = get(event.world);
        boolean changed = false;
        for (Settlement settlement : data.settlements.values()) {
            if (!event.world.isAreaLoaded(settlement.origin.add(-16, -4, -16),
                    settlement.origin.add(30, 16, 30))) continue;
            long cycles = Math.max(1L, Math.min(10L,
                (event.world.getTotalWorldTime() - settlement.lastCycle) / CYCLE_TICKS));
            settlement.lastCycle = event.world.getTotalWorldTime();
            absorbPhysicalStock(event.world, settlement);
            produce(settlement, cycles);
            if (canUpgrade(settlement)) {
                payUpgrade(settlement);
                settlement.tier++;
                CivilizationWorldGenerator.applySettlementUpgrade(event.world,
                    settlement.origin, settlement.tier);
            }
            changed = true;
        }
        if (changed) data.markDirty();
    }

    private static void produce(Settlement s, long cycles) {
        // Output is formula-driven. Higher products require accumulated inputs;
        // there is no upgrade roll or random free material injection.
        s.food += (2L + s.tier) * cycles;
        s.wood += (s.tier == 0 ? 1L : 2L) * cycles;
        for (long i = 0; i < cycles; i++) {
            if (s.wood > 0 && s.food > 0) {
                s.wood--; s.food--; s.stone += 2 + s.tier;
            }
            if (s.tier >= 1 && s.iron > 0 && s.fuel > 0) {
                s.iron--; s.fuel--; s.circuits += 1;
            }
            if (s.tier >= 2 && s.stone >= 4 && s.fuel > 0) {
                s.stone -= 4; s.fuel--; s.iron += 2;
            }
        }
    }

    private static void absorbPhysicalStock(World world, Settlement s) {
        int budget = 16; // bounded per cycle to prevent hopper-sized tick spikes
        for (TileEntity tile : world.loadedTileEntityList) {
            if (budget <= 0 || !(tile instanceof IInventory)
                    || tile.getPos().distanceSq(s.origin) > 24.0D * 24.0D) continue;
            IInventory inventory = (IInventory) tile;
            for (int slot = 0; slot < inventory.getSizeInventory() && budget > 0; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                Material material = classify(stack);
                if (material == null) continue;
                int moved = Math.min(stack.getCount(), Math.min(4, budget));
                inventory.decrStackSize(slot, moved);
                s.add(material, moved);
                budget -= moved;
            }
            tile.markDirty();
        }
    }

    private static Material classify(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.getItem() == IndustrialCivilizationCore.INDUSTRIAL_CREDIT) return Material.CREDITS;
        if (stack.getItem() == Items.IRON_INGOT) return Material.IRON;
        if (stack.getItem() == Items.COAL || stack.getItem() == Items.BLAZE_POWDER) return Material.FUEL;
        if (stack.getItem() == Items.BREAD || stack.getItem() == Items.WHEAT
                || stack.getItem() == Items.POTATO || stack.getItem() == Items.CARROT) return Material.FOOD;
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.PLANKS) return Material.WOOD;
        if (block == Blocks.COBBLESTONE || block == Blocks.STONE || block == Blocks.STONEBRICK) return Material.STONE;
        if (stack.getItem().getRegistryName() != null) {
            String id = stack.getItem().getRegistryName().toString();
            if (id.contains("circuit") || id.contains("electronic")) return Material.CIRCUITS;
        }
        return null;
    }

    private static boolean canUpgrade(Settlement s) {
        if (!"primitive".equals(s.kind) || s.tier >= 3) return false;
        if (s.tier == 0) return s.wood >= 64 && s.stone >= 32 && s.food >= 32;
        if (s.tier == 1) return s.wood >= 128 && s.stone >= 192 && s.iron >= 48 && s.food >= 64;
        return s.stone >= 384 && s.iron >= 192 && s.circuits >= 32
            && s.fuel >= 64 && s.credits >= 128;
    }

    private static void payUpgrade(Settlement s) {
        if (s.tier == 0) { s.wood -= 64; s.stone -= 32; s.food -= 32; }
        else if (s.tier == 1) { s.wood -= 128; s.stone -= 192; s.iron -= 48; s.food -= 64; }
        else { s.stone -= 384; s.iron -= 192; s.circuits -= 32; s.fuel -= 64; s.credits -= 128; }
    }

    private Settlement nearest(BlockPos pos, double radius) {
        Settlement result = null; double best = radius * radius;
        for (Settlement candidate : settlements.values()) {
            double distance = candidate.origin.distanceSq(pos);
            if (distance <= best) { best = distance; result = candidate; }
        }
        return result;
    }

    private static SettlementEconomySystem get(World world) {
        SettlementEconomySystem data = (SettlementEconomySystem) world.getPerWorldStorage()
            .getOrLoadData(SettlementEconomySystem.class, DATA_NAME);
        if (data == null) {
            data = new SettlementEconomySystem();
            world.getPerWorldStorage().setData(DATA_NAME, data);
        }
        return data;
    }

    private static String id(BlockPos pos) { return pos.getX() + ":" + pos.getY() + ":" + pos.getZ(); }

    @Override public void readFromNBT(NBTTagCompound tag) {
        settlements.clear();
        NBTTagList list = tag.getTagList("Settlements", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            Settlement s = new Settlement(list.getCompoundTagAt(i));
            settlements.put(id(s.origin), s);
        }
    }

    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Settlement settlement : settlements.values()) list.appendTag(settlement.write());
        tag.setTag("Settlements", list);
        return tag;
    }

    private enum Material { WOOD, STONE, IRON, CIRCUITS, FUEL, FOOD, CREDITS }

    private static final class Settlement {
        final BlockPos origin; final String kind; final String specialty;
        int tier; long lastCycle, wood, stone, iron, circuits, fuel, food, credits;
        Settlement(BlockPos origin, String kind, String specialty, int tier) {
            this.origin = origin; this.kind = kind; this.specialty = specialty; this.tier = tier;
        }
        Settlement(NBTTagCompound n) {
            this(new BlockPos(n.getInteger("X"), n.getInteger("Y"), n.getInteger("Z")),
                n.getString("Kind"), n.getString("Specialty"), n.getInteger("Tier"));
            lastCycle=n.getLong("LastCycle"); wood=n.getLong("Wood"); stone=n.getLong("Stone");
            iron=n.getLong("Iron"); circuits=n.getLong("Circuits"); fuel=n.getLong("Fuel");
            food=n.getLong("Food"); credits=n.getLong("Credits");
        }
        void add(Material m, long amount) {
            switch(m) { case WOOD:wood+=amount;break; case STONE:stone+=amount;break;
                case IRON:iron+=amount;break; case CIRCUITS:circuits+=amount;break;
                case FUEL:fuel+=amount;break; case FOOD:food+=amount;break; case CREDITS:credits+=amount;break; }
        }
        NBTTagCompound write() {
            NBTTagCompound n=new NBTTagCompound(); n.setInteger("X",origin.getX()); n.setInteger("Y",origin.getY());
            n.setInteger("Z",origin.getZ()); n.setString("Kind",kind); n.setString("Specialty",specialty);
            n.setInteger("Tier",tier); n.setLong("LastCycle",lastCycle); n.setLong("Wood",wood);
            n.setLong("Stone",stone); n.setLong("Iron",iron); n.setLong("Circuits",circuits);
            n.setLong("Fuel",fuel); n.setLong("Food",food); n.setLong("Credits",credits); return n;
        }
    }

    private SettlementEconomySystem(String unused, boolean ignored) { super(DATA_NAME); }
}
