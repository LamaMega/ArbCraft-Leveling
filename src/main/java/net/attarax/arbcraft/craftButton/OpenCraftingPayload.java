package net.attarax.arbcraft.craftButton;

import io.netty.buffer.ByteBuf;
import net.attarax.arbcraft.ArbCraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent from the client to the server when the inventory "Crafting" button is clicked. */
public record OpenCraftingPayload() implements CustomPacketPayload {

    public static final Type<OpenCraftingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ArbCraft.MODID, "open_crafting"));

    public static final StreamCodec<ByteBuf, OpenCraftingPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenCraftingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}