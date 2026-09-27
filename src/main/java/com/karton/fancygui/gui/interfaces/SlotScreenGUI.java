package com.karton.fancygui.gui.interfaces;

import com.karton.fancygui.gui.Button;
import com.karton.fancygui.util.NumberRange;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface SlotScreenGUI {

    int getRows();

    void setTitle(String text);

    void setTitle(Component component);

    boolean open();

    void close(boolean skipSync);

    default void setButton(
            NumberRange slotsToSet,
            Button button
    ) {
        for (
                int buttonIndex :
                slotsToSet.getRangeArray()
        ) {
            setButton(
                    buttonIndex,
                    button
            );
        }
    }

    void setButton(
            int slotIndex,
            Button button
    );

    void setSlot(
            int slotIndex,
            Slot slot
    );

    default void setSlot(
            NumberRange[] neededSlots,
            Slot slot
    ) {
        for (NumberRange range : neededSlots) {
            for (
                    int index :
                    range.getRangeArray()
            ) {
                setSlot(
                        index,
                        slot
                );
            }
        }
    }

    ItemStack getCarried();

    void setCarried(ItemStack stack);
}