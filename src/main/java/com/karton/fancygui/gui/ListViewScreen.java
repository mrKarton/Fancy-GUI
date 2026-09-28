package com.karton.fancygui.gui;

import com.karton.fancygui.gui.interfaces.ListViewGUI;
import com.karton.fancygui.gui.interfaces.ListViewItemClickCallback;
import com.karton.fancygui.gui.modded.ListViewScreenSession;
import com.karton.fancygui.gui.modded.SlotScreenSession;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.gui.server.screens.SimpleListViewScreen;
import com.karton.fancygui.network.FancyGUINetworking;
import com.karton.fancygui.util.NumberRange;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ListViewScreen {
    private final List<ItemStack> items;
    private List<ItemStack> displayItems;
    private final ServerPlayer player;
    private final String title;

    private final ListViewGUI gui;

    public ListViewScreen(
            ServerPlayer player,
            String title,
            List<ItemStack> itemStackList,
            NumberRange[] neededSlots
            ) {
        this.items = itemStackList;
        this.displayItems = itemStackList;
        this.title = title + this.items.size();
        this.player = player;

        if (FancyGUINetworking.supportsClientGui(player)) {
            this.gui = new ListViewScreenSession(
                    player,
                    title,
                    displayItems,
                    neededSlots
            );
            return;
        }

        gui = new SimpleListViewScreen(
                player,
                title,
                neededSlots
        );

        gui.setDisplayItems(items);
    }

    public void open() {
        gui.open();
    }

    public void setDisplayItems(List<ItemStack> items) {gui.setDisplayItems(items);}

    public void close(boolean skipSync) {gui.close(skipSync);}

    public void setItemClickCallback(ListViewItemClickCallback callback) {gui.setItemClickCallback(callback);}

    public void setSearchCallback(Runnable callback) {gui.setSearchCallback(callback);}

    public String getSearchInput() {return  gui.getSearchInput();}
}
