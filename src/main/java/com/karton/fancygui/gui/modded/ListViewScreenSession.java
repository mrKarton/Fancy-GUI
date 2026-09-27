package com.karton.fancygui.gui.modded;

import com.karton.fancygui.gui.interfaces.ListViewGUI;
import com.karton.fancygui.gui.interfaces.ListViewItemClickCallback;
import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ListViewClickPayload;
import com.karton.fancygui.network.ListViewContentPayload;
import com.karton.fancygui.network.ScreenActionPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenType;
import com.karton.fancygui.util.NumberRange;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ListViewScreenSession extends SlotScreenSession implements ListViewGUI {
    private List<ItemStack> displayItems = List.of();
    private long revision;
    private String searchInput = "";
    private Runnable searchCallback;
    private ListViewItemClickCallback itemClickCallback;

    public ListViewScreenSession(ServerPlayer player, String title, List<ItemStack> items) {
        super(player, ScreenType.LIST_VIEW, title, 6,
                new NumberRange[]{new NumberRange(45, 53)});
        setDisplayItems(items);
    }

    @Override
    public void setDisplayItems(List<ItemStack> items) {
        if (items != null && items.size() > ListViewContentPayload.MAX_ITEMS) {
            throw new IllegalArgumentException("ListView supports at most "
                    + ListViewContentPayload.MAX_ITEMS + " items");
        }
        List<ItemStack> snapshot = new ArrayList<>();
        if (items != null) {
            for (ItemStack item : items) {
                snapshot.add(item == null ? ItemStack.EMPTY : item.copy());
            }
        }
        displayItems = List.copyOf(snapshot);
        revision++;
        if (isOpened()) {
            sendListContents();
        }
    }

    @Override
    public boolean open() {
        boolean opened = super.open();
        if (opened) sendListContents();
        return opened;
    }

    private void sendListContents() {
        ServerPlayNetworking.send(getPlayer(),
                new ListViewContentPayload(getScreenId(), revision, displayItems));
    }

    @Override
    protected List<ScreenElementData> buildElements() {
        List<ScreenElementData> elements = super.buildElements();
        elements.add(ScreenElementData.textInput(8, 18, 142, 18, searchInput, "Поиск"));
        return elements;
    }

    @Override
    protected void handleScreenAction(ScreenActionPayload payload) {
        if (payload.elementId() == ElementIds.TEXT_INPUT
                && payload.action() == ScreenActionType.TEXT_CHANGED) {
            String value = payload.value() == null ? "" : payload.value();
            if (value.length() > 128) value = value.substring(0, 128);
            searchInput = value;
            if (searchCallback != null) searchCallback.run();
            return;
        }
        super.handleScreenAction(payload);
    }

    public void handleListClick(ListViewClickPayload payload) {
        if (!isOpened() || payload.screenId() != getScreenId()
                || payload.revision() != revision
                || payload.index() < 0 || payload.index() >= displayItems.size()) return;
        if (itemClickCallback != null) itemClickCallback.onClick(payload.index());
    }

    @Override
    public void setItemClickCallback(ListViewItemClickCallback callback) {
        itemClickCallback = callback;
    }

    @Override
    public void setSearchCallback(Runnable callback) {
        searchCallback = callback;
    }

    @Override
    public String getSearchInput() {
        return searchInput;
    }
}
