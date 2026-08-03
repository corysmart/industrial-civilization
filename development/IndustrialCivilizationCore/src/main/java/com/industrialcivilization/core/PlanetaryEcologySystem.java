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
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
    private static final String PATROL = "IndustrialMilitiaPatrol";
    private static final String PATROL_AGGRESSOR = "IndustrialPatrolAggressor";

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
        } else if (event.getWorld().provider.getDimension() == 0
                && event.getEntity().getClass() == EntitySkeleton.class) {
            configurePatrol((EntitySkeleton) event.getEntity());
        }
    }

    private static void configurePatrol(EntitySkeleton patrol) {
        NBTTagCompound tag = patrol.getEntityData();
        // AI task lists are reconstructed on entity load, so neutral targeting
        // must be removed on both first conversion and every subsequent load.
        patrol.targetTasks.taskEntries.clear();
        if (tag.getBoolean(PATROL)) return;
        tag.setBoolean(PATROL, true);
        patrol.setCustomNameTag("Civil Defense Patrol Rifleman");
        patrol.setAlwaysRenderNameTag(false);
        patrol.enablePersistence();
        ItemStack rifle = external("techguns:boltaction");
        if (rifle.isEmpty()) rifle = external("techguns:m4");
        if (rifle.isEmpty()) rifle = new ItemStack(Items.BOW);
        patrol.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, MarketEconomy.newCondition(rifle));
        patrol.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        patrol.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        patrol.setDropChance(EntityEquipmentSlot.MAINHAND, 0.35F);
        patrol.setDropChance(EntityEquipmentSlot.HEAD, 0.05F);
        patrol.setDropChance(EntityEquipmentSlot.CHEST, 0.05F);
        // Neutral patrols must not inherit skeleton target acquisition. Movement
        // and looking tasks remain, while targeting and fire are owned below.
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

    @SubscribeEvent
    public static void patrolTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntitySkeleton) || event.getEntityLiving().world.isRemote) return;
        EntitySkeleton patrol = (EntitySkeleton) event.getEntityLiving();
        if (!patrol.getEntityData().getBoolean(PATROL)) return;
        patrol.extinguish();
        EntityPlayer target = null;
        double closest = Double.MAX_VALUE;
        for (EntityPlayer player : patrol.world.playerEntities) {
            double distance = patrol.getDistanceSq(player);
            if (distance > 36.0D * 36.0D || distance >= closest) continue;
            if (isPatrolHostileTo(patrol, player)) { target = player; closest = distance; }
        }
        patrol.setAttackTarget(target);
        if (target == null) return;
        if (closest < 5.0D * 5.0D) patrol.getNavigator().tryMoveToEntityLiving(target, 0.9D);
        else if (closest > 22.0D * 22.0D) patrol.getNavigator().tryMoveToEntityLiving(target, 1.05D);
        else patrol.getNavigator().clearPath();
        int cooldown = patrol.getEntityData().getInteger("IndustrialPatrolFireCooldown");
        if (cooldown > 0) patrol.getEntityData().setInteger("IndustrialPatrolFireCooldown", cooldown - 1);
        else if (closest >= 4.0D * 4.0D && patrol.canEntityBeSeen(target)) {
            // Coordinated patrols kill a sword-rushing player quickly, but cover,
            // terrain, arrows, and environmental traps remain valid counters.
            EntityTippedArrow round = new EntityTippedArrow(patrol.world, patrol);
            double dx = target.posX - patrol.posX;
            double dz = target.posZ - patrol.posZ;
            double dy = target.getEntityBoundingBox().minY + target.height * 0.55D
                - round.posY + Math.sqrt(dx * dx + dz * dz) * 0.06D;
            round.setDamage(5.5D);
            round.shoot(dx, dy, dz, 2.15F, 1.5F);
            patrol.world.spawnEntity(round);
            patrol.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            patrol.getEntityData().setInteger("IndustrialPatrolFireCooldown", 24 + patrol.world.rand.nextInt(9));
        }
    }

    private static boolean isPatrolHostileTo(EntitySkeleton patrol, EntityPlayer player) {
        NBTTagCompound tag = patrol.getEntityData();
        return isArmedWithGun(player)
            || ProgressionState.counter(player, "militia_outposts_taken_down") >= 3
            || (tag.hasUniqueId(PATROL_AGGRESSOR)
                && player.getUniqueID().equals(tag.getUniqueId(PATROL_AGGRESSOR)));
    }

    public static boolean isArmedWithGun(EntityPlayer player) {
        for (net.minecraft.inventory.Slot slot : player.inventoryContainer.inventorySlots) {
            if (isGun(slot.getStack())) return true;
        }
        return false;
    }

    private static boolean isGun(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (implementsInterface(stack.getItem().getClass(), "techguns.api.guns.IGenericGun")) return true;
        String path = id.getResourcePath().toLowerCase(Locale.ROOT);
        if ("icbmclassic".equals(id.getResourceDomain())
                && (path.contains("rocketlauncher") || path.contains("ballisticlauncher"))) return true;
        String className = stack.getItem().getClass().getName().toLowerCase(Locale.ROOT);
        if (className.contains(".guns.") && !className.contains(".ammo.")) return true;
        return path.contains("pistol") || path.contains("rifle") || path.contains("shotgun")
            || path.contains("revolver") || path.contains("blaster") || path.contains("flamethrower")
            || path.contains("minigun") || path.contains("machinegun") || path.contains("laser_gun")
            || path.contains("lasergun") || path.contains("teslagun") || path.contains("tesla_gun")
            || path.contains("pulse_gun") || path.contains("pulserifle");
    }

    private static boolean implementsInterface(Class<?> type, String target) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Class<?> contract : current.getInterfaces()) {
                if (contract.getName().equals(target) || implementsInterface(contract, target)) return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void patrolAttacked(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntitySkeleton)
                || !event.getEntityLiving().getEntityData().getBoolean(PATROL)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)
                || event.getSource().isExplosion()) return;
        EntitySkeleton patrol = (EntitySkeleton) event.getEntityLiving();
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (!patrol.getEntityData().hasUniqueId(PATROL_AGGRESSOR)) {
            boolean arrow = event.getSource().getImmediateSource() instanceof EntityArrow;
            FactionSystem.adjustMilitiaPatrolReputation(player, arrow ? -1 : -2,
                arrow ? "shot militia patrol" : "attacked militia patrol");
        }
        for (EntitySkeleton member : patrol.world.getEntitiesWithinAABB(EntitySkeleton.class,
                patrol.getEntityBoundingBox().grow(24.0D), candidate ->
                    candidate.getEntityData().getBoolean(PATROL))) {
            member.getEntityData().setUniqueId(PATROL_AGGRESSOR, player.getUniqueID());
            member.setAttackTarget(player);
        }
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

    @SubscribeEvent
    public static void patrolDied(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntitySkeleton)
                || !event.getEntityLiving().getEntityData().getBoolean(PATROL)) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getSource().isExplosion()) {
            EntityPlayer nearby = event.getEntityLiving().world.getClosestPlayerToEntity(event.getEntityLiving(), 48.0D);
            if (nearby != null) ProgressionState.increment(nearby, "militia_patrol_trap_kills", 1);
        } else {
            ProgressionState.increment((EntityPlayer) event.getSource().getTrueSource(),
                "militia_patrol_direct_kills", 1);
        }
    }

    private static ItemStack external(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private PlanetaryEcologySystem() {}
}
