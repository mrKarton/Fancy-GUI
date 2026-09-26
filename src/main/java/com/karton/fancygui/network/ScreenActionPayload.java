package com.karton.fancygui.network;

import com.karton.fancygui.FancyGUI;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ScreenActionPayload(
        int screenId,
        int elementId,
        ScreenActionType action,
        String value
) implements CustomPacketPayload {

    public static final Type<ScreenActionPayload> TYPE =
            new Type<>(FancyGUI.id("screen_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenActionPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ScreenActionPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ScreenActionPayload(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            ScreenActionType.byId(buf.readVarInt()),
                            buf.readUtf()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buf,
                        ScreenActionPayload payload
                ) {
                    buf.writeVarInt(payload.screenId());
                    buf.writeVarInt(payload.elementId());
                    buf.writeVarInt(payload.action().id());
                    buf.writeUtf(payload.value() != null ? payload.value() : "");
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
