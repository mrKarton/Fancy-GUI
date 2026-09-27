package com.karton.fancygui.gui.menu;

import com.karton.fancygui.network.ScreenType;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Локальный/native menu FancyGUI.
 *
 * Он НЕ регистрируется как MenuType и НЕ открывается через vanilla OpenScreen packet.
 * И на клиенте, и на сервере создаётся зеркальный экземпляр этого menu, а клики
 * передаются через FancyGUI networking.
 *
 * Главное свойство: GUI Slot-объекты создаются только для gridIndex, которые
 * реально объявлены в NumberRange[] на сервере.
 */
public final class FancyMenu extends AbstractContainerMenu {

    public static final int SLOT_SIZE = 18;
    public static final int SLOT_START_X = 8;

    private final int rows;
    private final int slotStartY;

    /** gridIndex FancyGUI -> index в AbstractContainerMenu.slots. */
    private final Map<Integer, Integer> gridToMenuSlot =
            new LinkedHashMap<>();

    /** Количество sparse GUI slots до добавления inventory игрока. */
    private int guiSlotCount;

    private FancyMenu(
            int containerId,
            int rows,
            int slotStartY
    ) {
        super(null, containerId);

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows must be between 1 and 6"
            );
        }

        this.rows = rows;
        this.slotStartY = slotStartY;
    }

    /**
     * Client-side mirror.
     *
     * Для GUI cells используется отдельный backing container, но Slot создаётся
     * только для тех gridIndex, которые присутствуют в guiStacks.
     */
    public static FancyMenu createClient(
            int containerId,
            Inventory inventory,
            int rows,
            int slotStartY,
            Map<Integer, ItemStack> guiStacks,
            ScreenType screenType
    ) {
        FancyMenu menu = new FancyMenu(
                containerId,
                rows,
                slotStartY
        );

        SimpleContainer container =
                new SimpleContainer(rows * 9);

        Map<Integer, ItemStack> sorted =
                new TreeMap<>(guiStacks);

        for (Map.Entry<Integer, ItemStack> entry : sorted.entrySet()) {
            int gridIndex = entry.getKey();
            menu.checkGridIndex(gridIndex);

            ItemStack stack = entry.getValue();
            container.setItem(
                    gridIndex,
                    stack != null ? stack.copy() : ItemStack.EMPTY
            );

            int x = getSlotX(
                    screenType,
                    gridIndex
            );

            int y = getSlotY(
                    screenType,
                    gridIndex,
                    slotStartY
            );

            menu.addGuiSlot(
                    gridIndex,
                    new Slot(
                            container,
                            gridIndex,
                            x,
                            y
                    )
            );
        }

        menu.finishPlayerInventory(inventory);
        return menu;
    }

    /**
     * Server-side authoritative mirror.
     *
     * Сюда передаются реальные Slot-объекты FancyGUI, включая InputSlot и другие
     * пользовательские реализации. Их mayPlace/mayPickup/setByPlayer остаются
     * серверным источником истины.
     */
    public static FancyMenu createServer(
            int containerId,
            Inventory inventory,
            int rows,
            int slotStartY,
            Map<Integer, Slot> guiSlots
    ) {
        FancyMenu menu = new FancyMenu(
                containerId,
                rows,
                slotStartY
        );

        Map<Integer, Slot> sorted =
                new TreeMap<>(guiSlots);

        for (Map.Entry<Integer, Slot> entry : sorted.entrySet()) {
            int gridIndex = entry.getKey();
            Slot slot = entry.getValue();

            menu.checkGridIndex(gridIndex);

            if (slot == null) {
                throw new IllegalArgumentException(
                        "Slot for grid index " + gridIndex + " cannot be null"
                );
            }

            menu.addGuiSlot(gridIndex, slot);
        }

        menu.finishPlayerInventory(inventory);
        return menu;
    }

    private void addGuiSlot(
            int gridIndex,
            Slot slot
    ) {
        Slot added = addSlot(slot);
        gridToMenuSlot.put(gridIndex, added.index);
    }

    private void finishPlayerInventory(
            Inventory inventory
    ) {
        this.guiSlotCount = this.slots.size();

        /*
         * Vanilla generic-container layout:
         * main inventory starts 13 px after the end of the top slot grid.
         * addStandardInventorySlots itself adds the 3x9 inventory + hotbar.
         */
        int playerInventoryY =
                slotStartY + rows * SLOT_SIZE + 13;

        addStandardInventorySlots(
                inventory,
                SLOT_START_X,
                playerInventoryY
        );
    }

    public int getRows() {
        return rows;
    }

    public int getSlotStartY() {
        return slotStartY;
    }

    public int getGuiSlotCount() {
        return guiSlotCount;
    }

    public static int getSlotX(
            ScreenType screenType,
            int gridIndex
    ) {
        if (screenType == ScreenType.TEXT_INPUT) {
            return switch (gridIndex) {
                case 0 -> (176 - SLOT_SIZE) / 2; // 79, центр
                case 1 -> SLOT_START_X;          // 8, слева
                case 2 -> 176 - SLOT_START_X - SLOT_SIZE; // 150, справа
                default ->
                        SLOT_START_X
                                + (gridIndex % 9) * SLOT_SIZE;
            };
        }

        return SLOT_START_X
                + (gridIndex % 9) * SLOT_SIZE;
    }

    public static int getSlotY(
            ScreenType screenType,
            int gridIndex,
            int slotStartY
    ) {
        if (
                screenType == ScreenType.TEXT_INPUT
                        && gridIndex <= 2
        ) {
            return slotStartY;
        }

        return slotStartY
                + (gridIndex / 9) * SLOT_SIZE;
    }

    public boolean hasGuiGridIndex(
            int gridIndex
    ) {
        return gridToMenuSlot.containsKey(gridIndex);
    }

    public Set<Integer> getGuiGridIndices() {
        return Collections.unmodifiableSet(
                gridToMenuSlot.keySet()
        );
    }

    public int getMenuSlotForGridIndex(
            int gridIndex
    ) {
        Integer result = gridToMenuSlot.get(gridIndex);

        if (result == null) {
            return -1;
        }

        return result;
    }

    /**
     * Авторитетный state packet меняет содержимое уже существующего sparse slot.
     * Новый Slot здесь никогда не создаётся.
     */
    public void setGuiStack(
            int gridIndex,
            ItemStack stack
    ) {
        Integer menuSlotIndex =
                gridToMenuSlot.get(gridIndex);

        if (menuSlotIndex == null) {
            return;
        }

        Slot slot = getSlot(menuSlotIndex);
        ItemStack safeStack =
                stack != null ? stack.copy() : ItemStack.EMPTY;

        slot.set(safeStack);
        slot.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /**
     * Vanilla shift-click implementation over sparse GUI slots.
     *
     * 0..guiSlotCount-1 contains ONLY real FancyGUI slots, therefore
     * moveItemStackTo() cannot ever put an item into a hole in NumberRange[].
     */
    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slotIndex
    ) {
        if (!isValidSlotIndex(slotIndex)) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = getSlot(slotIndex);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack original = sourceStack.copy();

        boolean moved;

        if (slotIndex < guiSlotCount) {
            // FancyGUI -> player inventory/hotbar.
            moved = moveItemStackTo(
                    sourceStack,
                    guiSlotCount,
                    this.slots.size(),
                    true
            );
        } else {
            // Player inventory/hotbar -> ONLY real FancyGUI slots.
            moved = moveItemStackTo(
                    sourceStack,
                    0,
                    guiSlotCount,
                    false
            );
        }

        if (!moved) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return original;
    }

    private void checkGridIndex(
            int gridIndex
    ) {
        int max = rows * 9;

        if (gridIndex < 0 || gridIndex >= max) {
            throw new IndexOutOfBoundsException(
                    "GUI grid index " + gridIndex
                            + " outside range 0.." + (max - 1)
            );
        }
    }
}
