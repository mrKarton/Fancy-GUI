package com.karton.fancygui.client.gui;

import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ListViewClickPayload;
import com.karton.fancygui.network.ListViewContentPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenElementType;
import com.karton.fancygui.network.ScreenStatePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class ListViewScreen extends SlotScreen {


    private static final int LIST_Y = 39;


    private final List<ItemStack> items =
            new ArrayList<>();

    private long revision = -1;

    private EditBox searchBox;

    private ScreenElementData searchData;

    private boolean applyingServerValue;

    private ItemListScrollArea itemList;

    public ListViewScreen(
            Inventory inventory,
            ScreenStatePayload state
    ) {
        super(
                inventory,
                state
        );

        readSearchData(state);
    }

    @Override
    public void applyState(
            ScreenStatePayload state
    ) {
        if (
                state.screenId()
                        != getScreenId()
        ) {
            return;
        }

        super.applyState(state);

        readSearchData(state);
    }

    private void readSearchData(
            ScreenStatePayload state
    ) {
        searchData = null;

        for (
                ScreenElementData element
                : state.elements()
        ) {
            if (
                    element.type()
                            == ScreenElementType.TEXT_INPUT
            ) {
                searchData = element;
                break;
            }
        }

        /*
         * State update уже открытого screen.
         */
        if (
                searchBox != null
                        && searchData != null
        ) {
            applyingServerValue = true;

            searchBox.setValue(
                    searchData.text()
            );

            applyingServerValue = false;
        }
    }

    // ============================================================
    // LIST CONTENT PAYLOAD
    // ============================================================

    public void applyListContents(
            ListViewContentPayload payload
    ) {
        if (
                payload.screenId()
                        != getScreenId()
        ) {
            return;
        }

        /*
         * Старый packet игнорируем.
         */
        if (
                payload.revision()
                        <= revision
        ) {
            return;
        }

        items.clear();

        for (
                ItemStack stack
                : payload.items()
        ) {
            items.add(
                    stack == null
                            ? ItemStack.EMPTY
                            : stack.copy()
            );
        }

        revision =
                payload.revision();

        /*
         * Screen уже initialized.
         */
        if (itemList != null) {
            itemList.setItems(items);
        }
    }

    // ============================================================
    // INIT
    // ============================================================

    @Override
    protected void init() {
        super.init();

        createItemList();
        createSearchBox();
    }

    private void createItemList() {
        itemList =
                new ItemListScrollArea(
                        leftPos + SLOT_START_X,
                        topPos + LIST_Y,
                        font,
                        this::onListItemClicked
                );

        /*
         * Например screen был resized:
         * network payload повторно не придёт,
         * поэтому восстанавливаем текущий snapshot.
         */
        itemList.setItems(items);

        addRenderableWidget(itemList);
    }

    private void createSearchBox() {
        if (searchData == null) {
            searchBox = null;
            return;
        }

        searchBox =
                new EditBox(
                        font,

                        leftPos
                                + searchData.x(),

                        topPos
                                + searchData.y(),

                        searchData.width(),
                        searchData.height(),

                        Component.translatable("fancy-gui.simple-list-view.search-hint")
                );

        applyingServerValue = true;

        searchBox.setValue(
                searchData.text()
        );

        applyingServerValue = false;

        searchBox.setHint(
                Component.literal(
                        searchData.secondaryText()
                )
        );

        searchBox.setResponder(
                value -> {
                    if (applyingServerValue) {
                        return;
                    }

                    /*
                     * При новом поиске начинаем сверху.
                     */
                    if (itemList != null) {
                        itemList.setScrollAmount(0);
                    }

                    sendAction(
                            ElementIds.TEXT_INPUT,
                            ScreenActionType.TEXT_CHANGED,
                            value
                    );
                }
        );

        addRenderableWidget(searchBox);
    }

    // ============================================================
    // ITEM CLICK
    // ============================================================

    private void onListItemClicked(
            int index
    ) {
        /*
         * Пока content payload вообще не был получен.
         */
        if (revision < 0) {
            return;
        }

        if (
                index < 0
                        || index >= items.size()
        ) {
            return;
        }

        ClientPlayNetworking.send(
                new ListViewClickPayload(
                        getScreenId(),
                        revision,
                        index
                )
        );
    }

    // ============================================================
    // SEARCH INPUT
    // ============================================================

    @Override
    public boolean keyPressed(
            KeyEvent event
    ) {
        if (
                searchBox != null
                        && searchBox.isFocused()
        ) {
            /*
             * Escape должен закрывать весь screen,
             * а не просто обрабатываться EditBox.
             */
            if (event.isEscape()) {
                return super.keyPressed(event);
            }

            searchBox.keyPressed(event);

            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(
            CharacterEvent event
    ) {
        if (
                searchBox != null
                        && searchBox.isFocused()
        ) {
            return searchBox.charTyped(
                    event
            );
        }

        return super.charTyped(event);
    }

    @Override
    public boolean preeditUpdated(
            @Nullable PreeditEvent event
    ) {
        if (
                searchBox != null
                        && searchBox.isFocused()
        ) {
            return searchBox.preeditUpdated(
                    event
            );
        }

        return super.preeditUpdated(event);
    }

    @Override
    public boolean isInputCaptured() {
        return searchBox != null
                && searchBox.capturesInput();
    }
}