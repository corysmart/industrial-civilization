package com.industrialcivilization.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Redstone sequencer for a player-built ProjectRed mobile Quarry carriage. */
public final class BlockMobileQuarryController extends BlockContainer {
    public BlockMobileQuarryController() {
        super(Material.IRON);
        setRegistryName(IndustrialCivilizationCore.MODID, "mobile_quarry_controller");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".mobile_quarry_controller");
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(20.0F);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileMobileQuarryController();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos,
            EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileMobileQuarryController
            ? ((TileMobileQuarryController) tile).redstoneLevel(side) : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos,
            EnumFacing side) {
        return getWeakPower(state, world, pos, side);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
            EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileMobileQuarryController) {
            ((TileMobileQuarryController) tile).setFacing(placer.getHorizontalFacing());
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileMobileQuarryController) {
                ((TileMobileQuarryController) tile).interact(player, player.isSneaking());
            }
        }
        return true;
    }
}
