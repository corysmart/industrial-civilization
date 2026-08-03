package com.industrialcivilization.core;

import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Dimension ecology and the escalating Earth robber replacement for zombies. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class PlanetaryEcologySystem {
    private static final String ROBBER = "IndustrialRobber";
    private static final String ROBBER_TIER = "IndustrialRobberTier";
    private static final String STOLEN = "IndustrialStolenItems";

    @SubscribeEvent
    public static void joined(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityLivingBase)
                || event.getEntity() instanceof EntityPlayer) return;
        String dimension = event.getWorld().provider.getDimensionType().getName().toLowerCase(Locale.ROOT);
        if (dimension.contains("moon")) {
            event.setCanceled(true);
            return;
        }
        if (dimension.contains("mars")) {
            ResourceLocation key = EntityList.getKey(event.getEntity());
            String domain = key == null ? "" : key.getResourceDomain();
            boolean galacticraft = domain.startsWith("galacticraft");
            boolean postAiCitizen = event.getEntity().getEntityData().hasKey("IndustrialFaction", 8)
                && anyAiPlayer(event.getWorld().playerEntities);
            if (!galacticraft && !postAiCitizen) event.setCanceled(true);
            return;
        }
        if (event.getWorld().provider.getDimension() == 0
                && event.getEntity().getClass() == EntityZombie.class) {
            configureRobber((EntityZombie) event.getEntity(), nearestStage(event.getEntity()));
        }
    }

    private static boolean anyAiPlayer(List<EntityPlayer> players) {
        for (EntityPlayer player : players) if (MarketEconomy.playerStage(player) >= 7) return true;
        return false;
    }

    private static int nearestStage(Entity entity) {
        EntityPlayer player = entity.world.getClosestPlayerToEntity(entity, 256.0D);
        return player == null ? 1 : MarketEconomy.playerStage(player);
    }

    private static void configureRobber(EntityZombie robber, int stage) {
        NBTTagCompound tag = robber.getEntityData();
        if (tag.getBoolean(ROBBER)) return;
        int tier = Math.max(1, Math.min(7, stage));
        tag.setBoolean(ROBBER, true);
        tag.setInteger(ROBBER_TIER, tier);
        robber.setCustomNameTag(tier >= 5 ? "Armed Robber" : tier >= 3 ? "Organized Robber" : "Robber");
        robber.setBreakDoorsAItask(true);
        robber.enablePersistence();
        ItemStack weapon = tier >= 5 ? external("techguns:m4")
            : tier >= 3 ? external("techguns:pistol") : new ItemStack(Items.IRON_SWORD);
        if (weapon.isEmpty()) weapon = new ItemStack(tier >= 3 ? Items.BOW : Items.IRON_SWORD);
        robber.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, weapon);
        if (tier >= 4 && !tag.getBoolean("IndustrialSquadMember") && robber.world.rand.nextInt(4) == 0) {
            for (int index = 0; index < 2; index++) {
                EntityZombie member = new EntityZombie(robber.world);
                member.setPosition(robber.posX + index + 1, robber.posY, robber.posZ + index);
                member.getEntityData().setBoolean("IndustrialSquadMember", true);
                configureRobber(member, tier);
                robber.world.spawnEntity(member);
            }
        }
    }

    @SubscribeEvent
    public static void robberTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityZombie) || event.getEntityLiving().world.isRemote) return;
        EntityZombie robber = (EntityZombie) event.getEntityLiving();
        if (!robber.getEntityData().getBoolean(ROBBER)) return;
        int tier = robber.getEntityData().getInteger(ROBBER_TIER);
        EntityPlayer target = robber.world.getClosestPlayerToEntity(robber, tier >= 3 ? 28.0D : 12.0D);
        if (target != null) robber.setAttackTarget(target);
        if (target != null && tier >= 3 && robber.ticksExisted % Math.max(25, 70 - tier * 7) == 0
                && robber.getDistanceSq(target) > 25.0D && robber.canEntityBeSeen(target)) {
            target.attackEntityFrom(DamageSource.causeMobDamage(robber), 2.0F + tier * 0.65F);
            robber.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
        }
        if (target != null && tier >= 6 && robber.ticksExisted % 240 == 0
                && robber.getDistanceSq(target) > 36.0D
                && robber.world.getGameRules().getBoolean("mobGriefing")) {
            robber.world.createExplosion(robber, target.posX, target.posY, target.posZ, 1.5F, false);
        }
        if (robber.ticksExisted % 40 == 0 && robber.world.getGameRules().getBoolean("mobGriefing")) steal(robber);
    }

    private static void steal(EntityZombie robber) {
        BlockPos center = robber.getPosition();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-2, -1, -2), center.add(2, 2, 2))) {
            TileEntity tile = robber.world.getTileEntity(pos);
            if (!(tile instanceof IInventory)) continue;
            IInventory inventory = (IInventory) tile;
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (found.isEmpty()) continue;
                ItemStack taken = inventory.decrStackSize(slot, Math.min(found.getCount(), 1 + robber.world.rand.nextInt(3)));
                remember(robber, taken);
                return;
            }
        }
        if (robber.getEntityData().getInteger(ROBBER_TIER) < 3) return;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-1, -1, -1), center.add(1, 2, 1))) {
            Block block = robber.world.getBlockState(pos).getBlock();
            Material material = robber.world.getBlockState(pos).getMaterial();
            if (material != Material.WOOD && block != Blocks.FURNACE && block != Blocks.CRAFTING_TABLE) continue;
            ItemStack taken = new ItemStack(block);
            if (!taken.isEmpty() && robber.world.destroyBlock(pos, false)) remember(robber, taken);
            return;
        }
    }

    private static void remember(EntityZombie robber, ItemStack stack) {
        if (stack.isEmpty()) return;
        NBTTagCompound data = robber.getEntityData();
        NBTTagList stolen = data.getTagList(STOLEN, 10);
        stolen.appendTag(stack.serializeNBT());
        data.setTag(STOLEN, stolen);
    }

    @SubscribeEvent
    public static void robberDied(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityZombie)
                || !event.getEntityLiving().getEntityData().getBoolean(ROBBER)) return;
        NBTTagList stolen = event.getEntityLiving().getEntityData().getTagList(STOLEN, 10);
        for (int index = 0; index < stolen.tagCount(); index++) {
            ItemStack stack = new ItemStack(stolen.getCompoundTagAt(index));
            if (!stack.isEmpty()) event.getEntityLiving().entityDropItem(stack, 0.0F);
        }
    }

    private static ItemStack external(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private PlanetaryEcologySystem() {}
}
