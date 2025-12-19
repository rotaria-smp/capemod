package dev.thestaticvoid.capejs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CapeTextureData(String capeId, byte[] data) implements CustomPacketPayload {

    public static final Type<CapeTextureData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("capejs", "cape_texture"));

    public static final StreamCodec<ByteBuf, CapeTextureData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    CapeTextureData::capeId,
                    ByteBufCodecs.BYTE_ARRAY,
                    CapeTextureData::data,
                    CapeTextureData::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}