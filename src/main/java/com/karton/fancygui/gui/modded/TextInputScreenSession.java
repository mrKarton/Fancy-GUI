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


    // 18px * 9cells
    private static final int INPUT_WIDTH = 162;
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

                submit();

                return;
            }
        }

        super.handleScreenAction(payload);
    }

    @Override
    protected void onClientClosed() {
        super.onClientClosed();

        cancel();
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

    @Override
    public void submit() {
        if (submitCallback == null) {
            this.close(false);
            return;
        }

        submitCallback.run();
    }

    @Override
    public void cancel() {
        if (cancelCallback == null) {
            this.close(false);
            return;
        }

        cancelCallback.run();
    }

    public void setHint(String hint) {
        this.hint = hint != null ? hint : "";
        syncIfOpened();
    }
}
