package com.karton.fancygui.network;

public enum ScreenElementType {
    SLOT(0),
    BUTTON(1),
    TEXT_INPUT(2),
    CARRIED_STACK(3);

    private final int id;

    ScreenElementType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ScreenElementType byId(int id) {
        for (ScreenElementType value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown FancyGUI element type: " + id);
    }
}
