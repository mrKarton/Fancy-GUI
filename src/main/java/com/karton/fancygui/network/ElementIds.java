package com.karton.fancygui.network;

public final class ElementIds {

    private static final int BUTTON_BASE = 1_000;
    public static final int TEXT_INPUT = 2_000;
    public static final int CARRIED_STACK = 3_000;
    private static final int PLAYER_INVENTORY_BASE = 10_000;
    private static final int PLAYER_INVENTORY_SIZE = 36;

    private ElementIds() {
    }

    public static int slot(int gridIndex) {
        return gridIndex;
    }

    public static boolean isSlot(int elementId) {
        return elementId >= 0 && elementId < BUTTON_BASE;
    }

    public static int slotGridIndex(int elementId) {
        return elementId;
    }

    public static int button(int gridIndex) {
        return BUTTON_BASE + gridIndex;
    }

    public static int buttonGridIndex(int elementId) {
        return elementId - BUTTON_BASE;
    }

    public static int playerInventory(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= PLAYER_INVENTORY_SIZE) {
            throw new IndexOutOfBoundsException(
                    "Player inventory index " + inventoryIndex + " outside range 0..35"
            );
        }

        return PLAYER_INVENTORY_BASE + inventoryIndex;
    }

    public static boolean isPlayerInventory(int elementId) {
        return elementId >= PLAYER_INVENTORY_BASE
                && elementId < PLAYER_INVENTORY_BASE + PLAYER_INVENTORY_SIZE;
    }

    public static int playerInventoryIndex(int elementId) {
        return elementId - PLAYER_INVENTORY_BASE;
    }
}
