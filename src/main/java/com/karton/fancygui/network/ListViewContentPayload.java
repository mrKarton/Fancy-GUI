package com.karton.fancygui.network;

import com.karton.fancygui.FancyGUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ListViewContentPayload(int screenId, long revision, List<ItemStack> items)
        implements CustomPacketPayload {

    // Keep the count bounded on decode; the normal use case is hundreds of items.
    public static final int MAX_ITEMS = 4096;
    public static final Type<ListViewContentPayload> TYPE =
            new Type<>(FancyGUI.id("list_view_content"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ListViewContentPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public ListViewContentPayload decode(RegistryFriendlyByteBuf buf) {
                    int screenId = buf.readVarInt();
                    long revision = buf.readVarLong();
                    int count = buf.readVarInt();
                    if (count < 0 || count > MAX_ITEMS) {
                        throw new IllegalArgumentException("Invalid ListView item count: " + count);
                    }
                    List<ItemStack> items = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                    }
                    return new ListViewContentPayload(screenId, revision, items);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ListViewContentPayload value) {
                    if (value.items().size() > MAX_ITEMS) {
                        throw new IllegalArgumentException("ListView supports at most " + MAX_ITEMS + " items");
                    }
                    buf.writeVarInt(value.screenId());
                    buf.writeVarLong(value.revision());
                    buf.writeVarInt(value.items().size());
                    for (ItemStack stack : value.items()) {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
