package com.industrialcivilization.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public final class BlockEnvironmentalSolarArray extends BlockContainer {
    private final boolean tracking;

    public BlockEnvironmentalSolarArray(String id, boolean tracking) {
        super(Material.IRON);
        this.tracking = tracking;
        setRegistryName(IndustrialCivilizationCore.MODID, id);
        setUnlocalizedName(IndustrialCivilizationCore.MODID + "." + id);
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(20.0F);
    }

    public boolean isTracking() { return tracking; }

    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEnvironmentalSolarArray();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof TileEnvironmentalSolarArray) {
            TileEnvironmentalSolarArray tile = (TileEnvironmentalSolarArray) world.getTileEntity(pos);
            tile.setLastUser(player);
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.solar.status", tile.getGenerationRate(),
                tile.getEnergyStored(), tile.environment()), false);
        }
        return true;
    }
}
