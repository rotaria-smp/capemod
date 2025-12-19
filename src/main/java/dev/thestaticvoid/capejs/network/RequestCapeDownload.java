package dev.thestaticvoid.capejs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestCapeDownload(String capeId) implements CustomPacketPayload {

    public static final Type<RequestCapeDownload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("capejs", "cape_request"));

    public static final StreamCodec<ByteBuf, RequestCapeDownload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(RequestCapeDownload::new, RequestCapeDownload::capeId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}