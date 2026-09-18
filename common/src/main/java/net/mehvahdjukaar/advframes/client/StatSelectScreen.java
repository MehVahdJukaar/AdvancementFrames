package net.mehvahdjukaar.advframes.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.network.ServerBoundSetStatFramePacket;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.LoadingTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class StatSelectScreen extends StatsScreen {
    private static final Component TITLE = Component.translatable("gui.stats");
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier HEADER_SPRITE = Identifier.withDefaultNamespace("statistics/header");
    private static final Identifier SORT_UP_SPRITE = Identifier.withDefaultNamespace("statistics/sort_up");
    private static final Identifier SORT_DOWN_SPRITE = Identifier.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    private static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    private static final int HOVER_HIGHLIGHT = 0x80FFFFFF;

    private final StatFrameBlockTile tile;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;
    private final StatsCounter stats;
    private boolean isLoading = true;

    public StatSelectScreen(StatFrameBlockTile tile, StatsCounter stats) {
        super(null, stats);
        this.tile = tile;
        this.stats = stats;
    }

    @Override
    protected void init() {
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(
                new LoadingTab(this.getFont(), GENERAL_BUTTON, PENDING_TEXT),
                new LoadingTab(this.getFont(), ITEMS_BUTTON, PENDING_TEXT),
                new LoadingTab(this.getFont(), MOBS_BUTTON, PENDING_TEXT)).build();
        this.addRenderableWidget(this.tabNavigationBar);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.tabNavigationBar.setTabActiveState(0, true);
        this.tabNavigationBar.setTabActiveState(1, false);
        this.tabNavigationBar.setTabActiveState(2, false);
        this.layout.visitWidgets(button -> {
            button.setTabOrderGroup(1);
            this.addRenderableWidget(button);
        });
        this.tabNavigationBar.selectTab(0, false);
        this.repositionElements();
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
    }

    @Override
    public void onStatsUpdated() {
        if (this.isLoading) {
            if (this.tabNavigationBar != null) {
                this.removeWidget(this.tabNavigationBar);
            }

            this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width).addTabs(
                    new StatisticsTab(GENERAL_BUTTON, new GeneralStatisticsList(this.minecraft)),
                    new StatisticsTab(ITEMS_BUTTON, new ItemStatisticsList(this.minecraft)),
                    new StatisticsTab(MOBS_BUTTON, new MobsStatisticsList(this.minecraft))).build();
            this.setFocused(this.tabNavigationBar);
            this.addRenderableWidget(this.tabNavigationBar);
            this.setTabActiveStateAndTooltip(1);
            this.setTabActiveStateAndTooltip(2);
            this.tabNavigationBar.selectTab(0, false);
            this.repositionElements();
            this.isLoading = false;
        }
    }

    private void setTabActiveStateAndTooltip(int index) {
        if (this.tabNavigationBar == null) return;
        boolean active = this.tabNavigationBar.getTabs().get(index) instanceof StatisticsTab statsTab
                && !statsTab.list.children().isEmpty();
        this.tabNavigationBar.setTabActiveState(index, active);
        if (active) {
            this.tabNavigationBar.setTabTooltip(index, null);
        } else {
            this.tabNavigationBar.setTabTooltip(index, Tooltip.create(Component.translatable("gui.stats.none_found")));
        }
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.updateWidth(this.width);
            int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle tabArea = new ScreenRectangle(0, tabAreaTop, this.width,
                    this.height - this.layout.getFooterHeight() - tabAreaTop);
            this.tabNavigationBar.getTabs().forEach(tab -> tab.visitChildren(child -> child.setHeight(tabArea.height())));
            this.tabManager.setTabArea(tabArea);
            this.layout.setHeaderHeight(tabAreaTop);
            this.layout.arrangeElements();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(event)) return true;
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(),
                0.0F, 0.0F, this.width, 2, 32, 2);
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F,
                this.width, this.layout.getHeaderHeight(), 16, 16);
        this.extractMenuBackground(graphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    private static String getTranslationKey(Stat<Identifier> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    private <T> void selectStat(StatType<T> statType, T obj) {
        NetworkHelper.sendToServer(new ServerBoundSetStatFramePacket(tile.getBlockPos(), statType, obj));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.onClose();
    }

    private class StatisticsTab extends GridLayoutTab {
        protected final AbstractSelectionList<?> list;

        public StatisticsTab(Component title, AbstractSelectionList<?> list) {
            super(title);
            this.layout.addChild(list, 1, 1);
            this.list = list;
        }

        @Override
        public void doLayout(ScreenRectangle screenRectangle) {
            this.list.updateSizeAndPosition(StatSelectScreen.this.width, StatSelectScreen.this.layout.getContentHeight(),
                    StatSelectScreen.this.layout.getHeaderHeight());
            super.doLayout(screenRectangle);
        }
    }

    private class GeneralStatisticsList extends ObjectSelectionList<GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.layout.getContentHeight(), 33, 14);
            ObjectArrayList<Stat<Identifier>> customStats = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            customStats.sort(Comparator.comparing(k -> I18n.get(getTranslationKey(k))));

            for (Stat<Identifier> stat : customStats) {
                this.addEntry(new Entry(stat));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final Stat<Identifier> stat;
            private final Component statDisplay;

            private Entry(Stat<Identifier> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(StatSelectScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                int y = this.getContentYMiddle() - 9 / 2;
                int index = GeneralStatisticsList.this.children().indexOf(this);
                int color = index % 2 == 0 ? -1 : -4539718;
                graphics.text(StatSelectScreen.this.font, this.statDisplay, this.getContentX() + 2, y, color);
                String msg = this.getValueText();
                graphics.text(StatSelectScreen.this.font, msg, this.getContentRight() - StatSelectScreen.this.font.width(msg) - 4, y, color);
                if (hovered) {
                    graphics.fill(this.getContentX(), this.getContentY(), this.getContentRight(), this.getContentBottom(), HOVER_HIGHLIGHT);
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay)
                        .append(CommonComponents.SPACE).append(this.getValueText()));
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                selectStat(stat.getType(), stat.getValue());
                return true;
            }
        }
    }

    private class ItemStatisticsList extends ContainerObjectSelectionList<ItemStatisticsList.Entry> {
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<ItemRow> itemStatSorter = new ItemRowComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.layout.getContentHeight(), 33, 22);
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            Set<Item> items = Sets.newIdentityHashSet();

            for (Item item : BuiltInRegistries.ITEM) {
                for (StatType<Item> type : this.itemColumns) {
                    if (type.contains(item) && StatSelectScreen.this.stats.getValue(type.get(item)) > 0) {
                        items.add(item);
                    }
                }
            }

            for (Block block : BuiltInRegistries.BLOCK) {
                for (StatType<Block> type : this.blockColumns) {
                    if (type.contains(block) && StatSelectScreen.this.stats.getValue(type.get(block)) > 0) {
                        items.add(block.asItem());
                    }
                }
            }

            items.remove(Items.AIR);
            if (!items.isEmpty()) {
                this.addEntry(new HeaderEntry());
                for (Item item : items) {
                    this.addEntry(new ItemRow(item));
                }
            }
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }

        private int getColumnX(int col) {
            return 75 + 40 * col;
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        private StatType<?> getColumn(int i) {
            return i < this.blockColumns.size() ? this.blockColumns.get(i) : this.itemColumns.get(i - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> column) {
            int i = this.blockColumns.indexOf(column);
            if (i >= 0) return i;
            int j = this.itemColumns.indexOf(column);
            return j >= 0 ? j + this.blockColumns.size() : -1;
        }

        protected void sortByColumn(StatType<?> column) {
            if (column != this.sortColumn) {
                this.sortColumn = column;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }

            List<ItemRow> itemRows = new ArrayList<>();
            for (Entry entry : this.children()) {
                if (entry instanceof ItemRow itemRow) itemRows.add(itemRow);
            }
            itemRows.sort(this.itemStatSorter);
            this.clearEntriesExcept(this.children().getFirst());
            for (ItemRow row : itemRows) {
                this.addEntry(row);
            }
        }

        private class ItemRowComparator implements Comparator<ItemRow> {
            @Override
            @SuppressWarnings("unchecked")
            public int compare(ItemRow one, ItemRow two) {
                Item item1 = one.getItem();
                Item item2 = two.getItem();
                int key1;
                int key2;
                if (sortColumn == null) {
                    key1 = 0;
                    key2 = 0;
                } else if (blockColumns.contains(sortColumn)) {
                    StatType<Block> type = (StatType<Block>) sortColumn;
                    key1 = item1 instanceof BlockItem bi ? StatSelectScreen.this.stats.getValue(type, bi.getBlock()) : -1;
                    key2 = item2 instanceof BlockItem bi ? StatSelectScreen.this.stats.getValue(type, bi.getBlock()) : -1;
                } else {
                    StatType<Item> type = (StatType<Item>) sortColumn;
                    key1 = StatSelectScreen.this.stats.getValue(type, item1);
                    key2 = StatSelectScreen.this.stats.getValue(type, item2);
                }

                return key1 == key2
                        ? sortOrder * Integer.compare(Item.getId(item1), Item.getId(item2))
                        : sortOrder * Integer.compare(key1, key2);
            }
        }

        private abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        }

        private class ItemRow extends Entry {
            private final Item item;
            private final ItemRowWidget itemRowWidget;
            @Nullable
            private Stat<?> hovered = null;

            private ItemRow(Item item) {
                this.item = item;
                this.itemRowWidget = new ItemRowWidget(item.getDefaultInstance());
            }

            protected Item getItem() {
                return this.item;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                this.itemRowWidget.setPosition(this.getContentX(), this.getContentY());
                this.itemRowWidget.extractRenderState(graphics, mouseX, mouseY, a);
                this.hovered = null;
                int index = ItemStatisticsList.this.children().indexOf(this);
                int y = this.getContentYMiddle() - 9 / 2;

                for (int col = 0; col < blockColumns.size(); col++) {
                    Stat<Block> stat = this.item instanceof BlockItem blockItem ? blockColumns.get(col).get(blockItem.getBlock()) : null;
                    this.extractStat(graphics, stat, this.getContentX() + getColumnX(col), y, index % 2 == 0, hovered, mouseX);
                }

                for (int col = 0; col < itemColumns.size(); col++) {
                    Stat<Item> stat = itemColumns.get(col).get(this.item);
                    this.extractStat(graphics, stat, this.getContentX() + getColumnX(col + blockColumns.size()), y,
                            index % 2 == 0, hovered, mouseX);
                }
            }

            protected void extractStat(GuiGraphicsExtractor graphics, @Nullable Stat<?> stat, int x, int y, boolean shaded,
                                       boolean rowHovered, int mouseX) {
                Component msg = stat == null ? NO_VALUE_DISPLAY : Component.literal(stat.format(StatSelectScreen.this.stats.getValue(stat)));
                graphics.text(StatSelectScreen.this.font, msg, x - StatSelectScreen.this.font.width(msg), y, shaded ? -1 : -4539718);

                int w = 18;
                if (stat != null && rowHovered && mouseX >= x - w && mouseX < x) {
                    graphics.fill(x - w, y - 5, x, y - 5 + w, HOVER_HIGHLIGHT);
                    this.hovered = stat;
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (hovered != null) {
                    StatType<?> type = hovered.getType();
                    if (type.getRegistry() == BuiltInRegistries.BLOCK) {
                        selectStat((StatType<Block>) type, ((BlockItem) item).getBlock());
                    } else {
                        selectStat((StatType<Item>) type, item);
                    }
                    return true;
                }
                return super.mouseClicked(event, doubleClick);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.itemRowWidget);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(this.itemRowWidget);
            }

            private class ItemRowWidget extends ItemDisplayWidget {
                private ItemRowWidget(ItemStack itemStack) {
                    super(ItemStatisticsList.this.minecraft, 1, 1, 18, 18, itemStack.getHoverName(), itemStack, false, true);
                }

                @Override
                protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, ItemRow.this.getContentX(), ItemRow.this.getContentY(), 18, 18);
                    super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
                }

                @Override
                protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
                    super.extractTooltip(graphics, ItemRow.this.getContentX() + 18, ItemRow.this.getContentY() + 18);
                }
            }
        }

        private class HeaderEntry extends Entry {
            private static final Identifier[] COLUMN_SPRITES = new Identifier[]{
                    Identifier.withDefaultNamespace("statistics/block_mined"),
                    Identifier.withDefaultNamespace("statistics/item_broken"),
                    Identifier.withDefaultNamespace("statistics/item_crafted"),
                    Identifier.withDefaultNamespace("statistics/item_used"),
                    Identifier.withDefaultNamespace("statistics/item_picked_up"),
                    Identifier.withDefaultNamespace("statistics/item_dropped")
            };
            private final List<AbstractWidget> children = new ArrayList<>();

            private HeaderEntry() {
                for (int i = 0; i < COLUMN_SPRITES.length; i++) {
                    this.children.add(new StatSortButton(i, COLUMN_SPRITES[i]));
                }
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                for (int i = 0; i < this.children.size(); i++) {
                    AbstractWidget button = this.children.get(i);
                    button.setPosition(this.getContentX() + getColumnX(i) - 18, this.getContentY() + 1);
                    button.extractRenderState(graphics, mouseX, mouseY, a);
                }

                if (sortColumn != null) {
                    int offset = getColumnX(getColumnIndex(sortColumn)) - 36;
                    Identifier sprite = sortOrder == 1 ? SORT_UP_SPRITE : SORT_DOWN_SPRITE;
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getContentX() + offset, this.getContentY() + 1, 18, 18);
                }
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }

            private class StatSortButton extends ImageButton {
                private final Identifier sprite;

                private StatSortButton(int column, Identifier sprite) {
                    super(18, 18, new WidgetSprites(HEADER_SPRITE, SLOT_SPRITE),
                            button -> sortByColumn(getColumn(column)), getColumn(column).getDisplayName());
                    this.sprite = sprite;
                    this.setTooltip(Tooltip.create(this.getMessage()));
                }

                @Override
                public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    Identifier background = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, background, this.getX(), this.getY(), this.width, this.height);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.getX(), this.getY(), this.width, this.height);
                }
            }
        }
    }

    private class MobsStatisticsList extends ObjectSelectionList<MobsStatisticsList.MobRow> {
        public MobsStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.layout.getContentHeight(), 33, 9 * 4);

            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(type)) > 0
                        || StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(type)) > 0) {
                    this.addEntry(new MobRow(type));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor graphics) {
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor graphics) {
        }

        private class MobRow extends ObjectSelectionList.Entry<MobRow> {
            private final EntityType<?> entity;
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;
            private int hoveredLine = 0;

            public MobRow(EntityType<?> type) {
                this.entity = type;
                this.mobName = type.getDescription();
                int killCount = StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(type));
                if (killCount == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", killCount, this.mobName);
                    this.hasKills = true;
                }

                int killedByCount = StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(type));
                if (killedByCount == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, killedByCount);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                this.hoveredLine = 0;
                int x = this.getContentX() + 2;
                int y = this.getContentY() + 1;
                graphics.text(StatSelectScreen.this.font, this.mobName, x, y, -1);

                int lineX = x + 10;
                int killsY = y + 9;
                graphics.text(StatSelectScreen.this.font, this.kills, lineX, killsY, this.hasKills ? -4539718 : -8355712);
                if (hovered && mouseY >= killsY && mouseY <= killsY + 9) {
                    this.hoveredLine = 1;
                    graphics.fill(lineX, killsY, this.getContentRight() - 12, killsY + 9, HOVER_HIGHLIGHT);
                }

                int killedByY = y + 9 * 2;
                graphics.text(StatSelectScreen.this.font, this.killedBy, lineX, killedByY, this.wasKilledBy ? -4539718 : -8355712);
                if (hovered && mouseY >= killedByY && mouseY <= killedByY + 9) {
                    this.hoveredLine = 2;
                    graphics.fill(lineX, killedByY, this.getContentRight() - 12, killedByY + 9, HOVER_HIGHLIGHT);
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (hoveredLine == 1) {
                    selectStat(Stats.ENTITY_KILLED, entity);
                    return true;
                } else if (hoveredLine == 2) {
                    selectStat(Stats.ENTITY_KILLED_BY, entity);
                    return true;
                }
                return false;
            }
        }
    }
}
