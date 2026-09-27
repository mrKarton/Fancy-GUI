package com.karton.fancygui.network;

import com.karton.fancygui.FancyGUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ListViewClickPayload(int screenId, long revision, int index)
        implements CustomPacketPayload {

    public static final Type<ListViewClickPayload> TYPE =
            new Type<>(FancyGUI.id("list_view_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ListViewClickPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ListViewClickPayload decode(RegistryFriendlyByteBuf buf) {
                    return new ListViewClickPayload(
                            buf.readVarInt(), buf.readVarLong(), buf.readVarInt()
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ListViewClickPayload value) {
                    buf.writeVarInt(value.screenId());
                    buf.writeVarLong(value.revision());
                    buf.writeVarInt(value.index());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
