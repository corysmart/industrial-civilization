package com.industrialcivilization.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Persistent faction rules applied to both generated citizens and surviving
 * vanilla villagers. Custom NPCs remains available for authored encounters,
 * but core reputation, trade, hostility and companionship do not depend on a
 * world-specific clone database.
 */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class FactionSystem {
    public static final int HOSTILE_REPUTATION = -20;
    public static final int FRIENDLY_REPUTATION = 25;
    public static final int MEMBERSHIP_REPUTATION = 35;
    public static final int COMPANION_REPUTATION = 60;
    private static final String FACTION = "IndustrialFaction";
    private static final String ROLE = "IndustrialRole";
    private static final String SPECIALTY = "IndustrialSpecialty";
    private static final String COMPANION = "IndustrialCompanion";
    private static final String COMPANION_OWNER = "IndustrialCompanionOwner";
    private static final String MARKET_CAPACITY = "IndustrialMarketCapacity";

    public static final Definition[] DEFINITIONS = {
        new Definition("frontier_cooperative", "Frontier Cooperative", 10,
            "Primitive villages", "Food, coal, iron and basic supplies",
            "Trade fairly, avoid civilian violence, and establish a secure workshop."),
        new Definition("riverside_works", "Riverside Works", 0,
            "Industrial cities and guarded factories", "Steel, electronics, fuel and machine components",
            "Demonstrate industrial capacity, restore machinery, and avoid theft from guarded factories."),
        new Definition("civil_defense_militia", "Civil Defense Militia", 0,
            "Militia outposts and city checkpoints", "Ammunition, armor and field supplies",
            "Defeat raiders, protect civilians, and remain trusted by settlements."),
        new Definition("survey_detachment_7", "Survey Detachment 7", 0,
            "Research compounds and specialist factories", "Data cartridges, precision parts and survey equipment",
            "Produce research archives and respect restricted scientific sites."),
        new Definition("ashline_raiders", "Ashline Raiders", -100,
            "Captured ruins and raider checkpoints", "Stolen ammunition, salvage and contraband",
            "Only an openly predatory playstyle earns an invitation; joining excludes settlement factions.")
    };

    public static final class Definition {
        public final String id;
        public final String name;
        public final int initialReputation;
        public final String settlements;
        public final String products;
        public final String membershipRule;

        Definition(String id, String name, int initialReputation, String settlements,
                String products, String membershipRule) {
            this.id = id;
            this.name = name;
            this.initialReputation = initialReputation;
            this.settlements = settlements;
            this.products = products;
            this.membershipRule = membershipRule;
        }
    }

    public static Definition definition(String id) {
        for (Definition definition : DEFINITIONS) if (definition.id.equals(id)) return definition;
        return DEFINITIONS[0];
    }

    public static int reputation(EntityPlayer player, String faction) {
        NBTTagCompound reputations = reputationData(player);
        if (!reputations.hasKey(faction, 99)) {
            reputations.setInteger(faction, definition(faction).initialReputation);
        }
        return reputations.getInteger(faction);
    }

    public static String membership(EntityPlayer player) {
        return ProgressionState.data(player).getString("faction_membership");
    }

    public static boolean known(EntityPlayer player, String faction) {
        return ProgressionState.data(player).getBoolean("known_faction_" + faction)
            || faction.equals("frontier_cooperative");
    }

    public static boolean eligible(EntityPlayer player, String faction) {
        if (!membership(player).isEmpty() && !membership(player).equals(faction)) return false;
        if (reputation(player, faction) < MEMBERSHIP_REPUTATION) return false;
        long civilianHarm = ProgressionState.counter(player, "faction_civilian_harm");
        long propertyDamage = ProgressionState.counter(player, "faction_property_damage");
        if ("frontier_cooperative".equals(faction)) {
            return civilianHarm == 0 && (ProgressionState.has(player, "secure_workshop")
                || ProgressionState.counter(player, "manual_crafts") >= 25
                || ProgressionState.counter(player, "faction_trade_contacts") >= 4);
        }
        if ("riverside_works".equals(faction)) {
            return propertyDamage < 3 && (ProgressionState.has(player, "industrial_capacity_access")
                || ProgressionState.has(player, "abandoned_factory_operational")
                || ProgressionState.counter(player, "manual_crafts") >= 80);
        }
        if ("civil_defense_militia".equals(faction)) {
            return civilianHarm == 0 && ProgressionState.counter(player, "raiders_defeated") >= 3;
        }
        if ("survey_detachment_7".equals(faction)) {
            return propertyDamage == 0 && (ProgressionState.has(player, "orbital_research_archive")
                || ProgressionState.has(player, "lunar_engineering_archive")
                || ProgressionState.has(player, "martian_autonomy_archive"));
        }
        return "ashline_raiders".equals(faction) && civilianHarm >= 3;
    }

    public static String attitude(EntityPlayer player, String faction) {
        if (membership(player).equals(faction)) return "MEMBER";
        int reputation = reputation(player, faction);
        if (reputation <= HOSTILE_REPUTATION) return "HOSTILE";
        if (eligible(player, faction)) return "ELIGIBLE";
        if (reputation >= FRIENDLY_REPUTATION) return "FRIENDLY";
        return reputation < 0 ? "GUARDED" : "NEUTRAL";
    }

    public static void adjustReputation(EntityPlayer player, String faction, int amount, String reason) {
        NBTTagCompound reputations = reputationData(player);
        int value = Math.max(-100, Math.min(100, reputation(player, faction) + amount));
        reputations.setInteger(faction, value);
        ProgressionState.increment(player, amount >= 0 ? "faction_positive_actions" : "faction_negative_actions", 1);
        if ("ashline_raiders".equals(faction)) {
            for (Definition definition : DEFINITIONS) {
                if (!definition.id.equals(faction)) setReputation(player, definition.id,
                    reputation(player, definition.id) - amount / 3);
            }
        } else {
            setReputation(player, "ashline_raiders", reputation(player, "ashline_raiders") - amount / 2);
        }
        if (player instanceof EntityPlayerMP) FactionNetwork.sendSnapshot((EntityPlayerMP) player);
        IndustrialCivilizationCore.LOGGER.info("faction reputation player={} faction={} delta={} value={} reason={}",
            player.getName(), faction, amount, value, reason);
    }

    private static void setReputation(EntityPlayer player, String faction, int value) {
        reputationData(player).setInteger(faction, Math.max(-100, Math.min(100, value)));
    }

    /** Patrol incidents matter for trust and membership, but can never alone start a war. */
    public static void adjustMilitiaPatrolReputation(EntityPlayer player, int amount, String reason) {
        int value = Math.max(-10, Math.min(100,
            reputation(player, "civil_defense_militia") + amount));
        setReputation(player, "civil_defense_militia", value);
        if (player instanceof EntityPlayerMP) FactionNetwork.sendSnapshot((EntityPlayerMP) player);
        IndustrialCivilizationCore.LOGGER.info(
            "militia patrol reputation player={} delta={} boundedValue={} reason={}",
            player.getName(), amount, value, reason);
    }

    private static NBTTagCompound reputationData(EntityPlayer player) {
        NBTTagCompound data = ProgressionState.data(player);
        if (!data.hasKey("FactionReputation", 10)) data.setTag("FactionReputation", new NBTTagCompound());
        return data.getCompoundTag("FactionReputation");
    }

    public static EntityVillager spawnCitizen(World world, double x, double y, double z,
            String faction, String role, String specialty, String name) {
        return spawnCitizen(world, x, y, z, faction, role, specialty, name, 1);
    }

    public static EntityVillager spawnCitizen(World world, double x, double y, double z,
            String faction, String role, String specialty, String name, int marketCapacity) {
        EntityVillager citizen = new EntityVillager(world);
        citizen.setPosition(x, y, z);
        citizen.setCustomNameTag(name);
        citizen.setAlwaysRenderNameTag(true);
        citizen.getEntityData().setString(FACTION, faction);
        citizen.getEntityData().setString(ROLE, role);
        citizen.getEntityData().setString(SPECIALTY, specialty);
        citizen.getEntityData().setInteger(MARKET_CAPACITY, Math.max(0, marketCapacity));
        if ("guard".equals(role) || "raider".equals(role)) {
            citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        }
        configureTrades(citizen);
        world.spawnEntity(citizen);
        return citizen;
    }

    private static void configureTrades(EntityVillager villager) {
        configureTrades(villager, null, 0);
    }

    private static void configureTrades(EntityVillager villager, @Nullable EntityPlayer player,
            int reputationDiscount) {
        NBTTagCompound tag = villager.getEntityData();
        if (!tag.hasKey(FACTION, 8)) {
            tag.setString(FACTION, "frontier_cooperative");
            tag.setString(ROLE, "villager");
            tag.setString(SPECIALTY, "general");
            tag.setInteger(MARKET_CAPACITY, 1);
        }
        MerchantRecipeList offers = new MerchantRecipeList();
        String specialty = tag.getString(SPECIALTY);
        String faction = tag.getString(FACTION);
        int stage = player == null ? 0 : MarketEconomy.marketStage(player,
            Math.max(1, tag.getInteger(MARKET_CAPACITY)));
        boolean earthMarket = villager.world.provider.getDimension() == 0;
        addPurchase(offers, Items.WHEAT, 12, 2, reputationDiscount);
        addPurchase(offers, Items.COAL, 16, 1, reputationDiscount);
        addSale(offers, Items.BREAD, 4, 1, reputationDiscount);
        addSale(offers, Items.IRON_INGOT, 1, 3, reputationDiscount);
        if ("steel".equals(specialty)) {
            addExternalSale(offers, "railcraft:ingot_steel", 0, 2, 8, reputationDiscount);
            addPurchase(offers, Items.IRON_INGOT, 6, 3, reputationDiscount);
        } else if ("electronics".equals(specialty)) {
            addExternalSale(offers, "ic2:itemmisc", 451, 1, 10, reputationDiscount);
            addExternalSale(offers, "ic2:itemmisc", 452, 1, 24, reputationDiscount);
            addSale(offers, Items.REDSTONE, 8, 3, reputationDiscount);
        } else if ("fuel".equals(specialty)) {
            addSale(offers, Items.COAL, 16, 4, reputationDiscount);
            addSale(offers, Items.BLAZE_POWDER, 4, 8, reputationDiscount);
        } else if ("armaments".equals(specialty)) {
            addExternalSale(offers, "techguns:itemshared", 11, 1, 8, reputationDiscount);
            addExternalSale(offers, "techguns:itemshared", 2, 8, 6, reputationDiscount);
            addSale(offers, Items.IRON_SWORD, 1, 7, reputationDiscount);
        } else if ("research".equals(specialty)) {
            addSale(offers, IndustrialCivilizationCore.BLANK_DATA_CARTRIDGE, 1, 8, reputationDiscount);
            addSale(offers, Items.PAPER, 16, 2, reputationDiscount);
            addSale(offers, Items.GLASS_BOTTLE, 8, 2, reputationDiscount);
        } else if ("food".equals(specialty)) {
            addSale(offers, Items.COOKED_BEEF, 6, 3, reputationDiscount);
            addSale(offers, Items.BAKED_POTATO, 8, 2, reputationDiscount);
        }
        // Vehicles and firearms remain luxuries. Each community stocks equipment
        // relevant to its work, and never sells at the player's current tech stage.
        if (earthMarket && stage >= 2 && "frontier_cooperative".equals(faction)) {
            addVehicleSale(offers, "golf_cart", 72, reputationDiscount);
            if (stage >= 3) addVehicleSale(offers, "tractor", 104, reputationDiscount);
        }
        if (earthMarket && stage >= 3 && "riverside_works".equals(faction)) {
            addVehicleSale(offers, "smart_car", 96, reputationDiscount);
            if (stage >= 4) addVehicleSale(offers, "mini_bus", 128, reputationDiscount);
        }
        if (earthMarket && stage >= 3 && "civil_defense_militia".equals(faction)) {
            addVehicleSale(offers, "atv", 88, reputationDiscount);
            addConditionedExternalSale(offers, "techguns:pistol", 0, 36, reputationDiscount);
            if (stage >= 4) {
                addVehicleSale(offers, "off_roader", 112, reputationDiscount);
                addConditionedExternalSale(offers, "techguns:combatshotgun", 0, 72, reputationDiscount);
            }
            if (stage >= 6) addConditionedExternalSale(offers, "techguns:m4", 0, 112, reputationDiscount);
        }
        if (earthMarket && stage >= 4 && "survey_detachment_7".equals(faction)) {
            addVehicleSale(offers, "off_roader", 116, reputationDiscount);
        }
        if (earthMarket && player != null) addConditionedBuyback(offers, player.getHeldItemMainhand());
        villager.setRecipes(offers);
        tag.setBoolean("IndustrialTrades", true);
    }

    private static void addSale(MerchantRecipeList offers, Item output, int count, int price, int discount) {
        offers.add(new MerchantRecipe(new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT,
            Math.max(1, price - discount)),
            new ItemStack(output, count)));
    }

    private static void addPurchase(MerchantRecipeList offers, Item input, int count, int credits, int discount) {
        offers.add(new MerchantRecipe(new ItemStack(input, count),
            new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT, credits + (discount >= 2 ? 1 : 0))));
    }

    private static void addExternalSale(MerchantRecipeList offers, String id, int metadata,
            int count, int price, int discount) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item != null) offers.add(new MerchantRecipe(
            new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT, Math.max(1, price - discount)),
            new ItemStack(item, count, metadata)));
    }

    private static void addConditionedExternalSale(MerchantRecipeList offers, String id, int metadata,
            int price, int discount) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item != null) addHighPriceSale(offers,
            MarketEconomy.newCondition(new ItemStack(item, 1, metadata)), price, discount);
    }

    private static void addVehicleSale(MerchantRecipeList offers, String vehicleId,
            int price, int discount) {
        ItemStack crate = MachineRecipe.vehicleCrate(vehicleId);
        if (!crate.isEmpty()) addHighPriceSale(offers, MarketEconomy.newCondition(crate), price, discount);
    }

    private static void addHighPriceSale(MerchantRecipeList offers, ItemStack output,
            int price, int discount) {
        int adjusted = Math.max(1, price - Math.min(4, discount * 2));
        ItemStack first = new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT,
            Math.min(64, adjusted));
        if (adjusted <= 64) offers.add(new MerchantRecipe(first, output));
        else offers.add(new MerchantRecipe(first, new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT,
            adjusted - 64), output));
    }

    private static void addConditionedBuyback(MerchantRecipeList offers, ItemStack held) {
        if (!MarketEconomy.isConditioned(held)) return;
        ItemStack exact = held.copy();
        exact.setCount(1);
        int newPrice = exact.getItem().getRegistryName() != null
            && "vehicle".equals(exact.getItem().getRegistryName().getResourceDomain()) ? 104 : 64;
        offers.add(new MerchantRecipe(exact, new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT,
            MarketEconomy.usedValue(newPrice, MarketEconomy.condition(exact)))));
    }

    @SubscribeEvent
    public static void entityJoined(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) event.getEntity();
        configureTrades(villager);
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isRemote || !(event.getTarget() instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) event.getTarget();
        NBTTagCompound tag = villager.getEntityData();
        if (!tag.hasKey(FACTION, 8)) return;
        EntityPlayer player = event.getEntityPlayer();
        String faction = tag.getString(FACTION);
        discover(player, faction);
        if (reputation(player, faction) <= HOSTILE_REPUTATION && !membership(player).equals(faction)) {
            cancel(event);
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.faction.hostile", definition(faction).name), false);
            villager.setAttackTarget(player);
            return;
        }
        if (player.isSneaking()) {
            cancel(event);
            if (tag.getBoolean(COMPANION) && owns(player, villager)) {
                tag.setBoolean(COMPANION, false);
                tag.removeTag(COMPANION_OWNER);
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.faction.companion_dismissed", villager.getName()), false);
                return;
            }
            ItemStack held = player.getHeldItem(event.getHand());
            if (membership(player).equals(faction) && reputation(player, faction) >= COMPANION_REPUTATION
                    && held.getItem() == IndustrialCivilizationCore.INDUSTRIAL_CREDIT
                    && held.getCount() >= 8) {
                if (!player.capabilities.isCreativeMode) held.shrink(8);
                tag.setBoolean(COMPANION, true);
                tag.setUniqueId(COMPANION_OWNER, player.getUniqueID());
                ProgressionState.data(player).setUniqueId("faction_companion", villager.getUniqueID());
                RuntimeAdvancements.grant(player, "faction_companion");
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.faction.companion_recruited", villager.getName()), false);
                return;
            }
            if (membership(player).equals(faction)) {
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.faction.companion_cost"), false);
                return;
            }
            if (eligible(player, faction)) {
                ProgressionState.data(player).setString("faction_membership", faction);
                adjustReputation(player, faction, 10, "membership accepted");
                RuntimeAdvancements.grant(player, "faction_membership");
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.faction.joined", definition(faction).name), false);
            } else {
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.faction.ineligible", definition(faction).membershipRule), false);
            }
            return;
        }
        int rep = reputation(player, faction);
        configureTrades(villager, player,
            rep >= COMPANION_REPUTATION ? 2 : rep >= FRIENDLY_REPUTATION ? 1 : 0);
        long day = event.getWorld().getTotalWorldTime() / 24000L;
        String contactKey = "trade_contact_" + player.getUniqueID().toString();
        if (tag.getLong(contactKey) != day + 1 && player.getHeldItem(event.getHand()).getItem()
                == IndustrialCivilizationCore.INDUSTRIAL_CREDIT) {
            tag.setLong(contactKey, day + 1);
            ProgressionState.increment(player, "faction_trade_contacts", 1);
            adjustReputation(player, faction, 3, "trade contact");
            checkFactionContacts(player);
            if (ProgressionState.counter(player, "faction_trade_contacts") >= 2
                    && !ProgressionState.has(player, "underworld_lead")) {
                give(player, IndustrialCivilizationCore.UNDERWORLD_DOSSIER, 1);
                ProgressionState.record(player, "underworld_lead");
            }
        }
    }

    private static void cancel(PlayerInteractEvent.EntityInteract event) {
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void attacked(LivingAttackEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityVillager)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        String faction = villager.getEntityData().getString(FACTION);
        if (faction.isEmpty()) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ProgressionState.increment(player, "faction_civilian_harm", 1);
        adjustReputation(player, faction, -8, "attacked faction member");
        villager.setAttackTarget(player);
    }

    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if (event.getEntityLiving().world.isRemote
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        String faction = event.getEntityLiving().getEntityData().getString(FACTION);
        if (faction.isEmpty()) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if ("ashline_raiders".equals(faction)) {
            ProgressionState.increment(player, "raiders_defeated", 1);
            adjustReputation(player, "civil_defense_militia", 8, "defeated raider");
            adjustReputation(player, "riverside_works", 3, "protected industry");
            adjustReputation(player, "frontier_cooperative", 3, "protected settlements");
            giveCredits(player, 3 + player.world.rand.nextInt(4));
        } else {
            ProgressionState.increment(player, "faction_civilian_harm", 2);
            adjustReputation(player, faction, -25, "killed faction member");
            adjustReputation(player, "ashline_raiders", 12, "predatory action");
        }
    }

    @SubscribeEvent
    public static void blockBroken(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote || event.getPlayer() == null) return;
        if (event.getWorld().provider.getDimension() == 0) {
            String outpost = MilitiaOutpostRegistry.nearby(event.getWorld(), event.getPos(), 18);
            if (outpost != null) {
                NBTTagCompound data = ProgressionState.data(event.getPlayer());
                String damageKey = "militia_outpost_damage_" + outpost;
                String takenKey = "militia_outpost_taken_" + outpost;
                int damage = data.getInteger(damageKey) + 1;
                data.setInteger(damageKey, damage);
                if (damage >= 16 && !data.getBoolean(takenKey)) {
                    data.setBoolean(takenKey, true);
                    ProgressionState.increment(event.getPlayer(), "militia_outposts_taken_down", 1);
                    adjustReputation(event.getPlayer(), "civil_defense_militia", -8,
                        "dismantled militia outpost");
                    event.getPlayer().sendStatusMessage(new net.minecraft.util.text.TextComponentString(
                        "Civil Defense records this outpost as dismantled."), false);
                }
                return;
            }
        }
        List<EntityVillager> nearby = event.getWorld().getEntitiesWithinAABB(EntityVillager.class,
            new AxisAlignedBB(event.getPos()).grow(12), npc -> npc.getEntityData().hasKey(FACTION, 8)
                && !"ashline_raiders".equals(npc.getEntityData().getString(FACTION))
                && ("guard".equals(npc.getEntityData().getString(ROLE))
                    || "militia".equals(npc.getEntityData().getString(ROLE))));
        if (nearby.isEmpty()) return;
        String faction = nearby.get(0).getEntityData().getString(FACTION);
        ProgressionState.increment(event.getPlayer(), "faction_property_damage", 1);
        adjustReputation(event.getPlayer(), faction, -2, "damaged guarded property");
    }

    @SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityVillager)
                || event.getEntityLiving().ticksExisted % 10 != 0) return;
        EntityVillager villager = (EntityVillager) event.getEntityLiving();
        NBTTagCompound tag = villager.getEntityData();
        String faction = tag.getString(FACTION);
        if (faction.isEmpty()) return;
        for (EntityPlayer player : villager.world.playerEntities) {
            if (villager.getDistanceSq(player) < 96 * 96) discover(player, faction);
        }
        if (tag.getBoolean(COMPANION) && tag.hasUniqueId(COMPANION_OWNER)) {
            EntityPlayer owner = playerById(villager.world, tag.getUniqueId(COMPANION_OWNER));
            if (owner != null) companionUpdate(villager, owner);
            return;
        }
        EntityPlayer hostile = villager.world.getClosestPlayerToEntity(villager, 18);
        if (hostile != null && reputation(hostile, faction) <= HOSTILE_REPUTATION
                && !membership(hostile).equals(faction)) {
            attack(villager, hostile);
        } else if ("guard".equals(tag.getString(ROLE)) || "militia".equals(tag.getString(ROLE))) {
            List<EntityMob> threats = villager.world.getEntitiesWithinAABB(EntityMob.class,
                villager.getEntityBoundingBox().grow(12));
            if (!threats.isEmpty()) attack(villager, threats.get(0));
        }
    }

    private static void companionUpdate(EntityVillager villager, EntityPlayer owner) {
        EntityLivingBase target = owner.getRevengeTarget();
        if (target == null) target = owner.getLastAttackedEntity();
        if (target != null && target.isEntityAlive() && target != villager) {
            attack(villager, target);
            return;
        }
        double distance = villager.getDistanceSq(owner);
        if (distance > 28 * 28) villager.setPositionAndUpdate(owner.posX + 1, owner.posY, owner.posZ + 1);
        else if (distance > 4 * 4) villager.getNavigator().tryMoveToEntityLiving(owner, 1.1D);
    }

    private static void attack(EntityVillager attacker, EntityLivingBase target) {
        attacker.setAttackTarget(target);
        attacker.getNavigator().tryMoveToEntityLiving(target, 1.15D);
        int cooldown = attacker.getEntityData().getInteger("IndustrialAttackCooldown");
        if (cooldown > 0) {
            attacker.getEntityData().setInteger("IndustrialAttackCooldown", Math.max(0, cooldown - 10));
        } else if (attacker.getDistanceSq(target) < 2.6D * 2.6D && attacker.canEntityBeSeen(target)) {
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), 4.0F);
            attacker.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            attacker.getEntityData().setInteger("IndustrialAttackCooldown", 20);
        }
    }

    @Nullable
    private static EntityPlayer playerById(World world, UUID id) {
        for (EntityPlayer player : world.playerEntities) if (player.getUniqueID().equals(id)) return player;
        return null;
    }

    private static boolean owns(EntityPlayer player, EntityVillager villager) {
        return villager.getEntityData().hasUniqueId(COMPANION_OWNER)
            && player.getUniqueID().equals(villager.getEntityData().getUniqueId(COMPANION_OWNER));
    }

    private static void discover(EntityPlayer player, String faction) {
        NBTTagCompound data = ProgressionState.data(player);
        String key = "known_faction_" + faction;
        if (data.getBoolean(key)) return;
        data.setBoolean(key, true);
        player.sendStatusMessage(new TextComponentTranslation(
            "message.industrialcivilization.faction.discovered", definition(faction).name), false);
        if (player instanceof EntityPlayerMP) FactionNetwork.sendSnapshot((EntityPlayerMP) player);
        checkFactionContacts(player);
    }

    public static void discoverFaction(EntityPlayer player, String faction) {
        discover(player, faction);
    }

    private static void checkFactionContacts(EntityPlayer player) {
        if (known(player, "frontier_cooperative") && known(player, "survey_detachment_7")
                && known(player, "ashline_raiders")
                && ProgressionState.counter(player, "faction_trade_contacts") >= 1) {
            RuntimeAdvancements.grant(player, "faction_contacts");
        }
    }

    public static void updatePlaystyleReputation(EntityPlayer player) {
        NBTTagCompound data = ProgressionState.data(player);
        awardOnce(player, data, "rep_workshop_builder",
            ProgressionState.counter(player, "manual_crafts") >= 25,
            "frontier_cooperative", 15, "built a working settlement-scale workshop");
        awardOnce(player, data, "rep_industrial_builder",
            ProgressionState.counter(player, "manual_crafts") >= 80,
            "riverside_works", 20, "demonstrated sustained industrial construction");
        awardOnce(player, data, "rep_factory_restorer",
            ProgressionState.has(player, "abandoned_factory_operational"),
            "riverside_works", 25, "restored abandoned industry");
        awardOnce(player, data, "rep_orbital_research",
            ProgressionState.has(player, "orbital_research_archive"),
            "survey_detachment_7", 15, "produced orbital research");
        awardOnce(player, data, "rep_lunar_research",
            ProgressionState.has(player, "lunar_engineering_archive"),
            "survey_detachment_7", 15, "produced lunar research");
        awardOnce(player, data, "rep_martian_research",
            ProgressionState.has(player, "martian_autonomy_archive"),
            "survey_detachment_7", 15, "produced Martian autonomy research");
    }

    private static void awardOnce(EntityPlayer player, NBTTagCompound data, String key,
            boolean condition, String faction, int amount, String reason) {
        if (!condition || data.getBoolean(key)) return;
        data.setBoolean(key, true);
        adjustReputation(player, faction, amount, reason);
    }

    private static void giveCredits(EntityPlayer player, int amount) {
        give(player, IndustrialCivilizationCore.INDUSTRIAL_CREDIT, amount);
    }

    private static void give(EntityPlayer player, Item item, int amount) {
        ItemStack stack = new ItemStack(item, amount);
        if (!player.inventory.addItemStackToInventory(stack)) player.dropItem(stack, false);
    }

    private FactionSystem() {}
}
