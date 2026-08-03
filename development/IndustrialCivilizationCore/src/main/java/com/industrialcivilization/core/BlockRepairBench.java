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
}
