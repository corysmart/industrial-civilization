package com.industrialcivilization.core;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** Independently craftable architecture blocks used by deployed workshops. */
public final class BlockWorkshopComponent extends Block {
    public BlockWorkshopComponent(String id) {
        super(Material.IRON);
        setRegistryName(IndustrialCivilizationCore.MODID, id);
        setUnlocalizedName(IndustrialCivilizationCore.MODID + "." + id);
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setHardness(3.5F);
        setResistance(12.0F);
    }
}
