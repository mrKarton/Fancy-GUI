package com.karton.fancygui.gui.server.buttons;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ButtonItem extends Item implements PolymerItem {

    private final Identifier model;
    private final Item fallbackItem;

    public ButtonItem(
            Properties properties,
            Item fallbackItem,
            Identifier model
    ) {
        super(properties);

        this.fallbackItem = fallbackItem;
        this.model = model;
    }

    public Identifier getModel() {
        return model;
    }

    public Item getFallbackItem() {
        return fallbackItem;
    }

    @Override
    public Item getPolymerItem(
            ItemStack stack,
            PacketContext context
    ) {
        return fallbackItem;
    }

    @Override
    public Identifier getPolymerItemModel(
            ItemStack stack,
            PacketContext context,
            HolderLookup.Provider lookup
    ) {
        return model;
    }
}