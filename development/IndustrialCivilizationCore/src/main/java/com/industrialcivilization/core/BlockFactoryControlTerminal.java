package com.industrialcivilization.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockFactoryControlTerminal extends BlockContainer {
    public BlockFactoryControlTerminal() {
        super(Material.IRON);
        setRegistryName(IndustrialCivilizationCore.MODID, "factory_control_terminal");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".factory_control_terminal");
        setHardness(6.0F);
        setResistance(30.0F);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileFactoryControlTerminal();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileFactoryControlTerminal) {
            ((TileFactoryControlTerminal) world.getTileEntity(pos)).interact(player);
        }
        return true;
    }
}
