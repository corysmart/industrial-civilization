package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shared progression and condition rules for Earth-only industrial markets. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class MarketEconomy {
    public static final String CONDITION = "IndustrialCondition";
    public static final String USED = "IndustrialUsed";
    public static final int NEW_CONDITION = 10000;

    public static int playerStage(EntityPlayer player) {
        if (ProgressionState.has(player, "ai_age") || has(player, IndustrialCivilizationCore.AI_CORE)) return 7;
        if (ProgressionState.has(player, "martian_autonomy_archive") || has(player, IndustrialCivilizationCore.MARTIAN_AUTONOMY_ARCHIVE)) return 6;
        if (ProgressionState.has(player, "lunar_engineering_archive") || has(player, IndustrialCivilizationCore.LUNAR_ENGINEERING_ARCHIVE)) return 5;
        if (ProgressionState.has(player, "orbital_research_archive") || has(player, IndustrialCivilizationCore.ORBITAL_RESEARCH_ARCHIVE)) return 4;
        if (ProgressionState.has(player, "industrial_capacity_access") || ProgressionState.has(player, "abandoned_factory_operational")) return 3;
        if (ProgressionState.has(player, "secure_workshop") || ProgressionState.counter(player, "manual_crafts") >= 25) return 2;
        return 1;
    }

    /** A market can never sell at the player's current stage or beyond. */
    public static int marketStage(EntityPlayer player, int settlementCapacity) {
        return Math.max(0, Math.min(settlementCapacity, playerStage(player) - 1));
    }

    public static ItemStack newCondition(ItemStack stack) {
        return withCondition(stack, NEW_CONDITION, false);
    }

    public static ItemStack usedCondition(ItemStack stack, int condition) {
        return withCondition(stack, condition, true);
    }

    public static ItemStack withCondition(ItemStack stack, int condition, boolean used) {
        if (stack.isEmpty()) return stack;
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        tag.setInteger(CONDITION, Math.max(0, Math.min(NEW_CONDITION, condition)));
        tag.setBoolean(USED, used);
        stack.setTagCompound(tag);
        return stack;
    }

    public static boolean isConditioned(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(CONDITION, 3);
    }

    public static int condition(ItemStack stack) {
        return isConditioned(stack) ? stack.getTagCompound().getInteger(CONDITION) : NEW_CONDITION;
    }

    public static int usedValue(int newPrice, int condition) {
        // A pristine used item returns at most 32%; severe damage pushes this near scrap value.
        double health = Math.max(0.05D, Math.min(1.0D, condition / (double) NEW_CONDITION));
        return Math.max(1, (int) Math.floor(newPrice * 0.32D * health));
    }

    @SubscribeEvent
    public static void weaponUsed(LivingAttackEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        ItemStack weapon = ((EntityPlayer) event.getSource().getTrueSource()).getHeldItemMainhand();
        if (!isConditioned(weapon) || weapon.getItem().getRegistryName() == null
                || !"techguns".equals(weapon.getItem().getRegistryName().getResourceDomain())) return;
        NBTTagCompound tag = weapon.getTagCompound();
        tag.setInteger(CONDITION, Math.max(0, tag.getInteger(CONDITION) - 4));
        tag.setBoolean(USED, true);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (!isConditioned(event.getItemStack())) return;
        int percent = condition(event.getItemStack()) / 100;
        event.getToolTip().add(new TextComponentString("IC2 service condition: " + percent + "%").getFormattedText());
        if (percent <= 20) event.getToolTip().add(new TextComponentString(
            "Requires IC2 machine-block service before reliable use").getFormattedText());
    }

    private static boolean has(EntityPlayer player, net.minecraft.item.Item item) {
        for (ItemStack stack : player.inventory.mainInventory) if (!stack.isEmpty() && stack.getItem() == item) return true;
        return false;
    }

    private MarketEconomy() {}
}
