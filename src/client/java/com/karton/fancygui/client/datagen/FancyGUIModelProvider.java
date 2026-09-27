package com.karton.fancygui.client.datagen;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.resources.Identifier;

public class FancyGUIModelProvider extends FabricModelProvider {

    public FancyGUIModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(
            BlockModelGenerators generator
    ) {
        // FancyGUI not creating blocks at the moment
    }

    @Override
    public void generateItemModels(
            ItemModelGenerators generator
    ) {
        ButtonModelGenerator.generate(
                ButtonsRegistrator.NEXT_BUTTON,
                FancyGUI.id("item/page_forward"),
                generator
        );

        ButtonModelGenerator.generate(
                ButtonsRegistrator.PERVIUS_BUTTON,
                FancyGUI.id("item/page_backward"),
                generator
        );

        ButtonModelGenerator.generate(
                ButtonsRegistrator.SEARCH_BUTTON,
                Identifier.withDefaultNamespace("item/spyglass"),
                generator
        );

        ButtonModelGenerator.generate(
                ButtonsRegistrator.CHECKED_BUTTON,
                FancyGUI.id("item/checkmark"),
                generator
        );

        ButtonModelGenerator.generate(
                ButtonsRegistrator.CROSS_BUTTON,
                FancyGUI.id("item/cross"),
                generator
        );

        ButtonModelGenerator.generate(
                ButtonsRegistrator.INFO_BUTTON,
                FancyGUI.id("item/link"),
                generator
        );
    }

    @Override
    public String getName() {
        return "FancyGUI Button Models";
    }
}