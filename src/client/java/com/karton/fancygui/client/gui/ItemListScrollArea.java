package com.karton.fancygui.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;


public final class ItemListScrollArea extends AbstractScrollArea {

    /**
     * Количество колонок списка.
     *
     * Девятая колонка GUI оставлена под scrollbar.
     */
    public static final int COLUMNS = 8;

    /**
     * Размер vanilla slot cell:
     *
     * 16px item
     * + 1px border слева
     * + 1px border справа
     */
    public static final int SLOT_SIZE = 18;

    /**
     * Количество одновременно видимых строк.
     */
    public static final int VISIBLE_ROWS = 4;

    /**
     * Ширина области самих предметов.
     */
    public static final int CONTENT_WIDTH =
            COLUMNS * SLOT_SIZE; // 144

    /**
     * Высота viewport.
     */
    public static final int VIEW_HEIGHT =
            VISIBLE_ROWS * SLOT_SIZE - 6; // -6 for margin betwen view area and bottom slots

    /**
     * Общая ширина widget.
     *
     * 144px занимает item-grid.
     * Остальное место используется scrollbar.
     */
    public static final int WIDGET_WIDTH = 160;

    /**
     * Scroll step.
     *
     * 9 = половина слота.
     *
     * Поэтому wheel двигает содержимое плавнее,
     * чем построчное перемещение по 18px.
     *
     * Если нужен строго row-by-row scroll:
     *
     *     SCROLL_RATE = SLOT_SIZE;
     */
    private static final int SCROLL_RATE = 9;

    private static final Identifier SLOT_SPRITE =
            Identifier.withDefaultNamespace(
                    "container/slot"
            );

    private static final Identifier SCROLLER_SPRITE =
            Identifier.withDefaultNamespace(
                    "container/creative_inventory/scroller"
            );

    private static final Identifier SCROLLER_DISABLED_SPRITE =
            Identifier.withDefaultNamespace(
                    "container/creative_inventory/scroller"
            );
    private static final Identifier SCROLLER_BACKGROUND_SPRITE =
            Identifier.withDefaultNamespace(
                    "widget/scroller_background"
            );

    private final Font font;
    private final IntConsumer itemClickCallback;

    private List<ItemStack> items = List.of();

    public ItemListScrollArea(
            int x,
            int y,
            Font font,
            IntConsumer itemClickCallback
    ) {
        super(
                x,
                y,
                WIDGET_WIDTH,
                VIEW_HEIGHT,
                Component.literal("Item list"),
                new ScrollbarSettings(
                        SCROLLER_SPRITE,
                        SCROLLER_DISABLED_SPRITE,
                        SCROLLER_BACKGROUND_SPRITE,
                        16,
                        108,
                        SCROLL_RATE,
                        true
                )
        );

        this.font = font;
        this.itemClickCallback = itemClickCallback;
    }

    // ============================================================
    // DATA
    // ============================================================

    /**
     * Полностью заменяет отображаемый список.
     *
     * Все ItemStack копируются, чтобы screen не зависел
     * от mutable server/network snapshot.
     */
    public void setItems(
            List<ItemStack> newItems
    ) {
        if (newItems == null || newItems.isEmpty()) {
            items = List.of();

            /*
             * Clamp старого scrollAmount.
             */
            refreshScrollAmount();

            return;
        }

        List<ItemStack> copy =
                new ArrayList<>(newItems.size());

        for (ItemStack stack : newItems) {
            copy.add(
                    stack == null
                            ? ItemStack.EMPTY
                            : stack.copy()
            );
        }

        items = List.copyOf(copy);

        /*
         * Например:
         *
         * было 500 items, пользователь был внизу;
         * стало 10 items.
         *
         * Старый scrollAmount больше нового maxScrollAmount.
         * refreshScrollAmount() его обрежет.
         */
        refreshScrollAmount();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public int getItemCount() {
        return items.size();
    }

    // ============================================================
    // ABSTRACT SCROLL AREA
    // ============================================================

    /**
     * Полная высота scrollable content.
     *
     * Здесь используется именно PIXEL height, а не номер страницы.
     */
    @Override
    protected int contentHeight() {
        int size =
                items == null
                        ? 0
                        : items.size();

        int itemRows =
                (size + COLUMNS - 1)
                        / COLUMNS;

        /*
         * Даже если items меньше 32,
         * визуально всё равно оставляем 4 ряда slots.
         */
        int rows =
                Math.max(
                        VISIBLE_ROWS,
                        itemRows
                );

        return rows * SLOT_SIZE;
    }

    /**
     * Ставим vanilla scrollbar на место девятой колонки.
     *
     * ListView:
     *
     * x = 8
     *
     * 8 columns:
     * 8 + 8 * 18 = 152
     *
     * + 3px gap:
     * 155
     */
    @Override
    protected int scrollBarX() {
        return getX()
                + CONTENT_WIDTH
                + 3;
    }

    // ============================================================
    // RENDER
    // ============================================================

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        double scroll = scrollAmount();

        /*
         * Gui rendering работает с integer coordinates,
         * поэтому scrollAmount округляем только непосредственно
         * при вычислении pixel position.
         */
        int scrollPixels =
                (int) Math.floor(scroll);

        int totalRows =
                Math.max(
                        VISIBLE_ROWS,
                        (items.size() + COLUMNS - 1)
                                / COLUMNS
                );

        /*
         * Не рендерим весь гигантский список.
         *
         * Клиент МОЖЕТ хранить весь список,
         * но рендерятся только строки,
         * пересекающие viewport.
         */
        int firstRow =
                Math.max(
                        0,
                        (int) Math.floor(
                                scroll / SLOT_SIZE
                        )
                );

        int lastRowExclusive =
                Math.min(
                        totalRows,
                        (int) Math.ceil(
                                (scroll + getHeight())
                                        / SLOT_SIZE
                        )
                );

        int hoveredIndex =
                itemIndexAt(
                        mouseX,
                        mouseY
                );

        /*
         * Clip только item-grid.
         *
         * Scrollbar рендерим ПОСЛЕ disableScissor(),
         * иначе его можно случайно обрезать.
         *
         * -1 нужен из-за vanilla slot border.
         */
        graphics.enableScissor(
                getX() - 1,
                getY() - 1,
                getX() + CONTENT_WIDTH,
                getY() + getHeight()
        );

        for (
                int row = firstRow;
                row < lastRowExclusive;
                row++
        ) {
            int slotY =
                    getY()
                            + row * SLOT_SIZE
                            - scrollPixels;

            for (
                    int column = 0;
                    column < COLUMNS;
                    column++
            ) {
                int slotX =
                        getX()
                                + column * SLOT_SIZE;

                int index =
                        row * COLUMNS
                                + column;

                renderSlotBackground(
                        graphics,
                        slotX,
                        slotY
                );

                if (index >= items.size()) {
                    continue;
                }

                ItemStack stack =
                        items.get(index);

                if (stack.isEmpty()) {
                    continue;
                }

                renderItem(
                        graphics,
                        stack,
                        slotX,
                        slotY
                );

                if (index == hoveredIndex) {
                    renderHighlight(
                            graphics,
                            slotX,
                            slotY
                    );

                    graphics.setTooltipForNextFrame(
                            font,
                            stack,
                            mouseX,
                            mouseY
                    );
                }
            }
        }

        graphics.disableScissor();

        /*
         * Vanilla 26.3 scrollbar.
         */
        extractScrollbar(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void renderSlotBackground(
            GuiGraphicsExtractor graphics,
            int slotX,
            int slotY
    ) {
        /*
         * slotX/slotY соответствуют координатам ItemStack,
         * поэтому vanilla slot background начинается на 1px раньше.
         */
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SLOT_SPRITE,
                slotX - 1,
                slotY - 1,
                SLOT_SIZE,
                SLOT_SIZE
        );
    }

    private void renderItem(
            GuiGraphicsExtractor graphics,
            ItemStack stack,
            int slotX,
            int slotY
    ) {
        graphics.item(
                stack,
                slotX,
                slotY
        );

        /*
         * count / durability / cooldown decorations.
         */
        graphics.itemDecorations(
                font,
                stack,
                slotX,
                slotY
        );
    }

    private void renderHighlight(
            GuiGraphicsExtractor graphics,
            int slotX,
            int slotY
    ) {
        /*
         * Только внутренняя 16x16 область.
         */
        graphics.fill(
                slotX,
                slotY,
                slotX + 16,
                slotY + 16,
                0x80FFFFFF
        );
    }

    // ============================================================
    // HIT TEST
    // ============================================================

    /**
     * Получает индекс именно во ВСЁМ items,
     * а не индекс текущего viewport.
     */
    private int itemIndexAt(
            double mouseX,
            double mouseY
    ) {
        /*
         * Предметы занимают только первые 144px.
         * Scrollbar справа сюда не входит.
         */
        if (
                mouseX < getX()
                        || mouseX >= getX() + CONTENT_WIDTH
        ) {
            return -1;
        }

        if (
                mouseY < getY()
                        || mouseY >= getY() + getHeight()
        ) {
            return -1;
        }

        double contentX =
                mouseX - getX();

        double contentY =
                mouseY
                        - getY()
                        + scrollAmount();

        int column =
                (int) Math.floor(
                        contentX / SLOT_SIZE
                );

        int row =
                (int) Math.floor(
                        contentY / SLOT_SIZE
                );

        if (
                column < 0
                        || column >= COLUMNS
                        || row < 0
        ) {
            return -1;
        }

        int index =
                row * COLUMNS
                        + column;

        if (
                index < 0
                        || index >= items.size()
        ) {
            return -1;
        }

        return index;
    }

    // ============================================================
    // INPUT
    // ============================================================

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (
                !active
                        || !visible
                        || !isMouseOver(
                        event.x(),
                        event.y()
                )
        ) {
            return false;
        }

        /*
         * AbstractScrollArea сам определяет,
         * попал ли click на scrollbar,
         * и включает режим dragging.
         */
        if (updateScrolling(event)) {
            return true;
        }

        /*
         * Callback ListView пока не хранит mouse button,
         * поэтому вызываем его только для LMB.
         */
        if (event.button() == 0) {
            int index =
                    itemIndexAt(
                            event.x(),
                            event.y()
                    );

            if (index >= 0) {
                itemClickCallback.accept(index);
            }
        }

        /*
         * ВАЖНО:
         *
         * Даже click по пустой клетке поглощаем.
         *
         * Иначе событие может пройти ниже в
         * AbstractContainerScreen.
         */
        return true;
    }

    // mouseScrolled(...)
    // mouseDragged(...)
    // onRelease(...)
    //
    // НЕ переопределяем.
    //
    // Всё это уже реализует AbstractScrollArea.

    // ============================================================
    // NARRATION
    // ============================================================

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        /*
         * Пока отдельная accessibility narration для virtual item
         * cells не нужна.
         *
         * Метод обязателен из-за AbstractWidget.
         */
    }
}