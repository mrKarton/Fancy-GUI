package com.karton.fancygui.client.gui;

import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenElementType;
import com.karton.fancygui.network.ScreenStatePayload;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.Nullable;

/**
 * Native/modded TextInputScreen поверх SlotScreen.
 *
 * EditBox остаётся обычным vanilla widget. Ключевой момент здесь —
 * отдавать ему keyboard/character input раньше container hotkeys.
 */
public class TextInputScreen extends SlotScreen {

    private ScreenElementData inputData;
    private EditBox inputBox;

    /** true только когда value меняется из server state, а не пользователем. */
    private boolean applyingServerValue;

    public TextInputScreen(
            Inventory inventory,
            ScreenStatePayload state
    ) {
        super(inventory, state);
        applyTextInputState(state);
    }

    @Override
    public void applyState(
            ScreenStatePayload state
    ) {
        super.applyState(state);
        applyTextInputState(state);
    }

    private void applyTextInputState(
            ScreenStatePayload state
    ) {
        ScreenElementData found = null;

        for (ScreenElementData element : state.elements()) {
            if (element.type() == ScreenElementType.TEXT_INPUT) {
                found = element;
                break;
            }
        }

        this.inputData = found;

        if (inputBox == null || inputData == null) {
            return;
        }

        /*
         * Пока пользователь печатает, не перетираем локальное значение
         * очередным state packet'ом. Иначе при параллельной синхронизации
         * inventory курсор/текст могут визуально "откатываться".
         */
        if (!inputBox.isFocused()) {
            String serverValue = inputData.text();

            if (!inputBox.getValue().equals(serverValue)) {
                applyingServerValue = true;
                inputBox.setValue(serverValue);
                applyingServerValue = false;
            }
        }

        inputBox.setHint(
                inputData.secondaryText().isEmpty()
                        ? Component.empty()
                        : Component.literal(inputData.secondaryText())
        );
    }

    @Override
    protected void init() {
        super.init();

        if (inputData == null) {
            return;
        }

        int x = leftPos + inputData.x();
        int y = topPos + inputData.y();
        int width = Math.max(16, inputData.width());
        int height = Math.max(12, inputData.height());

        /*
         * Конструктор с oldBox сохраняет cursor/selection/value при re-init
         * (например, после resize GUI).
         */
        EditBox oldBox = inputBox;

        inputBox = new EditBox(
                font,
                x,
                y,
                width,
                height,
                oldBox,
                Component.literal("FancyGUI text input")
        );

        inputBox.setBordered(true);
        inputBox.setEditable(true);
        inputBox.setCanLoseFocus(false);
        inputBox.setMaxLength(256);

        /*
         * Не задаём цвета вручную. В 26.3 EditBox использует ARGB,
         * а старый 0xFFFFFF имеет alpha=0 и делает текст прозрачным.
         * Vanilla defaults дают правильный вид и для editable/uneditable state.
         */

        if (oldBox == null) {
            applyingServerValue = true;
            inputBox.setValue(inputData.text());
            applyingServerValue = false;
        }

        inputBox.setHint(
                inputData.secondaryText().isEmpty()
                        ? Component.empty()
                        : Component.literal(inputData.secondaryText())
        );

        inputBox.setResponder(value -> {
            if (applyingServerValue) {
                return;
            }

            sendAction(
                    ElementIds.TEXT_INPUT,
                    ScreenActionType.TEXT_CHANGED,
                    value
            );
        });

        addRenderableWidget(inputBox);

        /*
         * В 26.3 одного setInitialFocus() недостаточно полагаться как на
         * источник истины для самого EditBox. Явно держим widget focused.
         */
        setInitialFocus(inputBox);
        inputBox.setFocused(true);
    }

    @Override
    public boolean keyPressed(
            KeyEvent event
    ) {
        if (inputBox != null && inputBox.isFocused()) {
            /* Esc должен закрывать Screen стандартным способом. */
            if (event.isEscape()) {
                return super.keyPressed(event);
            }

            /* Enter = submit, а не container hotkey. */
            if (event.isConfirmation()) {
                sendAction(
                        ElementIds.TEXT_INPUT,
                        ScreenActionType.TEXT_SUBMIT,
                        inputBox.getValue()
                );

                return true;
            }

            /*
             * Backspace/Delete/arrows/Home/End/clipboard обрабатывает
             * непосредственно vanilla EditBox.
             */
            if (inputBox.keyPressed(event)) {
                return true;
            }

            /*
             * Обычная буква придёт отдельным CharacterEvent.
             * Сам key event поглощаем, чтобы E/Q/1..9 не стали
             * inventory controls в AbstractContainerScreen.
             */
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(
            CharacterEvent event
    ) {
        if (inputBox != null && inputBox.isFocused()) {
            /*
             * Сначала используем штатный EditBox path.
             */
            if (inputBox.charTyped(event)) {
                return true;
            }

            /*
             * Fallback для 26.x input pipeline: если widget не consume'нул
             * разрешённый символ, вставляем codepoint напрямую. Это не
             * затрагивает control keys — они идут через keyPressed().
             */
            if (event.isAllowedChatCharacter()) {
                inputBox.insertText(event.codepointAsString());
                return true;
            }

            return false;
        }

        return super.charTyped(event);
    }

    @Override
    public boolean preeditUpdated(
            @Nullable PreeditEvent event
    ) {
        if (inputBox != null && inputBox.isFocused()) {
            return inputBox.preeditUpdated(event);
        }

        return super.preeditUpdated(event);
    }

    @Override
    public boolean isInputCaptured() {
        return inputBox != null && inputBox.capturesInput();
    }
}
