package com.industrialcivilization.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.advancements.GuiAdvancement;
import net.minecraft.client.gui.advancements.GuiAdvancementTab;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Responsive advancement browser for the pack's wide unified campaign tree. */
public final class GuiIndustrialAdvancements extends GuiScreenAdvancements {
    private static final Set<GuiAdvancementTab> ARRANGED_TABS = Collections.newSetFromMap(
        new WeakHashMap<GuiAdvancementTab, Boolean>());
    private static final Field SELECTED_TAB = ReflectionHelper.findField(
        GuiScreenAdvancements.class, "selectedTab", "field_191940_s");
    private static final Field ROOT = ReflectionHelper.findField(
        GuiAdvancementTab.class, "root", "field_191809_l");
    private static final Field GUIS = ReflectionHelper.findField(
        GuiAdvancementTab.class, "guis", "field_191810_m");
    private static final Field SCROLL_X = ReflectionHelper.findField(
        GuiAdvancementTab.class, "scrollX", "field_191811_n");
    private static final Field SCROLL_Y = ReflectionHelper.findField(
        GuiAdvancementTab.class, "scrollY", "field_191812_o");
    private static final Field MIN_X = ReflectionHelper.findField(
        GuiAdvancementTab.class, "minX", "field_193939_q");
    private static final Field MIN_Y = ReflectionHelper.findField(
        GuiAdvancementTab.class, "minY", "field_193940_r");
    private static final Field MAX_X = ReflectionHelper.findField(
        GuiAdvancementTab.class, "maxX", "field_191813_p");
    private static final Field MAX_Y = ReflectionHelper.findField(
        GuiAdvancementTab.class, "maxY", "field_191814_q");
    private static final Field GUI_X = ReflectionHelper.findField(
        GuiAdvancement.class, "x", "field_191837_o");
    private static final Field GUI_Y = ReflectionHelper.findField(
        GuiAdvancement.class, "y", "field_191826_p");

    private boolean dragging;
    private int dragMouseX;
    private int dragMouseY;

    public GuiIndustrialAdvancements(ClientAdvancementManager manager) {
        super(manager);
    }

    /** Fold vanilla's extremely tall auto-layout into readable two-axis columns. */
    @SuppressWarnings("unchecked")
    static void arrangeWideCampaign(GuiAdvancementTab tab) throws IllegalAccessException {
        if (!ARRANGED_TABS.add(tab)) return;
        List<Map.Entry<Advancement, GuiAdvancement>> nodes = new ArrayList<>(
            ((Map<Advancement, GuiAdvancement>) GUIS.get(tab)).entrySet());
        nodes.sort(Comparator
            .comparingInt((Map.Entry<Advancement, GuiAdvancement> entry) -> {
                try { return GUI_Y.getInt(entry.getValue()); }
                catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
            })
            .thenComparingInt(entry -> {
                try { return GUI_X.getInt(entry.getValue()); }
                catch (IllegalAccessException exception) { throw new IllegalStateException(exception); }
            })
            .thenComparing(entry -> entry.getKey().getId().toString()));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int index = 0; index < nodes.size(); index++) {
            GuiAdvancement gui = nodes.get(index).getValue();
            int originalDepth = Math.max(0, GUI_X.getInt(gui) / 28);
            int x = (index / 12) * 180 + Math.min(3, originalDepth) * 34;
            int y = (index % 12) * 52;
            GUI_X.setInt(gui, x);
            GUI_Y.setInt(gui, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + 28);
            maxY = Math.max(maxY, y + 27);
        }
        if (!nodes.isEmpty()) {
            MIN_X.setInt(tab, minX);
            MIN_Y.setInt(tab, minY);
            MAX_X.setInt(tab, maxX);
            MAX_Y.setInt(tab, maxY);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Layout layout = layout();
        GuiAdvancementTab tab = selectedTab();
        updateDragging(tab, layout, mouseX, mouseY);

        drawDefaultBackground();
        drawRect(layout.left, layout.top, layout.right(), layout.bottom(), 0xFF11191D);
        drawRect(layout.left + 2, layout.top + 2, layout.right() - 2, layout.bottom() - 2,
            0xFF8A999D);
        drawRect(layout.left + 4, layout.top + 4, layout.right() - 4, layout.bottom() - 4,
            0xFF253238);
        drawRect(layout.viewportX - 1, layout.viewportY - 1,
            layout.viewportX + layout.viewportWidth + 1,
            layout.viewportY + layout.viewportHeight + 1, 0xFF050708);

        String title = "Industrial Civilization Advancements";
        fontRenderer.drawString(title, layout.left + 9, layout.top + 8, 0xFFF2F5F5);
        String hint = "Pan: arrows/WASD, drag, or wheel (Shift+wheel: horizontal)";
        int hintX = layout.right() - 9 - fontRenderer.getStringWidth(hint);
        if (hintX > layout.left + 19 + fontRenderer.getStringWidth(title)) {
            fontRenderer.drawString(hint, hintX, layout.top + 8, 0xFFB8C7CA);
        }

        if (tab != null) drawTab(tab, layout, mouseX, mouseY);
        else drawCenteredString(fontRenderer, "No Industrial Civilization advancements loaded",
            width / 2, height / 2, 0xFFFFFFFF);

        for (GuiButton button : buttonList) button.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        GuiAdvancementTab tab = selectedTab();
        if (tab == null) return;
        int step = wheel > 0 ? 24 : -24;
        try {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                pan(tab, step, 0, layout());
            } else {
                pan(tab, 0, step, layout());
            }
        } catch (ReflectiveOperationException exception) {
            IndustrialCivilizationCore.LOGGER.warn("Could not wheel-pan advancement tree", exception);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        GuiAdvancementTab tab = selectedTab();
        if (tab != null) {
            try {
                if (panForKey(tab, keyCode, layout())) return;
            } catch (ReflectiveOperationException exception) {
                IndustrialCivilizationCore.LOGGER.warn(
                    "Could not keyboard-pan advancement tree", exception);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    private static boolean panForKey(GuiAdvancementTab tab, int keyCode, Layout layout)
            throws IllegalAccessException {
        int step = 24;
        if (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_A) {
            pan(tab, step, 0, layout);
        } else if (keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_D) {
            pan(tab, -step, 0, layout);
        } else if (keyCode == Keyboard.KEY_UP || keyCode == Keyboard.KEY_W) {
            pan(tab, 0, step, layout);
        } else if (keyCode == Keyboard.KEY_DOWN || keyCode == Keyboard.KEY_S) {
            pan(tab, 0, -step, layout);
        } else if (keyCode == Keyboard.KEY_PRIOR) {
            pan(tab, 0, Math.max(step, layout.viewportHeight * 3 / 4), layout);
        } else if (keyCode == Keyboard.KEY_NEXT) {
            pan(tab, 0, -Math.max(step, layout.viewportHeight * 3 / 4), layout);
        } else {
            return false;
        }
        return true;
    }

    private void drawTab(GuiAdvancementTab tab, Layout layout, int mouseX, int mouseY) {
        try {
            int scrollX = SCROLL_X.getInt(tab);
            int scrollY = SCROLL_Y.getInt(tab);
            GuiAdvancement root = (GuiAdvancement) ROOT.get(tab);
            if (root == null) return;

            enableScissor(layout);
            GlStateManager.pushMatrix();
            GlStateManager.translate(layout.viewportX, layout.viewportY, -400.0F);
            GlStateManager.enableDepth();
            drawBackground(tab, layout.viewportWidth, layout.viewportHeight, scrollX, scrollY);
            root.drawConnectivity(scrollX, scrollY, true);
            root.drawConnectivity(scrollX, scrollY, false);
            root.draw(scrollX, scrollY);
            GlStateManager.popMatrix();
            GlStateManager.depthFunc(515);
            GlStateManager.disableDepth();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            int localX = mouseX - layout.viewportX;
            int localY = mouseY - layout.viewportY;
            if (localX > 0 && localX < layout.viewportWidth
                    && localY > 0 && localY < layout.viewportHeight) {
                drawAdvancementTooltip(tab, layout, scrollX, scrollY, localX, localY);
            }
        } catch (ReflectiveOperationException exception) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            IndustrialCivilizationCore.LOGGER.warn("Could not render responsive advancement tree", exception);
        }
    }

    private void drawBackground(GuiAdvancementTab tab, int viewportWidth, int viewportHeight,
            int scrollX, int scrollY) {
        DisplayInfo display = tab.getAdvancement().getDisplay();
        ResourceLocation background = display == null ? null : display.getBackground();
        mc.getTextureManager().bindTexture(background == null
            ? TextureManager.RESOURCE_LOCATION_EMPTY : background);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int offsetX = Math.floorMod(scrollX, 16);
        int offsetY = Math.floorMod(scrollY, 16);
        for (int x = -1; x <= viewportWidth / 16 + 1; x++) {
            for (int y = -1; y <= viewportHeight / 16 + 1; y++) {
                drawModalRectWithCustomSizedTexture(offsetX + x * 16, offsetY + y * 16,
                    0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void drawAdvancementTooltip(GuiAdvancementTab tab, Layout layout, int scrollX,
            int scrollY, int localX, int localY) throws IllegalAccessException {
        for (GuiAdvancement advancement :
                ((Map<Advancement, GuiAdvancement>) GUIS.get(tab)).values()) {
            if (!advancement.isMouseOver(scrollX, scrollY, localX, localY)) continue;
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(layout.viewportX, layout.viewportY, 400.0F);
            RenderHelper.enableGUIStandardItemLighting();
            advancement.drawHover(scrollX, scrollY, 0.3F, layout.left, layout.top);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            return;
        }
    }

    private void updateDragging(GuiAdvancementTab tab, Layout layout, int mouseX, int mouseY) {
        boolean down = Mouse.isButtonDown(0);
        if (!down || tab == null) {
            dragging = false;
            return;
        }
        if (!dragging) {
            if (!layout.contains(mouseX, mouseY)) return;
            dragging = true;
            dragMouseX = mouseX;
            dragMouseY = mouseY;
            return;
        }
        int deltaX = mouseX - dragMouseX;
        int deltaY = mouseY - dragMouseY;
        dragMouseX = mouseX;
        dragMouseY = mouseY;
        if (deltaX == 0 && deltaY == 0) return;
        try {
            pan(tab, deltaX, deltaY, layout);
        } catch (ReflectiveOperationException exception) {
            dragging = false;
            IndustrialCivilizationCore.LOGGER.warn("Could not drag advancement tree", exception);
        }
    }

    private static void pan(GuiAdvancementTab tab, int deltaX, int deltaY, Layout layout)
            throws IllegalAccessException {
        SCROLL_X.setInt(tab, GameplayRules.advancementBoundedScroll(
            SCROLL_X.getInt(tab) + deltaX, layout.viewportWidth,
            MIN_X.getInt(tab), MAX_X.getInt(tab)));
        SCROLL_Y.setInt(tab, GameplayRules.advancementBoundedScroll(
            SCROLL_Y.getInt(tab) + deltaY, layout.viewportHeight,
            MIN_Y.getInt(tab), MAX_Y.getInt(tab)));
    }

    private void enableScissor(Layout layout) {
        int scale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(layout.viewportX * scale,
            mc.displayHeight - (layout.viewportY + layout.viewportHeight) * scale,
            layout.viewportWidth * scale, layout.viewportHeight * scale);
    }

    private GuiAdvancementTab selectedTab() {
        try {
            return (GuiAdvancementTab) SELECTED_TAB.get(this);
        } catch (IllegalAccessException exception) {
            IndustrialCivilizationCore.LOGGER.warn("Could not read selected advancement tab", exception);
            return null;
        }
    }

    private Layout layout() {
        int outerWidth = GameplayRules.advancementWindowWidth(width);
        int outerHeight = GameplayRules.advancementWindowHeight(height);
        return new Layout((width - outerWidth) / 2, (height - outerHeight) / 2,
            outerWidth, outerHeight);
    }

    /** Deterministic real-client acceptance probe used only by the E2E bridge. */
    String exerciseTwoAxisPanForTest() {
        GuiAdvancementTab tab = selectedTab();
        Layout layout = layout();
        if (tab == null) return "FAIL|advancement_ui|reason=no_selected_tab";
        try {
            pan(tab, -100000, -100000, layout);
            int lowX = SCROLL_X.getInt(tab);
            int lowY = SCROLL_Y.getInt(tab);
            pan(tab, 200000, 200000, layout);
            int highX = SCROLL_X.getInt(tab);
            int highY = SCROLL_Y.getInt(tab);
            int minX = MIN_X.getInt(tab);
            int maxX = MAX_X.getInt(tab);
            int minY = MIN_Y.getInt(tab);
            int maxY = MAX_Y.getInt(tab);
            boolean responsive = layout.width > 252 && layout.height > 140;
            boolean horizontal = lowX != highX && minX < maxX;
            boolean vertical = lowY != highY && minY < maxY;
            int keyboardStartX = highX;
            int keyboardStartY = highY;
            boolean acceptedRight = panForKey(tab, Keyboard.KEY_RIGHT, layout);
            boolean acceptedDown = panForKey(tab, Keyboard.KEY_DOWN, layout);
            int keyboardX = SCROLL_X.getInt(tab);
            int keyboardY = SCROLL_Y.getInt(tab);
            boolean keyboard = acceptedRight && acceptedDown
                && keyboardX < keyboardStartX && keyboardY < keyboardStartY;
            SCROLL_X.setInt(tab, highX);
            SCROLL_Y.setInt(tab, highY);
            return (responsive && horizontal && vertical && keyboard ? "PASS" : "FAIL")
                + "|advancement_ui|screen=" + width + "x" + height
                + "|window=" + layout.width + "x" + layout.height
                + "|viewport=" + layout.viewportWidth + "x" + layout.viewportHeight
                + "|tree_x=" + minX + ".." + maxX
                + "|tree_y=" + minY + ".." + maxY
                + "|pan_x=" + lowX + ".." + highX
                + "|pan_y=" + lowY + ".." + highY
                + "|horizontal=" + horizontal + "|vertical=" + vertical
                + "|keyboard=" + keyboard;
        } catch (ReflectiveOperationException exception) {
            IndustrialCivilizationCore.LOGGER.warn("Could not exercise advancement UI", exception);
            return "FAIL|advancement_ui|reason=reflection";
        }
    }

    private static final class Layout {
        final int left;
        final int top;
        final int width;
        final int height;
        final int viewportX;
        final int viewportY;
        final int viewportWidth;
        final int viewportHeight;

        Layout(int left, int top, int width, int height) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.viewportX = left + 7;
            this.viewportY = top + 23;
            this.viewportWidth = width - 14;
            this.viewportHeight = height - 30;
        }

        int right() { return left + width; }
        int bottom() { return top + height; }
        boolean contains(int x, int y) {
            return x >= viewportX && x < viewportX + viewportWidth
                && y >= viewportY && y < viewportY + viewportHeight;
        }
    }
}
