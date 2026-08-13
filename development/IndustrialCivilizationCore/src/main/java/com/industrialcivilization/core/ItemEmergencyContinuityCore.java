package com.industrialcivilization.core;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/** Consumable AI-age failover hardware used by the Postmortal goal. */
public final class ItemEmergencyContinuityCore extends Item {
    public ItemEmergencyContinuityCore() {
        setRegistryName(IndustrialCivilizationCore.MODID, "emergency_continuity_core");
        setUnlocalizedName(IndustrialCivilizationCore.MODID + ".emergency_continuity_core");
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
            ITooltipFlag flag) {
        tooltip.add(new TextComponentTranslation(
            "item.industrialcivilizationcore.emergency_continuity_core.tooltip").getFormattedText());
    }
}
