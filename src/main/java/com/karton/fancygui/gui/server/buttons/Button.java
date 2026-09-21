package com.karton.fancygui.gui.server.buttons;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Button extends Item implements PolymerItem {
    private final Identifier MODEL;
    private final Item baseItem;

    public Button(
            Properties properties,
            Item baseItem,
            Identifier model
    ) {
        super(properties);

        this.baseItem = baseItem;
        this.MODEL = model;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.baseItem;
    }

    @Override
    public Identifier getPolymerItemModel(
            ItemStack stack,
            PacketContext context,
            HolderLookup.Provider lookup
    ) {
        return this.MODEL;
    }
}
