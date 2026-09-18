package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.AdvFrames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabPageButtons extends AbstractWidget {
    private static final ResourceLocation BACKGROUND = AdvFrames.res("widget/page_background");
    private static final ResourceLocation PREVIOUS = AdvFrames.res("widget/page_previous");
    private static final ResourceLocation PREVIOUS_HIGHLIGHTED = AdvFrames.res("widget/page_previous_highlighted");
    private static final ResourceLocation NEXT = AdvFrames.res("widget/page_next");
    private static final ResourceLocation NEXT_HIGHLIGHTED = AdvFrames.res("widget/page_next_highlighted");

    private static final int ARROW_SIZE = 9;
    private static final int WIDTH = ARROW_SIZE * 2 + 2;
    private static final int HEIGHT = ARROW_SIZE + 2;

    private final AdvancementSelectScreen screen;

    public TabPageButtons(AdvancementSelectScreen screen) {
        super((screen.width - AdvancementsScreen.WINDOW_WIDTH) / 2 + 224,
                (screen.height - AdvancementsScreen.WINDOW_HEIGHT) / 2 + 4,
                WIDTH, HEIGHT, CommonComponents.EMPTY);
        this.screen = screen;
    }

    private boolean hasPrevious() {
        return screen.getPage() > 0;
    }

    private boolean hasNext() {
        return screen.getPage() + 1 < screen.getPageCount();
    }

    private int nextArrowX() {
        return this.getX() + 1 + ARROW_SIZE;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!screen.hasPages()) return;
        graphics.blitSprite(BACKGROUND, this.getX(), this.getY(), WIDTH, HEIGHT);
        int y = this.getY() + 1;
        if (hasPrevious()) {
            boolean hovered = this.isHovered() && mouseX < nextArrowX();
            graphics.blitSprite(hovered ? PREVIOUS_HIGHLIGHTED : PREVIOUS, this.getX() + 1, y, ARROW_SIZE, ARROW_SIZE);
        }
        if (hasNext()) {
            boolean hovered = this.isHovered() && mouseX >= nextArrowX();
            graphics.blitSprite(hovered ? NEXT_HIGHLIGHTED : NEXT, nextArrowX(), y, ARROW_SIZE, ARROW_SIZE);
        }
        if (this.isHovered()) {
            Component tooltip = Component.translatable("advancementframes.gui.page",
                    screen.getPage() + 1, screen.getPageCount());
            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!screen.hasPages()) return;
        screen.turnPage(mouseX < nextArrowX() ? -1 : 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
