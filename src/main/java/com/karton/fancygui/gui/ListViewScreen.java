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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.server.translations.api.Localization;

import java.util.List;

public class ListViewScreen {
    private List<ItemStack> items;
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

        setSearchCallback(this::defaultSearchCallbac);
    }

    public void open() {
        gui.open();
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        setDisplayItems(items);
    }

    public void setDisplayItems(List<ItemStack> items) {gui.setDisplayItems(items);}

    public void close(boolean skipSync) {gui.close(skipSync);}

    public void setItemClickCallback(ListViewItemClickCallback callback) {gui.setItemClickCallback(callback);}

    public void setSearchCallback(Runnable callback) {gui.setSearchCallback(callback);}

    public String getSearchInput() {return  gui.getSearchInput();}


    public void setSlot(int slotIndex, Slot slot) {
        this.gui.setSlot(slotIndex, slot);
    }

    public void setButton(int buttonIndex, Button button) {
        this.gui.setButton(buttonIndex, button);
    }

    /// Currently only sgui fallback supported
    public void clearSlot(int slotIndex) {
        if (this.gui instanceof SimpleListViewScreen sgui) {
            sgui.clearSlot(slotIndex);
        }
    }

    public void setTitle(Component title) {
        gui.setTitle(title);
    }

    public void setCarried(ItemStack carried) {
        gui.setCarried(carried);
    }

    public ItemStack getCarried() {
        return gui.getCarried();
    }

    private void defaultSearchCallbac() {
        String search = gui.getSearchInput();
        if (search.isEmpty()) {
            setDisplayItems(items);
        }
        List<ItemStack> filtered = items.stream()
                .filter(
                        (ItemStack stack) -> {
                            String itemKey = stack.getItem().getDescriptionId();
                            String stackName = Localization.component(
                                    stack.getHoverName(),
                                    player
                            ).getString();

                            return stackName.toLowerCase().contains(search.toLowerCase());
                        }
                ).toList();

        setDisplayItems(filtered);
    }
}
