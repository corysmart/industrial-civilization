package com.industrialcivilization.core;

import ic2.core.block.crop.TileEntityCrop;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/** Runtime evidence for the renewable-crop, livestock and LV forestry side paths. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class AgriculturalSidePathSystem {
    private static final String OWNER = "IndustrialAgricultureOwner";
    private static final double FARM_RANGE_SQ = 24D * 24D;

    @SubscribeEvent
    public static void cropInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (!(tile instanceof TileEntityCrop)) return;
        EntityPlayer player = event.getEntityPlayer();
        evaluateCropEngineering(player, event.getPos(), 8);
        TileEntityCrop crop = (TileEntityCrop) tile;
        if (isMatureHemp(crop)) {
            ProgressionState.record(player, "hemp_manually_harvested");
            RuntimeAdvancements.grant(player, "breed_hemp");
        }
    }

    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!event.player.world.isRemote) recordCraft(event.player, event.crafting, event.craftMatrix);
    }

    @SubscribeEvent
    public static void leashInteraction(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote || !(event.getTarget() instanceof EntityAnimal)
                || event.getEntityPlayer().getHeldItem(event.getHand()).getItem() != Items.LEAD) return;
        ProgressionState.record(event.getEntityPlayer(), "controlled_livestock_leashed");
        checkLivestock(event.getEntityPlayer());
    }

    @SubscribeEvent
    public static void bred(BabyEntitySpawnEvent event) {
        EntityPlayer player = event.getCausedByPlayer();
        if (player == null || player.world.isRemote) return;
        ProgressionState.record(player, "controlled_livestock_bred");
        checkLivestock(player);
    }

    @SubscribeEvent
    public static void machinePlaced(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || event.getPlayer() == null) return;
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile != null && isAgriculturalMachine(tile)) markOwner(tile, event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote
                || event.world.getTotalWorldTime() % 40L != 0L) return;
        for (EntityPlayer player : event.world.playerEntities) if (player instanceof EntityPlayerMP) {
            evaluateForestry((EntityPlayerMP) player);
            evaluateAutomation((EntityPlayerMP) player);
        }
    }

    private static void recordCraft(EntityPlayer player, ItemStack output, IInventory inputs) {
        int hemp = 0;
        int strings = 0;
        boolean resin = false;
        for (int slot = 0; slot < inputs.getSizeInventory(); slot++) {
            ItemStack stack = inputs.getStackInSlot(slot);
            ResourceLocation id = stack.isEmpty() ? null : stack.getItem().getRegistryName();
            if (id == null) continue;
            if ("ic2:itemmisc".equals(id.toString()) && stack.getMetadata() == 159)
                hemp += stack.getCount();
            if (stack.getItem() == Items.STRING) strings += stack.getCount();
            if ("ic2:itemmisc".equals(id.toString()) && stack.getMetadata() != 159) resin = true;
        }
        if (output.getItem() == Items.STRING && hemp > 0
                && ProgressionState.has(player, "hemp_manually_harvested"))
            RuntimeAdvancements.grant(player, "renewable_string");
        if (output.getItem() == Items.LEAD && output.getCount() >= 2 && strings >= 4 && resin) {
            ProgressionState.record(player, "controlled_livestock_leads_crafted");
            checkLivestock(player);
        }
    }

    private static void checkLivestock(EntityPlayer player) {
        if (ProgressionState.has(player, "controlled_livestock_leads_crafted")
                && ProgressionState.has(player, "controlled_livestock_leashed")
                && ProgressionState.has(player, "controlled_livestock_bred"))
            RuntimeAdvancements.grant(player, "controlled_livestock");
    }

    private static boolean evaluateCropEngineering(EntityPlayer player, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-radius, -2, -radius),
                center.add(radius, 2, radius))) {
            TileEntity tile = player.world.getTileEntity(pos);
            if (!(tile instanceof TileEntityCrop) || !((TileEntityCrop) tile).isCrossingBase()) continue;
            int matureParents = 0;
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                TileEntity adjacent = player.world.getTileEntity(pos.offset(facing));
                if (adjacent instanceof TileEntityCrop && mature((TileEntityCrop) adjacent))
                    matureParents++;
            }
            if (matureParents >= 2) {
                RuntimeAdvancements.grant(player, "crop_engineering");
                return true;
            }
        }
        return false;
    }

    private static boolean mature(TileEntityCrop crop) {
        return crop.getCrop() != null && crop.getCurrentSize() >= crop.getCrop().getMaxSize();
    }

    private static boolean isMatureHemp(TileEntityCrop crop) {
        return mature(crop) && "Hemp".equalsIgnoreCase(crop.getCrop().getId());
    }

    private static void evaluateForestry(EntityPlayerMP player) {
        TileEntity sower = null, gatherer = null, furnace = null, generator = null;
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (tile.getPos().distanceSq(player.getPosition()) > FARM_RANGE_SQ) continue;
            String id = blockId(tile);
            if ("industrialforegoing:crop_sower".equals(id) && owned(tile, player)) sower = tile;
            else if ("industrialforegoing:crop_recolector".equals(id) && owned(tile, player)) gatherer = tile;
            else if ("industrialforegoing:resourceful_furnace".equals(id) && owned(tile, player)) furnace = tile;
            else if ("ic2:blockgenerator".equals(id) && containsCharcoal(tile)) generator = tile;
        }
        boolean planting = sower != null && powered(sower) && containsSapling(sower)
            && hasTreeMaterialNearby(player, sower.getPos());
        if (planting) RuntimeAdvancements.grant(player, "lv_tree_planting");
        if (planting && gatherer != null && powered(gatherer) && furnace != null
                && powered(furnace) && generator != null && containsCharcoal(furnace)
                && containsCharcoal(generator))
            RuntimeAdvancements.grant(player, "lv_charcoal_tree_farm");
    }

    /**
     * Proves that the late agriculture machines form a live district. The checks intentionally
     * combine owned powered machinery with physical work targets and produced material; merely
     * carrying or placing the blocks cannot satisfy any milestone.
     */
    private static void evaluateAutomation(EntityPlayerMP player) {
        TileEntity interactor = machine(player, "industrialforegoing:plant_interactor");
        TileEntity fertilizer = machine(player, "industrialforegoing:crop_enrich_material_injector");
        boolean field = live(interactor) && live(fertilizer)
            && hasFieldCrops(player, interactor.getPos()) && containsFertilizer(fertilizer)
            && adjacentContains(interactor, AgriculturalSidePathSystem::isCropOutput);
        if (field) RuntimeAdvancements.grant(player, "automated_field_agriculture");

        TileEntity breeder = machine(player, "industrialforegoing:animal_stock_increaser");
        TileEntity growth = machine(player, "industrialforegoing:animal_growth_increaser");
        TileEntity separator = machine(player, "industrialforegoing:animal_independence_selector");
        boolean husbandry = field && live(breeder) && live(growth) && live(separator)
            && containsBreedingFood(breeder) && hasControlledHerd(player, breeder.getPos());
        if (husbandry) RuntimeAdvancements.grant(player, "automated_animal_husbandry");

        TileEntity harvester = machine(player, "industrialforegoing:animal_resource_harvester");
        TileEntity collector = machine(player, "industrialforegoing:animal_byproduct_recolector");
        TileEntity composter = machine(player, "industrialforegoing:sewage_composter_solidifier");
        boolean resources = husbandry && live(harvester) && live(collector) && live(composter)
            && hasAdultAnimal(player, harvester.getPos())
            && adjacentContains(harvester, AgriculturalSidePathSystem::isRenewableAnimalProduct)
            && (containsFluid(collector, "sewage") || containsFluid(composter, "sewage"))
            && (containsFertilizer(composter)
                || adjacentContains(composter, AgriculturalSidePathSystem::isFertilizer));
        if (resources) RuntimeAdvancements.grant(player, "automated_animal_resources");

        TileEntity water = machine(player, "industrialforegoing:water_resources_collector");
        boolean waterResources = resources && live(water) && hasWaterArea(player, water.getPos())
            && adjacentContains(water, AgriculturalSidePathSystem::isAquaticOutput);
        if (waterResources) RuntimeAdvancements.grant(player, "automated_water_resources");
    }

    private static TileEntity machine(EntityPlayerMP player, String wanted) {
        for (TileEntity tile : player.world.loadedTileEntityList)
            if (tile.getPos().distanceSq(player.getPosition()) <= FARM_RANGE_SQ
                    && wanted.equals(blockId(tile)) && owned(tile, player)) return tile;
        return null;
    }

    private static boolean live(TileEntity tile) {
        return tile != null && powered(tile);
    }

    private static boolean hasFieldCrops(EntityPlayer player, BlockPos center) {
        int planted = 0;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-6, -2, -6), center.add(6, 3, 6))) {
            Block block = player.world.getBlockState(pos).getBlock();
            if (block instanceof net.minecraft.block.BlockCrops
                    || player.world.getTileEntity(pos) instanceof TileEntityCrop) planted++;
        }
        return planted >= 4;
    }

    private static boolean hasControlledHerd(EntityPlayer player, BlockPos center) {
        int adults = 0, young = 0;
        for (EntityAnimal animal : player.world.getEntitiesWithinAABB(EntityAnimal.class,
                new net.minecraft.util.math.AxisAlignedBB(center).grow(8, 4, 8))) {
            if (animal.isChild()) young++; else adults++;
        }
        return adults >= 2 && young >= 1;
    }

    private static boolean hasAdultAnimal(EntityPlayer player, BlockPos center) {
        for (EntityAnimal animal : player.world.getEntitiesWithinAABB(EntityAnimal.class,
                new net.minecraft.util.math.AxisAlignedBB(center).grow(8, 4, 8)))
            if (!animal.isChild()) return true;
        return false;
    }

    private static boolean hasWaterArea(EntityPlayer player, BlockPos center) {
        int water = 0;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-6, -2, -6), center.add(6, 2, 6)))
            if (player.world.getBlockState(pos).getMaterial()
                    == net.minecraft.block.material.Material.WATER) water++;
        return water >= 9;
    }

    private static boolean adjacentContains(TileEntity tile,
            java.util.function.Predicate<ItemStack> test) {
        for (EnumFacing side : EnumFacing.HORIZONTALS) {
            TileEntity adjacent = tile.getWorld().getTileEntity(tile.getPos().offset(side));
            if (adjacent != null && contains(adjacent, test)) return true;
        }
        return false;
    }

    private static boolean containsFluid(TileEntity tile, String fluidName) {
        for (EnumFacing side : EnumFacing.values()) {
            IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if (handler == null) continue;
            for (net.minecraftforge.fluids.capability.IFluidTankProperties tank : handler.getTankProperties()) {
                FluidStack contents = tank.getContents();
                if (contents != null && contents.amount > 0
                        && fluidName.equals(contents.getFluid().getName())) return true;
            }
        }
        return false;
    }

    private static boolean containsBreedingFood(TileEntity tile) {
        return contains(tile, stack -> stack.getItem() == Items.WHEAT
            || stack.getItem() == Items.CARROT || stack.getItem() == Items.POTATO
            || stack.getItem() == Items.BEETROOT);
    }

    private static boolean containsFertilizer(TileEntity tile) {
        return contains(tile, AgriculturalSidePathSystem::isFertilizer);
    }

    private static boolean isFertilizer(ItemStack stack) {
        ResourceLocation id = stack.isEmpty() ? null : stack.getItem().getRegistryName();
        return (stack.getItem() == Items.DYE && stack.getMetadata() == 15)
            || (id != null && ("industrialforegoing:fertilizer".equals(id.toString())
                || "forestry:fertilizer_compound".equals(id.toString())));
    }

    private static boolean isCropOutput(ItemStack stack) {
        return stack.getItem() == Items.WHEAT || stack.getItem() == Items.CARROT
            || stack.getItem() == Items.POTATO || stack.getItem() == Items.BEETROOT
            || stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.BEETROOT_SEEDS;
    }

    private static boolean isRenewableAnimalProduct(ItemStack stack) {
        return stack.getItem() == Items.MILK_BUCKET
            || Block.getBlockFromItem(stack.getItem()) == Blocks.WOOL;
    }

    private static boolean isAquaticOutput(ItemStack stack) {
        return stack.getItem() == Items.FISH || stack.getItem() == Items.COOKED_FISH;
    }

    private static boolean powered(TileEntity tile) {
        for (EnumFacing side : EnumFacing.values()) {
            IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, side);
            if (energy != null && energy.getEnergyStored() > 0) return true;
        }
        IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, null);
        if (energy != null && energy.getEnergyStored() > 0) return true;
        for (EnumFacing side : EnumFacing.values()) {
            net.modcrafters.mclib.energy.IGenericEnergyStorage generic =
                net.ndrei.teslacorelib.energy.EnergySystemFactory.INSTANCE.wrapTileEntity(tile, side);
            if (generic != null && generic.getStored() > 0) return true;
        }
        return false;
    }

    private static boolean containsSapling(TileEntity tile) {
        return contains(tile, stack -> Block.getBlockFromItem(stack.getItem()) instanceof net.minecraft.block.BlockSapling);
    }

    private static boolean containsCharcoal(TileEntity tile) {
        return contains(tile, stack -> stack.getItem() == Items.COAL && stack.getMetadata() == 1);
    }

    private static boolean contains(TileEntity tile, java.util.function.Predicate<ItemStack> test) {
        for (EnumFacing side : EnumFacing.values()) {
            IItemHandler items = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            if (items == null) continue;
            for (int slot = 0; slot < items.getSlots(); slot++) if (test.test(items.getStackInSlot(slot))) return true;
        }
        IItemHandler items = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (items != null) for (int slot = 0; slot < items.getSlots(); slot++)
            if (test.test(items.getStackInSlot(slot))) return true;
        if (tile instanceof IInventory) for (int slot = 0; slot < ((IInventory) tile).getSizeInventory(); slot++)
            if (test.test(((IInventory) tile).getStackInSlot(slot))) return true;
        return false;
    }

    private static boolean hasTreeMaterialNearby(EntityPlayer player, BlockPos center) {
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-4, 1, -4), center.add(4, 8, 4))) {
            Block block = player.world.getBlockState(pos).getBlock();
            if (block instanceof net.minecraft.block.BlockSapling || block.isWood(player.world, pos)) return true;
        }
        return false;
    }

    private static boolean isForestryMachine(TileEntity tile) {
        String id = blockId(tile);
        return "industrialforegoing:crop_sower".equals(id)
            || "industrialforegoing:crop_recolector".equals(id)
            || "industrialforegoing:resourceful_furnace".equals(id);
    }

    private static boolean isAgriculturalMachine(TileEntity tile) {
        String id = blockId(tile);
        return isForestryMachine(tile) || "industrialforegoing:plant_interactor".equals(id)
            || "industrialforegoing:crop_enrich_material_injector".equals(id)
            || "industrialforegoing:animal_stock_increaser".equals(id)
            || "industrialforegoing:animal_growth_increaser".equals(id)
            || "industrialforegoing:animal_independence_selector".equals(id)
            || "industrialforegoing:animal_resource_harvester".equals(id)
            || "industrialforegoing:animal_byproduct_recolector".equals(id)
            || "industrialforegoing:sewage_composter_solidifier".equals(id)
            || "industrialforegoing:water_resources_collector".equals(id);
    }

    private static String blockId(TileEntity tile) {
        ResourceLocation id = tile.getWorld().getBlockState(tile.getPos()).getBlock().getRegistryName();
        return id == null ? "" : id.toString();
    }

    private static void markOwner(TileEntity tile, UUID owner) {
        tile.getTileData().setUniqueId(OWNER, owner);
        tile.markDirty();
    }

    private static boolean owned(TileEntity tile, EntityPlayer player) {
        return tile.getTileData().hasUniqueId(OWNER)
            && player.getUniqueID().equals(tile.getTileData().getUniqueId(OWNER));
    }

    static boolean evaluateCropForTest(EntityPlayerMP player, BlockPos center) {
        return evaluateCropEngineering(player, center, 8);
    }

    static void harvestHempForTest(EntityPlayerMP player, TileEntityCrop crop) {
        if (isMatureHemp(crop)) {
            ProgressionState.record(player, "hemp_manually_harvested");
            crop.performManualHarvest();
            RuntimeAdvancements.grant(player, "breed_hemp");
        }
    }

    static void recordCraftForTest(EntityPlayerMP player, ItemStack output, IInventory inputs) {
        recordCraft(player, output, inputs);
    }

    static void completeLivestockForTest(EntityPlayerMP player, EntityAnimal animal) {
        ProgressionState.record(player, "controlled_livestock_leads_crafted");
        if (animal.getLeashed()) ProgressionState.record(player, "controlled_livestock_leashed");
        ProgressionState.record(player, "controlled_livestock_bred");
        checkLivestock(player);
    }

    static void markForestryForTest(TileEntity tile, EntityPlayerMP player) {
        if (tile != null && isForestryMachine(tile)) markOwner(tile, player.getUniqueID());
    }

    static void markAutomationForTest(TileEntity tile, EntityPlayerMP player) {
        if (tile != null && isAgriculturalMachine(tile)) markOwner(tile, player.getUniqueID());
    }

    static void evaluateAutomationForTest(EntityPlayerMP player) {
        TileEntity water = machine(player, "industrialforegoing:water_resources_collector");
        IndustrialCivilizationCore.LOGGER.info(
            "IC_TEST|AUTOMATED_AGRICULTURE|water_machine={}|powered={}|water_area={}|output={}",
            water != null, live(water), water != null && hasWaterArea(player, water.getPos()),
            water != null && adjacentContains(water, AgriculturalSidePathSystem::isAquaticOutput));
        evaluateAutomation(player);
    }

    static void evaluateForestryForTest(EntityPlayerMP player) {
        for (TileEntity tile : player.world.loadedTileEntityList) {
            if (tile.getPos().distanceSq(player.getPosition()) > FARM_RANGE_SQ) continue;
            String id = blockId(tile);
            if (id.startsWith("industrialforegoing:") || id.startsWith("ic2:"))
                IndustrialCivilizationCore.LOGGER.info(
                    "IC_TEST|FORESTRY|id={}|class={}|owned={}|powered={}|sapling={}|charcoal={}|pos={}",
                    id, tile.getClass().getName(), owned(tile, player), powered(tile),
                    containsSapling(tile), containsCharcoal(tile), tile.getPos());
        }
        evaluateForestry(player);
    }

    private AgriculturalSidePathSystem() {}
}
