package com.karton.fancygui.gui;

import com.karton.fancygui.gui.server.buttons.ButtonItem;
import com.karton.fancygui.gui.server.buttons.ServerButton;
import net.minecraft.network.chat.Component;

public class Button {

    private final ButtonItem item;

    private Component title = Component.empty();
    private Component caption = Component.empty();

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

    public Button(ButtonItem item) {
        this.item = item;
    }

    public Button(ButtonItem item, String title) {
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
        this.caption = caption;
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

    public ButtonItem getItem() {
        return item;
    }

    public Component getTitle() {
        return title;
    }

    public Component getCaption() {
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