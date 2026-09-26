package com.karton.fancygui.network;

public enum ScreenActionType {
    BUTTON_CLICK(0),
    TEXT_CHANGED(1),
    TEXT_SUBMIT(2),
    CLOSE(3),
    SLOT_CLICK(4);

    private final int id;

    ScreenActionType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ScreenActionType byId(int id) {
        for (ScreenActionType value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown FancyGUI action type: " + id);
    }
}
