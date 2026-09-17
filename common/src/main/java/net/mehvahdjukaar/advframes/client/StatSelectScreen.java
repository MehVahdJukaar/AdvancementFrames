package net.mehvahdjukaar.advframes.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.network.ServerBoundSetStatFramePacket;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class StatSelectScreen extends StatsScreen {
    private final StatFrameBlockTile tile;

    private static final Component TITLE = Component.translatable("gui.stats");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    static final ResourceLocation HEADER_SPRITE = ResourceLocation.withDefaultNamespace("statistics/header");
    static final ResourceLocation SORT_UP_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_up");
    static final ResourceLocation SORT_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    private static final int LIST_WIDTH = 280;
    private static final int PADDING = 5;
    private static final int FOOTER_HEIGHT = 58;
    private HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 58);
    @Nullable
    private GeneralStatisticsList statsList;
    @Nullable
    StatSelectScreen.ItemStatisticsList itemStatsList;
    @Nullable
    private StatSelectScreen.MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    /**
     * When true, the game will be paused when the gui is shown
     */
    private boolean isLoading = true;

    public StatSelectScreen(StatFrameBlockTile tile, StatsCounter stats) {
        super(null, stats);
        this.tile = tile;
        this.stats = stats;
    }

    @Override
    protected void init() {
        this.layout.addToContents(new LoadingDotsWidget(this.font, PENDING_TEXT));
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        HeaderAndFooterLayout headerAndFooterLayout = new HeaderAndFooterLayout(this, 33, 58);
        headerAndFooterLayout.addTitleHeader(TITLE, this.font);
        LinearLayout linearLayout = headerAndFooterLayout.addToFooter(LinearLayout.vertical()).spacing(5);
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal()).spacing(5);
        linearLayout2.addChild(Button.builder(GENERAL_BUTTON, buttonx -> this.setActiveList(this.statsList)).width(120).build());
        Button button = linearLayout2.addChild(Button.builder(ITEMS_BUTTON, buttonx -> this.setActiveList(this.itemStatsList)).width(120).build());
        Button button2 = linearLayout2.addChild(Button.builder(MOBS_BUTTON, buttonx -> this.setActiveList(this.mobsStatsList)).width(120).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, buttonx -> this.onClose()).width(200).build());
        if (this.itemStatsList != null && this.itemStatsList.children().isEmpty()) {
            button.active = false;
        }

        if (this.mobsStatsList != null && this.mobsStatsList.children().isEmpty()) {
            button2.active = false;
        }

        this.layout = headerAndFooterLayout;
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.activeList != null) {
            this.activeList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.setActiveList(this.statsList);
            this.initButtons();
            this.setInitialFocus();
            this.isLoading = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> activeList) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }

        if (activeList != null) {
            this.addRenderableWidget(activeList);
            this.activeList = activeList;
            this.repositionElements();
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    private <T> void selectStat(StatType<T> statType, T obj) {
        NetworkHelper.sendToServer(
                new ServerBoundSetStatFramePacket(tile.getBlockPos(), statType, obj));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.onClose();
    }

    class GeneralStatisticsList extends ObjectSelectionList<GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(final Minecraft minecraft) {
            super(minecraft, StatSelectScreen. this.width, StatSelectScreen.this.height - 33 - 58, 33, 14);
            ObjectArrayList<Stat<ResourceLocation>> objectArrayList = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            objectArrayList.sort(Comparator.comparing(statx -> I18n.get(getTranslationKey(statx))));

            for (Stat<ResourceLocation> stat : objectArrayList) {
                this.addEntry(new GeneralStatisticsList.Entry(stat));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        class Entry extends ObjectSelectionList.Entry<GeneralStatisticsList.Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(final Stat<ResourceLocation> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(StatSelectScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                int i = top + height / 2 - 9 / 2;
                int j = index % 2 == 0 ? -1 : -4539718;
                guiGraphics.drawString(StatSelectScreen.this.font, this.statDisplay, left + 2, i, j);
                String string = this.getValueText();
                guiGraphics.drawString(StatSelectScreen.this.font, string, left + width - StatSelectScreen.this.font.width(string) - 4, i, j);
                if (hovering) {
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            left, top, left + width, top + height + 2, -2130706433, -2130706433, 0);
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                selectStat(stat.getType(), stat.getValue());
                return false;
            }

        }
    }

    class ItemStatisticsList extends ObjectSelectionList<ItemStatisticsList.ItemRow> {
        private final ResourceLocation[] iconSprites = new ResourceLocation[]{
                ResourceLocation.withDefaultNamespace("statistics/block_mined"),
                ResourceLocation.withDefaultNamespace("statistics/item_broken"),
                ResourceLocation.withDefaultNamespace("statistics/item_crafted"),
                ResourceLocation.withDefaultNamespace("statistics/item_used"),
                ResourceLocation.withDefaultNamespace("statistics/item_picked_up"),
                ResourceLocation.withDefaultNamespace("statistics/item_dropped")
        };
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<ItemStatisticsList.ItemRow> itemStatSorter = new ItemStatisticsList.ItemRowComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int headerPressed = -1;
        protected int sortOrder;

        public ItemStatisticsList(final Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.height - 33 - 58, 33, 22);
            this.blockColumns = Lists.<StatType<Block>>newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.<StatType<Item>>newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            this.setRenderHeader(true, 22);
            Set<Item> set = Sets.newIdentityHashSet();

            for (Item item : BuiltInRegistries.ITEM) {
                boolean bl = false;

                for (StatType<Item> statType : this.itemColumns) {
                    if (statType.contains(item) && StatSelectScreen.this.stats.getValue(statType.get(item)) > 0) {
                        bl = true;
                    }
                }

                if (bl) {
                    set.add(item);
                }
            }

            for (Block block : BuiltInRegistries.BLOCK) {
                boolean bl = false;

                for (StatType<Block> statTypex : this.blockColumns) {
                    if (statTypex.contains(block) &&StatSelectScreen. this.stats.getValue(statTypex.get(block)) > 0) {
                        bl = true;
                    }
                }

                if (bl) {
                    set.add(block.asItem());
                }
            }

            set.remove(Items.AIR);

            for (Item item : set) {
                this.addEntry(new ItemStatisticsList.ItemRow(item));
            }
        }

        int getColumnX(int index) {
            return 75 + 40 * index;
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int x, int y) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }

            for (int i = 0; i < this.iconSprites.length; i++) {
                ResourceLocation resourceLocation = this.headerPressed == i ? SLOT_SPRITE : HEADER_SPRITE;
                guiGraphics.blitSprite(resourceLocation, x + this.getColumnX(i) - 18, y + 1, 0, 18, 18);
            }

            if (this.sortColumn != null) {
                int i = this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                ResourceLocation resourceLocation = this.sortOrder == 1 ? SORT_UP_SPRITE : SORT_DOWN_SPRITE;
                guiGraphics.blitSprite(resourceLocation, x + i, y + 1, 0, 18, 18);
            }

            for (int i = 0; i < this.iconSprites.length; i++) {
                int j = this.headerPressed == i ? 1 : 0;
                guiGraphics.blitSprite(this.iconSprites[i], x + this.getColumnX(i) - 18 + j, y + 1 + j, 0, 18, 18);
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected boolean clickedHeader(int x, int y) {
            this.headerPressed = -1;

            for (int i = 0; i < this.iconSprites.length; i++) {
                int j = x - this.getColumnX(i);
                if (j >= -36 && j <= 0) {
                    this.headerPressed = i;
                    break;
                }
            }

            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return super.clickedHeader(x, y);
            }
        }

        private StatType<?> getColumn(int index) {
            return index < this.blockColumns.size() ? (StatType)this.blockColumns.get(index) : (StatType)this.itemColumns.get(index - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> statType) {
            int i = this.blockColumns.indexOf(statType);
            if (i >= 0) {
                return i;
            } else {
                int j = this.itemColumns.indexOf(statType);
                return j >= 0 ? j + this.blockColumns.size() : -1;
            }
        }

        @Override
        protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (mouseY >= this.getY() && mouseY <= this.getBottom()) {
                ItemStatisticsList.ItemRow itemRow = this.getHovered();
                int i = this.getRowLeft();
                if (itemRow != null) {
                    if (mouseX < i || mouseX > i + 18) {
                        return;
                    }

                    Item item = itemRow.getItem();
                    guiGraphics.renderTooltip(StatSelectScreen.this.font, item.getDescription(), mouseX, mouseY);
                } else {
                    Component component = null;
                    int j = mouseX - i;

                    for (int k = 0; k < this.iconSprites.length; k++) {
                        int l = this.getColumnX(k);
                        if (j >= l - 18 && j <= l) {
                            component = this.getColumn(k).getDisplayName();
                            break;
                        }
                    }

                    if (component != null) {
                        guiGraphics.renderTooltip(StatSelectScreen.this.font, component, mouseX, mouseY);
                    }
                }
            }
        }

        protected void sortByColumn(StatType<?> statType) {
            if (statType != this.sortColumn) {
                this.sortColumn = statType;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }

            this.children().sort(this.itemStatSorter);
        }

        class ItemRow extends ObjectSelectionList.Entry<ItemStatisticsList.ItemRow> {
            private final Item item;

            private Stat<?> hovered = null;

            ItemRow(final Item item) {
                this.item = item;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.blitSprite(SLOT_SPRITE, left, top, 0, 18, 18);
                guiGraphics.renderFakeItem(this.item.getDefaultInstance(), left + 1, top + 1);
                hovered = null;

                if (StatSelectScreen.this.itemStatsList != null) {
                    for (int i = 0; i < StatSelectScreen.this.itemStatsList.blockColumns.size(); i++) {
                        Stat<Block> stat;
                        if (this.item instanceof BlockItem blockItem) {
                            stat = ((StatType)StatSelectScreen.this.itemStatsList.blockColumns.get(i)).get(blockItem.getBlock());
                        } else {
                            stat = null;
                        }

                        this.renderStat(guiGraphics, stat, left + ItemStatisticsList.this.getColumnX(i),
                                top + height / 2 - 9 / 2, index % 2 == 0,
                                hovering, mouseX);
                    }
                    for (int i = 0; i <StatSelectScreen. this.itemStatsList.itemColumns.size(); i++) {
                        this.renderStat(
                                guiGraphics,
                                ((StatType)StatSelectScreen.this.itemStatsList.itemColumns.get(i)).get(this.item),
                                left + ItemStatisticsList.this.getColumnX(i + StatSelectScreen.this.itemStatsList.blockColumns.size()),
                                top + height / 2 - 9 / 2,
                                index % 2 == 0,
                                hovering, mouseX
                        );
                    }
                }
            }

            protected void renderStat(GuiGraphics guiGraphics, @Nullable Stat<?> stat, int x, int y,
                                      boolean odd, boolean isMouseOver, int mouseX) {
                String string = stat == null ? "-" : stat.format(stats.getValue(stat));
                guiGraphics.drawString(font, string, x - font.width(string), y + 5, odd ? 16777215 : 9474192);

                int w = 18;
                if (stat != null && isMouseOver && mouseX >= x - w && mouseX < x) {
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            x - w, y, x, y + w, -2130706433, -2130706433, 0);
                    hovered = stat;
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getDescription());
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (hovered != null) {
                    StatType<?> type = hovered.getType();
                    if (type.getRegistry() == BuiltInRegistries.BLOCK) {
                        selectStat((StatType<Block>) type, ((BlockItem) item).getBlock());
                    } else {
                        selectStat((StatType<Item>) type, item);
                    }
                }
                return false;
            }
        }

        class ItemRowComparator implements Comparator<ItemStatisticsList.ItemRow> {
            public int compare(ItemStatisticsList.ItemRow row1, ItemStatisticsList.ItemRow row2) {
                Item item = row1.getItem();
                Item item2 = row2.getItem();
                int i;
                int j;
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<Block> statType = (StatType<Block>)ItemStatisticsList.this.sortColumn;
                    i = item instanceof BlockItem ?StatSelectScreen. this.stats.getValue(statType, ((BlockItem)item).getBlock()) : -1;
                    j = item2 instanceof BlockItem ? StatSelectScreen.this.stats.getValue(statType, ((BlockItem)item2).getBlock()) : -1;
                } else {
                    StatType<Item> statType = (StatType<Item>)ItemStatisticsList.this.sortColumn;
                    i = StatSelectScreen.this.stats.getValue(statType, item);
                    j = StatSelectScreen.this.stats.getValue(statType, item2);
                }

                return i == j
                        ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2))
                        : ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }
        }
    }

    class MobsStatisticsList extends ObjectSelectionList<MobsStatisticsList.MobRow> {
        public MobsStatisticsList(final Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width,  StatSelectScreen.this.height - 33 - 58, 33, 9 * 4);

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) > 0 || StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) > 0
                )
                {
                    this.addEntry(new MobsStatisticsList.MobRow(entityType));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        class MobRow extends ObjectSelectionList.Entry<MobsStatisticsList.MobRow> {
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;

            private final EntityType<?> entity;
            private int hovered = 0;

            public MobRow(final EntityType<?> entityType) {
                this.mobName = entityType.getDescription();
                this.entity = entityType;

                int i = StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType));
                if (i == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
                    this.hasKills = true;
                }

                int j = StatSelectScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
                if (j == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean mouseOver, float partialTicks) {

                hovered = 0;

                guiGraphics.drawString(font, this.mobName, left + 2, top + 1, 16777215);
                int stringX = left + 2 + 10;
                int stringY = top + 1 + 10;
                guiGraphics.drawString(font, kills, stringX, stringY, this.hasKills ? 9474192 : 6316128);

                if (mouseY >= stringY && mouseY <= stringY + 9) {
                    hovered = 1;
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            stringX, stringY, left + width - 12, stringY + 9, -2130706433, -2130706433, 0);
                }
                stringY = top + 1 + 10 * 2;
                guiGraphics.drawString(font, killedBy, stringX, stringY, this.wasKilledBy ? 9474192 : 6316128);
                if (mouseY >= stringY && mouseY <= stringY + 9) {
                    hovered = 2;
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            stringX, stringY, left + width - 12, stringY + 9, -2130706433, -2130706433, 0);
                }
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }


            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (hovered == 1) {
                    selectStat(Stats.ENTITY_KILLED, entity);
                } else if (hovered == 2) {
                    selectStat(Stats.ENTITY_KILLED_BY, entity);
                }
                return false;
            }
        }
    }
}
