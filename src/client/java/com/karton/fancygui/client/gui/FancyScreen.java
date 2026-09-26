package com.karton.fancygui.client.gui;

import com.karton.fancygui.network.ScreenActionPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenStatePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Общий contract всех native FancyGUI screens.
 *
 * Это interface специально: SlotScreen должен наследоваться от
 * AbstractContainerScreen, а экраны без inventory смогут наследоваться
 * напрямую от обычного Screen.
 */
public interface FancyScreen {

    int getScreenId();

    void applyState(ScreenStatePayload state);

    void closeFromServer();

    default void sendAction(
            int elementId,
            ScreenActionType action
    ) {
        sendAction(elementId, action, "");
    }

    default void sendAction(
            int elementId,
            ScreenActionType action,
            String value
    ) {
        ClientPlayNetworking.send(
                new ScreenActionPayload(
                        getScreenId(),
                        elementId,
                        action,
                        value != null ? value : ""
                )
        );
    }
}
