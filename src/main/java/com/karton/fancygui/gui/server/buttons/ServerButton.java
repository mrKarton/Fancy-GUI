package com.karton.fancygui.gui.server.buttons;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElement;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

public class ServerButton implements GuiElement {

    private Runnable clickAction = () -> {};

    private final ButtonItem item;
    private final ItemStack stack;

    public ServerButton(ButtonItem item) {
        this.item = item;
        this.stack = new ItemStack(item);
    }

    public void setTitle(Component title) {
        if (title == null || title.equals(Component.empty())) {
            return;
        }

        stack.set(
                DataComponents.CUSTOM_NAME,
                title.copy()
                        .withStyle(SguiUtils.STYLE_CLEARER)
        );
    }

    public void setCaption(Component caption) {
        if (caption == null || caption.equals(Component.empty())) {
            return;
        }

        stack.set(
                DataComponents.LORE,
                new ItemLore(
                        List.of(
                                caption.copy()
                                        .withStyle(SguiUtils.STYLE_CLEARER)
                        )
                )
        );
    }

    public void setAction(Runnable action) {
        this.clickAction = action != null
                ? action
                : () -> {};
    }

    public Runnable getAction() {
        return clickAction;
    }

    public ButtonItem getButtonItem() {
        return item;
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public ClickCallback getGuiCallback() {
        return GuiElement.callback(clickAction);
    }
}