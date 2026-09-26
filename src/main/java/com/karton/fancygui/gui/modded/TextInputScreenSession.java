package com.karton.fancygui.gui.modded;

import com.karton.fancygui.gui.interfaces.TextInputGUI;
import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ScreenActionPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenType;
import com.karton.fancygui.util.NumberRange;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class TextInputScreenSession
        extends SlotScreenSession
        implements TextInputGUI {

    /*
     * 176px — ширина vanilla container.
     * 80px визуально ближе к компактному rename/search field,
     * а не к полосе на всю ширину окна.
     */
    private static final int INPUT_WIDTH = 80;
    private static final int INPUT_HEIGHT = 18;
    private static final int INPUT_X = (176 - INPUT_WIDTH) / 2;
    private static final int INPUT_Y = 20;

    private Runnable submitCallback;
    private Runnable cancelCallback;
    private Runnable onInputCallback;

    private String input = "";
    private String hint = "";

    public TextInputScreenSession(
            ServerPlayer player,
            String title
    ) {
        super(
                player,
                ScreenType.TEXT_INPUT,
                title,
                1,
                new NumberRange[]{}
        );
    }

    @Override
    protected int getSlotStartY() {
        /*
         * EditBox: y=20..37.
         * Grid начинается с y=46 — остаётся нормальный зазор.
         */
        return 46;
    }

    @Override
    protected List<ScreenElementData> buildElements() {
        List<ScreenElementData> result =
                super.buildElements();

        result.add(
                ScreenElementData.textInput(
                        INPUT_X,
                        INPUT_Y,
                        INPUT_WIDTH,
                        INPUT_HEIGHT,
                        input,
                        hint
                )
        );

        return result;
    }

    @Override
    protected void handleScreenAction(
            ScreenActionPayload payload
    ) {
        if (payload.elementId() == ElementIds.TEXT_INPUT) {
            if (payload.action() == ScreenActionType.TEXT_CHANGED) {
                input = payload.value() != null
                        ? payload.value()
                        : "";

                if (onInputCallback != null) {
                    onInputCallback.run();
                }

                return;
            }

            if (payload.action() == ScreenActionType.TEXT_SUBMIT) {
                input = payload.value() != null
                        ? payload.value()
                        : "";

                if (submitCallback != null) {
                    submitCallback.run();
                } else {
                    close(false);
                }

                return;
            }
        }

        super.handleScreenAction(payload);
    }

    @Override
    protected void onClientClosed() {
        super.onClientClosed();

        if (cancelCallback != null) {
            cancelCallback.run();
        }
    }

    @Override
    public void setOnInputCallback(
            Runnable callback
    ) {
        this.onInputCallback = callback;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public void setSubmitCallback(
            Runnable callback
    ) {
        this.submitCallback = callback;
    }

    @Override
    public void setCancelCallback(
            Runnable callback
    ) {
        this.cancelCallback = callback;
    }

    public void setHint(String hint) {
        this.hint = hint != null ? hint : "";
        syncIfOpened();
    }
}
