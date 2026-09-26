package com.karton.fancygui.network;

import com.karton.fancygui.FancyGUI;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CloseScreenPayload(
        int screenId
) implements CustomPacketPayload {

    public static final Type<CloseScreenPayload> TYPE =
            new Type<>(FancyGUI.id("close_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseScreenPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public CloseScreenPayload decode(RegistryFriendlyByteBuf buf) {
                    return new CloseScreenPayload(buf.readVarInt());
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buf,
                        CloseScreenPayload payload
                ) {
                    buf.writeVarInt(payload.screenId());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
