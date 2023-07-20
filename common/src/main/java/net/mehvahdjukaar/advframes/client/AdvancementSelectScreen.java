package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.mehvahdjukaar.advframes.network.ServerBoundSetAdvancementFramePacket;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.Objects;

public class AdvancementSelectScreen extends AdvancementsScreen {
    private final AdvancementFrameBlockTile tile;

    public AdvancementSelectScreen(AdvancementFrameBlockTile tile, ClientAdvancements clientAdvancements) {
        super(clientAdvancements);
        this.tile = tile;
    }

    //why is this here??
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvancementSelectScreen that = (AdvancementSelectScreen) o;
        return Objects.equals(tile, that.tile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tile);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            AdvancementTab tab = this.selectedTab;
            if (tab != null) {
                int oX = (this.width - 252) / 2;
                int oY = (this.height - 140) / 2;
                int x = (int) (mouseX - oX - 9);
                int y = (int) (mouseY - oY - 18);

                int scrollX = Mth.floor(tab.scrollX);
                int scrollY = Mth.floor(tab.scrollY);
                if (x > 0 && x < 234 && y > 0 && y < 113) {
                    for (AdvancementWidget advancementwidget : tab.widgets.values()) {
                        if (advancementwidget.isMouseOver(scrollX, scrollY, x, y)) {
                            AdvancementProgress p = advancementwidget.progress;
                            if (p != null && p.isDone()) {
                                NetworkHandler.CHANNEL.sendToServer(
                                        new ServerBoundSetAdvancementFramePacket(tile.getBlockPos(), advancementwidget.advancement));
                                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                                this.onClose();
                                return true;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");

    public void renderWindow(GuiGraphics graphics, int x, int y) {
        super.renderWindow(graphics, x, y);

        graphics.blit(WINDOW_LOCATION, x, y + 5, 0, 5, 252, 140);
        Component c = Component.translatable("advancementframes.gui");
        float posX = this.width / 2f - this.font.width(c) / 2f;
        graphics.drawString(this.font, c,(int) posX,  (y + 6), 4210752, false);
    }
}
