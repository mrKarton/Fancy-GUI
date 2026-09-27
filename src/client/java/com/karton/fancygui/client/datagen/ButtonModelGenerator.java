package com.karton.fancygui.client.datagen;

import com.karton.fancygui.FancyGUI;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Optional;

public final class ButtonModelGenerator {
    private ButtonModelGenerator() {
    }

    /// Button model parent
    private static final Identifier BUTTON_PARENT =
            FancyGUI.id("gui/gui_button_base");


    /// Button background texture
    private static final Identifier BASE_BUTTON_TEXTURE =
            FancyGUI.id("item/button_base");


    /// Button model
    private static final ModelTemplate BUTTON_MODEL =
            new ModelTemplate(
                    Optional.of(BUTTON_PARENT),
                    Optional.empty(),
                    TextureSlot.LAYER0,
                    TextureSlot.LAYER1
            );


    /// Generating model and item for button
    public static void generate(
            Item item,
            Identifier iconTexture,
            ItemModelGenerators generator
    ) {

        // Defining textures
        TextureMapping textures = new TextureMapping()
                .put(
                        TextureSlot.LAYER0,
                        new Material(BASE_BUTTON_TEXTURE)
                )
                .put(
                        TextureSlot.LAYER1,
                        new Material(iconTexture)
                );


        // Creating model for button
        Identifier modelId = BUTTON_MODEL.create(
                item,
                textures,
                generator.modelOutput
        );


        // Creating item for button
        generator.itemModelOutput.accept(
                item,
                ItemModelUtils.plainModel(modelId)
        );
    }
}