package com.industrialcivilization.core;

import ic2.api.classic.item.IMiningDrill;
import ic2.api.item.ElectricItem;
import ic2.core.item.base.ItemElectricTool;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Balanced tree felling and plane mining for the pack's established tools. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class ToolAreaHandler {
    private static final String HIT_POS = "ic_area_hit_pos";
    private static final String HIT_FACE = "ic_area_hit_face";
    private static final String HIT_TICK = "ic_area_hit_tick";
    private static final int TREE_LIMIT = 96;
    private static final int TREE_RADIUS = 8;
    private static final Set<UUID> ACTIVE_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public static void rememberMiningFace(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote || event.getFace() == null) return;
        NBTTagCompound data = event.getEntityPlayer().getEntityData();
        data.setLong(HIT_POS, event.getPos().toLong());
        data.setInteger(HIT_FACE, event.getFace().getIndex());
        data.setInteger(HIT_TICK, event.getEntityPlayer().ticksExisted);
    }

    @SubscribeEvent
    public static void breakConnectedBlocks(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (!(player instanceof EntityPlayerMP) || player.world.isRemote
                || event.isCanceled() || ACTIVE_PLAYERS.contains(player.getUniqueID())) return;

        ItemStack tool = player.getHeldItemMainhand();
        if (tool.isEmpty() || player.isSneaking()) return;
        if (isTreeTool(tool, player, event.getState())) {
            fellTree((EntityPlayerMP) player, event.getPos(), tool);
            return;
        }

        int radius = drillRadius(tool);
        if (radius > 0) minePlane((EntityPlayerMP) player, event.getPos(), tool, radius);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void describeToolModes(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (isIc2Tool(stack, "itemtoolchainsaw")) {
            event.getToolTip().add(I18n.format("tooltip.industrialcivilization.tree_chainsaw"));
        } else if (isIc2Tool(stack, "itemdrills")) {
            event.getToolTip().add(I18n.format(stack.getMetadata() == 1
                ? "tooltip.industrialcivilization.diamond_drill_area"
                : "tooltip.industrialcivilization.drill_area"));
        } else if (event.getEntityPlayer() != null && stack.getItem().getToolClasses(stack).contains("axe")
                && stack.getItem().getHarvestLevel(stack, "axe", event.getEntityPlayer(),
                    Blocks.LOG.getDefaultState()) >= 1) {
            event.getToolTip().add(I18n.format("tooltip.industrialcivilization.tree_axe"));
        }
    }

    private static boolean isTreeTool(ItemStack stack, EntityPlayer player, IBlockState state) {
        if (isIc2Tool(stack, "itemtoolchainsaw")) return true;
        Item item = stack.getItem();
        return item.getToolClasses(stack).contains("axe")
            && item.getHarvestLevel(stack, "axe", player, state) >= 1;
    }

    private static int drillRadius(ItemStack stack) {
        if (!isIc2Tool(stack, "itemdrills")) return 0;
        return stack.getMetadata() == 1 ? 4 : stack.getMetadata() == 0 ? 1 : 0;
    }

    private static boolean isIc2Tool(ItemStack stack, String path) {
        ResourceLocation id = stack.getItem().getRegistryName();
        return id != null && "ic2".equals(id.getResourceDomain()) && path.equals(id.getResourcePath());
    }

    private static void fellTree(EntityPlayerMP player, BlockPos origin, ItemStack initialTool) {
        World world = player.world;
        if (!world.getBlockState(origin).getBlock().isWood(world, origin)) return;
        List<BlockPos> logs = connectedLogs(world, origin);
        if (logs.size() <= 1 || !hasLeaves(world, logs)) return;
        logs.remove(origin);
        logs.sort(Comparator.comparingInt(BlockPos::getY).reversed());
        harvest(player, logs, initialTool, false);
    }

    private static List<BlockPos> connectedLogs(World world, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(origin); visited.add(origin);
        while (!pending.isEmpty() && result.size() < TREE_LIMIT) {
            BlockPos current = pending.removeFirst();
            if (!world.isBlockLoaded(current)
                    || !world.getBlockState(current).getBlock().isWood(world, current)) continue;
            result.add(current);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = current.add(dx, dy, dz);
                        if (next.getY() < origin.getY() - 1
                                || Math.abs(next.getX() - origin.getX()) > TREE_RADIUS
                                || Math.abs(next.getZ() - origin.getZ()) > TREE_RADIUS) continue;
                        if (visited.add(next)) pending.addLast(next);
                    }
                }
            }
        }
        return result;
    }

    private static boolean hasLeaves(World world, List<BlockPos> logs) {
        for (BlockPos log : logs) {
            for (BlockPos check : BlockPos.getAllInBoxMutable(log.add(-2, -1, -2), log.add(2, 2, 2))) {
                if (world.isBlockLoaded(check) && world.getBlockState(check).getBlock().isLeaves(
                        world.getBlockState(check), world, check)) return true;
            }
        }
        return false;
    }

    private static void minePlane(EntityPlayerMP player, BlockPos origin, ItemStack initialTool, int radius) {
        EnumFacing face = rememberedFace(player, origin);
        if (face == null || !(initialTool.getItem() instanceof IMiningDrill)) return;
        List<BlockPos> targets = new ArrayList<>();
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                if (first == 0 && second == 0) continue;
                BlockPos target;
                switch (face.getAxis()) {
                    case X: target = origin.add(0, first, second); break;
                    case Y: target = origin.add(first, 0, second); break;
                    default: target = origin.add(first, second, 0); break;
                }
                if (!player.world.isBlockLoaded(target)) continue;
                IBlockState state = player.world.getBlockState(target);
                if (state.getBlockHardness(player.world, target) < 0
                        || state.getBlock().hasTileEntity(state)) continue;
                if (((IMiningDrill) initialTool.getItem()).canMineBlock(
                        initialTool, state, player.world, target)) targets.add(target);
            }
        }
        targets.sort(Comparator.comparingDouble(pos -> pos.distanceSq(origin)));
        harvest(player, targets, initialTool, true);
    }

    private static EnumFacing rememberedFace(EntityPlayer player, BlockPos origin) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(HIT_POS) || BlockPos.fromLong(data.getLong(HIT_POS)).distanceSq(origin) != 0
                || player.ticksExisted - data.getInteger(HIT_TICK) > 20) return null;
        return EnumFacing.getFront(data.getInteger(HIT_FACE));
    }

    private static void harvest(EntityPlayerMP player, List<BlockPos> targets,
                                ItemStack initialTool, boolean requireDrill) {
        UUID id = player.getUniqueID();
        ACTIVE_PLAYERS.add(id);
        try {
            for (BlockPos target : targets) {
                ItemStack current = player.getHeldItemMainhand();
                if (current.isEmpty() || current.getItem() != initialTool.getItem()
                        || current.getMetadata() != initialTool.getMetadata()) break;
                if (current.getItem() instanceof ItemElectricTool) {
                    int cost = ((ItemElectricTool) current.getItem()).getEnergyCost(current);
                    if (!ElectricItem.manager.canUse(current, cost)) break;
                } else if (current.isItemStackDamageable()
                        && current.getItemDamage() >= current.getMaxDamage() - 1) break;
                if (requireDrill && (!(current.getItem() instanceof IMiningDrill)
                        || !((IMiningDrill) current.getItem()).canMineBlock(current,
                            player.world.getBlockState(target), player.world, target))) continue;
                player.interactionManager.tryHarvestBlock(target);
            }
        } finally {
            ACTIVE_PLAYERS.remove(id);
        }
    }

    private ToolAreaHandler() {}
}
