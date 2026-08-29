package com.industrialcivilization.core;

import java.io.IOException;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public final class GuiIndustrialMachine extends GuiContainer {
    private static final int BASE_WIDTH = 208;
    private static final int BASE_HEIGHT = 190;
    private static final int TITLE_LEFT = 12;
    private static final int TITLE_WIDTH = 184;
    private static final int STATUS_LEFT = 37;
    private static final int STATUS_RIGHT = 190;
    private static final int STATUS_Y = 80;
    private static final int STATUS_COLOR = 0xD7E0E3;
    private static final int ENERGY_BAR_HEIGHT = 36;
    private static final int ENERGY_BAR_BOTTOM = 67;
    private static final int PROGRESS_LEFT = 125;
    private static final int PROGRESS_WIDTH = 23;
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        IndustrialCivilizationCore.MODID, "textures/gui/industrial_machine.png");
    private final TileIndustrialMachine tile;
    private float interfaceScale = 1.0F;

    public GuiIndustrialMachine(InventoryPlayer inventory, TileIndustrialMachine tile) {
        super(new ContainerIndustrialMachine(inventory, tile));
        this.tile = tile;
        xSize = BASE_WIDTH;
        ySize = BASE_HEIGHT;
    }

    @Override
    public void initGui() {
        interfaceScale = GameplayRules.machineGuiScale(width, height);
        xSize = Math.round(BASE_WIDTH * interfaceScale);
        ySize = Math.round(BASE_HEIGHT * interfaceScale);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // GuiContainer does not provide the standard dimmed world background or
        // hovered-slot tooltip by itself. Vanilla container screens add both in
        // their concrete GUI class, and HEI also relies on BackgroundDrawnEvent
        // firing here to lay out its side panels correctly on narrow screens.
        drawDefaultBackground();
        int virtualMouseX = toVirtualX(mouseX);
        int virtualMouseY = toVirtualY(mouseY);
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        GlStateManager.scale(interfaceScale, interfaceScale, 1.0F);
        GlStateManager.translate(-guiLeft, -guiTop, 0.0F);
        super.drawScreen(virtualMouseX, virtualMouseY, partialTicks);
        renderHoveredToolTip(virtualMouseX, virtualMouseY);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = fontRenderer.trimStringToWidth(
            tile.getOperationsGuiTitle(), TITLE_WIDTH);
        int titleX = TITLE_LEFT + (TITLE_WIDTH - fontRenderer.getStringWidth(title)) / 2;
        fontRenderer.drawString(title, titleX, 6, 0x25333A);
        if (tile.hasLocalOperationsStatus()) {
            String[] lines = tile.getOperationsGuiLines();
            for (int index = 0; index < Math.min(3, lines.length); index++) {
                String line = fontRenderer.trimStringToWidth(lines[index], STATUS_RIGHT - STATUS_LEFT);
                fontRenderer.drawString(line, STATUS_LEFT, 58 + index * 11, STATUS_COLOR);
            }
            return;
        }
        String input = "Input " + IndustrialUiText.compactNumber(tile.getAcceptedEUThisTick())
            + " EU/t  " + IndustrialUiText.speedMultiplier(tile.getEffectiveSpeedMultiplier());
        fontRenderer.drawString(input, STATUS_LEFT, 58, STATUS_COLOR);
        String work = "Work " + IndustrialUiText.compactNumber(tile.getWorkCompletedEU())
            + "/" + IndustrialUiText.compactNumber(tile.getWorkRequiredEU())
            + "  ETA " + IndustrialUiText.ticksAsEta(tile.getEstimatedTicksRemaining());
        fontRenderer.drawString(work, STATUS_LEFT, 69, STATUS_COLOR);
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
        int left = guiLeft;
        int top = guiTop;
        drawTexturedModalRect(left, top, 0, 0, BASE_WIDTH, BASE_HEIGHT);

        int energyHeight = tile.getCapacity() <= 0 ? 0
            : (int) ((long) ENERGY_BAR_HEIGHT * tile.getEnergyStored() / tile.getCapacity());
        drawTexturedModalRect(left + 21, top + ENERGY_BAR_BOTTOM - energyHeight,
            208, ENERGY_BAR_HEIGHT - energyHeight, 8, energyHeight);
        int progressWidth = tile.getWorkRequiredEU() <= 0 ? 0
            : (int) (PROGRESS_WIDTH * tile.getWorkCompletedEU() / tile.getWorkRequiredEU());
        drawTexturedModalRect(left + PROGRESS_LEFT, top + 39,
            208, 49, progressWidth, 16);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(toVirtualX(mouseX), toVirtualY(mouseY), mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
            long timeSinceLastClick) {
        super.mouseClickMove(toVirtualX(mouseX), toVirtualY(mouseY),
            clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(toVirtualX(mouseX), toVirtualY(mouseY), state);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeft,
            int guiTop) {
        return mouseX < guiLeft || mouseY < guiTop
            || mouseX >= guiLeft + BASE_WIDTH || mouseY >= guiTop + BASE_HEIGHT;
    }

    private int toVirtualX(int mouseX) {
        return guiLeft + Math.round((mouseX - guiLeft) / interfaceScale);
    }

    private int toVirtualY(int mouseY) {
        return guiTop + Math.round((mouseY - guiTop) / interfaceScale);
    }
}
