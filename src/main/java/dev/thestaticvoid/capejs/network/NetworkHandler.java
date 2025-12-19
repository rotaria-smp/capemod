package dev.thestaticvoid.capejs.network;

import dev.thestaticvoid.capejs.client.ClientCapeStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

public final class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("1");

        // Existing cape data packet (for equipping/unequipping capes on other players)
        registrar.playBidirectional(
                CapeData.TYPE,
                CapeData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleCape,
                        ServerPayloadHandler::handleCape
                )
        );
        System.out.println("REGISTERED PACKET: " + CapeData.TYPE.id());

        // NEW: Cape unlock packet (server -> client)
        registrar.playToClient(
                CapeUnlockPayload.TYPE,
                CapeUnlockPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        System.out.println("[ClientCapeStorage] Received unlock: " + payload.capeId());
                        ClientCapeStorage.unlockCape(payload.capeId());
                    });
                }
        );
        System.out.println("REGISTERED PACKET: " + CapeUnlockPayload.TYPE.id());

        // NEW: Cape lock packet (server -> client)
        registrar.playToClient(
                CapeLockPayload.TYPE,
                CapeLockPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        System.out.println("[ClientCapeStorage] Received lock: " + payload.capeId());
                        ClientCapeStorage.lockCape(payload.capeId());
                    });
                }
        );
        System.out.println("REGISTERED PACKET: " + CapeLockPayload.TYPE.id());

        // NEW: Cape equip packet (server -> client)
        registrar.playToClient(
                CapeEquipPayload.TYPE,
                CapeEquipPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        System.out.println("[ClientCapeStorage] Received equip: " + payload.capeId());
                        ClientCapeStorage.setEquippedCape(payload.capeId());
                    });
                }
        );
        System.out.println("REGISTERED PACKET: " + CapeEquipPayload.TYPE.id());

        // NEW: Cape list sync packet (server -> client, on login)
        registrar.playToClient(
                CapeListSyncPayload.TYPE,
                CapeListSyncPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        System.out.println("[ClientCapeStorage] Received full sync: " + payload.unlockedCapes().size() + " capes");
                        ClientCapeStorage.setUnlockedCapes(payload.unlockedCapes());
                        ClientCapeStorage.setEquippedCape(payload.equippedCape());
                    });
                }
        );
        System.out.println("REGISTERED PACKET: " + CapeListSyncPayload.TYPE.id());

        // Existing manifest packet
        registrar.playToClient(
                CapeManifestData.TYPE,
                CapeManifestData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientManifestPayloadHandler::handle,
                        (payload, ctx) -> {}
                )
        );

        // Existing texture request packet
        registrar.playBidirectional(
                RequestCapeDownload.TYPE,
                RequestCapeDownload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        (payload, ctx) -> {},
                        ServerTextureRequestHandler::handle
                )
        );

        // Existing texture data packet
        registrar.playToClient(
                CapeTextureData.TYPE,
                CapeTextureData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientTexturePayloadHandler::handleTexture,
                        (payload, ctx) -> {}
                )
        );

    }

    // ===== EXISTING PACKET =====
    public record CapeData(String playerId, String capeId, Boolean remove) implements CustomPacketPayload {

        public static final Type<CapeData> TYPE =
                new Type<>(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "cape_data")
                );

        public static final StreamCodec<ByteBuf, CapeData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                CapeData::playerId,

                ByteBufCodecs.STRING_UTF8,
                CapeData::capeId,

                ByteBufCodecs.BOOL,
                CapeData::remove,

                CapeData::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ===== NEW PACKETS FOR CLIENT CAPE STORAGE SYNC =====

    /**
     * Sent from server to client when a cape is unlocked
     */
    public record CapeUnlockPayload(String capeId) implements CustomPacketPayload {
        public static final Type<CapeUnlockPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cape_unlock"));

        public static final StreamCodec<ByteBuf, CapeUnlockPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(CapeUnlockPayload::new, CapeUnlockPayload::capeId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Sent from server to client when a cape is removed/locked
     */
    public record CapeLockPayload(String capeId) implements CustomPacketPayload {
        public static final Type<CapeLockPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cape_lock"));

        public static final StreamCodec<ByteBuf, CapeLockPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(CapeLockPayload::new, CapeLockPayload::capeId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Sent from server to client when a cape is equipped/unequipped
     */
    public record CapeEquipPayload(String capeId) implements CustomPacketPayload {
        public static final Type<CapeEquipPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cape_equip"));

        public static final StreamCodec<ByteBuf, CapeEquipPayload> STREAM_CODEC =
                ByteBufCodecs.STRING_UTF8.map(CapeEquipPayload::new, CapeEquipPayload::capeId);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Sent from server to client on login with full cape list
     */
    public record CapeListSyncPayload(List<String> unlockedCapes, String equippedCape) implements CustomPacketPayload {
        public static final Type<CapeListSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cape_list_sync"));

        public static final StreamCodec<ByteBuf, CapeListSyncPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                CapeListSyncPayload::unlockedCapes,
                ByteBufCodecs.STRING_UTF8,
                CapeListSyncPayload::equippedCape,
                CapeListSyncPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}