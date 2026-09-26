package com.karton.fancygui.network;

public enum ScreenType {
    SLOT(0),
    TEXT_INPUT(1);

    private final int id;

    ScreenType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ScreenType byId(int id) {
        for (ScreenType value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown FancyGUI screen type: " + id);
    }
}
