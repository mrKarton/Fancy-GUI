package com.karton.fancygui.client;

import com.karton.fancygui.FancyGUI;
import com.karton.fancygui.client.gui.FancyScreen;
import com.karton.fancygui.client.gui.SlotScreen;
import com.karton.fancygui.client.gui.TextInputScreen;
import com.karton.fancygui.network.CloseScreenPayload;
import com.karton.fancygui.network.ScreenStatePayload;
import com.karton.fancygui.network.ScreenType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;

public class FancyGUIClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FancyGUI.LOGGER.info("FancyGUI client initialized");

        ClientPlayNetworking.registerGlobalReceiver(
                ScreenStatePayload.TYPE,
                (payload, context) ->
                        openOrSyncScreen(
                                context.client(),
                                payload
                        )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                CloseScreenPayload.TYPE,
                (payload, context) ->
                        closeScreen(
                                context.client(),
                                payload
                        )
        );
    }

    private static void openOrSyncScreen(
            Minecraft minecraft,
            ScreenStatePayload payload
    ) {
        if (
                minecraft.gui.screen()
                        instanceof FancyScreen fancyScreen
                        && fancyScreen.getScreenId()
                        == payload.screenId()
        ) {
            fancyScreen.applyState(payload);
            return;
        }

        if (minecraft.player == null) {
            return;
        }

        Inventory inventory =
                minecraft.player.getInventory();

        Screen screen = createScreen(
                inventory,
                payload
        );

        minecraft.setScreenAndShow(screen);
    }

    private static Screen createScreen(
            Inventory inventory,
            ScreenStatePayload payload
    ) {
        if (payload.screenType() == ScreenType.TEXT_INPUT) {
            return new TextInputScreen(
                    inventory,
                    payload
            );
        }

        return new SlotScreen(
                inventory,
                payload
        );
    }

    private static void closeScreen(
            Minecraft minecraft,
            CloseScreenPayload payload
    ) {
        if (
                minecraft.gui.screen()
                        instanceof FancyScreen fancyScreen
                        && fancyScreen.getScreenId()
                        == payload.screenId()
        ) {
            fancyScreen.closeFromServer();
        }
    }
}
