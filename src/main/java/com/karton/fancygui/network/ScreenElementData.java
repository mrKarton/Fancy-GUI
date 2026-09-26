package com.karton.fancygui.network;

import net.minecraft.world.item.ItemStack;

public record ScreenElementData(
        int id,
        ScreenElementType type,
        int gridIndex,
        int x,
        int y,
        int width,
        int height,
        String text,
        String secondaryText,
        ItemStack item
) {
    public ScreenElementData {
        text = text != null ? text : "";
        secondaryText = secondaryText != null ? secondaryText : "";
        item = item != null ? item : ItemStack.EMPTY;
    }

    public static ScreenElementData slot(
            int gridIndex,
            ItemStack item
    ) {
        return new ScreenElementData(
                ElementIds.slot(gridIndex),
                ScreenElementType.SLOT,
                gridIndex,
                -1,
                -1,
                18,
                18,
                "",
                "",
                item
        );
    }

    public static ScreenElementData button(
            int gridIndex,
            int width,
            int height,
            String title,
            String caption,
            ItemStack icon
    ) {
        return new ScreenElementData(
                ElementIds.button(gridIndex),
                ScreenElementType.BUTTON,
                gridIndex,
                -1,
                -1,
                width,
                height,
                title,
                caption,
                icon
        );
    }

    public static ScreenElementData textInput(
            int x,
            int y,
            int width,
            int height,
            String value,
            String hint
    ) {
        return new ScreenElementData(
                ElementIds.TEXT_INPUT,
                ScreenElementType.TEXT_INPUT,
                -1,
                x,
                y,
                width,
                height,
                value,
                hint,
                ItemStack.EMPTY
        );
    }

    public static ScreenElementData carriedStack(ItemStack item) {
        return new ScreenElementData(
                ElementIds.CARRIED_STACK,
                ScreenElementType.CARRIED_STACK,
                -1,
                -1,
                -1,
                16,
                16,
                "",
                "",
                item
        );
    }
}
