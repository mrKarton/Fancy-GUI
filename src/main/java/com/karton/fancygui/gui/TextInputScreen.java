package com.karton.fancygui.gui;

import com.karton.fancygui.gui.interfaces.TextInputGUI;
import com.karton.fancygui.gui.modded.TextInputScreenSession;
import com.karton.fancygui.gui.server.screens.SimpleTextInputSlotScreen;
import com.karton.fancygui.network.FancyGUINetworking;
import com.karton.fancygui.util.NumberRange;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TextInputScreen implements TextInputGUI {

    private final TextInputGUI gui;

    public TextInputScreen(
            ServerPlayer player,
            String title
    ) {
        this (
                player,
                Component.literal(title != null ? title : "")
        );
    }

    public TextInputScreen(
            ServerPlayer player,
            Component title
    ) {
        if (FancyGUINetworking.supportsClientGui(player)) {
            this.gui = new TextInputScreenSession(
                    player,
                    title
            );
            return;
        }

        this.gui = new SimpleTextInputSlotScreen(
                player,
                title
        );
    }

    public boolean open() {
        return gui.open();
    }

    @Override
    public void close(boolean skipSync) {
        gui.close(skipSync);
    }

    public void close() {
        gui.close(true);
    }

    public int getRows() {
        return gui.getRows();
    }

    public void setTitle(String title) {
        gui.setTitle(title);
    }

    public void setTitle(Component title) {
        gui.setTitle(title);
    }

    public void submit() { gui.submit(); }

    public void cancel() { gui.cancel(); }

    public void setHint(String hint) { gui.setHint(hint); }

    public void setHint(Component hint) {gui.setHint(hint);}

    public void setButton(
            int slotIndex,
            Button button
    ) {
        gui.setButton(slotIndex, button);
    }

    public void setButton(
            NumberRange slots,
            Button button
    ) {
        gui.setButton(slots, button);
    }

    public void setSlot(
            int slotIndex,
            Slot slot
    ) {
        gui.setSlot(slotIndex, slot);
    }

    @Override
    public ItemStack getCarried() {
        return gui.getCarried();
    }

    @Override
    public void setCarried(ItemStack stack) {
        gui.setCarried(stack);
    }

    public void setOnInputCallback(Runnable callback) {
        gui.setOnInputCallback(callback);
    }

    public String getInput() {
        return gui.getInput();
    }

    public void setSubmitCallback(Runnable callback) {
        gui.setSubmitCallback(callback);
    }

    public void setCancelCallback(Runnable callback) {
        gui.setCancelCallback(callback);
    }

    public void setOnCloseCallback(Runnable callback) {
        gui.setOnCloseCallback(callback);
    }
}
