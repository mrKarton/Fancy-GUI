package com.karton.fancygui.network;

import com.karton.fancygui.gui.modded.FancyScreenManager;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

public final class FancyGUINetworking {

    private FancyGUINetworking() {
    }

    public static void register() {
        PayloadTypeRegistry
                .clientboundPlay()
                .register(
                        ScreenStatePayload.TYPE,
                        ScreenStatePayload.CODEC
                );

        PayloadTypeRegistry
                .clientboundPlay()
                .register(
                        CloseScreenPayload.TYPE,
                        CloseScreenPayload.CODEC
                );

        PayloadTypeRegistry
                .serverboundPlay()
                .register(
                        ScreenActionPayload.TYPE,
                        ScreenActionPayload.CODEC
                );

        ServerPlayNetworking.registerGlobalReceiver(
                ScreenActionPayload.TYPE,
                (payload, context) ->
                        FancyScreenManager.handleAction(
                                context.player(),
                                payload
                        )
        );
    }

    /**
     * Vanilla client -> false.
     * FancyGUI client -> true.
     */
    public static boolean supportsClientGui(
            ServerPlayer player
    ) {
        return ServerPlayNetworking.canSend(
                player,
                ScreenStatePayload.TYPE
        );
    }
}
