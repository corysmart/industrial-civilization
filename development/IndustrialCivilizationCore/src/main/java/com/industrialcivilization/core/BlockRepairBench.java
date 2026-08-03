package com.industrialcivilization.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Repairs one nearby rain-rusted industrial workshop per IC2 machine block. */
public final class BlockRepairBench extends Block {
    public BlockRepairBench() {
        super(Material.IRON);
        setRegistryName(IndustrialCivilizationCore.MODID, "repair_bench");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".repair_bench");
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(4.0F);
        setResistance(12.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, net.minecraft.block.state.IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        Item machine = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2:blockmachinelv"));
        ItemStack held = player.getHeldItem(hand);
        if (MarketEconomy.isConditioned(held) && held.getItem().getRegistryName() != null
                && "techguns".equals(held.getItem().getRegistryName().getResourceDomain())) {
            int materialSlot = findItem(player, machine);
            if (materialSlot < 0) {
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                    "Gun service requires one IC2 machine block in your inventory."), false);
                return true;
            }
            MarketEconomy.withCondition(held, MarketEconomy.NEW_CONDITION, true);
            if (!player.capabilities.isCreativeMode) player.inventory.decrStackSize(materialSlot, 1);
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                "Weapon restored at the IC2 repair bench."), false);
            return true;
        }
        if (machine == null || held.getItem() != machine) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.repair_bench.requirement"), false);
            return true;
        }
        for (BlockPos target : BlockPos.getAllInBoxMutable(pos.add(-12, -4, -12), pos.add(12, 4, 12))) {
            TileEntity tile = world.getTileEntity(target);
            if (tile instanceof TileIndustrialMachine && ((TileIndustrialMachine) tile).repairRust()) {
                if (!player.capabilities.isCreativeMode) held.shrink(1);
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.repair_bench.complete"), false);
                return true;
            }
        }
        player.sendStatusMessage(new TextComponentTranslation(
            "message.industrialcivilization.repair_bench.none"), false);
        return true;
    }

    private static int findItem(EntityPlayer player, Item item) {
        if (item == null) return -1;
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (!stack.isEmpty() && stack.getItem() == item) return slot;
        }
        return -1;
    }
}
