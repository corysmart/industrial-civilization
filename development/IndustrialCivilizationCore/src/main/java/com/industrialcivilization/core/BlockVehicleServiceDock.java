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

/** Stationary capability bridge between the service vehicle and BuildCraft pipes. */
public final class BlockVehicleServiceDock extends BlockContainer {
    public BlockVehicleServiceDock() {
        super(Material.IRON);
        setRegistryName(IndustrialCivilizationCore.MODID, "vehicle_service_dock");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".vehicle_service_dock");
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(4.0F);
        setResistance(12.0F);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileVehicleServiceDock(); }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) player.sendStatusMessage(new TextComponentTranslation(
            VehicleIntegrationSystem.findServiceVehicle(world, pos) == null
                ? "message.industrialcivilization.vehicle_dock.missing"
                : "message.industrialcivilization.vehicle_dock.connected"), false);
        return true;
    }
}
