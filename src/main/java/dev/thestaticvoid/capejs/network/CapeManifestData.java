package dev.thestaticvoid.capejs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record CapeManifestData(Map<String, String> hashes)
        implements CustomPacketPayload {

    public static final Type<CapeManifestData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("capejs", "cape_manifest"));

    public static final StreamCodec<ByteBuf, CapeManifestData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.STRING_UTF8
                    ),
                    CapeManifestData::hashes,
                    CapeManifestData::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
