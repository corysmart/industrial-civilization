package com.industrialcivilization.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Radiation makes sealed, powered habitats a real space-progression requirement. */
@Mod.EventBusSubscriber(modid = IndustrialCivilizationCore.MODID)
public final class SpaceSurvivalSystem {
    private static final DamageSource RADIATION = new DamageSource("industrialRadiation")
        .setDamageBypassesArmor();

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % 100 != 0 || !isExposedDimension(player)) return;
        if (player.capabilities.isCreativeMode || protectedByHabitat(player) || fullQuantumSuit(player)) {
            ProgressionState.data(player).setInteger("radiation_exposure", 0);
            return;
        }
        int exposure = ProgressionState.data(player).getInteger("radiation_exposure") + 1;
        ProgressionState.data(player).setInteger("radiation_exposure", exposure);
        if (exposure == 1) {
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.radiation.warning"), false);
        }
        if (exposure >= 6) {
            player.attackEntityFrom(RADIATION, 2.0F);
            if (exposure % 6 == 0) player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.radiation.damage"), false);
        }
    }

    private static boolean isExposedDimension(EntityPlayer player) {
        String name = player.world.provider.getDimensionType().getName().toLowerCase();
        return name.contains("moon") || name.contains("mars") || name.contains("orbit")
            || name.contains("space station");
    }

    private static boolean protectedByHabitat(EntityPlayer player) {
        if (player.world.canSeeSky(player.getPosition())) return false;
        BlockPos center = player.getPosition();
        for (BlockPos position : BlockPos.getAllInBoxMutable(center.add(-10, -6, -10),
                center.add(10, 6, 10))) {
            TileEntity tile = player.world.getTileEntity(position);
            if (tile == null || !"TileEntityOxygenSealer".equals(tile.getClass().getSimpleName())) continue;
            try {
                boolean active = tile.getClass().getField("active").getBoolean(tile);
                boolean sealed = tile.getClass().getField("sealed").getBoolean(tile);
                if (active && sealed) return true;
            } catch (ReflectiveOperationException ignored) {
                IndustrialCivilizationCore.LOGGER.warn("Could not read Galacticraft sealer state", ignored);
            }
        }
        return false;
    }

    private static boolean fullQuantumSuit(EntityPlayer player) {
        int pieces = 0;
        for (ItemStack stack : player.inventory.armorInventory) {
            if (!stack.isEmpty() && stack.getItem().getRegistryName() != null
                    && stack.getItem().getRegistryName().toString().startsWith("ic2:itemarmorquantum")) {
                pieces++;
            }
        }
        return pieces == 4;
    }

    private SpaceSurvivalSystem() {}
}
