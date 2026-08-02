package com.industrialcivilization.core;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockMolecularAnalyzer extends Block implements ITileEntityProvider {
    public BlockMolecularAnalyzer() {
        super(Material.IRON);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileMolecularAnalyzer();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileMolecularAnalyzer
            && ((TileMolecularAnalyzer) tile).analyze(player, hand);
    }
}
