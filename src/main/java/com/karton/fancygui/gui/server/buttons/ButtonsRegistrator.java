package com.karton.fancygui.gui.server.buttons;

import com.karton.fancygui.FancyGUI;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonsRegistrator {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(
                    String.format(
                            "%s/ButtonRegistrator",
                            FancyGUI.MOD_ID
                    )
            );

    public static Button NEXT_BUTTON = registerButton(
            "next_button",
            Items.DIAMOND_SWORD
    );

    public static Button PERVIOUS_BUTTON = registerButton(
            "pervious_button",
            Items.DIAMOND_PICKAXE
    );

    public static Button SEARCH_BUTTON = registerButton(
            "search_button",
            Items.SPYGLASS
    );

    public static Button CHECKED_BUTTON = registerButton(
            "checked_button",
            Items.AZALEA
    );

    public static Button CROSS_BUTTON = registerButton(
            "cross_button",
            Items.AZALEA
    );

    public static Button TRANSPARENT_BUTTON = registerButton(
            "transparent_button",
            Items.AZALEA
    );

    private static Button registerButton(String name, Item baseItem) {
        Identifier buttonId = FancyGUI.id(name);

        ResourceKey<Item> buttonKey = ResourceKey.create(
                Registries.ITEM,
                buttonId
        );

        LOGGER.info(
                String.format("Registered button %s", name)
        );

        return Registry.register(
                BuiltInRegistries.ITEM,
                buttonKey,
                new Button(
                        new Item.Properties()
                                .setId(buttonKey),
                        baseItem,
                        buttonId
                )
        );
    }

    public static void register() {
        LOGGER.info("ButtonRegistrator class pinged. All buttons should be registered at the moment");
    }
}