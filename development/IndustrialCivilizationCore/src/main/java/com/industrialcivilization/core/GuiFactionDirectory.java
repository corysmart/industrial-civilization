package com.industrialcivilization.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

/** Read-only faction, reputation, membership and settlement reference. */
public final class GuiFactionDirectory extends GuiScreen {
    private static final int DONE = 200;
    private static final int SCROLL_UP = 201;
    private static final int SCROLL_DOWN = 202;
    private final GuiScreen parent;
    private int selected;
    private int scrollLine;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private int divider;
    private int detailLeft;
    private int detailWidth;
    private int detailTop;
    private int detailBottom;

    public GuiFactionDirectory(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(520, Math.max(300, width - 16));
        int panelHeight = Math.min(330, Math.max(190, height - 16));
        left = (width - panelWidth) / 2;
        right = left + panelWidth;
        top = (height - panelHeight) / 2;
        bottom = top + panelHeight;
        int listWidth = Math.min(155, Math.max(105, panelWidth / 3));
        divider = left + listWidth;
        detailLeft = divider + 10;
        detailWidth = right - detailLeft - 10;
        detailTop = top + 43;
        detailBottom = bottom - 34;

        int count = FactionSystem.DEFINITIONS.length;
        int available = Math.max(90, panelHeight - 62);
        int buttonHeight = Math.min(20, Math.max(14, (available - (count - 1) * 3) / count));
        int listTop = top + 28;
        for (int index = 0; index < count; index++) {
            buttonList.add(new GuiButton(100 + index, left + 6,
                listTop + index * (buttonHeight + 3), listWidth - 12, buttonHeight,
                FactionSystem.DEFINITIONS[index].name));
        }
        buttonList.add(new GuiButton(SCROLL_UP, right - 45, top + 25, 18, 16, "\u25b2"));
        buttonList.add(new GuiButton(SCROLL_DOWN, right - 25, top + 25, 18, 16, "\u25bc"));
        buttonList.add(new GuiButton(DONE, right - 106, bottom - 27, 100, 20,
            I18n.format("gui.done")));
        clampScroll();
        FactionNetwork.requestSnapshot();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 100 && button.id < 100 + FactionSystem.DEFINITIONS.length) {
            selected = button.id - 100;
            scrollLine = 0;
        } else if (button.id == SCROLL_UP) {
            scrollLine = Math.max(0, scrollLine - 2);
        } else if (button.id == SCROLL_DOWN) {
            scrollLine += 2;
            clampScroll();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollLine += wheel < 0 ? 3 : -3;
            clampScroll();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(left, top, right, bottom, 0xEE18242A);
        drawRect(divider, top + 24, divider + 2, bottom - 7, 0xFF71858B);
        drawCenteredString(fontRenderer, I18n.format("gui.industrialcivilization.factions"),
            (left + right) / 2, top + 8, 0xE5F0EF);

        List<Line> lines = detailLines();
        int visible = visibleLineCount();
        int y = detailTop;
        for (int index = scrollLine; index < lines.size() && index < scrollLine + visible; index++) {
            Line line = lines.get(index);
            fontRenderer.drawString(line.text, detailLeft, y, line.color);
            y += fontRenderer.FONT_HEIGHT + 2;
        }
        buttonList.stream().filter(button -> button.id == SCROLL_UP).findFirst()
            .ifPresent(button -> button.enabled = scrollLine > 0);
        buttonList.stream().filter(button -> button.id == SCROLL_DOWN).findFirst()
            .ifPresent(button -> button.enabled = scrollLine + visible < lines.size());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private List<Line> detailLines() {
        List<Line> result = new ArrayList<>();
        String membership = FactionNetwork.clientMembership.isEmpty()
            ? I18n.format("gui.industrialcivilization.factions.independent")
            : FactionSystem.definition(FactionNetwork.clientMembership).name;
        addWrapped(result, I18n.format("gui.industrialcivilization.factions.membership", membership),
            0x9AF8F4);
        result.add(new Line("", 0));

        FactionSystem.Definition definition = FactionSystem.DEFINITIONS[selected];
        int reputation = FactionNetwork.clientReputation[selected];
        String attitude = FactionNetwork.clientMembership.equals(definition.id) ? "MEMBER"
            : reputation <= FactionSystem.HOSTILE_REPUTATION ? "HOSTILE"
            : FactionNetwork.clientEligible[selected] ? "ELIGIBLE"
            : reputation >= FactionSystem.FRIENDLY_REPUTATION ? "FRIENDLY"
            : reputation < 0 ? "GUARDED" : "NEUTRAL";
        int attitudeColor = "HOSTILE".equals(attitude) ? 0xE75A4D
            : ("FRIENDLY".equals(attitude) || "MEMBER".equals(attitude) || "ELIGIBLE".equals(attitude))
                ? 0x70E0C0 : 0xD9D2B4;
        addWrapped(result, definition.name, 0xFFFFFF);
        addWrapped(result, "Reputation: " + reputation + "   " + attitude, attitudeColor);
        addWrapped(result, "Encountered: " + (FactionNetwork.clientKnown[selected] ? "yes" : "not yet"),
            0xAEBCC1);
        result.add(new Line("", 0));
        addWrapped(result, "Settlements: " + definition.settlements, 0xD9D2B4);
        result.add(new Line("", 0));
        addWrapped(result, "Trade specialties: " + definition.products, 0xD9D2B4);
        result.add(new Line("", 0));
        addWrapped(result, "Membership: " + definition.membershipRule, 0xD9D2B4);
        result.add(new Line("", 0));
        addWrapped(result, "How to interact: normal right-click trades with IC Credits. "
            + "Sneak-right-click requests membership. Members at 60 reputation can sneak-right-click "
            + "while holding 8 IC Credits to recruit or dismiss a companion.", 0x9AF8F4);
        return result;
    }

    private void addWrapped(List<Line> result, String text, int color) {
        for (String line : fontRenderer.listFormattedStringToWidth(text, Math.max(60, detailWidth))) {
            result.add(new Line(line, color));
        }
    }

    private int visibleLineCount() {
        return Math.max(1, (detailBottom - detailTop) / (fontRenderer.FONT_HEIGHT + 2));
    }

    private void clampScroll() {
        if (fontRenderer == null || detailWidth <= 0) return;
        scrollLine = Math.max(0, Math.min(scrollLine, Math.max(0, detailLines().size() - visibleLineCount())));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private static final class Line {
        private final String text;
        private final int color;

        private Line(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
