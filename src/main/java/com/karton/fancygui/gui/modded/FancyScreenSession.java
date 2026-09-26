package com.karton.fancygui.gui.modded;

import com.karton.fancygui.network.CloseScreenPayload;
import com.karton.fancygui.network.ScreenActionPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenStatePayload;
import com.karton.fancygui.network.ScreenType;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public abstract class FancyScreenSession {

    private final int screenId;
    private final ServerPlayer player;
    private final ScreenType screenType;

    private Component title;
    private boolean opened;

    protected FancyScreenSession(
            ServerPlayer player,
            ScreenType screenType,
            Component title
    ) {
        this.screenId = FancyScreenManager.nextScreenId();
        this.player = player;
        this.screenType = screenType;
        this.title = title != null ? title : Component.empty();
    }

    public final int getScreenId() {
        return screenId;
    }

    public final ServerPlayer getPlayer() {
        return player;
    }

    public final ScreenType getScreenType() {
        return screenType;
    }

    public final Component getTitle() {
        return title;
    }

    public final boolean isOpened() {
        return opened;
    }

    public void setTitle(String title) {
        setTitle(Component.literal(title != null ? title : ""));
    }

    public void setTitle(Component title) {
        this.title = title != null ? title : Component.empty();
        syncIfOpened();
    }

    public boolean open() {
        player.closeContainer();

        FancyScreenManager.register(this);
        opened = true;
        sync();

        return true;
    }

    public void close(boolean skipSync) {
        if (!opened) {
            return;
        }

        FancyScreenManager.unregister(this);
        opened = false;

        ServerPlayNetworking.send(
                player,
                new CloseScreenPayload(screenId)
        );
    }

    final void markReplaced() {
        if (!opened) {
            return;
        }

        opened = false;
        onReplaced();
    }

    final void handleAction(ScreenActionPayload payload) {
        if (!opened) {
            return;
        }

        if (payload.action() == ScreenActionType.CLOSE) {
            FancyScreenManager.unregister(this);
            opened = false;
            onClientClosed();
            return;
        }

        handleScreenAction(payload);
    }

    protected void onClientClosed() {
    }

    protected void onReplaced() {
    }

    protected abstract void handleScreenAction(ScreenActionPayload payload);

    protected abstract int getStateRows();

    protected abstract List<ScreenElementData> buildElements();

    protected final void syncIfOpened() {
        if (opened) {
            sync();
        }
    }

    public final void sync() {
        if (!opened) {
            return;
        }

        List<ScreenElementData> elements = buildElements();

        ServerPlayNetworking.send(
                player,
                new ScreenStatePayload(
                        screenId,
                        screenType,
                        getStateRows(),
                        title.getString(),
                        elements.toArray(ScreenElementData[]::new)
                )
        );
    }
}
