package net.mehvahdjukaar.advframes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.mehvahdjukaar.advframes.network.ServerBoundSetAdvancementFramePacket;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class AdvancementSelectScreen extends AdvancementsScreen {
    private final AdvancementFrameBlockTile tile;

    public AdvancementSelectScreen(AdvancementFrameBlockTile tile,ClientAdvancements clientAdvancements) {
        super(clientAdvancements);
        this.tile = tile;
    }

    @Override
    protected void init() {
        super.init();
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
                                NetworkHandler.INSTANCE.sendToServer(
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

    public void renderWindow(PoseStack poseStack, int x, int y) {
        super.renderWindow(poseStack, x, y);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
        this.blit(poseStack, x, y+5, 0, 5, 252, 140);
        Component c = new TranslatableComponent("advancementframes.gui");
        float posX = this.width / 2f - this.font.width(c) / 2f;
        this.font.draw(poseStack, c, posX, (float) (y + 6), 4210752);
    }
}
