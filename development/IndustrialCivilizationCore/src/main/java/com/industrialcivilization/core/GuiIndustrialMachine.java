package com.industrialcivilization.core;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public final class GuiIndustrialMachine extends GuiContainer {
    private static final int TITLE_LEFT = 12;
    private static final int TITLE_WIDTH = 184;
    private static final int STATUS_LEFT = 37;
    private static final int STATUS_RIGHT = 190;
    private static final int STATUS_Y = 69;
    private static final int STATUS_COLOR = 0xD7E0E3;
    private static final int ENERGY_BAR_HEIGHT = 36;
    private static final int ENERGY_BAR_BOTTOM = 67;
    private static final int PROGRESS_LEFT = 125;
    private static final int PROGRESS_WIDTH = 23;
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        IndustrialCivilizationCore.MODID, "textures/gui/industrial_machine.png");
    private final TileIndustrialMachine tile;

    public GuiIndustrialMachine(InventoryPlayer inventory, TileIndustrialMachine tile) {
        super(new ContainerIndustrialMachine(inventory, tile));
        this.tile = tile;
        xSize = 208;
        ySize = 190;
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
        // The status row belongs inside the dark process display. Its left edge
        // clears the energy meter and its baseline stays above the panel border.
        fontRenderer.drawString(energy, STATUS_LEFT, STATUS_Y, STATUS_COLOR);
        String operations = "Ops " + IndustrialUiText.compactNumber(tile.getCompletedOperations());
        fontRenderer.drawString(operations,
            STATUS_RIGHT - fontRenderer.getStringWidth(operations), STATUS_Y, STATUS_COLOR);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);

        int energyHeight = tile.getCapacity() <= 0 ? 0
            : (int) ((long) ENERGY_BAR_HEIGHT * tile.getEnergyStored() / tile.getCapacity());
        drawTexturedModalRect(left + 21, top + ENERGY_BAR_BOTTOM - energyHeight,
            208, ENERGY_BAR_HEIGHT - energyHeight, 8, energyHeight);
        int progressWidth = tile.getDuration() <= 0 ? 0
            : PROGRESS_WIDTH * tile.getProgress() / tile.getDuration();
        drawTexturedModalRect(left + PROGRESS_LEFT, top + 39,
            208, 49, progressWidth, 16);
    }
}
