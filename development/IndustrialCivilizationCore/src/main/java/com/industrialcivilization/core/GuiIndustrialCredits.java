package com.industrialcivilization.core;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/** The pack's post-AI credits, shown without ending or deleting the playable world. */
@SideOnly(Side.CLIENT)
public final class GuiIndustrialCredits extends GuiScreen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
        IndustrialCivilizationCore.MODID,
        "textures/mainmenu/industrial_civilization_background.png");
    private static final int BACKGROUND_WIDTH = 1672;
    private static final int BACKGROUND_HEIGHT = 941;

    private long openedAt;

    @Override
    public void initGui() {
        openedAt = System.currentTimeMillis();
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height - 28, 200, 20,
            I18n.format("gui.done")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawScaledCustomSizeModalRect(0, 0, 0, 0,
            BACKGROUND_WIDTH, BACKGROUND_HEIGHT, width, height,
            BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        drawGradientRect(0, 0, width, height, 0xB8000000, 0xD8000000);

        int scroll = (int) ((System.currentTimeMillis() - openedAt) / 55L);
        int y = height + 24 - scroll;
        y = line(I18n.format("credits.industrialcivilization.title"), y, 0xFFF0B35A, 2.0F);
        y += 18;
        y = line(I18n.format("credits.industrialcivilization.unlocked"), y, 0xFFB8EAF2, 1.0F);
        y += 34;
        y = line(I18n.format("credits.industrialcivilization.created_by"), y, 0xFFC8C8C8, 1.0F);
        y = line("corysmart", y, 0xFFFFFFFF, 2.0F);
        y += 34;
        y = line(I18n.format("credits.industrialcivilization.design"), y, 0xFFC8C8C8, 1.0F);
        y = line("corysmart", y, 0xFFFFFFFF, 1.0F);
        y += 34;
        y = line(I18n.format("credits.industrialcivilization.foundation"), y, 0xFFC8C8C8, 1.0F);
        y = line(I18n.format("credits.industrialcivilization.foundation_detail"), y, 0xFFFFFFFF, 1.0F);
        y += 34;
        y = line(I18n.format("credits.industrialcivilization.community"), y, 0xFFC8C8C8, 1.0F);
        y = line(I18n.format("credits.industrialcivilization.community_detail"), y, 0xFFFFFFFF, 1.0F);
        y += 48;
        line(I18n.format("credits.industrialcivilization.thanks"), y, 0xFFF0B35A, 1.0F);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int line(String text, int y, int color, float scale) {
        if (y > -30 && y < height + 30) {
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0F);
            drawCenteredString(fontRenderer, text, (int) (width / (2.0F * scale)),
                (int) (y / scale), color);
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
        return y + (int) (18 * scale);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
