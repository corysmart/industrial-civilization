package com.industrialcivilization.core;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * Keeps player input behind a loading overlay while client chunks arrive and
 * their render meshes compile. The world continues ticking underneath it.
 */
public final class GuiTerrainWarmup extends GuiScreen {
    private static final long MINIMUM_WARMUP_MS = 15000L;
    private static final long TIMEOUT_MS = 30000L;
    private static final int REQUIRED_STABLE_TICKS = 20;
    private final long startedAt = System.currentTimeMillis();
    private int loadedChunks;
    private int totalChunks = 1;
    private int stableTicks;

    @Override
    public void initGui() {
        buttonList.clear();
        int buttonWidth = Math.max(80, Math.min(150, width - 32));
        int buttonY = Math.min(height - 24, height / 2 + 42);
        buttonList.add(new GuiButton(0, width / 2 - buttonWidth / 2, buttonY, buttonWidth, 20,
            I18n.format("gui.industrialcivilization.terrain.enter_now")));
    }

    @Override
    public void updateScreen() {
        if (mc.world == null || mc.player == null) {
            finish();
            return;
        }
        // Four chunks matches the integrated server's minimum view distance.
        // Larger client distances stream after entry instead of holding the
        // player hostage while an enormous outer ring compiles.
        int radius = Math.max(2, Math.min(4, mc.gameSettings.renderDistanceChunks));
        int centerX = MathHelper.floor(mc.player.posX) >> 4;
        int centerZ = MathHelper.floor(mc.player.posZ) >> 4;
        loadedChunks = 0;
        totalChunks = (radius * 2 + 1) * (radius * 2 + 1);
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) loadedChunks++;
            }
        }
        long elapsed = System.currentTimeMillis() - startedAt;
        boolean ready = loadedChunks == totalChunks && elapsed >= MINIMUM_WARMUP_MS;
        stableTicks = ready ? stableTicks + 1 : 0;
        if (stableTicks >= REQUIRED_STABLE_TICKS
                || elapsed >= TIMEOUT_MS) {
            finish();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) finish();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) finish();
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int contentWidth = Math.max(80, Math.min(200, width - 32));
        drawWrappedCentered(I18n.format("gui.industrialcivilization.terrain.preparing"),
            height / 2 - 48, contentWidth, 0xE5F0EF);
        drawWrappedCentered(I18n.format("gui.industrialcivilization.terrain.detail",
            loadedChunks, totalChunks), height / 2 - 25, contentWidth, 0xA9C9C6);
        int barLeft = width / 2 - contentWidth / 2;
        int barTop = height / 2;
        int fillWidth = Math.max(0, contentWidth - 4);
        int fill = totalChunks <= 0 ? 0 : fillWidth * loadedChunks / totalChunks;
        drawRect(barLeft, barTop, barLeft + contentWidth, barTop + 12, 0xFF10191D);
        drawRect(barLeft + 2, barTop + 2, barLeft + 2 + fill, barTop + 10, 0xFF5BC4B8);
        drawWrappedCentered(I18n.format("gui.industrialcivilization.terrain.safe_skip"),
            height / 2 + 18, Math.max(80, width - 24), 0x82979B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawWrappedCentered(String text, int y, int maxWidth, int color) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(text, maxWidth);
        for (int index = 0; index < lines.size(); index++) {
            drawCenteredString(fontRenderer, lines.get(index), width / 2,
                y + index * (fontRenderer.FONT_HEIGHT + 2), color);
        }
    }

    private void finish() {
        mc.displayGuiScreen(null);
        if (mc.player != null) mc.setIngameFocus();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
