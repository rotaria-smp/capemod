package dev.thestaticvoid.capejs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static dev.thestaticvoid.capejs.CapeJS.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                CapeData.TYPE,
                CapeData.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        ClientPayloadHandler::handleCape,
                        ServerPayloadHandler::handleCape
                )
        );

        System.out.println("REGISTERED PACKET: " + CapeData.TYPE.id());
    }


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

}
