package com.industrialcivilization.core;

import java.util.List;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;

public final class TileFactoryControlTerminal extends TileEntity {
    private int stage;

    public void interact(EntityPlayer player) {
        if (stage == 0) {
            FactionSystem.discoverFaction(player, "ashline_raiders");
            spawnCriminalNetwork();
            give(player, IndustrialCivilizationCore.UNDERWORLD_DOSSIER);
            ProgressionState.record(player, "underworld_lead");
            stage = 1;
            markDirty();
            message(player, "message.industrialcivilization.factory.encounter_started");
            return;
        }
        if (stage == 1) {
            List<EntityVindicator> remaining = world.getEntitiesWithinAABB(EntityVindicator.class,
                new AxisAlignedBB(pos).grow(18), entity -> entity.getEntityData().getBoolean("IndustrialCriminal"));
            if (!remaining.isEmpty()) {
                player.sendStatusMessage(new TextComponentTranslation(
                    "message.industrialcivilization.factory.hostiles_remaining", remaining.size()), false);
                return;
            }
            give(player, IndustrialCivilizationCore.CRIMINAL_NETWORK_LEDGER);
            ProgressionState.record(player, "criminal_network_defeated");
            ProgressionState.record(player, "abandoned_factory_discovered");
            stage = 2;
            markDirty();
            message(player, "message.industrialcivilization.factory.site_secured");
            return;
        }
        if (stage == 2) {
            if (!has(player, Items.IRON_INGOT, 16) || !has(player, Items.REDSTONE, 8)) {
                message(player, "message.industrialcivilization.factory.repair_requirements");
                return;
            }
            consume(player, Items.IRON_INGOT, 16);
            consume(player, Items.REDSTONE, 8);
            give(player, IndustrialCivilizationCore.FACTORY_RESTORATION_CERTIFICATE);
            ProgressionState.record(player, "abandoned_factory_operational");
            stage = 3;
            markDirty();
            message(player, "message.industrialcivilization.factory.restored");
            return;
        }
        if (stage == 3) {
            if (!has(player, IndustrialCivilizationCore.CONTROL_PROCESSOR, 1)) {
                message(player, "message.industrialcivilization.factory.controller_requirement");
                return;
            }
            consume(player, IndustrialCivilizationCore.CONTROL_PROCESSOR, 1);
            give(player, IndustrialCivilizationCore.RECOVERED_FACTORY_CONTROL_SYSTEM);
            ProgressionState.record(player, "recovered_factory_control_system");
            stage = 4;
            markDirty();
            message(player, "message.industrialcivilization.factory.controller_recovered");
            return;
        }
        message(player, "message.industrialcivilization.factory.online");
    }

    private void spawnCriminalNetwork() {
        for (int index = 0; index < 4; index++) {
            EntityVindicator criminal = new EntityVindicator(world);
            double angle = Math.PI * 2 * index / 4.0;
            criminal.setPosition(pos.getX() + 0.5 + Math.cos(angle) * 5,
                pos.getY() + 1, pos.getZ() + 0.5 + Math.sin(angle) * 5);
            criminal.setCustomNameTag(index == 0 ? "Criminal Factory Overseer" : "Criminal Industrial Enforcer");
            criminal.setAlwaysRenderNameTag(true);
            criminal.getEntityData().setBoolean("IndustrialCriminal", true);
            criminal.getEntityData().setString("IndustrialFaction", "ashline_raiders");
            criminal.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            PlanetaryEcologySystem.equipQuantumSecurity(criminal);
            PlanetaryEcologySystem.guaranteedDrops(criminal);
            world.spawnEntity(criminal);
        }
    }

    private static boolean has(EntityPlayer player, Item item, int count) {
        int found = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!stack.isEmpty() && stack.getItem() == item) found += stack.getCount();
        }
        return found >= count;
    }

    private static void consume(EntityPlayer player, Item item, int count) {
        if (player.capabilities.isCreativeMode) return;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.isEmpty() || stack.getItem() != item) continue;
            int taken = Math.min(count, stack.getCount());
            stack.shrink(taken);
            count -= taken;
            if (count <= 0) break;
        }
    }

    private static void give(EntityPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        if (!player.inventory.addItemStackToInventory(stack)) player.dropItem(stack, false);
    }

    private static void message(EntityPlayer player, String key) {
        player.sendStatusMessage(new TextComponentTranslation(key), false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Stage", stage);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        stage = compound.getInteger("Stage");
    }
}
