package com.karton.fancygui.gui.modded;

import com.karton.fancygui.gui.Button;
import com.karton.fancygui.gui.interfaces.SlotScreenGUI;
import com.karton.fancygui.gui.menu.FancyMenu;
import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ScreenActionPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenType;
import com.karton.fancygui.util.NumberRange;
import com.karton.fancygui.util.NumberRangeUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server-side native SlotScreen session.
 *
 * Реальную container interaction механику выполняет vanilla FancyMenu
 * (AbstractContainerMenu), а FancyGUI networking только транспортирует
 * ContainerInput между клиентским и серверным mirror menu.
 */
public class SlotScreenSession
        extends FancyScreenSession
        implements SlotScreenGUI {

    private final int rows;

    /**
     * Единственные существующие FancyGUI grid slots.
     * Если index отсутствует здесь — Slot объекта для него не создаётся.
     */
    private final Set<Integer> declaredSlots =
            new HashSet<>();

    private final SimpleContainer defaultContainer;

    /** gridIndex -> реальный server-side Slot. */
    private final Map<Integer, Slot> slotContents =
            new HashMap<>();

    /** Buttons не являются Slot. */
    private final Map<Integer, Button> buttons =
            new HashMap<>();

    private FancyMenu serverMenu;

    public SlotScreenSession(
            ServerPlayer player,
            String title,
            int rows,
            NumberRange[] neededSlots
    ) {
        this(
                player,
                ScreenType.SLOT,
                title,
                rows,
                neededSlots
        );
    }

    protected SlotScreenSession(
            ServerPlayer player,
            ScreenType screenType,
            String title,
            int rows,
            NumberRange[] neededSlots
    ) {
        super(
                player,
                screenType,
                Component.literal(
                        title != null ? title : ""
                )
        );

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "Rows must be between 1 and 6"
            );
        }

        this.rows = rows;
        this.defaultContainer =
                new SimpleContainer(rows * 9);

        if (neededSlots != null) {
            for (
                    int gridIndex :
                    NumberRangeUtil.getFulRange(neededSlots)
            ) {
                checkGridIndex(gridIndex);

                if (!declaredSlots.add(gridIndex)) {
                    continue;
                }

                slotContents.put(
                        gridIndex,
                        new Slot(
                                defaultContainer,
                                gridIndex,
                                0,
                                0
                        )
                );
            }
        }
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    protected int getStateRows() {
        return rows;
    }

    protected int getSlotStartY() {
        return 18;
    }

    @Override
    public boolean open() {
        rebuildServerMenu();
        return super.open();
    }

    @Override
    public void setButton(
            int slotIndex,
            Button button
    ) {
        checkGridIndex(slotIndex);

        if (button == null) {
            throw new IllegalArgumentException(
                    "Button cannot be null"
            );
        }

        /* Button != Slot. */
        buttons.put(slotIndex, button);
        syncIfOpened();
    }

    @Override
    public void setSlot(
            int slotIndex,
            Slot slot
    ) {

        checkDeclaredSlot(slotIndex);

        if (slot == null) {
            throw new IllegalArgumentException(
                    "Slot cannot be null"
            );
        }

        Slot previous =
                slotContents.put(slotIndex, slot);

        if (
                previous != null
                        && !previous.getItem().isEmpty()
                        && slot.getItem().isEmpty()
        ) {
            slot.set(
                    previous.getItem().copy()
            );
        }

        if (isOpened()) {
            rebuildServerMenu();
            sync();
        }
    }

    @Override
    protected List<ScreenElementData> buildElements() {
        List<ScreenElementData> result =
                new ArrayList<>();

        declaredSlots
                .stream()
                .sorted()
                .forEach(gridIndex -> {
                    Slot slot = slotContents.get(gridIndex);

                    ItemStack stack =
                            slot != null
                                    ? slot.getItem().copy()
                                    : ItemStack.EMPTY;

                    result.add(
                            ScreenElementData.slot(
                                    gridIndex,
                                    stack
                            )
                    );
                });

        buttons
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int gridIndex = entry.getKey();
                    Button button = entry.getValue();

                    result.add(
                            ScreenElementData.button(
                                    gridIndex,
                                    Math.max(
                                            1,
                                            button.getClientWidth()
                                    ),
                                    Math.max(
                                            1,
                                            button.getClientHeight()
                                    ),
                                    button.getTitle().getString(),
                                    button.getCaption().getString(),
                                    new ItemStack(
                                            button.getItem()
                                    )
                            )
                    );
                });

        if (
                serverMenu != null
                        && !serverMenu.getCarried().isEmpty()
        ) {
            result.add(
                    ScreenElementData.carriedStack(
                            serverMenu
                                    .getCarried()
                                    .copy()
                    )
            );
        }

        return result;
    }

    @Override
    protected void handleScreenAction(
            ScreenActionPayload payload
    ) {
        if (payload.action() == ScreenActionType.BUTTON_CLICK) {
            handleButtonClick(payload);
            return;
        }

        if (payload.action() == ScreenActionType.SLOT_CLICK) {
            handleContainerClick(payload);
        }
    }

    private void handleButtonClick(
            ScreenActionPayload payload
    ) {
        int gridIndex =
                ElementIds.buttonGridIndex(
                        payload.elementId()
                );

        if (
                ElementIds.button(gridIndex)
                        != payload.elementId()
        ) {
            return;
        }

        Button button =
                buttons.get(gridIndex);

        if (button != null) {
            button.execute();
        }
    }

    private void handleContainerClick(
            ScreenActionPayload payload
    ) {
        if (serverMenu == null) {
            return;
        }

        ContainerClickData click =
                parseContainerClick(payload.value());

        if (click == null) {
            return;
        }

        int menuSlotId = payload.elementId();

        if (
                menuSlotId
                        != AbstractContainerMenu.SLOT_CLICKED_OUTSIDE
                        && !serverMenu.isValidSlotIndex(menuSlotId)
        ) {
            return;
        }

        /*
         * Сервер выполняет ровно тот же vanilla AbstractContainerMenu алгоритм,
         * что client-side prediction.
         */
        serverMenu.clicked(
                menuSlotId,
                click.buttonNum(),
                click.input(),
                getPlayer()
        );

        syncAfterContainerAction();
    }

    private ContainerClickData parseContainerClick(
            String value
    ) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] parts = value.split(":", 2);

        if (parts.length != 2) {
            return null;
        }

        try {
            int buttonNum =
                    Integer.parseInt(parts[0]);

            ContainerInput input =
                    ContainerInput.valueOf(parts[1]);

            return new ContainerClickData(
                    buttonNum,
                    input
            );
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void syncAfterContainerAction() {
        getPlayer().getInventory().setChanged();

        /*
         * Player inventory продолжает синхронизироваться vanilla packet'ами.
         * Sparse GUI slots + carried stack синхронизирует FancyGUI state packet.
         */
        getPlayer().inventoryMenu.broadcastChanges();
        sync();
    }

    /**
     * Пересобирается только когда API заменяет Slot implementation после open().
     * Обычные клики menu не пересоздают.
     */
    private void rebuildServerMenu() {
        ItemStack carried =
                serverMenu != null
                        ? serverMenu.getCarried().copy()
                        : ItemStack.EMPTY;

        serverMenu = FancyMenu.createServer(
                getScreenId(),
                getPlayer().getInventory(),
                rows,
                getSlotStartY(),
                slotContents
        );

        serverMenu.setCarried(carried);
    }

    @Override
    public void setCarried(ItemStack stack) {
        if (serverMenu == null) {
            return;
        }

        serverMenu.setCarried(
                stack != null
                        ? stack.copy()
                        : ItemStack.EMPTY
        );

        sync();
    }

    @Override
    public ItemStack getCarried() {
        if (serverMenu == null) {
            return ItemStack.EMPTY;
        }

        return serverMenu.getCarried().copy();
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    public void close(boolean skipSync) {
        cleanupServerMenu();
        super.close(skipSync);
    }

    @Override
    protected void onClientClosed() {
        cleanupServerMenu();
    }

    @Override
    protected void onReplaced() {
        cleanupServerMenu();
    }

    private void cleanupServerMenu() {
        if (serverMenu == null) {
            return;
        }

        /*
         * AbstractContainerMenu#removed() vanilla-образом возвращает carried
         * stack игроку / обрабатывает закрытие menu.
         */
        serverMenu.removed(getPlayer());
        serverMenu = null;

        getPlayer().getInventory().setChanged();
        getPlayer().inventoryMenu.broadcastChanges();
    }

    // ============================================================

    protected final void checkGridIndex(
            int slotIndex
    ) {
        int max = rows * 9;

        if (slotIndex < 0 || slotIndex >= max) {
            throw new IndexOutOfBoundsException(
                    "GUI index " + slotIndex
                            + " outside range 0.." + (max - 1)
            );
        }
    }

    protected final void checkDeclaredSlot(
            int slotIndex
    ) {
        checkGridIndex(slotIndex);

        if (!declaredSlots.contains(slotIndex)) {
            throw new IllegalArgumentException(
                    "Slot " + slotIndex
                            + " was not declared in NumberRange[]"
            );
        }
    }

    private record ContainerClickData(
            int buttonNum,
            ContainerInput input
    ) {
    }
}
