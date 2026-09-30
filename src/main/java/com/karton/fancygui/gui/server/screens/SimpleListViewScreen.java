package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.gui.Button;
import com.karton.fancygui.gui.TextInputScreen;
import com.karton.fancygui.gui.interfaces.ListViewGUI;
import com.karton.fancygui.gui.interfaces.ListViewItemClickCallback;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.util.NumberRange;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SimpleListViewScreen extends SimpleSlotScreen implements ListViewGUI {
    public final int ROWS = 6;
    private final NumberRange ITEMS_RANGE = new NumberRange(9, 44);
    private List<ItemStack> items;
    private List<ItemStack> displayItems;
    private ListViewItemClickCallback itemClickCallback;
    private String search = "";
    private Runnable searchCallback;
    private int page = 0;
    private int maxPages = 0;

    public SimpleListViewScreen(
            ServerPlayer player,
            String title,
            NumberRange[] neededSlots
    ) {
        NumberRange busySlots = new NumberRange(0, 44);
        for (NumberRange range : neededSlots) {
            if (range.intersects(busySlots)) {
                throw new IllegalArgumentException("Needed slots are intersects binded slots");
            }
        }
        super(
                player,
                title,
                6,
                ArrayUtils.addAll(
                        new NumberRange[] {
                                new NumberRange(9, 44),
                        },
                        neededSlots
                )
        );

        super.setButton(
                45,
                new Button(ButtonsRegistrator.PERVIUS_BUTTON)
                        .withTitle(Component.translatable("fancy-gui.simple-list-view.pervious-page"))
                        .withAction(this::perviousPage)
        );

        super.setButton(
                53,
                new Button(ButtonsRegistrator.NEXT_BUTTON)
                        .withTitle(Component.translatable("fancy-gui.simple-list-view.next-page"))
                        .withAction(this::nextPage)
        );

        super.setButton(
                8,
                new Button(ButtonsRegistrator.SEARCH_BUTTON)
                        .withTitle(Component.translatable("fancy-gui.simple-list-view.search"))
                        .withAction(this::openSearchScreen)

        );
    }

    @Override
    public void setDisplayItems(List<ItemStack> items) {
        this.items = items;

        maxPages = Math.ceilDiv(this.items.size(), 36) - 1;
        page = 0;

        this.displayItems = getItemsOnPage(0);

        showItems();
    }

    private void showItems() {
        for (int i = 0; i < 36; i++) {
            int slotIndex = 9 + i;

            if (i >= displayItems.size()) {
                super.clearSlot(slotIndex);
                continue;
            }

            ItemStack stack = displayItems.get(i);

            int itemIndex = 36 * page + i;

            super.setSlot(
                    slotIndex,
                    new GuiElementBuilder(stack)
                            .setCallback(() -> onClick(itemIndex))
            );
        }
    }

    private void onClick(int i) {
        if (itemClickCallback != null) {
            itemClickCallback.onClick(i);
        }
    }

    private void nextPage() {
        if (page == maxPages) {
            return;
        }
        this.page += 1;
        this.displayItems = getItemsOnPage(this.page);
        showItems();
    }

    private void perviousPage() {
        if (page == 0) {
            return;
        }
        this.page -= 1;
        this.displayItems = getItemsOnPage(this.page);
        showItems();
    }

    private List<ItemStack> getItemsOnPage(int page) {
        int from = page * 36;

        if (from >= this.items.size()) {
            return List.of();
        }

        int to = Math.min(
                this.items.size(),
                from + 36
        );

        return this.items.subList(from, to);
    }

    private void openSearchScreen() {
        TextInputScreen screen = new TextInputScreen(
                player,
                Component.translatable("fancy-gui.simple-list-view.search").getString()
        );

        screen.setButton(
                2,
                new Button(ButtonsRegistrator.SEARCH_BUTTON)
                        .withTitle(Component.translatable("fancy-gui.simple-list-view.search"))
                        .withAction(
                                () -> {
                                    this.search = screen.getInput();
                                    if (searchCallback != null) {
                                        searchCallback.run();
                                    }
                                    screen.close();
                                    this.open();
                                }
                        )
        );

        screen.setHint(Component.translatable("fancy-gui.simple-list-view.search-hint").getString());

        screen.open();
    }

    @Override
    public void setItemClickCallback(ListViewItemClickCallback callback) {
        this.itemClickCallback = callback;
    }

    @Override
    public void setSearchCallback(Runnable callback) {
        this.searchCallback = callback;
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
