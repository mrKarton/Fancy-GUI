package com.karton.fancygui.gui.modded;

import com.karton.fancygui.network.ScreenActionPayload;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class FancyScreenManager {

    private static final AtomicInteger NEXT_SCREEN_ID =
            new AtomicInteger(1);

    private static final Map<UUID, FancyScreenSession> OPEN_SCREENS =
            new ConcurrentHashMap<>();

    private FancyScreenManager() {
    }

    static int nextScreenId() {
        return NEXT_SCREEN_ID.getAndIncrement();
    }

    static void register(FancyScreenSession session) {
        FancyScreenSession previous = OPEN_SCREENS.put(
                session.getPlayer().getUUID(),
                session
        );

        if (previous != null && previous != session) {
            previous.markReplaced();
        }
    }

    static void unregister(FancyScreenSession session) {
        OPEN_SCREENS.remove(
                session.getPlayer().getUUID(),
                session
        );
    }

    public static void handleAction(
            ServerPlayer player,
            ScreenActionPayload payload
    ) {
        FancyScreenSession session = OPEN_SCREENS.get(player.getUUID());

        if (session == null) {
            return;
        }

        if (session.getScreenId() != payload.screenId()) {
            return;
        }

        session.handleAction(payload);
    }
}
