package com.industrialcivilization.core;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BlockIndustrialMachine extends BlockContainer {
    private final IndustrialMachineKind kind;

    public BlockIndustrialMachine(IndustrialMachineKind kind) {
        super(Material.IRON);
        this.kind = kind;
        setRegistryName(IndustrialCivilizationCore.MODID, kind.id);
        setUnlocalizedName(IndustrialCivilizationCore.MODID + "." + kind.id);
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(4.5F);
        setResistance(15.0F);
    }

    public IndustrialMachineKind getKind() {
        return kind;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileIndustrialMachine();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
            EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileIndustrialMachine) {
            ((TileIndustrialMachine) tile).setWorkshopFacing(placer.getHorizontalFacing());
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileIndustrialMachine) {
                ((TileIndustrialMachine) tile).setLastUser(player);
                if (((TileIndustrialMachine) tile).isRusted()) player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentTranslation(
                        "message.industrialcivilization.workshop.rusted"), false);
            }
            player.openGui(IndustrialCivilizationCore.INSTANCE,
                IndustrialCivilizationCore.GUI_INDUSTRIAL_MACHINE, world,
                pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileIndustrialMachine) {
            InventoryHelper.dropInventoryItems(world, pos, (TileIndustrialMachine) tile);
        }
        super.breakBlock(world, pos, state);
    }
}
