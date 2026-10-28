package com.karton.fancygui.gui;

import com.karton.fancygui.gui.interfaces.SlotScreenGUI;
import com.karton.fancygui.gui.modded.SlotScreenSession;
import com.karton.fancygui.gui.server.screens.SimpleListViewScreen;
import com.karton.fancygui.gui.server.screens.SimpleSlotScreen;
import com.karton.fancygui.network.FancyGUINetworking;
import com.karton.fancygui.util.NumberRange;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SlotScreen implements SlotScreenGUI {

    private final SlotScreenGUI gui;

    public SlotScreen(
            ServerPlayer player,
            Component title,
            int verticalSize,
            NumberRange[] neededSlots
    ) {
        if (FancyGUINetworking.supportsClientGui(player)) {
            this.gui = new SlotScreenSession(
                    player,
                    title,
                    verticalSize,
                    neededSlots
            );
            return;
        }

        this.gui = new SimpleSlotScreen(
                player,
                title,
                verticalSize,
                neededSlots
        );
    }

    public SlotScreen(
            ServerPlayer player,
            String title,
            int verticalSize,
            NumberRange[] neededSlots
    ) {
        this (
                player,
                Component.literal(title != null ? title : ""),
                verticalSize,
                neededSlots
        );
    }

    public SlotScreen(
            ServerPlayer player,
            Component title,
            int verticalSize
    ) {
        this(
                player,
                title,
                verticalSize,
                new NumberRange[0]
        );
    }

    public SlotScreen(
            ServerPlayer player,
            String title,
            int verticalSize
    ) {
        this(
                player,
                Component.literal(title != null ? title : ""),
                verticalSize
        );
    }

    public int getRows() {
        return gui.getRows();
    }

    public boolean open() {
        return gui.open();
    }

    @Override
    public void close(boolean skipSync) {
        gui.close(skipSync);
    }

    public void setButton(
            NumberRange slotsRange,
            Button button
    ) {
        gui.setButton(slotsRange, button);
    }

    public void setButton(
            int slot,
            Button button
    ) {
        gui.setButton(slot, button);
    }

    public void setTitle(String title) {
        gui.setTitle(title);
    }

    public void setTitle(Component title) {
        gui.setTitle(title);
    }

    public void close() {
        gui.close(true);
    }

    public void setSlot(
            int slotIndex,
            Slot slot
    ) {
        gui.setSlot(slotIndex, slot);
    }

    public ItemStack getCarried() {return gui.getCarried();}

    public void setCarried(ItemStack stack) {gui.setCarried(stack);}

    public void setOnCloseCallback(Runnable callback) {
        gui.setOnCloseCallback(callback);
    }
}
