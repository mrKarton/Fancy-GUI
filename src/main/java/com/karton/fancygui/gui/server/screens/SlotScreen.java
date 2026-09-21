package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.gui.server.font;
import com.karton.fancygui.util.NumberRange;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

import java.util.ArrayList;

public class SlotScreen extends SimpleGui {
    private final FontDescription containerFont = new FontDescription.Resource(
            FancyGUI.id("slot_screen")
    );

    private final Style containerStyle = Style.EMPTY.withFont(containerFont);

    private final int rows;
    private final ArrayList<Integer> neededSlots;

    private Component background = Component.empty();

    public SlotScreen(
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
        this.neededSlots = getFullRange(neededSlots);

        if (PolymerResourcePackUtils.hasMainPack(player.connection)) {
            buildBackground();

            this.background = Component.empty()
                    .append(this.background)
                    .append(
                            Component.literal("\uE102")
                                    .withStyle(containerStyle)
                    )
                    .append(buildCells());
        }

        this.background = Component.empty()
                .append(this.background)
                .append(Component.literal(title));

        this.setTitle(this.background);
    }

    private void buildBackground() {
        Component base = Component
                .literal("\uE100")
                .withStyle(containerStyle);

        Component lineBreak = Component
                .literal("\uE101")
                .withStyle(containerStyle);

        for (int row = 0; row < rows; row++) {
            if (row == 0) {
                base = Component.empty()
                        .append(base)
                        .append(getSpriteByRow(row));
            } else {
                base = Component.empty()
                        .append(base)
                        .append(lineBreak)
                        .append(getSpriteByRow(row));
            }
        }

        base = Component.empty()
                .append(base)
                .append(lineBreak);

        this.background = base;
    }

    private Component buildCells() {
        Component base = Component
                .literal("\uE105")
                .withStyle(containerStyle);

        for (int row = 0; row < rows; row++) {
            for (int column = 1; column <= 9; column++) {
                int slot = row * 9 + column;

                String literal = "\uE103";

                if (neededSlots.contains(slot)) {
                    literal = getSlotLiteralByRow(row);
                }

                base = Component.empty()
                        .append(base)
                        .append(
                                Component.literal(literal)
                                        .withStyle(containerStyle)
                                        .withColor(0xFFFFFF)
                        )
                        .append(
                                Component.literal("\uE105")
                                        .withStyle(containerStyle)
                        );
            }

            base = Component.empty()
                    .append(base)
                    .append(
                            Component.literal("\uE104")
                                    .withStyle(containerStyle)
                    );
        }

        return base;
    }

    public void setTextOnRow(int row, String text) {
        this.background = Component.empty()
                .append(this.background)
                .append(
                        Component.literal(text)
                                .withStyle(font.FIRST_ROW_STYLE)
                );

        this.setTitle(this.background);
    }

    private static MenuType<?> getMenuTypeByRows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows count should be in range from 1 to 6"
            );
        }

        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> throw new IllegalArgumentException(
                    "Rows count should be in range from 1 to 6"
            );
        };
    }

    private String getSlotLiteralByRow(int row) {
        return switch (row) {
            case 0 -> "\uE010";
            case 1 -> "\uE011";
            case 2 -> "\uE012";
            case 3 -> "\uE013";
            case 4 -> "\uE014";
            case 5 -> "\uE015";
            default -> throw new IndexOutOfBoundsException(
                    "Row should be in range from 0 to 5"
            );
        };
    }

    private Component getSpriteByRow(int row) {
        String literal = switch (row) {
            case 0 -> "\uE001";
            case 1 -> "\uE002";
            case 2 -> "\uE003";
            case 3 -> "\uE004";
            case 4 -> "\uE005";
            case 5 -> "\uE006";
            default -> throw new IndexOutOfBoundsException(
                    "Row should be in range from 0 to 5"
            );
        };

        return Component.literal(literal)
                .withStyle(containerStyle)
                .withColor(0xFFFFFF);
    }

    private static ArrayList<Integer> getFullRange(NumberRange[] ranges) {
        ArrayList<Integer> list = new ArrayList<>();

        for (NumberRange range : ranges) {
            for (int num : range.getRangeArray()) {
                list.add(num);
            }
        }

        return list;
    }
}