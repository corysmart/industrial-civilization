package com.industrialcivilization.core;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * Keeps player input behind a loading overlay while client chunks arrive and
 * their render meshes compile. The world continues ticking underneath it.
 */
public final class GuiTerrainWarmup extends GuiScreen {
    private static final long TIMEOUT_MS = 60000L;
    private static final int REQUIRED_STABLE_TICKS = 10;
    private final long startedAt = System.currentTimeMillis();
    private int loadedChunks;
    private int totalChunks = 1;
    private int stableTicks;

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 75, height / 2 + 42, 150, 20,
            I18n.format("gui.industrialcivilization.terrain.enter_now")));
    }

    @Override
    public void updateScreen() {
        if (mc.world == null || mc.player == null) {
            finish();
            return;
        }
        int radius = Math.max(2, Math.min(8, mc.gameSettings.renderDistanceChunks));
        int centerX = MathHelper.floor(mc.player.posX) >> 4;
        int centerZ = MathHelper.floor(mc.player.posZ) >> 4;
        loadedChunks = 0;
        totalChunks = (radius * 2 + 1) * (radius * 2 + 1);
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) loadedChunks++;
            }
        }
        boolean ready = loadedChunks == totalChunks && mc.renderGlobal.hasNoChunkUpdates();
        stableTicks = ready ? stableTicks + 1 : 0;
        if (stableTicks >= REQUIRED_STABLE_TICKS
                || System.currentTimeMillis() - startedAt >= TIMEOUT_MS) {
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
        drawCenteredString(fontRenderer,
            I18n.format("gui.industrialcivilization.terrain.preparing"),
            width / 2, height / 2 - 38, 0xE5F0EF);
        drawCenteredString(fontRenderer,
            I18n.format("gui.industrialcivilization.terrain.detail", loadedChunks, totalChunks),
            width / 2, height / 2 - 20, 0xA9C9C6);
        int barLeft = width / 2 - 100;
        int barTop = height / 2;
        int fill = totalChunks <= 0 ? 0 : 196 * loadedChunks / totalChunks;
        drawRect(barLeft, barTop, barLeft + 200, barTop + 12, 0xFF10191D);
        drawRect(barLeft + 2, barTop + 2, barLeft + 2 + fill, barTop + 10, 0xFF5BC4B8);
        drawCenteredString(fontRenderer,
            I18n.format("gui.industrialcivilization.terrain.safe_skip"),
            width / 2, height / 2 + 20, 0x82979B);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
