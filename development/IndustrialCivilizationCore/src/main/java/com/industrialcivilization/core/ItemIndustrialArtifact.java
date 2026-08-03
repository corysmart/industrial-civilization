package com.industrialcivilization.core;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** A real, persistent research or engineering output. */
public final class ItemIndustrialArtifact extends Item {
    private final String id;
    private final boolean activatesMilestone;

    public ItemIndustrialArtifact(String id) {
        this(id, true);
    }

    public ItemIndustrialArtifact(String id, boolean activatesMilestone) {
        this.id = id;
        this.activatesMilestone = activatesMilestone;
        setRegistryName(IndustrialCivilizationCore.MODID, id);
        setUnlocalizedName(IndustrialCivilizationCore.MODID + "." + id);
        setCreativeTab(IndustrialCivilizationCore.CREATIVE_TAB);
        setMaxStackSize(id.endsWith("archive") || id.endsWith("authorization")
            || id.endsWith("core") || id.endsWith("system") ? 1 : 16);
    }

    public String getArtifactId() {
        return id;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return "artificial_industrial_intelligence_core".equals(id);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return hasContainerItem(itemStack) ? itemStack.copy() : ItemStack.EMPTY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip,
            ITooltipFlag flag) {
        tooltip.add(new TextComponentTranslation(
            "item.industrialcivilizationcore." + id + ".tooltip").getFormattedText());
        if (activatesMilestone) {
            tooltip.add(new TextComponentTranslation(
                "tooltip.industrialcivilizationcore.research_record").getFormattedText());
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (!world.isRemote && activatesMilestone) {
            ProgressionState.record(player, id);
            player.sendStatusMessage(new TextComponentTranslation(
                "message.industrialcivilization.recorded", held.getDisplayName()), true);
            if ("controlled_replication_record".equals(id)) {
                Item replicated = ForgeRegistries.ITEMS.getValue(new ResourceLocation(
                    "galacticraftplanets:item_basic_mars"));
                if (replicated != null) {
                    ItemStack output = new ItemStack(replicated, 1, 2);
                    if (!player.capabilities.isCreativeMode) held.shrink(1);
                    if (!player.inventory.addItemStackToInventory(output)) player.dropItem(output, false);
                    player.sendStatusMessage(new TextComponentTranslation(
                        "message.industrialcivilization.replication.released"), false);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }
}
