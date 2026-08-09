package com.industrialcivilization.core;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public final class GuiIndustrialMachine extends GuiContainer {
    private static final int TITLE_LEFT = 32;
    private static final int TITLE_WIDTH = 136;
    private static final int STATUS_RIGHT = 168;
    private static final int STATUS_Y = 68;
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
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // GuiContainer does not provide the standard dimmed world background or
        // hovered-slot tooltip by itself. Vanilla container screens add both in
        // their concrete GUI class, and HEI also relies on BackgroundDrawnEvent
        // firing here to lay out its side panels correctly on narrow screens.
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = fontRenderer.trimStringToWidth(
            tile.getDisplayName().getUnformattedText(), TITLE_WIDTH);
        int titleX = TITLE_LEFT + (TITLE_WIDTH - fontRenderer.getStringWidth(title)) / 2;
        fontRenderer.drawString(title, titleX, 6, 0x25333A);
        String energy = "EU " + IndustrialUiText.compactNumber(tile.getEnergyStored())
            + "/" + IndustrialUiText.compactNumber(tile.getCapacity());
        // Reserve this entire strip for one normal-height status row. Trying to
        // fit a second "Inventory" row here either overlaps or mangles the
        // bitmap font; the slot grid already makes its purpose unambiguous.
        fontRenderer.drawString(energy, 8, STATUS_Y, 0x25333A);
        String operations = "Ops " + IndustrialUiText.compactNumber(tile.getCompletedOperations());
        fontRenderer.drawString(operations,
            STATUS_RIGHT - fontRenderer.getStringWidth(operations), STATUS_Y, 0x25333A);
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
