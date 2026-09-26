package com.karton.fancygui.gui.server.screens;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.gui.Button;
import com.karton.fancygui.gui.interfaces.TextInputGUI;
import com.karton.fancygui.gui.server.buttons.ButtonsRegistrator;
import com.karton.fancygui.util.NumberRange;
import com.karton.fancygui.util.NumberRangeUtil;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

public class TextInputSlotScreen
        extends AnvilInputGui
        implements TextInputGUI {

    private final FontDescription containerFont =
            new FontDescription.Resource(
                    FancyGUI.id("text_input_screen")
            );

    private final Style containerStyle =
            Style.EMPTY.withFont(containerFont);

    private Runnable submitCallback;
    private Runnable cancelCallback;
    private Runnable onInputCallback;

    /**
     * Текущий пользовательский title.
     *
     * Храним отдельно от background, чтобы при повторном
     * setTitle() старый фон не добавлялся снова.
     */
    private Component screenTitle =
            Component.empty();

    /**
     * Полностью собранный title:
     *
     * custom background + screenTitle
     */
    private Component background =
            Component.empty();

    public TextInputSlotScreen(
            ServerPlayer player,
            String title
    ) {
        super(player, false);

        /*
         * Первый слот anvil используется Minecraft как
         * source item для текстового поля.
         *
         * Делаем его визуально прозрачным.
         */
        super.setSlot(
                0,
                new GuiElementBuilder()
                        .setName(
                                Component.literal("")
                        )
                        .setItem(
                                ButtonsRegistrator.transparentButtonItem
                        )
        );

        setTitle(title);
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    /**
     * Собирает кастомный фон.
     *
     * ВАЖНО:
     * этот метод сам не вызывает setTitle(),
     * а только возвращает Component.
     */
    private Component buildClearBackground() {

        /*
         * Если игрок не загрузил Polymer resource pack,
         * glyph'ы не рисуем вообще.
         */
        if (!PolymerResourcePackUtils.hasMainPack(
                player.connection
        )) {
            return Component.empty();
        }

        return Component.empty()
                .append(
                        Component.literal("\uE100")
                                .withStyle(
                                        containerStyle
                                )
                )
                .append(
                        Component.literal("\uE001")
                                .withStyle(
                                        containerStyle
                                )
                                .withColor(
                                        0xFFFFFF
                                )
                )
                .append(
                        Component.literal("\uE101")
                                .withStyle(
                                        containerStyle
                                )
                );
    }

    /**
     * Полностью пересобирает отображаемый title.
     */
    private void rebuildTitle() {

        this.background =
                Component.empty()
                        .append(
                                buildClearBackground()
                        )
                        .append(
                                screenTitle
                        );

        /*
         * Именно super.setTitle(), чтобы не попасть обратно
         * в наш override.
         */
        super.setTitle(
                this.background
        );
    }

    // ============================================================
    // INPUT
    // ============================================================

    @Override
    public void onInput(String str) {

        /*
         * Не показываем vanilla "Required Level".
         */
        this.sendProperty(
                ScreenProperty.LEVEL_COST,
                0
        );

        if (onInputCallback != null) {
            onInputCallback.run();
        }
    }

    public void setOnInputCallback(
            Runnable func
    ) {
        this.onInputCallback = func;
    }

    public void setSubmitCallback(
            Runnable func
    ) {
        this.submitCallback = func;
    }

    public void setCancelCallback(
            Runnable func
    ) {
        this.cancelCallback = func;
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    private void cancel() {

        if (cancelCallback == null) {
            this.close(false);
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

    // ============================================================
    // TITLE
    // ============================================================

    @Override
    public int getRows() {
        return 0;
    }

    @Override
    public void setTitle(
            String text
    ) {
        setTitle(
                Component.literal(
                        text != null
                                ? text
                                : ""
                )
        );
    }

    @Override
    public void setTitle(
            Component component
    ) {
        this.screenTitle =
                component != null
                        ? component
                        : Component.empty();

        rebuildTitle();
    }

    // ============================================================
    // BUTTONS
    // ============================================================

    @Override
    public void setButton(
            int slotIndex,
            Button button
    ) {
        if (button == null) {
            return;
        }

        /*
         * Button — общая FancyGUI-модель.
         *
         * Для SGUI используем ServerButton только как fallback.
         */
        super.setSlot(
                slotIndex,
                button.createServerFallback()
        );
    }

    @Override
    public void setButton(
            NumberRange slotsToSet,
            Button button
    ) {
        for (
                int slotIndex :
                slotsToSet.getRangeArray()
        ) {
            setButton(
                    slotIndex,
                    button
            );
        }
    }

    // ============================================================
    // SLOTS
    // ============================================================

    @Override
    public void setSlot(
            NumberRange[] neededSlots,
            Slot slot
    ) {
        for (
                int index :
                NumberRangeUtil.getFulRange(
                        neededSlots
                )
        ) {
            setSlot(
                    index,
                    slot
            );
        }
    }
}