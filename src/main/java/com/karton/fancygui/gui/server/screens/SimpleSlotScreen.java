package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.gui.Button;
import com.karton.fancygui.gui.interfaces.SlotScreenGUI;
import com.karton.fancygui.gui.server.font;
import com.karton.fancygui.util.NumberRange;
import com.karton.fancygui.util.NumberRangeUtil;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.gui.SimpleGui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class SimpleSlotScreen extends SimpleGui implements SlotScreenGUI {

    private final FontDescription containerFont =
            new FontDescription.Resource(
                    FancyGUI.id("slot_screen")
            );

    private final Style containerStyle =
            Style.EMPTY.withFont(containerFont);

    private final int rows;

    /**
     * Слоты, которые должны визуально отображаться
     * как доступные/активные.
     *
     * В твоём GUI используется 1-based визуальная нумерация
     * при buildCells(), поэтому тут сохраняем именно те значения,
     * которые передаются через NumberRange.
     */
    protected final ArrayList<Number> neededSlots =
            new ArrayList<>();

    /**
     * Текущий background/title component GUI.
     */
    private Component background =
            Component.empty();

    /**
     * Отдельно храним пользовательский title,
     * чтобы buildBackground() не терял его.
     */
    private Component screenTitle =
            Component.empty();

    public SimpleSlotScreen(
            ServerPlayer player,
            String title,
            int rows
    ) {
        super(
                getMenuTypeByRows(rows),
                player,
                false
        );

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows count should be in range from 1 to 6"
            );
        }

        this.rows = rows;

        setTitle(title);
    }

    /**
     * Вариант конструктора с initial needed slots.
     *
     * Его можно использовать прямо из facade SlotScreen,
     * чтобы NumberRange[] из OpenTestScreenCommand действительно
     * передавался SGUI backend'у.
     */
    public SimpleSlotScreen(
            ServerPlayer player,
            String title,
            int rows,
            NumberRange[] neededSlots
    ) {
        super(
                getMenuTypeByRows(rows),
                player,
                false
        );

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows count should be in range from 1 to 6"
            );
        }

        this.rows = rows;

        if (neededSlots != null) {
            ArrayList<Integer> range =
                    NumberRangeUtil.getFulRange(neededSlots);

            this.neededSlots.addAll(range);
        }

        setTitle(title);
    }

    @Override
    public int getRows() {
        return rows;
    }

    /**
     * Полностью пересобирает title/background.
     */
    private void buildBackground() {

        if (!PolymerResourcePackUtils.hasMainPack(
                player.connection
        )) {
            /*
             * Если resource pack не загружен —
             * показываем обычный title без кастомных glyph.
             */
            super.setTitle(screenTitle);

            return;
        }

        this.background = Component.empty()
                .append(buildClearBackground())
                .append(
                        Component.literal("\uE102")
                                .withStyle(containerStyle)
                )
                .append(
                        buildCells(neededSlots)
                )
                .append(screenTitle);

        super.setTitle(background);
    }

    /**
     * Рисует базовый фон контейнера.
     */
    private Component buildClearBackground() {

        Component base =
                Component.literal("\uE100")
                        .withStyle(containerStyle);

        Component lineBreak =
                Component.literal("\uE101")
                        .withStyle(containerStyle);

        for (int row = 0; row < rows; row++) {

            if (row == 0) {

                base = Component.empty()
                        .append(base)
                        .append(
                                getSpriteByRow(row)
                        );

            } else {

                base = Component.empty()
                        .append(base)
                        .append(lineBreak)
                        .append(
                                getSpriteByRow(row)
                        );
            }
        }

        base = Component.empty()
                .append(base)
                .append(lineBreak);

        return base;
    }

    /**
     * Рисует сетку кастомных ячеек.
     */
    private Component buildCells(
            ArrayList<Number> neededSlots
    ) {
        Component base =
                Component.literal("\uE105")
                        .withStyle(containerStyle);

        for (int row = 0; row < rows; row++) {

            for (int column = 0; column < 9; column++) {

                /*
                 * 0-based нумерация:
                 *
                 * row 0: 0..8
                 * row 1: 9..17
                 * row 2: 18..26
                 * ...
                 */
                int slot =
                        row * 9 + column;

                String literal = "\uE103";

                if (neededSlots.contains(slot)) {
                    literal =
                            getSlotLiteralByRow(row);
                }

                base = Component.empty()
                        .append(base)
                        .append(
                                Component.literal(literal)
                                        .withStyle(
                                                containerStyle
                                        )
                                        .withColor(
                                                0xFFFFFF
                                        )
                        )
                        .append(
                                Component.literal("\uE105")
                                        .withStyle(
                                                containerStyle
                                        )
                        );
            }

            base = Component.empty()
                    .append(base)
                    .append(
                            Component.literal("\uE104")
                                    .withStyle(
                                            containerStyle
                                    )
                    );
        }

        return base;
    }

    /**
     * Добавляет текст поверх текущего GUI.
     *
     * Пока оставляю поведение максимально близким
     * к твоей текущей реализации.
     */
    public void setTextOnRow(
            int row,
            String text
    ) {
        this.background =
                Component.empty()
                        .append(this.background)
                        .append(
                                Component.literal(text)
                                        .withStyle(
                                                font.FIRST_ROW_STYLE
                                        )
                        );

        super.setTitle(
                this.background
        );
    }

    // ============================================================
    // TITLE
    // ============================================================

    @Override
    public void setTitle(String title) {
        setTitle(
                Component.literal(title)
        );
    }

    @Override
    public void setTitle(
            Component component
    ) {
        this.screenTitle =
                component != null
                        ? component
                        : Component.empty();

        buildBackground();
    }

    // ============================================================
    // BUTTONS
    // ============================================================

    @Override
    public void setButton(
            NumberRange slotsToSet,
            Button button
    ) {
        for (
                int slot :
                slotsToSet.getRangeArray()
        ) {
            setButton(
                    slot,
                    button
            );
        }
    }

    @Override
    public void setButton(
            int slotIndex,
            Button button
    ) {
        /*
         * Button теперь является общей моделью.
         *
         * Для SGUI создаём ServerButton только здесь,
         * как fallback representation.
         */
        setSlot(
                slotIndex,
                button.createServerFallback()
        );
    }

    // ============================================================
    // SLOTS
    // ============================================================

    @Override
    public void setSlot(
            NumberRange[] neededSlots,
            Slot slot
    ) {
        ArrayList<Integer> range =
                NumberRangeUtil.getFulRange(
                        neededSlots
                );

        for (
                Integer slotIndex :
                range
        ) {
            setSlot(
                    slotIndex,
                    slot
            );
        }
    }

    @Override
    public ItemStack getCarried() {
        return player.containerMenu.getCarried();
    }

    @Override
    public void setCarried(ItemStack stack) {
        player.containerMenu.setCarried(stack);
        player.containerMenu.broadcastChanges();
    }

    @Override
    public void setSlot(
            int slotIndex,
            Slot slot
    ) {
        /*
         * Для background у тебя используется 1-based нумерация,
         * тогда как SGUI slotIndex — 0-based.
         *
         * Здесь пока сохраняю твой существующий контракт:
         * индекс добавляется как передан.
         *
         * Если neededSlots из NumberRange у тебя тоже 0-based,
         * потом унифицируем это отдельно.
         */
        if (!this.neededSlots.contains(slotIndex)) {
            this.neededSlots.add(
                    slotIndex
            );
        }

        super.setSlot(
                slotIndex,
                slot
        );

        buildBackground();
    }

    // ============================================================
    // UTIL
    // ============================================================

    private static MenuType<?> getMenuTypeByRows(
            int rows
    ) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows count should be in range from 1 to 6"
            );
        }

        return switch (rows) {
            case 1 ->
                    MenuType.GENERIC_9x1;

            case 2 ->
                    MenuType.GENERIC_9x2;

            case 3 ->
                    MenuType.GENERIC_9x3;

            case 4 ->
                    MenuType.GENERIC_9x4;

            case 5 ->
                    MenuType.GENERIC_9x5;

            case 6 ->
                    MenuType.GENERIC_9x6;

            default ->
                    throw new IllegalArgumentException(
                            "Rows count should be in range from 1 to 6"
                    );
        };
    }

    private String getSlotLiteralByRow(
            int row
    ) {
        return switch (row) {
            case 0 -> "\uE010";
            case 1 -> "\uE011";
            case 2 -> "\uE012";
            case 3 -> "\uE013";
            case 4 -> "\uE014";
            case 5 -> "\uE015";

            default ->
                    throw new IndexOutOfBoundsException(
                            "Row should be in range from 0 to 5"
                    );
        };
    }

    private Component getSpriteByRow(
            int row
    ) {
        String literal =
                switch (row) {
                    case 0 -> "\uE001";
                    case 1 -> "\uE002";
                    case 2 -> "\uE003";
                    case 3 -> "\uE004";
                    case 4 -> "\uE005";
                    case 5 -> "\uE006";

                    default ->
                            throw new IndexOutOfBoundsException(
                                    "Row should be in range from 0 to 5"
                            );
                };

        return Component.literal(literal)
                .withStyle(containerStyle)
                .withColor(0xFFFFFF);
    }
}