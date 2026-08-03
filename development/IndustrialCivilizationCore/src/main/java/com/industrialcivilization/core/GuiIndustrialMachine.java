package com.industrialcivilization.core;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public final class GuiIndustrialMachine extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        IndustrialCivilizationCore.MODID, "textures/gui/industrial_machine.png");
    private final TileIndustrialMachine tile;

    public GuiIndustrialMachine(InventoryPlayer inventory, TileIndustrialMachine tile) {
        super(new ContainerIndustrialMachine(inventory, tile));
        this.tile = tile;
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tile.getDisplayName().getUnformattedText(), 8, 6, 0x25333A);
        fontRenderer.drawString("EU " + tile.getEnergyStored() + "/" + tile.getCapacity(), 8, 65, 0x25333A);
        fontRenderer.drawString("Ops " + tile.getCompletedOperations(), 112, 65, 0x25333A);
        fontRenderer.drawString("Inventory", 8, 73, 0x404B50);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);

        int energyHeight = tile.getCapacity() <= 0 ? 0
            : (int) (48L * tile.getEnergyStored() / tile.getCapacity());
        drawTexturedModalRect(left + 17, top + 59 - energyHeight,
            176, 48 - energyHeight, 8, energyHeight);
        int progressWidth = tile.getDuration() <= 0 ? 0
            : 24 * tile.getProgress() / tile.getDuration();
        drawTexturedModalRect(left + 104, top + 35, 176, 49, progressWidth, 16);
    }
}
