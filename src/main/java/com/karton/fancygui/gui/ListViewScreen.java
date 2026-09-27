package com.karton.fancygui.gui;

import com.karton.fancygui.gui.interfaces.ListViewGUI;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.gui.server.screens.SimpleListViewScreen;
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

    private int page;
    private final int MAX_PAGES;

    public ListViewScreen(
            ServerPlayer player,
            String title,
            List<ItemStack> itemStackList
            ) {
        this.items = itemStackList;
        this.displayItems = itemStackList;
        this.title = title + this.items.size();
        this.player = player;

        MAX_PAGES = Math.ceilDiv(this.items.size(), 36) - 1;

        gui = new SimpleListViewScreen(
                player,
                title
        );

        gui.setButton(
                45,
                new Button(ButtonsRegistrator.PERVIUS_BUTTON)
                        .withTitle("Предыдущая страница")
                        .withAction(this::perviousPage)
        );

        gui.setButton(
                53,
                new Button(ButtonsRegistrator.NEXT_BUTTON)
                        .withTitle("Следующая страница")
                        .withAction(this::nextPage)
        );

        gui.setItemClickCallback(
                (int i) -> {
                    ItemStack stack = getItemsOnPage(page).get(i);
                    player.sendSystemMessage(Component.literal("Вы выбрали " + stack.getItemName()));
                }
        );

        gui.setDisplayItems(getItemsOnPage(0));
    }

    private void nextPage() {
        if (page == MAX_PAGES) {
            return;
        }
        this.page += 1;
        gui.setDisplayItems(getItemsOnPage(this.page));
    }

    private void perviousPage() {
        if (page == 0) {
            return;
        }
        this.page -= 1;
        gui.setDisplayItems(getItemsOnPage(this.page));
    }

    private List<ItemStack> getItemsOnPage(int page) {
        int from = Math.max(0, page * 36);
        int to = Math.min((page * 36) + 36, this.displayItems.size() - 1);
        return this.displayItems.subList(
                from,
                to
        );
    }

    public void open() {
        gui.open();
    }
}
