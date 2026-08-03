package com.industrialcivilization.core;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** Read-only faction, reputation, membership and settlement reference. */
public final class GuiFactionDirectory extends GuiScreen {
    private final GuiScreen parent;
    private int selected;

    public GuiFactionDirectory(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 190;
        int top = height / 2 - 98;
        for (int index = 0; index < FactionSystem.DEFINITIONS.length; index++) {
            buttonList.add(new GuiButton(100 + index, left, top + 28 + index * 24, 145, 20,
                FactionSystem.DEFINITIONS[index].name));
        }
        buttonList.add(new GuiButton(200, width / 2 - 50, height / 2 + 108, 100, 20,
            I18n.format("gui.done")));
        FactionNetwork.requestSnapshot();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 100 && button.id < 100 + FactionSystem.DEFINITIONS.length) {
            selected = button.id - 100;
        } else if (button.id == 200) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 200;
        int right = width / 2 + 200;
        int top = height / 2 - 116;
        int bottom = height / 2 + 104;
        drawRect(left, top, right, bottom, 0xDD18242A);
        drawRect(left + 156, top + 24, left + 158, bottom - 8, 0xFF71858B);
        drawCenteredString(fontRenderer, I18n.format("gui.industrialcivilization.factions"),
            width / 2, top + 8, 0xE5F0EF);
        String membership = FactionNetwork.clientMembership.isEmpty()
            ? I18n.format("gui.industrialcivilization.factions.independent")
            : FactionSystem.definition(FactionNetwork.clientMembership).name;
        fontRenderer.drawString(I18n.format("gui.industrialcivilization.factions.membership", membership),
            left + 166, top + 30, 0x9AF8F4);

        FactionSystem.Definition definition = FactionSystem.DEFINITIONS[selected];
        int reputation = FactionNetwork.clientReputation[selected];
        String attitude = FactionNetwork.clientMembership.equals(definition.id) ? "MEMBER"
            : reputation <= FactionSystem.HOSTILE_REPUTATION ? "HOSTILE"
            : FactionNetwork.clientEligible[selected] ? "ELIGIBLE"
            : reputation >= FactionSystem.FRIENDLY_REPUTATION ? "FRIENDLY"
            : reputation < 0 ? "GUARDED" : "NEUTRAL";
        int color = "HOSTILE".equals(attitude) ? 0xE75A4D
            : ("FRIENDLY".equals(attitude) || "MEMBER".equals(attitude) || "ELIGIBLE".equals(attitude))
                ? 0x70E0C0 : 0xD9D2B4;
        int x = left + 166;
        int y = top + 50;
        fontRenderer.drawString(definition.name, x, y, 0xFFFFFF); y += 14;
        fontRenderer.drawString("Reputation: " + reputation + "   " + attitude, x, y, color); y += 14;
        fontRenderer.drawString("Encountered: " + (FactionNetwork.clientKnown[selected] ? "yes" : "not yet"),
            x, y, 0xAEBCC1); y += 18;
        y = drawWrapped("Settlements: " + definition.settlements, x, y, 224, 0xD9D2B4);
        y = drawWrapped("Trade specialties: " + definition.products, x, y + 4, 224, 0xD9D2B4);
        y = drawWrapped("Membership: " + definition.membershipRule, x, y + 4, 224, 0xD9D2B4);
        drawWrapped("How to interact: normal right-click trades with IC Credits. Sneak-right-click requests membership. Members at 60 reputation can sneak-right-click while holding 8 IC Credits to recruit or dismiss a companion.",
            x, y + 6, 224, 0x9AF8F4);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int drawWrapped(String text, int x, int y, int width, int color) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(text, width);
        for (String line : lines) {
            fontRenderer.drawString(line, x, y, color);
            y += fontRenderer.FONT_HEIGHT + 2;
        }
        return y;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
