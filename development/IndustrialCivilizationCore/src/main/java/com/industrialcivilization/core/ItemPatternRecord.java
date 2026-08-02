package com.industrialcivilization.core;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public final class ItemPatternRecord extends Item {
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.AQUA + "Sample: Martian Desh");
        tooltip.add(TextFormatting.GRAY + "Fe-Ni alloy with non-terrestrial trace signature");
        tooltip.add(TextFormatting.GOLD + "AI Age — Not Yet Available");
    }
}
