package com.karton.fancygui.network;

import com.karton.fancygui.FancyGUI;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ScreenStatePayload(
        int screenId,
        ScreenType screenType,
        int rows,
        String title,
        ScreenElementData[] elements
) implements CustomPacketPayload {

    public static final Type<ScreenStatePayload> TYPE =
            new Type<>(FancyGUI.id("screen_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenStatePayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ScreenStatePayload decode(RegistryFriendlyByteBuf buf) {
                    int screenId = buf.readVarInt();
                    ScreenType screenType = ScreenType.byId(buf.readVarInt());
                    int rows = buf.readVarInt();
                    String title = buf.readUtf();

                    int count = buf.readVarInt();
                    ScreenElementData[] elements = new ScreenElementData[count];

                    for (int i = 0; i < count; i++) {
                        int id = buf.readVarInt();
                        ScreenElementType type = ScreenElementType.byId(buf.readVarInt());
                        int gridIndex = buf.readVarInt();
                        int x = buf.readVarInt();
                        int y = buf.readVarInt();
                        int width = buf.readVarInt();
                        int height = buf.readVarInt();
                        String text = buf.readUtf();
                        String secondaryText = buf.readUtf();
                        ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

                        elements[i] = new ScreenElementData(
                                id,
                                type,
                                gridIndex,
                                x,
                                y,
                                width,
                                height,
                                text,
                                secondaryText,
                                item
                        );
                    }

                    return new ScreenStatePayload(
                            screenId,
                            screenType,
                            rows,
                            title,
                            elements
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buf,
                        ScreenStatePayload payload
                ) {
                    buf.writeVarInt(payload.screenId());
                    buf.writeVarInt(payload.screenType().id());
                    buf.writeVarInt(payload.rows());
                    buf.writeUtf(payload.title());

                    ScreenElementData[] elements = payload.elements();
                    buf.writeVarInt(elements.length);

                    for (ScreenElementData element : elements) {
                        buf.writeVarInt(element.id());
                        buf.writeVarInt(element.type().id());
                        buf.writeVarInt(element.gridIndex());
                        buf.writeVarInt(element.x());
                        buf.writeVarInt(element.y());
                        buf.writeVarInt(element.width());
                        buf.writeVarInt(element.height());
                        buf.writeUtf(element.text());
                        buf.writeUtf(element.secondaryText());
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, element.item());
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
