package com.karton.fancygui.tests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomItems {

    private static final Random RANDOM = new Random();

    public static List<ItemStack> generateRandomItems(int minItems, int maxItems) {
        List<ItemStack> result = new ArrayList<>();

        List<Item> items = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.getDefaultInstance().getMaxStackSize() > 0)
                .toList();

        int amount = RANDOM.nextInt(maxItems - minItems + 1) + minItems;

        for (int i = 0; i < amount; i++) {
            Item item = items.get(RANDOM.nextInt(items.size()));

            int maxStackSize = item.getDefaultInstance().getMaxStackSize();
            int count = RANDOM.nextInt(maxStackSize) + 1;

            result.add(new ItemStack(item, count));
        }

        return result;
    }
}