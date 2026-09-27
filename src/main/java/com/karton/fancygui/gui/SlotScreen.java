package com.karton.fancygui.gui;

import com.karton.fancygui.gui.interfaces.SlotScreenGUI;
import com.karton.fancygui.gui.modded.SlotScreenSession;
import com.karton.fancygui.network.FancyGUINetworking;
import com.karton.fancygui.util.NumberRange;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotScreen {

    private final SlotScreenGUI gui;

    public SlotScreen(
            ServerPlayer player,
            String title,
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

        this.gui = new com.karton.fancygui.gui.server.screens.SlotScreen(
                player,
                title,
                verticalSize,
                neededSlots
        );
    }

    public int getRows() {
        return gui.getRows();
    }

    public void open() {
        gui.open();
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
}
