package com.karton.fancygui.gui;

import com.karton.fancygui.gui.server.buttons.ButtonItem;
import com.karton.fancygui.gui.server.buttons.ServerButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;

public class Button {

    private final Item item;

    private Component title = Component.empty();
    private ItemLore caption = ItemLore.EMPTY;

    /**
     * Выполняется ТОЛЬКО на сервере.
     */
    private Runnable action = () -> {};

    /*
     * Размер настоящего client-side widget.
     *
     * По умолчанию кнопка занимает размер одного слота.
     */
    private int clientWidth = 18;
    private int clientHeight = 18;

    public Button(Item item) {
        this.item = item;
    }

    public Button(Item item, String title) {
        this(item);
        this.title = Component.literal(title);
    }

    public Button withTitle(String title) {
        return withTitle(Component.literal(title));
    }

    public Button withTitle(Component title) {
        this.title = title;
        return this;
    }

    public Button withCaption(String caption) {
        return withCaption(Component.literal(caption));
    }

    public Button withCaption(Component caption) {
        this.caption = this.caption.withLineAdded(caption);
        return this;
    }

    public Button withAllCaption(ItemLore lore) {
        this.caption = lore;
        return this;
    }

    public Button withAction(Runnable action) {
        this.action = action != null
                ? action
                : () -> {};

        return this;
    }

    public Button withClientSize(int width, int height) {
        this.clientWidth = width;
        this.clientHeight = height;
        return this;
    }

    public Item getItem() {
        return item;
    }

    public Component getTitle() {
        return title;
    }

    public ItemLore getCaption() {
        return caption;
    }

    public int getClientWidth() {
        return clientWidth;
    }

    public int getClientHeight() {
        return clientHeight;
    }

    /**
     * Только сервер.
     */
    public void execute() {
        action.run();
    }

    /**
     * Создаёт SGUI-представление кнопки.
     *
     * ServerButton больше НЕ является основной моделью кнопки.
     */
    public ServerButton createServerFallback() {
        ServerButton fallback = new ServerButton(item);

        fallback.setTitle(this.title);
        fallback.setCaption(caption);
        fallback.setAction(this::execute);

        return fallback;
    }
}