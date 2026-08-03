package com.industrialcivilization.core;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/** Universal settlement currency. Emeralds remain materials, never money. */
public final class ItemIndustrialCredit extends Item {
    public ItemIndustrialCredit() {
        setRegistryName(IndustrialCivilizationCore.MODID, "industrial_credit");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".industrial_credit");
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setMaxStackSize(64);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
            ITooltipFlag flag) {
        tooltip.add(new TextComponentTranslation(
            "item.industrialcivilizationcore.industrial_credit.tooltip").getFormattedText());
    }
}
