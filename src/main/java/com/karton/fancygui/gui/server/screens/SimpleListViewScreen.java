package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.gui.Button;
import com.karton.fancygui.gui.TextInputScreen;
import com.karton.fancygui.gui.interfaces.ListViewGUI;
import com.karton.fancygui.gui.interfaces.ListViewItemClickCallback;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.util.NumberRange;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SimpleListViewScreen extends SimpleSlotScreen implements ListViewGUI {
    public final int ROWS = 6;
    private final NumberRange ITEMS_RANGE = new NumberRange(9, 44);
    private List<ItemStack> displayItems;
    private ListViewItemClickCallback itemClickCallback;
    private String search = "";
    private Runnable searchCallback;

    public SimpleListViewScreen(ServerPlayer player, String title) {
        super(
                player,
                title,
                6,
                new NumberRange[] {
                        new NumberRange(9, 44)
                }
        );
    }

    @Override
    public void setDisplayItems(List<ItemStack> items) {
        if (items.size() > 36) {
            throw new IllegalArgumentException("Display items cont can't be more than 36");
        }
        this.displayItems = items;

        showItems();
    }

    private void showItems() {
        for (int i : new NumberRange(0, 35).getRangeArray()) {
            int slotIndex = 9+i;
            if (this.displayItems.size() - 1 < i) {
                super.clearSlot(slotIndex);
                continue;
            }
            ItemStack stack = this.displayItems.get(i);

            super.setSlot(
                    slotIndex,
                    new GuiElementBuilder()
                            .setItem(stack.getItem())
                            .setCount(stack.getCount())
                            .setCallback(
                                    () -> {
                                        itemClickCallback.onClick(i);
                                    }
                            )
            );
        }
    }

    @Override
    public void setItemClickCallback(ListViewItemClickCallback callback) {
        this.itemClickCallback = callback;
    }

    @Override
    public void setSearchCallback(Runnable callback) {
        searchCallback.run();
    }

    @Override
    public String getSearchInput() {
        return search;
    }

    @Override
    public int getRows() {
        return ROWS;
    }

    @Override
    public void setTitle(String text) {
        super.setTitle(text);
    }

    @Override
    public void setTitle(Component component) {
        super.setTitle(component);
    }

    @Override
    public boolean open() {
        return super.open();
    }

    @Override
    public void close(boolean skipSync) {
        super.close(skipSync);
    }

    @Override
    public void setButton(int slotIndex, Button button) {
        if (!this.neededSlots.contains(slotIndex)) {
            super.setButton(slotIndex, button);
        } else {
            throw new IllegalArgumentException("Slot index shouldn't overlap item slots range (9-44)");
        }
    }

    @Override
    public void setSlot(int slotIndex, Slot slot) {
        if (!this.neededSlots.contains(slotIndex)) {
            super.setSlot(slotIndex, slot);
        } else {
            throw new IllegalArgumentException("Slot index shouldn't overlap item slots range (9-44)");
        }
    }

    @Override
    public ItemStack getCarried() {
        return super.getCarried();
    }

    @Override
    public void setCarried(ItemStack stack) {
        super.setCarried(stack);
    }
}
