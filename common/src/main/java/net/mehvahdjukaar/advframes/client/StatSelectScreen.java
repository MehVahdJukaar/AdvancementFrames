package net.mehvahdjukaar.advframes.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.advframes.network.NetworkHandler;
import net.mehvahdjukaar.advframes.network.ServerBoundSetStatFramePacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class StatSelectScreen extends Screen implements StatsUpdateListener {
    private final StatFrameBlockTile tile;

    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    private GeneralStatisticsList statsList;
    ItemStatisticsList itemStatsList;
    private MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    /**
     * When true, the game will be paused when the gui is shown
     */
    private boolean isLoading = true;

    public StatSelectScreen(StatFrameBlockTile tile, StatsCounter statsCounter) {
        super(Component.translatable("advancementframes.gui.statistics"));
        this.tile = tile;
        this.stats = statsCounter;
    }

    @Override
    protected void init() {
        this.isLoading = true;
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        this.addRenderableWidget(Button.builder(Component.translatable("stat.generalButton"), (buttonx) -> {
            this.setActiveList(this.statsList);
        }).bounds(this.width / 2 - 120, this.height - 52, 80, 20).build());
        Button button = this.addRenderableWidget(Button.builder(Component.translatable("stat.itemsButton"), (buttonx) -> {
            this.setActiveList(this.itemStatsList);
        }).bounds(this.width / 2 - 40, this.height - 52, 80, 20).build());
        Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("stat.mobsButton"), (buttonx) -> {
            this.setActiveList(this.mobsStatsList);
        }).bounds(this.width / 2 + 40, this.height - 52, 80, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (buttonx) -> {
            this.minecraft.setScreen(null);
        }).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
        if (this.itemStatsList.children().isEmpty()) {
            button.active = false;
        }

        if (this.mobsStatsList.children().isEmpty()) {
            button2.active = false;
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.isLoading) {
            this.renderBackground(guiGraphics);
            guiGraphics.drawCenteredString(this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
            String var10002 = LOADING_SYMBOLS[(int) (Util.getMillis() / 150L % LOADING_SYMBOLS.length)];
            int midX = this.width / 2;
            int mixY = this.height / 2;
            Objects.requireNonNull(this.font);
            guiGraphics.drawCenteredString(font, var10002, midX, mixY + 9 * 2, 16777215);
        } else {
            this.getActiveList().render(guiGraphics, i, j, f);
            guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
            super.render(guiGraphics, i, j, f);
        }
    }

    @Override
    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.initButtons();
            this.setActiveList(this.statsList);
            this.isLoading = false;
        }

    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    @Nullable
    public ObjectSelectionList<?> getActiveList() {
        return this.activeList;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> activeList) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }

        if (activeList != null) {
            this.addWidget(activeList);
            this.activeList = activeList;
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> stat) {
        String string = stat.getValue().toString();
        return "stat." + string.replace(':', '.');
    }

    int getColumnX(int index) {
        return 115 + 40 * index;
    }

    void blitSlot(GuiGraphics guiGraphics, int i, int j, Item item) {
        this.blitSlotIcon(guiGraphics, i + 1, j + 1, 0, 0);
        guiGraphics.renderFakeItem(item.getDefaultInstance(), i + 2, j + 2);
    }

    void blitSlotIcon(GuiGraphics guiGraphics, int i, int j, int k, int l) {
        guiGraphics.blit(STATS_ICON_LOCATION, i, j, 0, k, l, 18, 18, 128, 128);
    }

    private <T> void selectStat(StatType<T> statType, T obj) {
        NetworkHandler.CHANNEL.sendToServer(
                new ServerBoundSetStatFramePacket(tile.getBlockPos(), statType, obj));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.onClose();
    }

    class GeneralStatisticsList extends ObjectSelectionList<GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width - 8, StatSelectScreen.this.height, 32,
                    StatSelectScreen.this.height - 64, 10);
            ObjectArrayList<Stat<ResourceLocation>> objectArrayList = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            objectArrayList.sort(Comparator.comparing(statx -> I18n.get(getTranslationKey(statx))));

            for (var stat : objectArrayList) {
                this.addEntry(new GeneralStatisticsList.Entry(stat));
            }

            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {
            StatSelectScreen.this.renderBackground(guiGraphics);
        }

        private class Entry extends ObjectSelectionList.Entry<GeneralStatisticsList.Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(Stat<ResourceLocation> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(stats.getValue(this.stat));
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                left += 2;
                top += 1;
                guiGraphics.drawString(font, this.statDisplay, left, top, index % 2 == 0 ? 16777215 : 9474192);
                String string = this.getValueText();
                guiGraphics.drawString(font, string, left + width - font.width(string), top, index % 2 == 0 ? 16777215 : 9474192);
                if (isMouseOver) {
                    guiGraphics.fillGradient(RenderType.guiOverlay(),
                            left, top, left + width, top + height + 2, -2130706433, -2130706433, 0);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                selectStat(stat.getType(), stat.getValue());
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
            }
        }
    }

    class ItemStatisticsList extends ObjectSelectionList<ItemStatisticsList.ItemRow> {
        protected final List<StatType<Block>> blockColumns = Lists.newArrayList();
        protected final List<StatType<Item>> itemColumns;
        private final int[] iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
        protected int headerPressed = -1;
        protected final Comparator<ItemStatisticsList.ItemRow> itemStatSorter = new ItemStatisticsList.ItemRowComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.height, 32, StatSelectScreen.this.height - 64, 20);
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);

            this.setRenderHeader(true, 20);
            Set<Item> set = Sets.newIdentityHashSet();

            boolean success;
            for (Item item : BuiltInRegistries.ITEM) {
                success = false;

                for (StatType<Item> statType : this.itemColumns) {
                    if (statType.contains(item) && stats.getValue(statType.get(item)) > 0) {
                        success = true;
                    }
                }
                if (success) set.add(item);
            }


            for (Block block : BuiltInRegistries.BLOCK) {
                success = false;
                for (var statType : blockColumns) {
                    if (statType.contains(block) && stats.getValue(statType.get(block)) > 0) {
                        success = true;
                    }
                }
                if (success) set.add(block.asItem());
            }

            set.remove(Items.AIR);

            for (var item : set) {
                this.addEntry(new ItemStatisticsList.ItemRow(item));
            }

            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {
            StatSelectScreen.this.renderBackground(guiGraphics);
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int i, int j) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }

            int k;
            for (k = 0; k < this.iconOffsets.length; ++k) {
                blitSlotIcon(guiGraphics, i + getColumnX(k) - 18, j + 1, 0, this.headerPressed == k ? 0 : 18);
            }

            int l;
            if (this.sortColumn != null) {
                k = getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                l = this.sortOrder == 1 ? 2 : 1;
                blitSlotIcon(guiGraphics, i + k, j + 1, 18 * l, 0);
            }

            for (k = 0; k < this.iconOffsets.length; ++k) {
                l = this.headerPressed == k ? 1 : 0;
                blitSlotIcon(guiGraphics, i + getColumnX(k) - 18 + l, j + 1 + l, 18 * this.iconOffsets[k], 18);
            }

        }

        @Override
        public int getRowWidth() {
            return 375;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        @Override
        protected void clickedHeader(int mouseX, int mouseY) {
            this.headerPressed = -1;

            for (int i = 0; i < this.iconOffsets.length; ++i) {
                int j = mouseX - getColumnX(i);
                if (j >= -36 && j <= 0) {
                    this.headerPressed = i;
                    break;
                }
            }

            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }

        }

        private StatType<?> getColumn(int index) {
            return index < this.blockColumns.size() ? this.blockColumns.get(index) : this.itemColumns.get(index - this.blockColumns.size());
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
            if (mouseY >= this.y0 && mouseY < this.y1) {
                ItemStatisticsList.ItemRow itemRow = this.getHovered();
                int k = (this.width - this.getRowWidth()) / 2;
                Component component = null;
                if (itemRow != null) {
                    if (mouseX < k + 44 || mouseX > k + 44 + 18) {
                        return;
                    }
                    Item item = itemRow.getItem();
                    component = item.getDescription();
                } else {
                    int l = mouseX - k - 2;

                    for (int m = 0; m < this.iconOffsets.length; ++m) {
                        int n = getColumnX(m);
                        if (l >= n - 18 && l <= n) {
                            component = this.getColumn(m).getDisplayName();
                            break;
                        }
                    }
                }
                if (component != null)
                    StatSelectScreen.this.setTooltipForNextRenderPass(component);
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

        private class ItemRowComparator implements Comparator<ItemStatisticsList.ItemRow> {
            ItemRowComparator() {
            }

            public int compare(ItemStatisticsList.ItemRow row1, ItemStatisticsList.ItemRow row2) {
                Item item = row1.getItem();
                Item item2 = row2.getItem();
                int i;
                int j;
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else {
                    StatType statType;
                    if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                        statType = ItemStatisticsList.this.sortColumn;
                        i = item instanceof BlockItem bi ? stats.getValue(statType, bi.getBlock()) : -1;
                        j = item2 instanceof BlockItem bi ? stats.getValue(statType, bi.getBlock()) : -1;
                    } else {
                        statType = ItemStatisticsList.this.sortColumn;
                        i = stats.getValue(statType, item);
                        j = stats.getValue(statType, item2);
                    }
                }

                return i == j ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2)) : ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }
        }

        private class ItemRow extends ObjectSelectionList.Entry<ItemStatisticsList.ItemRow> {
            private final Item item;

            private Stat<Item> hovered = null;

            ItemRow(Item item) {
                this.item = item;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                blitSlot(guiGraphics, left + 40, top, this.item);
                hovered = null;
                int p;
                for (p = 0; p < itemStatsList.blockColumns.size(); ++p) {
                    Stat stat;
                    if (this.item instanceof BlockItem bi) {
                        stat = (itemStatsList.blockColumns.get(p)).get(bi.getBlock());
                    } else {
                        stat = null;
                    }

                    this.renderStat(guiGraphics, stat, left + getColumnX(p), top, index % 2 == 0,
                            isMouseOver, mouseX);
                }

                for (p = 0; p < itemStatsList.itemColumns.size(); ++p) {
                    this.renderStat(guiGraphics, (itemStatsList.itemColumns.get(p)).get(this.item),
                            left + getColumnX(p + itemStatsList.blockColumns.size()), top, index % 2 == 0,
                            isMouseOver, mouseX);
                }

            }

            protected void renderStat(GuiGraphics guiGraphics, @Nullable Stat<Item> stat, int x, int y,
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
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (hovered != null) {
                    selectStat(hovered.getType(), item);
                }
                return false;
            }

            @Nullable
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getDescription());
            }
        }
    }

    class MobsStatisticsList extends ObjectSelectionList<MobsStatisticsList.MobRow> {
        public MobsStatisticsList(Minecraft minecraft) {
            super(minecraft, StatSelectScreen.this.width, StatSelectScreen.this.height, 32, StatSelectScreen.this.height - 64, 32);

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (stats.getValue(Stats.ENTITY_KILLED.get(entityType)) > 0 ||
                        stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) > 0) {
                    this.addEntry(new MobsStatisticsList.MobRow(entityType));
                }
            }
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {
            StatSelectScreen.this.renderBackground(guiGraphics);
        }

        private class MobRow extends ObjectSelectionList.Entry<MobsStatisticsList.MobRow> {
            private final Component mobName;
            private final Component kills;
            private final boolean hasKills;
            private final Component killedBy;
            private final boolean wasKilledBy;
            private final EntityType<?> entity;

            private int hovered = 0;

            public MobRow(EntityType<?> entityType) {
                this.entity = entityType;
                this.mobName = entityType.getDescription();
                int entitiesKilled = stats.getValue(Stats.ENTITY_KILLED.get(entityType));
                if (entitiesKilled == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", entitiesKilled, this.mobName);
                    this.hasKills = true;
                }

                int killedByEntities = stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
                if (killedByEntities == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, killedByEntities);
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
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (hovered == 1) {
                    selectStat(Stats.ENTITY_KILLED, entity);
                } else if (hovered == 2) {
                    selectStat(Stats.ENTITY_KILLED_BY, entity);
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }

}

