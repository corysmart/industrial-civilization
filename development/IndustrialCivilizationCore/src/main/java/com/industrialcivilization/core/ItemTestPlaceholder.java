package com.industrialcivilization.core;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Explicitly temporary, centrally recipe-gated progression artifact. */
public final class ItemTestPlaceholder extends Item {
    public ItemTestPlaceholder(String registryId) {
        setRegistryName(IndustrialCivilizationCore.MODID, registryId);
        setUnlocalizedName(IndustrialCivilizationCore.MODID + "." + registryId);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
                               ITooltipFlag flag) {
        tooltip.add(TextFormatting.RED + I18n.format("tooltip.industrialcivilizationcore.placeholder.temporary"));
        tooltip.add(TextFormatting.GRAY + I18n.format(getUnlocalizedName(stack) + ".represents"));
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("tooltip.industrialcivilizationcore.placeholder.toggle"));
    }
}
