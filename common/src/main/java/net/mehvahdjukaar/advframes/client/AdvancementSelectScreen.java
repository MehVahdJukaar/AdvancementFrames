package net.mehvahdjukaar.advframes.client;

import net.mehvahdjukaar.advframes.blocks.AdvancementFrameBlockTile;
import net.mehvahdjukaar.advframes.network.ModMessages;
import net.mehvahdjukaar.advframes.network.ServerBoundSetAdvancementFramePacket;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//based off vanilla screen. should match it.
public class AdvancementSelectScreen extends AdvancementsScreen {
    private static final int TABS_PER_PAGE = 26;

    private final AdvancementFrameBlockTile tile;
    private final MutableComponent title2;
    private final ClientAdvancements advancements;
    private final List<AdvancementNode> roots = new ArrayList<>();
    private TabPageButtons pageButtons;
    private int page;

    public AdvancementSelectScreen(AdvancementFrameBlockTile tile, ClientAdvancements clientAdvancements) {
        super(clientAdvancements);
        this.tile = tile;
        this.advancements = clientAdvancements;
        this.title2 = Component.translatable("advancementframes.gui.advancements");

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
    protected void init() {
        super.init();
        pageButtons = new TabPageButtons(this);
        addWidget(pageButtons);
    }

    @Override
    public void onAddAdvancementRoot(AdvancementNode advancement) {
        if (advancement.advancement().display().isEmpty()) return;
        int index = roots.indexOf(advancement);
        if (index == -1) {
            index = roots.size();
            roots.add(advancement);
        }
        if (index / TABS_PER_PAGE == page) super.onAddAdvancementRoot(advancement);
    }

    @Override
    public void onAdvancementsCleared() {
        super.onAdvancementsCleared();
        roots.clear();
    }

    public int getPage() {
        return page;
    }

    public int getPageCount() {
        return Math.max(1, (roots.size() + TABS_PER_PAGE - 1) / TABS_PER_PAGE);
    }

    public boolean hasPages() {
        return getPageCount() > 1 || PlatHelper.isDev();
    }

    public void turnPage(int dir) {
        int newPage = page + dir;
        if (newPage < 0 || newPage >= getPageCount()) return;
        page = newPage;
        super.onAdvancementsCleared();
        advancements.setListener(this);
        if (this.selectedTab == null) {
            advancements.setSelectedTab(roots.get(page * TABS_PER_PAGE).holder(), true);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            AdvancementTab tab = this.selectedTab;
            if (tab != null) {
                int oX = (this.width - 252) / 2;
                int oY = (this.height - 140) / 2;
                int x = (int) (event.x() - oX - 9);
                int y = (int) (event.y() - oY - 18);

                int scrollX = Mth.floor(tab.scrollX);
                int scrollY = Mth.floor(tab.scrollY);
                if (x > 0 && x < 234 && y > 0 && y < 113) {
                    for (AdvancementWidget advancementwidget : tab.widgets.values()) {
                        if (advancementwidget.isMouseOver(scrollX, scrollY, x, y)) {
                            AdvancementProgress p = advancementwidget.progress;
                            if (p != null && p.isDone()) {

                                NetworkHelper.sendToServer(
                                        new ServerBoundSetAdvancementFramePacket(tile.getBlockPos(),
                                                advancementwidget.advancementNode.holder()));
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
        return super.mouseClicked(event, doubleClick);
    }

    private static final Identifier WINDOW_LOCATION = Identifier.withDefaultNamespace("textures/gui/advancements/window.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.nextStratum();
        pageButtons.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractWindow(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        super.extractWindow(graphics, x, y, mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, WINDOW_LOCATION, x, y + 5, 0, 5, 252, 11, 256, 256);
        graphics.text(this.font, title2, x + 8, y + 6, 0xFF404040, false);
    }
}
