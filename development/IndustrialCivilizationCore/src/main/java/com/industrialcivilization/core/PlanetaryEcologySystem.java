package com.industrialcivilization.core;

import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
    private static final String SPACE_PIRATE = "IndustrialSpacePirate";
    private static final String SPACE_ENVIRONMENT = "IndustrialSpaceEnvironment";
    static final String FORCE_ROBBER_REPLACEMENT = "IndustrialForceRobberReplacement";
    static final String FORCE_PATROL_REPLACEMENT = "IndustrialForcePatrolReplacement";

    @SubscribeEvent
    public static void joined(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote || !(event.getEntity() instanceof EntityLivingBase)
                || event.getEntity() instanceof EntityPlayer) return;
        String dimension = event.getWorld().provider.getDimensionType().getName().toLowerCase(Locale.ROOT);
        if (dimension.contains("moon") || dimension.contains("mars")) {
            if (event.getEntity() instanceof EntitySpacePirate
                    || event.getEntity() instanceof EntitySpaceMilitia
                    || event.getEntity() instanceof EntitySpaceCitizen) return;
            if (event.getEntity() instanceof IMob) {
                replaceWithSpacePirate(event, (EntityLivingBase) event.getEntity(), dimension);
            } else if (!event.getEntity().getEntityData().hasKey("IndustrialFaction", 8)) {
                event.setCanceled(true);
            }
            return;
        }
        if (event.getWorld().provider.getDimension() != 0) return;
        if (event.getEntity() instanceof EntityRobber
                || event.getEntity() instanceof EntityMilitiaPatrol) return;
        if (event.getEntity() instanceof EntityZombie) {
            replaceWithRobber(event, (EntityZombie) event.getEntity());
            return;
        }
        if (event.getEntity() instanceof EntitySkeleton) {
            replaceWithPatrol(event, (EntitySkeleton) event.getEntity());
            return;
        }
        if (event.getEntity() instanceof IMob && isVanilla(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void replaceWithSpacePirate(EntityJoinWorldEvent event,
            EntityLivingBase original, String environment) {
        event.setCanceled(true);
        AxisAlignedBB area = original.getEntityBoundingBox().grow(64.0D, 24.0D, 64.0D);
        int nearby = original.world.getEntitiesWithinAABB(EntitySpacePirate.class, area).size();
        if (!GameplayRules.robberSpawnAllowed(original.world.rand.nextInt(100),
                IndustrialCivilizationCore.ROBBER_SPAWN_PERCENT, nearby,
                IndustrialCivilizationCore.ROBBER_LOCAL_CAP, false)) return;
        EntitySpacePirate pirate = new EntitySpacePirate(event.getWorld());
        copyPlacement(original, pirate);
        configureRobber(pirate, nearestStage(original));
        configureSpacePirate(pirate, environment);
        event.getWorld().spawnEntity(pirate);
    }

    private static void replaceWithRobber(EntityJoinWorldEvent event, EntityZombie original) {
        if (!shouldCreateRobber(original)) {
            event.setCanceled(true);
            return;
        }
        EntityRobber robber = new EntityRobber(event.getWorld());
        copyPlacement(original, robber);
        configureRobber(robber, nearestStage(original));
        event.setCanceled(true);
        event.getWorld().spawnEntity(robber);
    }

    private static boolean shouldCreateRobber(EntityZombie original) {
        boolean forced = original.getEntityData().getBoolean(FORCE_ROBBER_REPLACEMENT);
        AxisAlignedBB area = original.getEntityBoundingBox().grow(64.0D, 24.0D, 64.0D);
        int nearby = original.world.getEntitiesWithinAABB(EntityRobber.class, area).size();
        return GameplayRules.robberSpawnAllowed(original.world.rand.nextInt(100),
            IndustrialCivilizationCore.ROBBER_SPAWN_PERCENT, nearby,
            IndustrialCivilizationCore.ROBBER_LOCAL_CAP, forced);
    }

    private static void replaceWithPatrol(EntityJoinWorldEvent event, EntitySkeleton original) {
        if (!shouldCreatePatrol(original)) {
            event.setCanceled(true);
            return;
        }
        EntityMilitiaPatrol patrol = new EntityMilitiaPatrol(event.getWorld());
        copyPlacement(original, patrol);
        configurePatrol(patrol);
        event.setCanceled(true);
        event.getWorld().spawnEntity(patrol);
    }

    private static boolean shouldCreatePatrol(EntitySkeleton original) {
        boolean forced = original.getEntityData().getBoolean(FORCE_PATROL_REPLACEMENT);
        if (forced) return true;
        int radius = IndustrialCivilizationCore.MILITIA_PATROL_RADIUS;
        boolean nearOutpost = MilitiaOutpostRegistry.nearby(
            original.world, original.getPosition(), radius) != null;
        AxisAlignedBB area = original.getEntityBoundingBox().grow(radius, 64.0D, radius);
        int nearby = original.world.getEntitiesWithinAABB(EntityMilitiaPatrol.class, area).size();
        return GameplayRules.militiaPatrolSpawnAllowed(nearOutpost, nearby,
            IndustrialCivilizationCore.MILITIA_PATROL_LOCAL_CAP, forced);
    }

    private static void copyPlacement(EntityLivingBase original, EntityLivingBase replacement) {
        replacement.setLocationAndAngles(original.posX, original.posY, original.posZ,
            original.rotationYaw, original.rotationPitch);
        replacement.rotationYawHead = original.rotationYawHead;
        replacement.renderYawOffset = original.renderYawOffset;
    }

    private static boolean isVanilla(Entity entity) {
        ResourceLocation key = EntityList.getKey(entity);
        return key != null && GameplayRules.suppressVanillaEarthHostile(
            key.getResourceDomain(), key.getResourcePath());
    }

    static void configurePatrol(EntityMilitiaPatrol patrol) {
        NBTTagCompound tag = patrol.getEntityData();
        // AI task lists are reconstructed on entity load, so neutral targeting
        // must be removed on both first conversion and every subsequent load.
        patrol.targetTasks.taskEntries.clear();
        if (tag.getBoolean(PATROL)) return;
        tag.setBoolean(PATROL, true);
        patrol.setCustomNameTag("Territorial Militia Patrol Rifleman");
        patrol.setAlwaysRenderNameTag(false);
        ItemStack rifle = external("techguns:boltaction");
        if (rifle.isEmpty()) rifle = external("techguns:m4");
        if (rifle.isEmpty()) rifle = new ItemStack(Items.BOW);
        patrol.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, MarketEconomy.newCondition(rifle));
        equipArmor(patrol, "quantum");
        guaranteedDrops(patrol);
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

    private static void configureRobber(EntityRobber robber, int stage) {
        NBTTagCompound tag = robber.getEntityData();
        if (tag.getBoolean(ROBBER)) return;
        int tier = Math.max(1, Math.min(7, stage));
        tag.setBoolean(ROBBER, true);
        tag.setInteger(ROBBER_TIER, tier);
        robber.setCustomNameTag(tier >= 5 ? "Armed Robber" : tier >= 3 ? "Organized Robber" : "Robber");
        ItemStack weapon = tier >= 5 ? external("techguns:m4")
            : tier >= 3 ? external("techguns:pistol") : new ItemStack(Items.IRON_SWORD);
        if (weapon.isEmpty()) weapon = new ItemStack(tier >= 3 ? Items.BOW : Items.IRON_SWORD);
        robber.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, weapon);
        guaranteedDrops(robber);
        if (tier >= 4 && !tag.getBoolean("IndustrialSquadMember") && robber.world.rand.nextInt(8) == 0) {
            for (int index = 0; index < 2; index++) {
                EntityRobber member = new EntityRobber(robber.world);
                member.setPosition(robber.posX + index + 1, robber.posY, robber.posZ + index);
                member.getEntityData().setBoolean("IndustrialSquadMember", true);
                configureRobber(member, tier);
                robber.world.spawnEntity(member);
            }
        }
    }

    private static void configureSpacePirate(EntitySpacePirate pirate, String environment) {
        NBTTagCompound tag = pirate.getEntityData();
        tag.setBoolean(SPACE_PIRATE, true);
        tag.setString(SPACE_ENVIRONMENT, environment);
        boolean nano = GameplayRules.spacePirateUsesNanoSuit(pirate.world.rand.nextInt(100));
        pirate.setCustomNameTag(nano ? "NanoSuit Space Pirate" : "Space Pirate");
        equipArmor(pirate, nano ? "nano" : "astronaut");
        guaranteedDrops(pirate);
    }

    public static EntitySpacePirate spawnSpacePirate(net.minecraft.world.World world,
            double x, double y, double z) {
        EntitySpacePirate pirate = new EntitySpacePirate(world);
        pirate.setPosition(x, y, z);
        configureRobber(pirate, 7);
        configureSpacePirate(pirate,
            world.provider.getDimensionType().getName().toLowerCase(Locale.ROOT));
        world.spawnEntity(pirate);
        return pirate;
    }

    public static void equipSpaceMilitia(EntitySpaceMilitia militia) {
        configurePatrol(militia);
        militia.setCustomNameTag("QuantumSuit Space Militia");
        equipArmor(militia, "quantum");
        guaranteedDrops(militia);
    }

    public static EntitySpaceMilitia spawnSpaceMilitia(net.minecraft.world.World world,
            double x, double y, double z) {
        EntitySpaceMilitia militia = new EntitySpaceMilitia(world);
        militia.setPosition(x, y, z);
        equipSpaceMilitia(militia);
        world.spawnEntity(militia);
        return militia;
    }

    public static void equipQuantumSecurity(EntityLiving security) {
        equipArmor(security, "quantum");
        guaranteedDrops(security);
    }

    private static void equipArmor(EntityLiving living, String tier) {
        if ("quantum".equals(tier)) {
            setExternal(living, EntityEquipmentSlot.HEAD, "ic2:itemarmorquantumhelmet");
            setExternal(living, EntityEquipmentSlot.CHEST, "ic2:itemarmorquantumchestplate");
            setExternal(living, EntityEquipmentSlot.LEGS, "ic2:itemarmorquantumlegs");
            setExternal(living, EntityEquipmentSlot.FEET, "ic2:itemarmorquantumboots");
        } else if ("nano".equals(tier)) {
            setExternal(living, EntityEquipmentSlot.HEAD, "ic2:itemarmornanohelmet");
            setExternal(living, EntityEquipmentSlot.CHEST, "ic2:itemarmornanochestplate");
            setExternal(living, EntityEquipmentSlot.LEGS, "ic2:itemarmornanolegs");
            setExternal(living, EntityEquipmentSlot.FEET, "ic2:itemarmornanoboots");
        } else {
            setExternal(living, EntityEquipmentSlot.HEAD, "galacticraftcore:oxygen_mask");
            setExternal(living, EntityEquipmentSlot.CHEST, "galacticraftcore:oxygen_gear");
            living.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            living.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        }
    }

    private static void setExternal(EntityLiving living, EntityEquipmentSlot slot, String id) {
        ItemStack stack = external(id);
        if (!stack.isEmpty()) living.setItemStackToSlot(slot, stack);
    }

    public static void guaranteedDrops(EntityLiving living) {
        for (EntityEquipmentSlot slot : new EntityEquipmentSlot[] {
                EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND,
                EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET}) {
            living.setDropChance(slot, 2.0F);
        }
    }

    @SubscribeEvent
    public static void robberTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityRobber) || event.getEntityLiving().world.isRemote) return;
        EntityRobber robber = (EntityRobber) event.getEntityLiving();
        if (!robber.getEntityData().getBoolean(ROBBER)) return;
        int tier = robber.getEntityData().getInteger(ROBBER_TIER);
        double range = tier >= 3 ? 28.0D : 12.0D;
        EntityPlayer target = closestRobberTarget(robber, range);
        EntityPlayer retaliatingAgainst = robber.getRevengeTarget() instanceof EntityPlayer
            ? (EntityPlayer) robber.getRevengeTarget() : null;
        if (retaliatingAgainst != null && robber.getDistanceSq(retaliatingAgainst) <= range * range) {
            target = retaliatingAgainst;
        }
        if (target != null) {
            robber.setAttackTarget(target);
        } else if (robber.getAttackTarget() instanceof EntityPlayer) {
            robber.setAttackTarget(null);
        }
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
        if (!(event.getEntityLiving() instanceof EntityMilitiaPatrol) || event.getEntityLiving().world.isRemote) return;
        EntityMilitiaPatrol patrol = (EntityMilitiaPatrol) event.getEntityLiving();
        if (!patrol.getEntityData().getBoolean(PATROL)) return;
        patrol.extinguish();
        EntityPlayer target = null;
        double closest = Double.MAX_VALUE;
        for (EntityPlayer player : patrol.world.playerEntities) {
            double distance = patrol.getDistanceSq(player);
            if (distance > 36.0D * 36.0D) continue;
            FactionSystem.discoverFaction(player, "territorial_militia");
            if (distance >= closest) continue;
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

    private static EntityPlayer closestRobberTarget(EntityRobber robber, double range) {
        EntityPlayer nearest = null;
        double nearestDistance = range * range;
        for (EntityPlayer player : robber.world.playerEntities) {
            boolean spacePirate = robber.getEntityData().getBoolean(SPACE_PIRATE);
            boolean profitable = spacePirate
                ? GameplayRules.spacePirateTargetsPlayer(
                    robber.getEntityData().getString(SPACE_ENVIRONMENT),
                    MarketEconomy.carriesLunarStrategicResource(player),
                    MarketEconomy.carriesMartianStrategicResource(player), false)
                : GameplayRules.robberTargetsPlayer(MarketEconomy.carriesRobberLoot(player), false);
            if (player.isSpectator() || player.capabilities.isCreativeMode || !profitable) continue;
            double distance = robber.getDistanceSq(player);
            if (distance <= nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean isPatrolHostileTo(EntityMilitiaPatrol patrol, EntityPlayer player) {
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
        if (!(event.getEntityLiving() instanceof EntityMilitiaPatrol)
                || !event.getEntityLiving().getEntityData().getBoolean(PATROL)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)
                || event.getSource().isExplosion()) return;
        EntityMilitiaPatrol patrol = (EntityMilitiaPatrol) event.getEntityLiving();
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (!patrol.getEntityData().hasUniqueId(PATROL_AGGRESSOR)) {
            boolean arrow = event.getSource().getImmediateSource() instanceof EntityArrow;
            FactionSystem.adjustMilitiaPatrolReputation(player, arrow ? -1 : -2,
                arrow ? "shot militia patrol" : "attacked militia patrol");
        }
        for (EntityMilitiaPatrol member : patrol.world.getEntitiesWithinAABB(EntityMilitiaPatrol.class,
                patrol.getEntityBoundingBox().grow(24.0D), candidate ->
                    candidate.getEntityData().getBoolean(PATROL))) {
            member.getEntityData().setUniqueId(PATROL_AGGRESSOR, player.getUniqueID());
            member.setAttackTarget(player);
        }
    }

    private static void steal(EntityRobber robber) {
        BlockPos center = robber.getPosition();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-2, -1, -2), center.add(2, 2, 2))) {
            if (!canStealAt(robber, pos)) continue;
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
            if (!canStealAt(robber, pos)) continue;
            Block block = robber.world.getBlockState(pos).getBlock();
            Material material = robber.world.getBlockState(pos).getMaterial();
            if (material != Material.WOOD && block != Blocks.FURNACE && block != Blocks.CRAFTING_TABLE) continue;
            ItemStack taken = new ItemStack(block);
            if (!taken.isEmpty() && robber.world.destroyBlock(pos, false)) remember(robber, taken);
            return;
        }
    }

    /** Invokes the production theft path from disposable integrated-server tests. */
    static void stealForTest(EntityRobber robber) {
        steal(robber);
    }

    private static boolean canStealAt(EntityRobber robber, BlockPos pos) {
        Vec3d target = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        if (!GameplayRules.robberTheftWithinReach(robber.getPositionVector().squareDistanceTo(target))) {
            return false;
        }
        Vec3d eyes = new Vec3d(robber.posX, robber.posY + robber.getEyeHeight(), robber.posZ);
        RayTraceResult hit = robber.world.rayTraceBlocks(eyes, target, false, true, false);
        return hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK
            && pos.equals(hit.getBlockPos());
    }

    private static void remember(EntityRobber robber, ItemStack stack) {
        if (stack.isEmpty()) return;
        NBTTagCompound data = robber.getEntityData();
        NBTTagList stolen = data.getTagList(STOLEN, 10);
        stolen.appendTag(stack.serializeNBT());
        data.setTag(STOLEN, stolen);
    }

    @SubscribeEvent
    public static void robberDied(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityRobber)
                || !event.getEntityLiving().getEntityData().getBoolean(ROBBER)) return;
        NBTTagList stolen = event.getEntityLiving().getEntityData().getTagList(STOLEN, 10);
        for (int index = 0; index < stolen.tagCount(); index++) {
            ItemStack stack = new ItemStack(stolen.getCompoundTagAt(index));
            if (!stack.isEmpty()) event.getEntityLiving().entityDropItem(stack, 0.0F);
        }
    }

    @SubscribeEvent
    public static void patrolDied(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityMilitiaPatrol)
                || !event.getEntityLiving().getEntityData().getBoolean(PATROL)) return;
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getSource().isExplosion()) {
            EntityPlayer nearby = event.getEntityLiving().world.getClosestPlayerToEntity(event.getEntityLiving(), 48.0D);
            if (nearby != null) ProgressionState.increment(nearby, "militia_patrol_trap_kills", 1);
        } else {
            ProgressionState.increment((EntityPlayer) event.getSource().getTrueSource(),
                "militia_patrol_direct_kills", 1);
        }
    }

    /** Contextual salvage stays rare; issued equipment is handled by drop chances. */
    @SubscribeEvent
    public static void contextualHumanSalvage(LivingDeathEvent event) {
        EntityLivingBase dead = event.getEntityLiving();
        boolean robber = dead instanceof EntityRobber;
        boolean patrol = dead instanceof EntityMilitiaPatrol;
        boolean faction = dead.getEntityData().hasKey("IndustrialFaction", 8);
        if ((!robber && !patrol && !faction) || dead.world.isRemote) return;
        if (dead.world.rand.nextInt(4) == 0) dead.entityDropItem(
            new ItemStack(IndustrialCivilizationCore.INDUSTRIAL_CREDIT, 1 + dead.world.rand.nextInt(3)), 0.0F);
        if ((robber || patrol) && dead.world.rand.nextInt(8) == 0) {
            ItemStack ammunition = external("techguns:itemshared");
            if (!ammunition.isEmpty()) {
                ammunition.setItemDamage(2);
                ammunition.setCount(2 + dead.world.rand.nextInt(5));
                dead.entityDropItem(ammunition, 0.0F);
            }
        }
        if (dead.getEntityData().getBoolean(SPACE_PIRATE) && dead.world.rand.nextInt(10) == 0) {
            ItemStack oxygen = external("galacticraftcore:oxygen_gear");
            if (!oxygen.isEmpty()) dead.entityDropItem(oxygen, 0.0F);
        }
        String environment = dead.world.provider.getDimensionType().getName().toLowerCase(Locale.ROOT);
        if (environment.contains("moon") && dead.world.rand.nextInt(16) == 0) {
            ItemStack meteor = external("galacticraftcore:meteoric_iron_raw");
            if (!meteor.isEmpty()) dead.entityDropItem(meteor, 0.0F);
        } else if (environment.contains("mars") && dead.world.rand.nextInt(16) == 0) {
            ItemStack desh = external("galacticraftplanets:item_basic_mars");
            if (!desh.isEmpty()) {
                desh.setItemDamage(2);
                dead.entityDropItem(desh, 0.0F);
            }
        } else if (dead.world.provider.getDimension() == 0 && dead.world.rand.nextInt(12) == 0) {
            dead.entityDropItem(new ItemStack(IndustrialCivilizationCore.PRECISION_FRAME), 0.0F);
        }
    }

    /**
     * Livestock butchery supplies a modest renewable bone stream without
     * turning civilians, pets, or faction combatants into the optimal source.
     * Existing animal drops are untouched.
     */
    @SubscribeEvent
    public static void supplementalLivestockBone(LivingDeathEvent event) {
        EntityLivingBase dead = event.getEntityLiving();
        boolean livestock = dead instanceof EntityCow || dead instanceof EntityPig
            || dead instanceof EntitySheep || dead instanceof EntityHorse;
        boolean playerKill = event.getSource().getTrueSource() instanceof EntityPlayer;
        boolean child = dead instanceof net.minecraft.entity.EntityAgeable
            && ((net.minecraft.entity.EntityAgeable) dead).isChild();
        boolean pet = dead instanceof AbstractHorse && ((AbstractHorse) dead).isTame();
        if (!dead.world.isRemote && GameplayRules.supplementalBoneDrop(
                livestock && !pet, child, playerKill, dead.world.rand.nextInt(8))) {
            dead.entityDropItem(new ItemStack(Items.BONE), 0.0F);
        }
    }

    private static ItemStack external(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private PlanetaryEcologySystem() {}
}
