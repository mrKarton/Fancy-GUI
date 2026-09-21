package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Function;

public class TextInputScreen extends AnvilInputGui {
    private final FontDescription containerFont = new FontDescription.Resource(
            FancyGUI.id("text_input_screen")
    );

    private final Style containerStyle = Style.EMPTY.withFont(containerFont);

    private Runnable submitCallback;
    private Runnable cancelCallback;

    private Component background = Component.empty();

    public TextInputScreen(
            ServerPlayer player,
            String title
    ) {
        super(player, false);

        if (PolymerResourcePackUtils.hasMainPack(player.connection)) {
            this.background = Component.empty()
                    .append(this.background)
                    .append(
                            Component.literal("\uE100")
                                    .withStyle(containerStyle)
                    )
                    .append(
                            Component.literal("\uE001")
                                    .withStyle(containerStyle)
                                    .withColor(0xFFFFFF)
                    )
                    .append(
                            Component.literal("\uE101")
                                    .withStyle(containerStyle)
                    );
        }

        this.background = Component.empty()
                .append(this.background)
                .append(Component.literal(title != null ? title : ""));

        this.setTitle(
                this.background
        );

        this.setSlot(
                0,
                new GuiElementBuilder()
                        .setName(Component.literal(""))
                        .setItem(ButtonsRegistrator.TRANSPARENT_BUTTON)
        );

        this.setSlot(
                2,
                new GuiElementBuilder()
                        .setItem(ButtonsRegistrator.CHECKED_BUTTON)
                        .setCallback(this::submit)
                        .setName(Component.literal("Отправить"))
        );

        this.setSlot(
                1,
                new GuiElementBuilder()
                        .setItem(ButtonsRegistrator.CROSS_BUTTON)
                        .setCallback(this::cancel)
                        .setName(Component.literal("Отменить"))
        );

    }

    @Override
    public void onInput(String str) {
        this.sendProperty(ScreenProperty.LEVEL_COST, 0);
    }

    public void setSubmit(Runnable func) {
        this.submitCallback = func;
    }

    public void setCancel(Runnable func) {
        this.cancelCallback = func;
    }

    private void cancel() {
        if (cancelCallback == null) {
            this.close();
            return;
        }

        cancelCallback.run();
    }

    private void submit() {
        if (submitCallback == null) {
            this.close();
            return;
        }

        submitCallback.run();
    }
}