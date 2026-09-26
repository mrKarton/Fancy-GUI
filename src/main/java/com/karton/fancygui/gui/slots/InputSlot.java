package com.karton.fancygui.gui.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class InputSlot extends Slot {
    private final Predicate<ItemStack> filter;
    private final Consumer<ItemStack> onInsert;

    public InputSlot(
            Container container,
            int index,
            Predicate<ItemStack> filter,
            Consumer<ItemStack> onInsert
    ) {
        super(container, index, 0, 0);

        this.filter = filter;
        this.onInsert = onInsert;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return filter.test(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void setByPlayer(
            ItemStack newStack,
            ItemStack oldStack
    ) {
        super.setByPlayer(newStack, oldStack);

        if (newStack.isEmpty()) {
            return;
        }

        ItemStack inserted = newStack.copy();

        // Слот должен снова стать пустым
        set(ItemStack.EMPTY);

        // Выполняем твою логику
        onInsert.accept(inserted);
    }
}