package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

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
        return GameplayRules.marketStage(playerStage(player), settlementCapacity);
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
        return GameplayRules.usedValue(newPrice, condition);
    }

    /** Loot worth risking a robbery for; mundane survival supplies do not count. */
    public static boolean carriesRobberLoot(EntityPlayer player) {
        for (net.minecraft.inventory.Slot slot : player.inventoryContainer.inventorySlots) {
            if (isRobberLoot(slot.getStack())) return true;
        }
        return false;
    }

    public static boolean isRobberLoot(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        String domain = id.getResourceDomain();
        String path = id.getResourcePath();
        if (domain.equals(IndustrialCivilizationCore.MODID) || domain.equals("ic2")
                || domain.startsWith("buildcraft") || domain.startsWith("projectred")
                || domain.equals("railcraft") || domain.equals("appliedenergistics2")
                || domain.equals("logisticspipes") || domain.equals("computercraft")
                || domain.equals("cctweaked") || domain.startsWith("galacticraft")
                || domain.equals("techguns") || domain.equals("vehicle")
                || domain.equals("icbmclassic") || domain.equals("modularforcefieldsystem")) {
            return true;
        }
        if (domain.equals("minecraft") && (path.startsWith("iron_")
                || path.startsWith("diamond_") || path.startsWith("golden_")
                || path.equals("diamond") || path.equals("emerald")
                || path.equals("gold_ingot") || path.equals("iron_ingot"))) return true;
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String ore = OreDictionary.getOreName(oreId);
            if (ore.startsWith("ingot") || ore.startsWith("gem") || ore.startsWith("block")
                    || ore.startsWith("plate") || ore.startsWith("gear")
                    || ore.toLowerCase(java.util.Locale.ROOT).contains("circuit")) return true;
        }
        return false;
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

    /** Curated player-weapon envelope; exploration finds remain useful, not dominant. */
    @SubscribeEvent
    public static void balanceWeaponDamage(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        ItemStack weapon = ((EntityPlayer) event.getSource().getTrueSource()).getHeldItemMainhand();
        if (weapon.isEmpty() || weapon.getItem().getRegistryName() == null
                || !"techguns".equals(weapon.getItem().getRegistryName().getResourceDomain())) return;
        String path = weapon.getItem().getRegistryName().getResourcePath();
        float scale = path.contains("m4") || path.contains("rifle") ? 0.78F
            : path.contains("shotgun") ? 0.84F : path.contains("pistol") ? 0.90F : 0.85F;
        if (isConditioned(weapon)) {
            float health = Math.max(0.35F, condition(weapon) / (float) NEW_CONDITION);
            scale *= 0.70F + 0.30F * health;
        }
        event.setAmount(event.getAmount() * scale);
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
